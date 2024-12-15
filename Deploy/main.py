from flask import Flask, render_template, request, redirect, url_for, flash
from flask_sqlalchemy import SQLAlchemy
from datetime import datetime
import os
from dotenv import load_dotenv
import git
import subprocess
import requests
from services.java_control import JavaAppController
from services.git_service import GitService
import threading
import time

# Load environment variables
load_dotenv()

app = Flask(__name__)
app.config['SECRET_KEY'] = os.getenv('SECRET_KEY', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86')
app.config['SQLALCHEMY_DATABASE_URI'] = (
    f"mysql+pymysql://{os.getenv('DB_USER')}:{os.getenv('DB_PASSWORD')}@"
    f"{os.getenv('DB_HOST')}/{os.getenv('DB_NAME')}"
)
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
app.config['REPO_PATH'] = os.getenv('REPO_PATH', '/home/partscom/autovio_app/PartsNepal/API/partsnepal-api')
app.config['ADMIN_PASSWORD'] = '4~i**cp0GsT;'  # Hardcoded admin password
app.config['JAVA_APP_URL'] = os.getenv('JAVA_APP_URL', 'http://localhost:9090')

db = SQLAlchemy(app)

# Database Models
class StatusEvent(db.Model):
    __tablename__ = 'status_event'
    id = db.Column(db.Integer, primary_key=True)
    event_type = db.Column(db.String(50), nullable=False)
    timestamp = db.Column(db.DateTime, nullable=False, default=datetime.utcnow)
    status = db.Column(db.String(20), nullable=False)
    details = db.Column(db.Text)

class Log(db.Model):
    __tablename__ = 'log'
    id = db.Column(db.Integer, primary_key=True)
    timestamp = db.Column(db.DateTime, nullable=False, default=datetime.utcnow)
    message = db.Column(db.Text, nullable=False)
    level = db.Column(db.String(20), nullable=False)

# Initialize services
java_controller = None
git_service = None

try:
    java_controller = JavaAppController(os.path.join(app.config['REPO_PATH'], 'API/partsnepal-api'))
except Exception as e:
    print(f"Error initializing Java controller: {e}")

try:
    git_service = GitService(app.config['REPO_PATH'])
except Exception as e:
    print(f"Error initializing Git service: {e}")

def background_tasks():
    with app.app_context():
        while True:
            time.sleep(60)  # Sleep for 60 seconds between checks

@app.route('/manage/status', methods=['GET', 'POST'])
def status():
    if request.method == 'POST':
        if request.form.get('auth_password') != app.config['ADMIN_PASSWORD']:
            flash('Invalid password')
            return redirect(url_for('status'))

        action = request.form.get('action')
        if action == 'start' and java_controller:
            success, message = java_controller.start()
            status = 'success' if success else 'failure'
            db.session.add(StatusEvent(
                event_type='start',
                status=status,
                details=message
            ))
        elif action == 'stop' and java_controller:
            success, message = java_controller.stop()
            status = 'success' if success else 'failure'
            db.session.add(StatusEvent(
                event_type='stop',
                status=status,
                details=message
            ))
        elif action == 'rebuild' and git_service:
            success, message = git_service.pull_changes()
            status = 'success' if success else 'failure'
            db.session.add(StatusEvent(
                event_type='rebuild',
                status=status,
                details=message
            ))
        
        db.session.commit()
        return redirect(url_for('status'))

    try:
        latest_events = StatusEvent.query.order_by(StatusEvent.timestamp.desc()).limit(5).all()
        app_running = java_controller.is_running() if java_controller else False
        
        return render_template('status.html', 
                             events=latest_events, 
                             app_running=app_running)
    except Exception as e:
        return f"Error: {str(e)}", 500

@app.route('/manage/logs')
def logs():
    try:
        # Get Python logs from stderr.log
        python_logs = []
        try:
            with open('stderr.log', 'r') as f:
                python_logs = f.readlines()
        except Exception as e:
            python_logs = [f"Error reading Python logs: {str(e)}"]

        # Get Java logs directly from the running application
        java_logs = []
        try:
            response = requests.get(f"{app.config['JAVA_APP_URL']}/logs")
            if response.status_code == 200:
                java_logs = response.json()
        except Exception as e:
            java_logs = [{"timestamp": datetime.now(), "level": "ERROR", 
                         "message": f"Error fetching Java logs: {str(e)}"}]

        return render_template('logs.html',
                             python_logs=python_logs,
                             java_logs=java_logs)
    except Exception as e:
        return f"Error: {str(e)}", 500

# Start background tasks
background_thread = threading.Thread(target=background_tasks, daemon=True)
background_thread.start()

def init_db():
    with app.app_context():
        try:
            db.create_all()
            print("Database tables created successfully")
        except Exception as e:
            print(f"Error creating database tables: {e}")

# Initialize database
init_db()

if __name__ == '__main__':
    app.run(debug=False, host='0.0.0.0')

from flask import Flask, render_template, request, redirect, url_for, flash
from flask_sqlalchemy import SQLAlchemy
from flask_login import LoginManager, UserMixin, login_user, login_required, logout_user
from datetime import datetime
import os
from dotenv import load_dotenv
import git
import subprocess
import requests
from services.java_control import JavaAppController
from services.git_service import GitService
from services.log_collector import LogCollector
from services.health_check import HealthChecker
import threading
import time

# Load environment variables
load_dotenv()

app = Flask(__name__)
app.config['SECRET_KEY'] = os.getenv('SECRET_KEY', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86')
app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///app.db'
app.config['JAVA_APP_URL'] = 'http://0.0.0.0:9090'
app.config['REPO_PATH'] = os.getenv('REPO_PATH', '/autovio_app/PartsNepal/API/partsnepal-api')
app.config['ADMIN_PASSWORD'] = os.getenv('ADMIN_PASSWORD', 'f6ecad38d969ec29a3280e686cf0c3f5d58d969ea86')

db = SQLAlchemy(app)
login_manager = LoginManager(app)

# Initialize services
java_controller = JavaAppController(os.path.join(app.config['REPO_PATH'], 'API/partsnepal-api'))
git_service = GitService(app.config['REPO_PATH'])
log_collector = LogCollector(app.config['JAVA_APP_URL'])
health_checker = HealthChecker(app.config['JAVA_APP_URL'], 
                             os.path.join(app.config['REPO_PATH'], 'API/partsnepal-api/app.pid'))

# Database Models
class Log(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    timestamp = db.Column(db.DateTime, nullable=False, default=datetime.utcnow)
    message = db.Column(db.Text, nullable=False)
    level = db.Column(db.String(20), nullable=False)

class StatusEvent(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    event_type = db.Column(db.String(50), nullable=False)
    timestamp = db.Column(db.DateTime, nullable=False, default=datetime.utcnow)
    status = db.Column(db.String(20), nullable=False)
    details = db.Column(db.Text)

# Routes
@app.route('/')
def index():
    return redirect(url_for('status'))

@app.route('/manage/status', methods=['GET', 'POST'])
def status():
    if request.method == 'POST':
        password = request.form.get('auth_password')
        if password != app.config['ADMIN_PASSWORD']:
            flash('Invalid password')
            return redirect(url_for('status'))

        action = request.form.get('action')
        if action == 'start':
            success, message = java_controller.start()
            status = 'success' if success else 'failure'
            db.session.add(StatusEvent(
                event_type='start',
                status=status,
                details=message
            ))
            
        elif action == 'stop':
            success, message = java_controller.stop()
            status = 'success' if success else 'failure'
            db.session.add(StatusEvent(
                event_type='stop',
                status=status,
                details=message
            ))
            
        elif action == 'rebuild':
            # Pull changes
            git_success, git_message = git_service.pull_changes()
            db.session.add(StatusEvent(
                event_type='git_pull',
                status='success' if git_success else 'failure',
                details=git_message
            ))

            # Build if git pull was successful
            if git_success:
                build_success, build_message = git_service.build_project()
                db.session.add(StatusEvent(
                    event_type='build',
                    status='success' if build_success else 'failure',
                    details=build_message
                ))

                # Restart application if build was successful
                if build_success:
                    java_controller.stop()
                    success, message = java_controller.start()
                    db.session.add(StatusEvent(
                        event_type='restart',
                        status='success' if success else 'failure',
                        details=message
                    ))

        db.session.commit()
        return redirect(url_for('status'))

    # Get latest status
    health_status = health_checker.check_java_app_health()
    latest_events = StatusEvent.query.order_by(StatusEvent.timestamp.desc()).limit(5).all()
    app_running = java_controller.is_running()
    
    return render_template('status.html', 
                         events=latest_events, 
                         app_running=app_running,
                         health_status=health_status[1])

@app.route('/manage/logs')
def logs():
    page = request.args.get('page', 1, type=int)
    date = request.args.get('date', datetime.now().strftime('%Y-%m-%d'))
    
    # Query logs for the specific date with pagination
    logs = Log.query.filter(
        db.func.date(Log.timestamp) == date
    ).paginate(page=page, per_page=100, error_out=False)
    
    return render_template('logs.html', logs=logs, current_date=date)

# Proxy route for Java app
@app.route('/api/<path:path>', methods=['GET', 'POST', 'PUT', 'DELETE'])
def proxy(path):
    url = f"{app.config['JAVA_APP_URL']}/{path}"
    
    try:
        resp = requests.request(
            method=request.method,
            url=url,
            headers={key: value for (key, value) in request.headers if key != 'Host'},
            data=request.get_data(),
            cookies=request.cookies,
            allow_redirects=False)

        return (resp.content, resp.status_code, resp.headers.items())
    except requests.exceptions.RequestException:
        return {'error': 'Java application is not responding'}, 503

def background_tasks():
    while True:
        try:
            # Collect logs
            success, logs_data = log_collector.fetch_logs()
            if success:
                log_collector.store_logs(db, Log, logs_data)

            # Check health
            health_status = health_checker.check_java_app_health()
            if not health_status[0]:
                # Log unhealthy status
                db.session.add(StatusEvent(
                    event_type='health_check',
                    status='unhealthy',
                    details=str(health_status[1])
                ))
                db.session.commit()

        except Exception as e:
            print(f"Background task error: {str(e)}")
        
        time.sleep(60)  # Run every minute

# Start background tasks
background_thread = threading.Thread(target=background_tasks, daemon=True)
background_thread.start()

if __name__ == '__main__':
    with app.app_context():
        db.create_all()
    app.run(debug=False, host='0.0.0.0')

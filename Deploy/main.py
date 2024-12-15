from flask import Flask, render_template, request, jsonify
import subprocess
import os
import datetime
from datetime import datetime
import threading
import time

app = Flask(__name__)

# Store logs and status
build_logs = []
application_status = {
    'last_git_pull': None,
    'last_build_time': None,
    'current_status': 'idle',
    'last_error': None
}

def run_command(command, cwd=None):
    """Helper function to run shell commands"""
    try:
        process = subprocess.Popen(
            command,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            shell=True,
            cwd=cwd,
            text=True
        )
        stdout, stderr = process.communicate()
        if process.returncode != 0:
            raise Exception(f"Command failed: {stderr}")
        return stdout
    except Exception as e:
        raise Exception(f"Error running command: {str(e)}")

def check_git_changes():
    """Function to check Git changes"""
    try:
        application_status['current_status'] = 'checking_git'
        repo_path = '/home/partscom/autovio_app/PartsNepal/API/partsnepal-api/'
        
        # Pull latest changes
        pull_output = run_command('git pull origin main', cwd=repo_path)
        application_status['last_git_pull'] = datetime.now()
        
        if "Already up to date" in pull_output:
            application_status['current_status'] = 'no_changes'
            return False

        # If changes detected, build and run Java application
        build_and_run_java()
        return True

    except Exception as error:
        application_status['current_status'] = 'error'
        application_status['last_error'] = str(error)
        print(f'Git check error: {error}')
        return False

def build_and_run_java():
    """Function to build and run Java application"""
    try:
        application_status['current_status'] = 'building'
        java_project_path = '/home/partscom/autovio_app/PartsNepal/API/partsnepal-api/'

        # Build with Gradle
        build_output = run_command('./gradlew clean build', cwd=java_project_path)
        build_logs.append({
            'timestamp': datetime.now(),
            'type': 'build',
            'message': build_output
        })
        
        application_status['last_build_time'] = datetime.now()

        # Run Java application
        application_status['current_status'] = 'running'
        java_process = subprocess.Popen(
            'java -jar build/libs/your-app.jar',
            shell=True,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            cwd=java_project_path,
            text=True
        )

        def log_output(pipe, log_type):
            for line in pipe:
                build_logs.append({
                    'timestamp': datetime.now(),
                    'type': log_type,
                    'message': line
                })

        # Start threads to monitor output
        threading.Thread(target=log_output, args=(java_process.stdout, 'application')).start()
        threading.Thread(target=log_output, args=(java_process.stderr, 'error')).start()

    except Exception as error:
        application_status['current_status'] = 'error'
        application_status['last_error'] = str(error)
        build_logs.append({
            'timestamp': datetime.now(),
            'type': 'error',
            'message': str(error)
        })

@app.route('/')
def index():
    page = int(request.args.get('page', 1))
    logs_per_page = 50
    
    # Filter logs for current day
    today = datetime.now().date()
    today_logs = [log for log in build_logs if log['timestamp'].date() == today]
    
    total_pages = (len(today_logs) + logs_per_page - 1) // logs_per_page
    paginated_logs = today_logs[(page-1)*logs_per_page : page*logs_per_page]
    
    return render_template('logs.html',
                         logs=paginated_logs,
                         current_page=page,
                         total_pages=total_pages)

@app.route('/status')
def status():
    return render_template('status.html', status=application_status)

@app.route('/webhook', methods=['POST'])
def webhook():
    check_git_changes()
    return '', 200

def periodic_git_check():
    """Function to periodically check for git changes"""
    while True:
        check_git_changes()
        time.sleep(5 * 60)  # Sleep for 5 minutes

if __name__ == '__main__':
    # Start the periodic git check in a separate thread
    threading.Thread(target=periodic_git_check, daemon=True).start()
    
    # For cPanel, we just need this:
    application = app
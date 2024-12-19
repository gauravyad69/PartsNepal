from flask import Flask, render_template, request, jsonify
import subprocess
import os
import datetime
from datetime import datetime
import threading
import time
import requests

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

def get_latest_release():
    """Get latest release from GitHub"""
    try:
        # You'll need to create a GitHub token with repo access
        headers = {
            'Authorization': f'token {os.getenv("GITHUB_TOKEN")}',
            'Accept': 'application/vnd.github.v3+json'
        }
        
        # Replace with your repo details
        repo_url = "https://api.github.com/repos/gauravyad69/PartsNepal/releases/latest"
        response = requests.get(repo_url, headers=headers)
        response.raise_for_status()
        
        release_data = response.json()
        for asset in release_data['assets']:
            if asset['name'] == 'np.com.parts.api-all.jar':
                return asset['browser_download_url']
        raise Exception("JAR file not found in latest release")
    except Exception as e:
        raise Exception(f"Failed to get latest release: {str(e)}")

def check_git_changes():
    """Function to check Git changes"""
    try:
        application_status['current_status'] = 'checking_git'
        
        # Get the download URL for the latest release
        download_url = get_latest_release()
        target_path = os.path.expanduser('~/app/np.com.parts.api-all.jar')
        
        # Ensure directory exists
        os.makedirs(os.path.dirname(target_path), exist_ok=True)
        
        # Download the file
        response = requests.get(download_url)
        response.raise_for_status()
        
        with open(target_path, 'wb') as f:
            f.write(response.content)
            
        application_status['last_build_time'] = datetime.now()
        
        # Stop existing Java process if running
        stop_java_process()
        
        # Run the new version
        run_java_application(target_path)
        return True

    except Exception as error:
        application_status['current_status'] = 'error'
        application_status['last_error'] = str(error)
        return False

def stop_java_process():
    """Stop existing Java process if running"""
    try:
        run_command("pkill -f 'java -jar.*np.com.parts.api-all.jar'")
    except:
        pass  # Process might not exist

def run_java_application(jar_path):
    """Run Java application"""
    try:
        application_status['current_status'] = 'running'
        
        # Start Java process
        process = subprocess.Popen(
            f'java -jar {jar_path}',
            shell=True,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True
        )

        def log_output(pipe, log_type):
            for line in pipe:
                build_logs.append({
                    'timestamp': datetime.now(),
                    'type': log_type,
                    'message': line
                })

        threading.Thread(target=log_output, args=(process.stdout, 'application')).start()
        threading.Thread(target=log_output, args=(process.stderr, 'error')).start()

    except Exception as error:
        application_status['current_status'] = 'error'
        application_status['last_error'] = str(error)

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
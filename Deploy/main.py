from flask import Flask, render_template, request, jsonify
import subprocess
import os
import datetime
from datetime import datetime
import threading
import time
import requests
import logging
import sys

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.StreamHandler(sys.stdout),
        logging.FileHandler('/var/log/autovio.log')
    ]
)
logger = logging.getLogger('autovio')

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
        # Hardcoded token (not recommended but works)
        github_token = "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" #token expiration date 2025-12-19
        headers = {
            'Authorization': f'Bearer {github_token}',
            'Accept': 'application/vnd.github+json',
            'X-GitHub-Api-Version': '2022-11-28'
        }
        
        application_status['current_status'] = 'checking_github_release'
        build_logs.append({
            'timestamp': datetime.now(),
            'type': 'info',
            'message': 'Checking for latest GitHub release'
        })
        
        # Get latest release using the documented endpoint
        repo_url = "https://api.github.com/repos/gauravyad69/PartsNepal/releases/latest"
        response = requests.get(repo_url, headers=headers)
        response.raise_for_status()
        
        release_data = response.json()
        build_logs.append({
            'timestamp': datetime.now(),
            'type': 'info',
            'message': f"Found release: {release_data.get('tag_name', 'unknown')}"
        })
        
        for asset in release_data['assets']:
            if asset['name'] == 'np.com.parts.api-all.jar':
                build_logs.append({
                    'timestamp': datetime.now(),
                    'type': 'success',
                    'message': f"Found JAR file in release: {asset['name']}"
                })
                return asset['browser_download_url']
                
        raise Exception("JAR file not found in latest release")
    except Exception as e:
        error_msg = f"Failed to get latest release: {str(e)}"
        build_logs.append({
            'timestamp': datetime.now(),
            'type': 'error',
            'message': error_msg
        })
        raise Exception(error_msg)

def check_git_changes():
    """Function to check Git changes"""
    try:
        application_status['current_status'] = 'checking_git'
        build_logs.append({
            'timestamp': datetime.now(),
            'type': 'info',
            'message': 'Starting deployment process'
        })
        
        # Get the download URL for the latest release
        download_url = get_latest_release()
        target_path = os.path.expanduser('~/app/np.com.parts.api-all.jar')
        
        # Ensure directory exists
        os.makedirs(os.path.dirname(target_path), exist_ok=True)
        
        build_logs.append({
            'timestamp': datetime.now(),
            'type': 'info',
            'message': 'Downloading JAR file'
        })
        
        # Download the file
        response = requests.get(download_url)
        response.raise_for_status()
        
        with open(target_path, 'wb') as f:
            f.write(response.content)
            
        build_logs.append({
            'timestamp': datetime.now(),
            'type': 'success',
            'message': 'JAR file downloaded successfully'
        })
            
        application_status['last_build_time'] = datetime.now()
        
        # Stop existing Java process if running
        build_logs.append({
            'timestamp': datetime.now(),
            'type': 'info',
            'message': 'Stopping existing Java process'
        })
        stop_java_process()
        
        # Run the new version
        build_logs.append({
            'timestamp': datetime.now(),
            'type': 'info',
            'message': 'Starting new Java process'
        })
        run_java_application(target_path)
        return True

    except Exception as error:
        error_msg = str(error)
        application_status['current_status'] = 'error'
        application_status['last_error'] = error_msg
        build_logs.append({
            'timestamp': datetime.now(),
            'type': 'error',
            'message': f'Deployment failed: {error_msg}'
        })
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
        
        # Start Java process with 1GB max heap
        command = f'''
        java \
        -Xmx1024m \
        -Xms512m \
        -XX:MaxMetaspaceSize=256m \
        -jar {jar_path}
        '''
        
        process = subprocess.Popen(
            command,
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
                    'message': line.strip()
                })
                # Check for successful startup
                if "Started PartsNepalApplication" in line:
                    build_logs.append({
                        'timestamp': datetime.now(),
                        'type': 'success',
                        'message': 'Application started successfully at http://0.0.0.0:9090'
                    })
                    application_status['application_url'] = 'http://0.0.0.0:9090'

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
    return render_template('status.html', 
                         status=application_status,
                         build_logs=build_logs)

@app.route('/webhook', methods=['POST'])
def webhook():
    try:
        # Verify webhook payload if needed
        payload = request.json
        
        # Check if this is a release event
        if request.headers.get('X-GitHub-Event') == 'release':
            if payload.get('action') == 'published':
                check_git_changes()
                return jsonify({'status': 'success', 'message': 'Deployment started'}), 200
        
        # For other push events
        elif request.headers.get('X-GitHub-Event') == 'push':
            if payload.get('ref') == 'refs/heads/main':  # Only deploy on main branch pushes
                check_git_changes()
                return jsonify({'status': 'success', 'message': 'Deployment started'}), 200
        
        return jsonify({'status': 'skipped', 'message': 'Event not processed'}), 200

    except Exception as error:
        application_status['current_status'] = 'error'
        application_status['last_error'] = str(error)
        return jsonify({'status': 'error', 'message': str(error)}), 500

def periodic_git_check():
    """Function to periodically check for git changes"""
    while True:
        check_git_changes()
        time.sleep(5 * 60)  # Sleep for 5 minutes

@app.route('/control/start', methods=['POST'])
def start_service():
    try:
        subprocess.run(['systemctl', 'start', 'autovio'], check=True)
        return jsonify({'status': 'success', 'message': 'Service started'})
    except Exception as e:
        return jsonify({'status': 'error', 'message': str(e)}), 500

@app.route('/control/stop', methods=['POST'])
def stop_service():
    try:
        subprocess.run(['systemctl', 'stop', 'autovio'], check=True)
        return jsonify({'status': 'success', 'message': 'Service stopped'})
    except Exception as e:
        return jsonify({'status': 'error', 'message': str(e)}), 500

@app.route('/control/fetch-latest', methods=['POST'])
def fetch_latest():
    try:
        check_git_changes()
        return jsonify({'status': 'success', 'message': 'Latest release fetched and deployed'})
    except Exception as e:
        return jsonify({'status': 'error', 'message': str(e)}), 500

if __name__ == '__main__':
    try:
        logger.info("Starting Autovio service...")
        logger.info("Current working directory: %s", os.getcwd())
        logger.info("Python path: %s", sys.executable)
        
        # Run Flask app
        app.run(
            host='0.0.0.0',
            port=5000,
            debug=False,
            use_reloader=False  # Important: disable reloader in production
        )
    except Exception as e:
        logger.error("Fatal error in main loop", exc_info=True)
        sys.exit(1)  # Exit with error status
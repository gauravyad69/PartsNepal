import subprocess
import os
import signal
import psutil
from datetime import datetime

class JavaAppController:
    def __init__(self, app_path, java_class="np.com.parts.ApplicationKt"):
        self.app_path = app_path
        self.java_class = java_class
        self.process = None
        self.pid_file = os.path.join(app_path, "app.pid")

    def start(self):
        try:
            # Check if already running
            if self.is_running():
                return False, "Application is already running"

            # Start the Java application
            process = subprocess.Popen(
                ["gradle", "run"],
                cwd=self.app_path,
                stdout=subprocess.PIPE,
                stderr=subprocess.PIPE
            )

            # Save PID
            with open(self.pid_file, 'w') as f:
                f.write(str(process.pid))

            return True, "Application started successfully"
        except Exception as e:
            return False, f"Failed to start application: {str(e)}"

    def stop(self):
        try:
            if not self.is_running():
                return False, "Application is not running"

            with open(self.pid_file, 'r') as f:
                pid = int(f.read().strip())

            # Kill the process and its children
            parent = psutil.Process(pid)
            for child in parent.children(recursive=True):
                child.kill()
            parent.kill()

            os.remove(self.pid_file)
            return True, "Application stopped successfully"
        except Exception as e:
            return False, f"Failed to stop application: {str(e)}"

    def is_running(self):
        if not os.path.exists(self.pid_file):
            return False
        
        try:
            with open(self.pid_file, 'r') as f:
                pid = int(f.read().strip())
            process = psutil.Process(pid)
            return process.is_running()
        except:
            return False
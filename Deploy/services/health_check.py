import requests
import psutil
import os

class HealthChecker:
    def __init__(self, java_app_url, pid_file):
        self.java_app_url = java_app_url
        self.pid_file = pid_file

    def check_java_app_health(self):
        # Check process
        process_status = self._check_process()
        if not process_status[0]:
            return process_status

        # Check API response
        try:
            response = requests.get(f"{self.java_app_url}/health", timeout=5)
            if response.status_code == 200:
                return True, {
                    "status": "healthy",
                    "process": process_status[1],
                    "api_response": response.json()
                }
            return False, {
                "status": "unhealthy",
                "process": process_status[1],
                "error": f"API returned {response.status_code}"
            }
        except requests.exceptions.RequestException as e:
            return False, {
                "status": "unhealthy",
                "process": process_status[1],
                "error": f"API connection failed: {str(e)}"
            }

    def _check_process(self):
        if not os.path.exists(self.pid_file):
            return False, {"status": "stopped", "error": "PID file not found"}
        
        try:
            with open(self.pid_file, 'r') as f:
                pid = int(f.read().strip())
            process = psutil.Process(pid)
            
            return True, {
                "status": "running",
                "pid": pid,
                "cpu_percent": process.cpu_percent(),
                "memory_percent": process.memory_percent(),
                "uptime": process.create_time()
            }
        except (ProcessLookupError, psutil.NoSuchProcess):
            return False, {"status": "stopped", "error": "Process not found"}
        except Exception as e:
            return False, {"status": "unknown", "error": str(e)}
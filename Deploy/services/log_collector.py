import requests
from datetime import datetime
import json

class LogCollector:
    def __init__(self, java_app_url):
        self.java_app_url = java_app_url

    def fetch_logs(self, date=None):
        try:
            # Assuming Java app has an endpoint for logs
            response = requests.get(f"{self.java_app_url}/api/logs", 
                                 params={'date': date} if date else None,
                                 timeout=5)
            if response.status_code == 200:
                return True, response.json()
            return False, f"Failed to fetch logs: {response.status_code}"
        except requests.exceptions.RequestException as e:
            return False, f"Error fetching logs: {str(e)}"

    def store_logs(self, db, Log, logs_data):
        try:
            for log in logs_data:
                log_entry = Log(
                    timestamp=datetime.fromisoformat(log['timestamp']),
                    message=log['message'],
                    level=log['level']
                )
                db.session.add(log_entry)
            db.session.commit()
            return True, "Logs stored successfully"
        except Exception as e:
            db.session.rollback()
            return False, f"Error storing logs: {str(e)}"
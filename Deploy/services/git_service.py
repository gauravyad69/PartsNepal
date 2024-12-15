import git
from datetime import datetime
import subprocess
import os

class GitService:
    def __init__(self, repo_path):
        self.repo_path = repo_path
        try:
            self.repo = git.Repo(repo_path)
        except git.exc.NoSuchPathError:
            print(f"Warning: Repository path not found: {repo_path}")
            self.repo = None

    def pull_changes(self):
        if not self.repo:
            return False, f"Repository not found at {self.repo_path}"
        try:
            origin = self.repo.remotes.origin
            origin.pull()
            return True, "Git pull successful"
        except Exception as e:
            return False, f"Git pull failed: {str(e)}"

    def get_latest_commit(self):
        if not self.repo:
            return None
        return self.repo.head.commit

    def build_project(self):
        if not os.path.exists(self.repo_path):
            return False, f"Project directory not found at {self.repo_path}"
        try:
            result = subprocess.run(
                ["gradle", "build"],
                cwd=self.repo_path,
                capture_output=True,
                text=True
            )
            if result.returncode == 0:
                return True, "Build successful"
            return False, f"Build failed: {result.stderr}"
        except Exception as e:
            return False, f"Build failed: {str(e)}"
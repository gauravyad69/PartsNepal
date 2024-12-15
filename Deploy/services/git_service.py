import git
from datetime import datetime

class GitService:
    def __init__(self, repo_path):
        self.repo_path = repo_path
        self.repo = git.Repo(repo_path)

    def pull_changes(self):
        try:
            origin = self.repo.remotes.origin
            origin.pull()
            return True, "Git pull successful"
        except Exception as e:
            return False, f"Git pull failed: {str(e)}"

    def get_latest_commit(self):
        return self.repo.head.commit

    def build_project(self):
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
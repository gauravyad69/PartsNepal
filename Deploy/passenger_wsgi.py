import sys
import os

# Update this path to match your actual virtual environment path
INTERP = "~/virtualenv/autovio_app/PartsNepal/Deploy/3.10/bin/python"

if sys.executable != INTERP:
    os.execl(INTERP, INTERP, *sys.argv)

# Add application directory to system path
sys.path.insert(0, os.path.dirname(__file__))

# Import the Flask application
from main import app as application
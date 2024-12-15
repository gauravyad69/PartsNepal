import sys
import os

# Add your application directory to Python path
INTERP = os.path.expanduser("/home/partscom/virtualenv/api.parts.com.np/3.11/bin/python")
if sys.executable != INTERP:
    os.execl(INTERP, INTERP, *sys.argv)

# Add application directory to system path
sys.path.insert(0, os.path.dirname(__file__))

# Import the Flask application
from main import app as application
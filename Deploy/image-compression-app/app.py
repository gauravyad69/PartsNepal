from flask import Flask, request, send_from_directory, render_template
from werkzeug.utils import secure_filename
import os
from PIL import Image
import time

app = Flask(__name__)
app.config['UPLOAD_FOLDER'] = 'uploads'
app.config['MAX_CONTENT_LENGTH'] = 5 * 1024 * 1024  # 5MB limit

# Create uploads directory if it doesn't exist
if not os.path.exists(app.config['UPLOAD_FOLDER']):
    os.makedirs(app.config['UPLOAD_FOLDER'])


@app.route('/')
def index():
    return render_template('main.html', result='')

@app.route('/upload', methods=['POST'])
def upload_file():
    if 'image' not in request.files:
        return 'No image uploaded', 400
    
    file = request.files['image']
    if file.filename == '':
        return 'No selected file', 400

    try:
        # Generate unique filename
        timestamp = int(time.time())
        filename = f'compressed-{timestamp}-{secure_filename(file.filename)}'
        input_path = os.path.join(app.config['UPLOAD_FOLDER'], 'temp_' + filename)
        output_path = os.path.join(app.config['UPLOAD_FOLDER'], filename)

        # Save original file temporarily
        file.save(input_path)
        
        # Get original size
        original_size = os.path.getsize(input_path)

        # Open and compress image
        with Image.open(input_path) as img:
            # Target size in bytes (300KB)
            target_size = 300 * 1024
            quality = 95
            
            while quality > 5:
                # Save with current quality
                img.save(output_path, 'JPEG', quality=quality)
                compressed_size = os.path.getsize(output_path)
                
                if compressed_size <= target_size:
                    break
                    
                quality -= 5

        # Clean up temporary file
        os.remove(input_path)

        # Generate URLs
        image_url = f'{request.host_url}uploads/{filename}'

        result_html = f'''
            <h2>Compression Results:</h2>
            <div class="stats">
                <div class="stat-box">
                    <strong>Original Size</strong>
                    <p>{original_size / 1024:.2f} KB</p>
                </div>
                <div class="stat-box">
                    <strong>Compressed Size</strong>
                    <p>{os.path.getsize(output_path) / 1024:.2f} KB</p>
                </div>
                <div class="stat-box">
                    <strong>Quality</strong>
                    <p>{quality}%</p>
                </div>
            </div>
            <div class="link-box">
                <strong>Direct Link:</strong>
                <span id="directLink">{image_url}</span>
                <button id="copyDirect" class="copy-btn" onclick="copyLink('directLink', 'copyDirect')">Copy</button>
            </div>
            <div class="link-box">
                <strong>HTML Embed:</strong>
                <span id="embedCode">&lt;img src="{image_url}" alt="Compressed image"&gt;</span>
                <button id="copyEmbed" class="copy-btn" onclick="copyLink('embedCode', 'copyEmbed')">Copy</button>
            </div>
            <img src="/uploads/{filename}" alt="Compressed image">
        '''

        return render_template('main.html', result=result_html)

    except Exception as e:
        return f'Error processing image: {str(e)}', 500

@app.route('/uploads/<filename>')
def uploaded_file(filename):
    return send_from_directory(app.config['UPLOAD_FOLDER'], filename)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=42069, debug=True) 
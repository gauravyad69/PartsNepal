const express = require('express');
const multer = require('multer');
const sharp = require('sharp');
const path = require('path');
const fs = require('fs');

const app = express();
const port = 42069;

// Create uploads directory if it doesn't exist
const uploadsDir = path.join(__dirname, 'uploads');
if (!fs.existsSync(uploadsDir)){
    fs.mkdirSync(uploadsDir);
}

// Configure multer for handling file uploads
const upload = multer({
    storage: multer.memoryStorage(),
    limits: {
        fileSize: 5 * 1024 * 1024 // 5MB limit
    }
});

// Serve static files from 'uploads' directory
app.use('/uploads', express.static('uploads'));

// Serve the HTML form
app.get('/', (req, res) => {
    res.send(`
        <!DOCTYPE html>
        <html>
        <head>
            <title>Image Compression</title>
            <style>
                body { font-family: Arial, sans-serif; max-width: 800px; margin: 0 auto; padding: 20px; }
                .result { margin-top: 20px; }
                img { max-width: 100%; }
                .link-box {
                    padding: 10px;
                    background: #f0f0f0;
                    border-radius: 4px;
                    margin: 10px 0;
                }
                .copy-btn {
                    margin-left: 10px;
                    padding: 5px 10px;
                    cursor: pointer;
                }
            </style>
            <script>
                function copyLink(elementId) {
                    const linkElement = document.getElementById(elementId);
                    navigator.clipboard.writeText(linkElement.textContent)
                        .then(() => alert('Link copied to clipboard!'))
                        .catch(err => console.error('Failed to copy:', err));
                }
            </script>
        </head>
        <body>
            <h1>Image Compression</h1>
            <form action="/upload" method="post" enctype="multipart/form-data">
                <input type="file" name="image" accept="image/*" required>
                <button type="submit">Upload and Compress</button>
            </form>
            <div class="result" id="result"></div>
        </body>
        </html>
    `);
});

// Handle image upload and compression
app.post('/upload', upload.single('image'), async (req, res) => {
    if (!req.file) {
        return res.status(400).send('No image uploaded');
    }

    try {
        const timestamp = Date.now();
        const filename = `compressed-${timestamp}-${req.file.originalname}`;
        const outputPath = path.join(uploadsDir, filename);

        // Process image with Sharp
        const image = sharp(req.file.buffer);
        const metadata = await image.metadata();

        // Target size in bytes (300KB)
        const targetSize = 300 * 1024;
        let quality = 100;
        let compressed;

        // Binary search approach to find optimal quality
        while (quality > 1) {
            compressed = await image
                .resize(metadata.width, metadata.height, {
                    fit: 'inside',
                    withoutEnlargement: true
                })
                .jpeg({ quality })
                .toBuffer();

            if (compressed.length <= targetSize) {
                break;
            }
            quality -= 5;
        }

        // Save the compressed image
        await fs.promises.writeFile(outputPath, compressed);

        // Calculate size in KB
        const finalSize = (compressed.length / 1024).toFixed(2);

        // Generate direct link to image
        const imageUrl = `${req.protocol}://${req.get('host')}/uploads/${filename}`;

        res.send(`
            <h2>Compression Results:</h2>
            <p>Original size: ${(req.file.size / 1024).toFixed(2)} KB</p>
            <p>Compressed size: ${finalSize} KB</p>
            <p>Quality setting: ${quality}%</p>
            <p>Saved as: ${filename}</p>
            <div class="link-box">
                <strong>Direct Link:</strong>
                <span id="directLink">${imageUrl}</span>
                <button class="copy-btn" onclick="copyLink('directLink')">Copy Link</button>
            </div>
            <div class="link-box">
                <strong>HTML Embed:</strong>
                <span id="embedCode">&lt;img src="${imageUrl}" alt="Compressed image"&gt;</span>
                <button class="copy-btn" onclick="copyLink('embedCode')">Copy Code</button>
            </div>
            <img src="/uploads/${filename}" alt="Compressed image">
        `);

    } catch (error) {
        console.error('Error processing image:', error);
        res.status(500).send('Error processing image');
    }
});

app.listen(port, () => {
    console.log(`Server running at http://localhost:${port}`);
});
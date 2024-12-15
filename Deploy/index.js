const express = require('express');
const { exec } = require('child_process');
const fs = require('fs');
const path = require('path');
const moment = require('moment');
const util = require('util');
const execPromise = util.promisify(exec);

const app = express();
const port = process.env.PORT || 3000;

// Store logs and status
let buildLogs = [];
let applicationStatus = {
    lastGitPull: null,
    lastBuildTime: null,
    currentStatus: 'idle',
    lastError: null
};

// Configure view engine
app.set('view engine', 'ejs');

// Middleware for parsing JSON
app.use(express.json());

// Function to check Git changes
async function checkGitChanges() {
    try {
        applicationStatus.currentStatus = 'checking_git';
        
        // Navigate to the repository directory
        const repoPath = '/path/to/your/repo';
        process.chdir(repoPath);

        // Pull latest changes
        const { stdout: pullOutput } = await execPromise('git pull origin main');
        applicationStatus.lastGitPull = new Date();
        
        if (pullOutput.includes('Already up to date')) {
            applicationStatus.currentStatus = 'no_changes';
            return false;
        }

        // If changes detected, build and run Java application
        await buildAndRunJava();
        return true;
    } catch (error) {
        applicationStatus.currentStatus = 'error';
        applicationStatus.lastError = error.message;
        console.error('Git check error:', error);
        return false;
    }
}

// Function to build and run Java application
async function buildAndRunJava() {
    try {
        applicationStatus.currentStatus = 'building';
        
        // Navigate to Java project directory
        const javaProjectPath = 'autovio_app/PartsNepal/API/parts-nepal-api';
        process.chdir(javaProjectPath);

        // Build with Gradle
        const { stdout: buildOutput } = await execPromise('./gradlew clean build');
        buildLogs.push({
            timestamp: new Date(),
            type: 'build',
            message: buildOutput
        });
        
        applicationStatus.lastBuildTime = new Date();

        // Run Java application
        applicationStatus.currentStatus = 'running';
        const javaProcess = exec('java -jar build/libs/your-app.jar');

        javaProcess.stdout.on('data', (data) => {
            buildLogs.push({
                timestamp: new Date(),
                type: 'application',
                message: data
            });
        });

        javaProcess.stderr.on('data', (data) => {
            buildLogs.push({
                timestamp: new Date(),
                type: 'error',
                message: data
            });
        });

    } catch (error) {
        applicationStatus.currentStatus = 'error';
        applicationStatus.lastError = error.message;
        buildLogs.push({
            timestamp: new Date(),
            type: 'error',
            message: error.message
        });
    }
}

// Routes
app.get('/', (req, res) => {
    const page = parseInt(req.query.page) || 1;
    const logsPerPage = 50;
    
    // Filter logs for current day
    const today = moment().startOf('day');
    const todayLogs = buildLogs.filter(log => 
        moment(log.timestamp).isSame(today, 'day')
    );

    const totalPages = Math.ceil(todayLogs.length / logsPerPage);
    const paginatedLogs = todayLogs.slice(
        (page - 1) * logsPerPage,
        page * logsPerPage
    );

    res.render('logs', {
        logs: paginatedLogs,
        currentPage: page,
        totalPages: totalPages
    });
});

app.get('/status', (req, res) => {
    res.render('status', { status: applicationStatus });
});

// Webhook endpoint for Git changes
app.post('/webhook', async (req, res) => {
    // Verify webhook secret if needed
    await checkGitChanges();
    res.sendStatus(200);
});

// Start server
app.listen(port, () => {
    console.log(`Server running on port ${port}`);
    
    // Check for changes every 5 minutes
    setInterval(checkGitChanges, 5 * 60 * 1000);
});

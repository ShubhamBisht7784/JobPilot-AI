// State management
let state = {
    isLoading: false,
    results: null,
    error: null
};

// DOM Elements
const textInput = document.getElementById('textInput');
const stepsContainer = document.querySelector('.steps-container');
const featuresGrid = document.querySelector('.features-grid');

// File handling
const dropZone = document.getElementById('dropZone');
const fileInput = document.getElementById('fileInput');

// Initialize the application
document.addEventListener('DOMContentLoaded', () => {
    initializeHowItWorks();
    initializeFeatures();
});

// Scroll to input function
function scrollToInput() {
    textInput.scrollIntoView({ behavior: 'smooth', block: 'center' });
}

// Text analysis function
async function analyzeText() {
    const text = textInput.value.trim();
    
    if (!text) {
        showError('Please enter some text to analyze');
        return;
    }

    try {
        setLoading(true);
        const response = await fetch('/api/analyze', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ text }),
        });

        if (!response.ok) {
            throw new Error('Analysis failed');
        }

        const data = await response.json();
        state.results = data;
        displayResults(data);
    } catch (error) {
        showError(error.message);
    } finally {
        setLoading(false);
    }
}

// Loading state management
function setLoading(isLoading) {
    state.isLoading = isLoading;
    const analyzeBtn = document.querySelector('.analyze-btn');
    analyzeBtn.disabled = isLoading;
    analyzeBtn.textContent = isLoading ? 'Analyzing...' : 'Analyze Text';
}

// Error handling
function showError(message) {
    state.error = message;
    // You can implement a toast or alert system here
    alert(message);
}

// Results display
function displayResults(results) {
    // Create results container if it doesn't exist
    let resultsContainer = document.querySelector('.results-container');
    if (!resultsContainer) {
        resultsContainer = document.createElement('div');
        resultsContainer.className = 'results-container';
        document.querySelector('.input-container').after(resultsContainer);
    }

    // Clear previous results
    resultsContainer.innerHTML = '';

    // Create results HTML
    const resultsHTML = `
        <div class="results-card">
            <h3>Analysis Results</h3>
            <div class="results-content">
                <div class="result-item">
                    <span class="label">Plagiarism Score:</span>
                    <span class="value">${results.plagiarismScore}%</span>
                </div>
                <div class="result-item">
                    <span class="label">Original Content:</span>
                    <span class="value">${results.originalContent}%</span>
                </div>
                ${results.matches ? `
                    <div class="matches-section">
                        <h4>Similar Content Found:</h4>
                        <ul class="matches-list">
                            ${results.matches.map(match => `
                                <li>
                                    <a href="${match.source}" target="_blank">${match.source}</a>
                                    <span class="similarity">${match.similarity}% similar</span>
                                </li>
                            `).join('')}
                        </ul>
                    </div>
                ` : ''}
            </div>
        </div>
    `;

    resultsContainer.innerHTML = resultsHTML;
}

// Initialize How It Works section
function initializeHowItWorks() {
    const steps = [
        {
            title: 'Paste Your Text',
            description: 'Copy and paste your text into the input field above.',
            icon: '📝'
        },
        {
            title: 'Analyze',
            description: 'Click the analyze button to start the plagiarism check.',
            icon: '🔍'
        },
        {
            title: 'Get Results',
            description: 'Receive detailed analysis of your text with similarity scores.',
            icon: '📊'
        }
    ];

    stepsContainer.innerHTML = steps.map(step => `
        <div class="step-card">
            <div class="step-icon">${step.icon}</div>
            <h3>${step.title}</h3>
            <p>${step.description}</p>
        </div>
    `).join('');
}

// Initialize Features section
function initializeFeatures() {
    const features = [
        {
            title: 'Advanced Detection',
            description: 'Our AI-powered system detects even the most sophisticated forms of plagiarism.',
            icon: '🛡️'
        },
        {
            title: 'Real-time Analysis',
            description: 'Get instant results with our fast and efficient analysis engine.',
            icon: '⚡'
        },
        {
            title: 'Detailed Reports',
            description: 'Receive comprehensive reports with source links and similarity scores.',
            icon: '📋'
        }
    ];

    featuresGrid.innerHTML = features.map(feature => `
        <div class="feature-card">
            <div class="feature-icon">${feature.icon}</div>
            <h3>${feature.title}</h3>
            <p>${feature.description}</p>
        </div>
    `).join('');
}

// Add smooth scrolling for navigation links
document.querySelectorAll('a[href^="#"]').forEach(anchor => {
    anchor.addEventListener('click', function (e) {
        e.preventDefault();
        const target = document.querySelector(this.getAttribute('href'));
        if (target) {
            target.scrollIntoView({
                behavior: 'smooth',
                block: 'start'
            });
        }
    });
});

// Prevent default drag behaviors
['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
    dropZone.addEventListener(eventName, preventDefaults, false);
    document.body.addEventListener(eventName, preventDefaults, false);
});

// Highlight drop zone when item is dragged over it
['dragenter', 'dragover'].forEach(eventName => {
    dropZone.addEventListener(eventName, highlight, false);
});

['dragleave', 'drop'].forEach(eventName => {
    dropZone.addEventListener(eventName, unhighlight, false);
});

// Handle dropped files
dropZone.addEventListener('drop', handleDrop, false);
fileInput.addEventListener('change', handleFileSelect, false);

function preventDefaults(e) {
    e.preventDefault();
    e.stopPropagation();
}

function highlight(e) {
    dropZone.classList.add('dragover');
}

function unhighlight(e) {
    dropZone.classList.remove('dragover');
}

function handleDrop(e) {
    const dt = e.dataTransfer;
    const files = dt.files;
    handleFiles(files);
}

function handleFileSelect(e) {
    const files = e.target.files;
    handleFiles(files);
}

function handleFiles(files) {
    if (files.length === 0) return;

    const file = files[0];
    if (!isValidFileType(file)) {
        showError('Please upload a valid text file (.txt, .doc, .docx)');
        return;
    }

    setLoading(true);
    readFile(file);
}

function isValidFileType(file) {
    const validTypes = ['.txt', '.doc', '.docx'];
    const fileExtension = '.' + file.name.split('.').pop().toLowerCase();
    return validTypes.includes(fileExtension);
}

function readFile(file) {
    const reader = new FileReader();

    reader.onload = function(e) {
        const content = e.target.result;
        textInput.value = content;
        setLoading(false);
    };

    reader.onerror = function() {
        showError('Error reading file');
        setLoading(false);
    };

    if (file.name.endsWith('.txt')) {
        reader.readAsText(file);
    } else {
        // For .doc and .docx files, we'll need to handle them differently
        // This is a placeholder for future implementation
        showError('Word document support coming soon');
        setLoading(false);
    }
} 
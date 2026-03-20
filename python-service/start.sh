#!/bin/bash

echo "Starting GLiNER FastAPI Service..."

cd "$(dirname "$0")"

if [ ! -d "venv" ]; then
    echo "Creating virtual environment..."
    python3 -m venv venv
fi

echo "Activating virtual environment..."
source venv/bin/activate

echo "Installing dependencies..."
pip install -r requirements.txt

# Check if model is downloaded
if [ ! -d "models/gliner_multi_pii-v1" ]; then
    echo ""
    echo "⚠️  Local model not found!"
    echo "Downloading GLiNER model for offline use..."
    echo "This is a one-time download (~500MB)"
    echo ""
    python download_model.py
    echo ""
fi

echo "Starting FastAPI server on port 5001..."
python main.py

#!/bin/bash

echo "Installing dependencies from offline cache..."

cd "$(dirname "$0")"

if [ ! -d "offline_packages" ]; then
    echo "❌ Error: offline_packages directory not found!"
    echo "Please run setup_offline.sh first to download packages."
    exit 1
fi

if [ ! -d "venv" ]; then
    echo "Creating virtual environment..."
    python3 -m venv venv
fi

echo "Activating virtual environment..."
source venv/bin/activate

echo "Installing packages from offline cache..."
pip install --no-index --find-links=offline_packages -r requirements.txt

echo ""
echo "✓ All dependencies installed from offline cache!"
echo "✓ No internet connection required"

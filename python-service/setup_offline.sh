#!/bin/bash

echo "Setting up offline-capable Python environment..."

cd "$(dirname "$0")"

# Create virtual environment if it doesn't exist
if [ ! -d "venv" ]; then
    echo "Creating virtual environment..."
    python3 -m venv venv
fi

echo "Activating virtual environment..."
source venv/bin/activate

# Upgrade pip
echo "Upgrading pip..."
pip install --upgrade pip

# Create a directory for offline packages
echo "Creating offline package cache..."
mkdir -p offline_packages

# Download all packages and dependencies to offline_packages directory
echo "Downloading all packages and dependencies..."
echo "This may take several minutes (~2GB)..."
pip download -r requirements.txt -d offline_packages

echo ""
echo "✓ Offline package cache created successfully!"
echo "✓ Location: $(pwd)/offline_packages"
echo ""
echo "To install from offline cache, run:"
echo "  pip install --no-index --find-links=offline_packages -r requirements.txt"

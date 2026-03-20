#!/usr/bin/env python3
"""
Download GLiNER model from HuggingFace and save it locally.
This script downloads the model once and saves it to the models/ directory.
"""

from gliner import GLiNER
import os

def download_model():
    model_name = "urchade/gliner_multi_pii-v1"
    local_model_path = "./models/gliner_multi_pii-v1"
    
    print(f"Downloading GLiNER model: {model_name}")
    print(f"This may take a few minutes (~500MB)...")
    
    # Download model from HuggingFace
    model = GLiNER.from_pretrained(model_name)
    
    # Create models directory if it doesn't exist
    os.makedirs(local_model_path, exist_ok=True)
    
    # Save model locally
    print(f"\nSaving model to: {local_model_path}")
    model.save_pretrained(local_model_path)
    
    print(f"\n✓ Model downloaded and saved successfully!")
    print(f"✓ Location: {os.path.abspath(local_model_path)}")
    print(f"\nYou can now run the service offline using the local model.")

if __name__ == "__main__":
    download_model()

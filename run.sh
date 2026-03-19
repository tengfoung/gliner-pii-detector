#!/bin/bash

# Set Java 17
export JAVA_HOME=/usr/local/opt/openjdk@17
export PATH="$JAVA_HOME/bin:$PATH"

echo "========================================="
echo "GLiNER PII Multi-Detector"
echo "========================================="
echo ""

# Check Java version
echo "Checking Java version..."
java -version 2>&1 | head -n 1

# Check Maven version
echo ""
echo "Checking Maven version..."
mvn -version | head -n 1

# Check model files
echo ""
echo "Checking model files..."
if [ -f "./src/main/resources/model.onnx" ]; then
    echo "✓ model.onnx found"
else
    echo "✗ model.onnx NOT found"
    exit 1
fi

if [ -f "./src/main/resources/tokenizer.json" ]; then
    echo "✓ tokenizer.json found"
else
    echo "✗ tokenizer.json NOT found"
    exit 1
fi

echo ""
echo "========================================="
echo "Building and starting application..."
echo "========================================="
echo ""

mvn spring-boot:run

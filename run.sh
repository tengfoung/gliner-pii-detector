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

echo ""
echo "========================================="
echo "Building and starting application..."
echo "========================================="
echo ""

mvn spring-boot:run

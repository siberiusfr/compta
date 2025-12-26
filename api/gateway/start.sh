#!/bin/bash

# ============================================
# COMPTA Gateway - Quick Start Script
# ============================================

echo "🚀 Starting COMPTA Gateway Service..."
echo ""

# Check if .env file exists
if [ ! -f .env ]; then
    echo "⚠️  .env file not found. Creating from .env.example..."
    cp .env.example .env
    echo "✅ .env file created. Please edit it with your configuration."
    echo ""
fi

# Check if Redis is running
echo "🔍 Checking Redis..."
if command -v redis-cli &> /dev/null; then
    if redis-cli ping &> /dev/null; then
        echo "✅ Redis is running"
    else
        echo "⚠️  Redis is not running. Starting Redis with Docker..."
        docker run -d -p 6379:6379 --name compta-redis redis:7-alpine
        sleep 2
        echo "✅ Redis started"
    fi
else
    echo "⚠️  redis-cli not found. Make sure Redis is installed or use Docker:"
    echo "   docker run -d -p 6379:6379 --name compta-redis redis:7-alpine"
fi

echo ""
echo "📦 Building the project..."
mvn clean install -DskipTests

if [ $? -eq 0 ]; then
    echo "✅ Build successful"
    echo ""
    echo "🎯 Starting Gateway Service..."
    echo ""
    
    # Load .env file
    export $(cat .env | xargs)
    
    # Start the application
    mvn spring-boot:run -Dspring-boot.run.profiles=dev
else
    echo "❌ Build failed. Please check the errors above."
    exit 1
fi

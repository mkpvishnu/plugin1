#!/bin/bash
# Seasons of Conflict - Build Script
# Version 1.0.0

set -e

echo "========================================="
echo "  Seasons of Conflict - Build Script"
echo "  Version 1.0.0"
echo "========================================="
echo ""

# Check for Java
if ! command -v java &> /dev/null; then
    echo "❌ Error: Java not found!"
    echo "Please install JDK 17 or higher"
    exit 1
fi

# Check for Maven
if ! command -v mvn &> /dev/null; then
    echo "❌ Error: Maven not found!"
    echo "Please install Maven 3.6 or higher"
    exit 1
fi

# Display versions
echo "📋 Checking prerequisites..."
echo "Java version:"
java -version 2>&1 | head -1
echo "Maven version:"
mvn -version | head -1
echo ""

# Clean previous builds
echo "🧹 Cleaning previous builds..."
mvn clean -q

# Build the plugin
echo "🔨 Building plugin..."
echo "This may take a minute on first build (downloading dependencies)..."
echo ""

if mvn package -DskipTests; then
    echo ""
    echo "========================================="
    echo "✅ BUILD SUCCESSFUL!"
    echo "========================================="
    echo ""
    echo "📦 JAR Location: target/SeasonsOfConflict-1.0.0.jar"
    
    # Display JAR info
    if [ -f target/SeasonsOfConflict-1.0.0.jar ]; then
        SIZE=$(ls -lh target/SeasonsOfConflict-1.0.0.jar | awk '{print $5}')
        echo "📊 JAR Size: $SIZE"
        echo ""
        echo "🚀 Next Steps:"
        echo "1. Copy JAR to your server: cp target/SeasonsOfConflict-1.0.0.jar /path/to/server/plugins/"
        echo "2. Restart your Minecraft server"
        echo "3. Configure: plugins/SeasonsOfConflict/config.yml"
        echo ""
        echo "📖 Documentation: README.md"
    fi
else
    echo ""
    echo "========================================="
    echo "❌ BUILD FAILED!"
    echo "========================================="
    echo ""
    echo "See error messages above for details."
    echo "Common issues:"
    echo "  - No internet connection (Maven needs to download dependencies)"
    echo "  - Wrong Java version (need JDK 17+)"
    echo "  - Maven not properly configured"
    echo ""
    echo "For help, see BUILD.md"
    exit 1
fi

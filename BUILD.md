# Building Seasons of Conflict

This guide will help you build the plugin JAR file from source.

## Prerequisites

- **Java Development Kit (JDK) 17 or higher**
- **Apache Maven 3.6+**
- **Internet connection** (for downloading dependencies)

### Install Prerequisites

**Windows:**
```powershell
# Install via Chocolatey
choco install openjdk17 maven

# Or download manually:
# JDK: https://adoptium.net/
# Maven: https://maven.apache.org/download.cgi
```

**macOS:**
```bash
# Install via Homebrew
brew install openjdk@17 maven
```

**Linux (Ubuntu/Debian):**
```bash
sudo apt update
sudo apt install openjdk-17-jdk maven
```

## Quick Build

```bash
# 1. Navigate to project directory
cd /path/to/plugin1

# 2. Build the plugin
mvn clean package

# 3. Find your JAR
ls -lh target/SeasonsOfConflict-1.0.0.jar
```

The compiled JAR will be in: `target/SeasonsOfConflict-1.0.0.jar`

## Build Commands

### Standard Build
```bash
mvn clean package
```

### Build without tests (faster)
```bash
mvn clean package -DskipTests
```

### Clean build directory
```bash
mvn clean
```

### Verify dependencies
```bash
mvn dependency:tree
```

## Expected Output

Successful build output:
```
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  XX.XXX s
[INFO] Finished at: YYYY-MM-DDTHH:MM:SS
[INFO] ------------------------------------------------------------------------
```

JAR file: `target/SeasonsOfConflict-1.0.0.jar` (~50-100 KB)

## Dependencies

The plugin uses these dependencies (auto-downloaded by Maven):

- **Spigot API 1.20.1** - Minecraft server API
- **SQLite JDBC 3.42.0.0** - Database driver

## Build Script

For convenience, use the provided build script:

**Linux/macOS:**
```bash
chmod +x build.sh
./build.sh
```

**Windows:**
```powershell
.\build.bat
```

## Troubleshooting

### "mvn: command not found"
- Maven is not installed or not in PATH
- Install Maven following prerequisites section

### "JAVA_HOME is not set"
```bash
# Linux/macOS
export JAVA_HOME=/path/to/jdk-17
export PATH=$JAVA_HOME/bin:$PATH

# Windows (PowerShell)
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
```

### "Compilation failure"
- Ensure you're using JDK 17 or higher
- Check `java -version` and `mvn -version`

### "Could not resolve dependencies"
- Check internet connection
- Maven needs to download Spigot API (~10 MB)
- Try: `mvn dependency:resolve`

### Build is slow
- First build downloads dependencies (~50 MB total)
- Subsequent builds are much faster
- Use `-DskipTests` to skip test compilation

## Manual Build (Without Maven)

If Maven fails, you can manually compile (advanced):

```bash
# Download Spigot API
wget https://hub.spigotmc.org/nexus/content/repositories/snapshots/org/spigotmc/spigot-api/1.20.1-R0.1-SNAPSHOT/spigot-api-1.20.1-R0.1-SNAPSHOT.jar

# Download SQLite JDBC
wget https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.42.0.0/sqlite-jdbc-3.42.0.0.jar

# Compile
javac -cp "spigot-api-1.20.1-R0.1-SNAPSHOT.jar:sqlite-jdbc-3.42.0.0.jar" \
  -d build/classes \
  src/main/java/com/seasonsofconflict/**/*.java

# Create JAR
cd build/classes
jar cvf ../../SeasonsOfConflict-1.0.0.jar .
cd ../..

# Add resources
jar uf SeasonsOfConflict-1.0.0.jar -C src/main/resources .
```

## Deployment

After successful build:

```bash
# 1. Locate the JAR
cd target/

# 2. Copy to your Minecraft server
cp SeasonsOfConflict-1.0.0.jar /path/to/minecraft/server/plugins/

# 3. Restart server
cd /path/to/minecraft/server/
./restart.sh  # or whatever your restart command is
```

## Verifying Installation

1. Start your Minecraft server
2. Check console for: `[SeasonsOfConflict] Seasons of Conflict has been enabled!`
3. In-game, type: `/soc` (should show admin commands if you're OP)
4. Check: `plugins/SeasonsOfConflict/` folder was created

## Build Information

- **Source Files:** 47 Java files
- **Lines of Code:** ~5,000
- **Compiled Size:** ~50-100 KB
- **With Dependencies:** ~500 KB (shaded JAR)

## Next Steps

After building:
1. Read [README.md](README.md) for configuration
2. Edit `plugins/SeasonsOfConflict/config.yml`
3. Set up your 5000x5000 world
4. Configure beacon coordinates
5. Test with players!

## Support

- **Issues:** https://github.com/mkpvishnu/plugin1/issues
- **Documentation:** [README.md](README.md)
- **Configuration:** See `src/main/resources/config.yml` for examples

---

**Build Time:** ~30-60 seconds (first build)  
**Rebuild Time:** ~5-10 seconds

Happy building! 🛠️

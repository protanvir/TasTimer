# RFID Timing System

A Java-based RFID timing application supporting both **Impinj Octane** and **Zebra FX Series** readers. It includes a Node.js mock server for testing and simulation.

## Features
- **Dual Reader Support**: Connect to either Impinj (Octane SDK) or Zebra (API3 SDK) readers.
- **Unified Interface**: Real-time tag reading visualization regardless of hardware.
- **Mock Server**: Node.js server to simulate tag reads (`http://localhost:3000`).
- **Data Export**: Save tag reads to CSV and send data to Wiclax / Local API.

## Prerequisites

1.  **Java Development Kit (JDK)**
    *   Version 21 or higher recommended.
    *   Ensure `java` and `javac` are in your PATH.
2.  **Node.js**
    *   Required for the mock server simulation.
3.  **Zebra SDK Dependencies (Windows Only)**
    *   **Requirement**: To use Zebra readers, the `RFIDAPI32PC.dll` file (Windows) or `.so` files (Linux) must be accessible.
    *   The `build_release.bat` script automatically copies these libraries to the distribution folder.
    *   If running manually, ensure the native libraries are in your path.

## Project Structure

*   `src/`: Java source code.
*   `lib/`: External dependencies (`octane-sdk.jar`, `Symbol.RFID.API3.jar`).
*   `mock-server/`: Node.js server to simulate RFID tag reads.
*   `build_release.bat`: Windows batch script to compile and create a release.
*   `run.sh`: Linux/macOS shell script.
*   `data/`: Directory where CSV logs are saved.

## Installation & Building

### Windows
1.  Run the build script:
    ```cmd
    build_release.bat
    ```
2.  This generates a `dist` folder containing:
    *   `TimingSoft.jar`
    *   `run.bat`
    *   `lib/` (Dependencies)
    *   `mock-server/`
    *   `lib/` (Dependencies)
    *   `mock-server/`
    *   `RFIDAPI32PC.dll` and Linux `.so` libraries

## Running the Application

### Using the Release (Recommended)
1.  Navigate to the `dist` folder.
2.  Run `run.bat`. This will:
    *   Start the Node.js mock server.
    *   Launch the Java GUI.

### Manual / Development Mode
1.  **Start Mock Server**:
    ```bash
    node mock-server/server.js
    ```
2.  **Run Java Application**:
    ```bash
    java -cp "lib/*;bin" TimingSystemGUI
    # Or if compiling from source directly:
    javac -cp "lib/*;." src/*.java
    java -cp "lib/*;src" TimingSystemGUI
    ```

## Usage
1.  **Select Reader Type**:
    *   **Impinj**: For Impinj Speedway/R-Series readers.
    *   **Zebra**: For Zebra FX7500/FX9600 readers.
2.  **Enter IP Address**: Input the IP of your reader.
3.  **Connect**: Click the **Connect** button.
    *   Status indicators for antennas (1-4) will light up upon successful connection and tag reads.

## Dependencies
*   **Impinj Octane SDK**: `lib/octane-sdk.jar`
*   **Zebra RFID API3 SDK**: `lib/Symbol.RFID.API3.jar` + Native Libraries (`.dll` for Windows, `.so` for Linux)

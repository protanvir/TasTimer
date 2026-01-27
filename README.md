# RFID Timing System

A Java-based RFID timing application with a Node.js mock server for testing and simulation. This system is designed to read RFID tags via the Impinj Octane SDK and display timing data.

## Prerequisites

Before running the application, ensure you have the following installed:

1.  **Java Development Kit (JDK)**
    *   Version 21 or higher is recommended.
    *   Ensure `java` and `javac` are in your system PATH.
2.  **Node.js**
    *   Required for running the mock server simulation.
    *   Download from [nodejs.org](https://nodejs.org/).

## Project Structure

*   `src/`: Java source code.
*   `lib/`: External dependencies (Impinj Octane SDK).
*   `mock-server/`: Node.js server to simulate RFID tag reads.
*   `build_release.bat`: Windows batch script to compile and create a release.
*   `run.sh`: Linux/macOS shell script to run the application (used in the release folder).

## Installation & Building

To build the project from source, use the provided build script.

### Windows
1.  Open a command prompt or PowerShell in the project root.
2.  Run the build script:
    ```cmd
    build_release.bat
    ```
3.  This will:
    *   Clean previous builds.
    *   Compile the Java source code.
    *   Package the application into a JAR file.
    *   Create a portable distribution folder named `dist`.

## Running the Application

The easiest way to run the application is using the generated release in the `dist` folder.

### Windows
1.  Navigate to the `dist` folder:
    ```cmd
    cd dist
    ```
2.  Double-click `run.bat` or run it from the command line.
    *   This will automatically start the mock server in the background and launch the Java GUI.

### Linux / macOS
1.  Navigate to the `dist` folder.
2.  Ensure `run.sh` is executable:
    ```bash
    chmod +x run.sh
    ```
3.  Run the script:
    ```bash
    ./run.sh
    ```

### Manual / Development Mode

If you prefer to run components manually or during development:

1.  **Start the Mock Server:**
    ```bash
    node mock-server/server.js
    ```
    The server will listen on `http://localhost:3000`. You can view live reads in your browser at this address.

2.  **Run the Java Application:**
    *   **From Source (after compiling to `bin`):**
        ```bash
        java -cp "lib/octane-sdk.jar;bin" TimingSystemGUI
        ```
        *(Note: On Linux/Mac use `:` instead of `;` as the classpath separator)*

    *   **Using the JAR:**
        ```bash
        java -jar dist/TimingSoft.jar
        ```

## Mock Server API

The mock server simulates an RFID reader pushing data to the application.
*   **Web Interface:** `http://localhost:3000` (Displays active tag reads).
*   **POST Endpoint:** `/api/tags` - Accepts JSON tag data.

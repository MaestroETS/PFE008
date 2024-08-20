# Backend Documentation

Author: Charlie Poncsak

## Project Overview

This backend is a RESTful API built using Gradle, Spring Boot, and Java. It facilitates the conversion of music sheets (in PDF, PNG, or other formats) to MIDI files using the Audiveris software. The application is designed to work in conjunction with a frontend and runs Audiveris in CLI mode using a custom distribution.

## API Documentation

### Endpoint Overview

There is currently one main route in the API:

#### `POST /convert`
- **Description**: Converts a music sheet file (PDF, PNG, or other supported formats) into a MIDI file.
- **Request**: Accepts a multipart file upload.
- **Response**: Returns a JSON object with details of the converted MIDI file.

**Example Request:**

```bash
curl -X POST -F "file=@path/to/file.pdf" http://localhost:8080/convert --output output.midi
```

The converted MIDI file will be saved as `output.midi`.

### Error Handling
- **400 Bad Request**: Returned if no file is provided or if the file is invalid (e.g., incorrect format or too large).
- **500 Internal Server Error**: Returned if the conversion fails due to a processing error.

## Setup Instructions

### Prerequisites

1. **Java Installation**:
   - Ensure you have Java installed. The version used for this project is:
     ```bash
     java version "22.0.1" 2024-04-16
     ```

2. **Gradle**:
   - The project includes a Gradle wrapper, so you don’t need to install Gradle manually. If needed, you can download Gradle [here](https://gradle.org/releases/). The project uses `Gradle v8.8`.

3. **OCR Language Data**:
   - Audiveris relies on Tesseract for Optical Character Recognition (OCR). You’ll need to install language data files. Refer to the [Audiveris guide](https://audiveris.github.io/audiveris/_pages/install/languages/) for setup instructions.

4. **Audiveris Setup**:
   - The Audiveris distribution is not included in the repository. You need to download it manually and place it in the `backend/audiveris/dist` directory.
   - Download the Audiveris distribution (Windows installer) from [here](https://github.com/Audiveris/audiveris/releases/tag/5.3.1).
   - After installation, copy the `bin` and `lib` directories from the Audiveris installation directory to `backend/audiveris/dist/`.
   
   Your project structure should look like this:

   ```
   backend
   │   README.md
   │   ...
   └───audiveris
       └───dist
           └───bin
           └───lib
   ```

### Running the Backend

1. **Navigate to the Project Directory**:
   ```bash
   cd backend
   ```

2. **Start the Backend**:
   Run the following command in PowerShell to start the backend on port 8080:
   ```bash
   .\gradlew.bat bootRun
   # or
   ./gradlew bootRun
   ```
   If everything is set up correctly, you should see output similar to:
   ```
   Started BackendApplication in x.xx seconds (process running for x.xxx)
   <==========---> 80% EXECUTING [0m 00s]
   ```

3. **Stopping the Backend**:
   - Press `Ctrl + C` to stop the application.
   - Confirm by pressing `Y` if prompted.

## Additional Notes

- **Configuration Options**: The API currently accepts requests from any origin (CORS policy is open). Modify the `@CrossOrigin` annotations in the `ConvertController` class if you need to restrict access.
- **File Size Limits**: Files larger than 10MB are automatically rejected.
- **Supported File Formats**: The backend supports `.pdf`, `.png`, `.jpg`, and `.jpeg` formats for conversion.

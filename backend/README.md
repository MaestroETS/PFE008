# Backend Documentation

Charlie Poncsak

## Description

This is the backend for the project. It is a RESTful API that serves data to the frontend. It is built using Gradle, Spring Boot, and Java.
It runs Audiveris in CLI using an Audiveris distribution.

## API Documentation

There is one route in the API:

- `POST /convert` - This route takes a multipart file upload with a PDF, PNG or other file and returns a JSON object with the converted MIDI file.

Example request:
```
curl -X POST -F "file=@path/to/file.pdf" http://localhost:8080/convert --output midi.midi
```

## Setup

1. Install Java. Exact version used is `java version "22.0.1" 2024-04-16`.
2. Gradle may also need to be installed. The Gradle wrapper is included in the repository, so it should not be necessary to install Gradle separately. Just in case, the version used is `Gradle v8.8` (download [here](https://gradle.org/releases/))
3. Install OCR language data. Audiveris uses Tesseract for OCR. Audiveris has a quick guide to set this up [here](https://audiveris.github.io/audiveris/_pages/install/languages/).
4. Audiveris files aren't included in the repository. You will need to download an Audiveris distribution and place it in the `backend/audiveris/dist` directory.
The simplest way is to use the .exe Windows installer for Audiveris from [here](https://github.com/Audiveris/audiveris/releases/tag/5.3.1).
Take note of the path to the Audiveris files. Once installed, copy the `bin` and the `lib` directories and their content from the installation directory to `backend/audiveris/dist/`.
File structure should look like this:
```
backend
│   README.md
│   ...
└───audiveris
│   └───dist
│       └───bin
│       └───lib
```

## Running the Backend

Run the backend by running the `bootRun` Gradle task. This will start the backend on port 8080.
To do so in powershell, cd into the backend directory and run `.\gradlew.bat bootRun`.
If done right, you should see a console output that says something like this :
```
Started BackendApplication in x.xx seconds (process running for x.xxx)
<==========---> 80% EXECUTING [0m 00s]
```

To exit, press `Ctrl+C` and then `Y` to confirm.

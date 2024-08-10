# Maestro

[![AGPL License](https://img.shields.io/badge/license-AGPL-blue.svg)](http://www.gnu.org/licenses/agpl-3.0)

Maestro is a web application that effortlessly converts music sheets into MIDI files using an advanced optical music recognition engine. Simply upload your PNG, PDF, or JPG files, adjust the tempo to your preference, and let the magic unfold.

<p align="center">
  <img src="https://github.com/PrimePhil/PFE008/blob/main/screenshots/meastro-banner.png" />
</p>

## Requirements
- Audiveris
- Java 22.0.2
- Python 3.12
- npm 8.19.4
- Nodejs 18.13.0

## Getting Started

Start by cloning the project

```bash
  git clone https://github.com/PrimePhil/PFE008
```

#### Frontend

From the root of the project, go to the frontend project directory

```bash
  cd maestro-app
```

Install dependencies

```bash
  npm install
```

Start the server

```bash
  npm run start
```

The web app will be available at **localhost:3000/**

#### Backend

Running the backend requires more steps, please see `backend/readme.md` for a step by step guide.


#### Tests
To run the tests suites go into the backend repository and run the command 
```bash
gradle test
```
It is important to note that the python's tests will be executed with the gradle command

#### Deployment
To build docker's image move to the root (where the Dockerfile is) and use : 
```bash
docker build -t maestro .
```
When you have a docker's image, you can run it with the command : 
```bash
docker run -p <portNumber>:3000 -p <portNumber>:8080 maestro
```

Note : To deploy the application, you must install Docker before hand.

## Screenshots

<p align="center">
  <img src="https://github.com/PrimePhil/PFE008/blob/main/screenshots/maestro-app-screenshot.png" />
</p>


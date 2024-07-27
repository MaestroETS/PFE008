#!/bin/bash

#Start Backend
java -jar backend/build/libs/backend-0.0.1-SNAPSHOT.jar &

#Start frontend
npm --prefix maestro-app/ run start
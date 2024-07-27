FROM ubuntu:latest

EXPOSE 3000
EXPOSE 8080

RUN apt-get update && apt-get upgrade -y && apt-get install -y \
    wget \
    curl \
    unzip \
    software-properties-common \
    lsb-release \
    gradle \
    git \
    maven \
    flatpak

# Installer le repo d'oracle pour installer Java 22
RUN wget https://download.oracle.com/java/22/latest/jdk-22_linux-x64_bin.deb && \
    apt install ./jdk-22_linux-x64_bin.deb

#Installer Python3.12 via le repo deadsnakes
RUN add-apt-repository ppa:deadsnakes/ppa -y && \
    apt update && \
    apt install python3.12 python3.12-venv pip -y

#Installer Gradle 8.8
RUN wget https://services.gradle.org/distributions/gradle-8.8-bin.zip && \
    unzip -d /opt/gradle ./gradle-8.8-bin.zip 

#Installer npm 8.19.3 et Nodejs 10.13.0
RUN curl -fsSL https://nodejs.org/dist/v18.13.0/node-v18.13.0-linux-x64.tar.xz | tar -xJf - -C /usr/local --strip-components=1
RUN npm install -g npm@8.19.4

#Installer Tesseract pour Audiveris
RUN mkdir -p /usr/share/tesseract-ocr/4.00/tessdata/ && \
    wget -P /usr/share/tesseract-ocr/4.00/tessdata/ https://github.com/tesseract-ocr/tessdata/raw/main/eng.traineddata && \
    wget -P /usr/share/tesseract-ocr/4.00/tessdata/ https://github.com/tesseract-ocr/tessdata/raw/main/deu.traineddata && \
    wget -P /usr/share/tesseract-ocr/4.00/tessdata/ https://github.com/tesseract-ocr/tessdata/raw/main/fra.traineddata && \
    wget -P /usr/share/tesseract-ocr/4.00/tessdata/ https://github.com/tesseract-ocr/tessdata/raw/main/ita.traineddata


#Installer Audiveris
RUN flatpak remote-add --if-not-exists flathub https://dl.flathub.org/repo/flathub.flatpakrepo && \
    flatpak install -y flathub org.audiveris.audiveris

#Installer les requirements Python
RUN python3.12 -m venv /venv && \
    /venv/bin/pip install --upgrade pip && \
    /venv/bin/pip install music21

ENV PATH="/venv/bin:${PATH}"
ENV JAVA_HOME=/usr/lib/jvm/jdk-22.0.2-oracle-x64
ENV PATH=$JAVA_HOME/bin:/opt/gradle/gradle-8.8/bin:$PATH
ENV TESSDATA_PREFIX=/usr/share/tesseract-ocr/4.00/


WORKDIR /app

COPY backend ./backend
COPY maestro-app ./maestro-app

#Le script d'entré
COPY entrypoint.sh ./entrypoint.sh 
RUN chmod +x ./entrypoint.sh

RUN cp -r $(flatpak info --show-location org.audiveris.audiveris)/files/bin backend/Audiveris/dist/
RUN cp -r $(flatpak info --show-location org.audiveris.audiveris)/files/lib backend/Audiveris/dist/

RUN gradle -p backend/ clean build

RUN npm --prefix maestro-app/ install

#Pour build : docker build -t maestro-app .
#Pour exécuter l'application : docker run -p 3000:3000 -p 8080:8080 maestro-app
ENTRYPOINT ["./entrypoint.sh"]

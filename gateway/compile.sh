#!/bin/sh

if [ ! -d "bin" ]; then
    mkdir bin
fi

javac -d ./bin -sourcepath ./src/main/java -cp ./lib/jssc-2.8.0.jar:./lib/org.eclipse.paho.client.mqttv3-1.2.0.jar ./src/main/java/gateway/Main.java -Xlint:unchecked




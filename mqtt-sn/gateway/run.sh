#!/bin/bash

# BSD 3-Clause Licence
# Updated by pierrickmarie on 28/11/2018.

if [ ! -d "bin" ]; then
    mkdir bin
fi

echo "Compile"
javac -d ./bin -sourcepath ./src/main/java -cp ./lib/jssc-2.8.0.jar:./lib/org.eclipse.paho.client.mqttv3-1.2.0.jar ./src/main/java/gateway/Main.java -Xlint:unchecked

echo "Run"
java -cp ./lib/jssc-2.8.0.jar:./lib/org.eclipse.paho.client.mqttv3-1.2.0.jar:./bin gateway.Main "$1" "$2" "$3"

#!/bin/sh

# BSD 3-Clause Licence
# Updated by Pierrick MARIE on 28/11/2018.

if [ ! -d "bin" ]; then
	echo "Create bin directory"
	mkdir ./bin
else
	echo "Clean bin directory"
	rm -rf ./bin/*
fi

echo "Compile gateway"

javac -d ./bin -sourcepath ./src/main/java -cp ./lib/jssc-2.8.0.jar:./lib/org.eclipse.paho.client.mqttv3-1.2.0.jar ./src/main/java/gateway/Main.java -Xlint:unchecked && echo "Done" && exit ;

echo "Error during compilation"

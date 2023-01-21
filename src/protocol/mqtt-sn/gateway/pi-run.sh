#!/bin/sh

# BSD 3-Clause Licence
# Updated by Pierrick MARIE on 28/11/2018.

sleep 30

killall java

echo "" >> /home/pi/Documents/java-log.txt
echo "$(date) - REBOOT" >> /home/pi/Documents/java-log.txt
echo "" >> /home/pi/Documents/java-log.txt

# the repository of the project
cd /home/pi/Downloads/mqtt-sn/

if [ ! -d "bin" ]; then
    mkdir bin
fi

javac -d ./bin -sourcepath ./src/main/java -cp ./lib/jssc-2.8.0.jar:./lib/org.eclipse.paho.client.mqttv3-1.2.0.jar ./src/main/java/gateway/Main.java -Xlint:unchecked

java -cp ./lib/jssc-2.8.0.jar:./lib/org.eclipse.paho.client.mqttv3-1.2.0.jar:./bin gateway.Main "$1" "$2" "$3"

#!/bin/bash

sleep 30

killall java

echo "" >> /home/pi/Documents/java-log.txt
echo "$(date) - REBOOT" >> /home/pi/Documents/java-log.txt
echo "" >> /home/pi/Documents/java-log.txt

cd /home/pi/Downloads/MQTT_SN/

java -cp ./lib/jssc-2.8.0.jar:./lib/hawtbuf-1.10.jar:./lib/gateway.mqtt-client-1.12.jar:./lib/hawtdispatch-1.21.jar:./lib/hawtdispatch-transport-1.21.jar:./bin gateway.Main >> /home/pi/Documents/java-log.txt 2>> /home/pi/Documents/java-error.txt




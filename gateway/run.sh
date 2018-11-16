#!/bin/bash

java -cp ./lib/jssc-2.8.0.jar:./lib/hawtbuf-1.10.jar:./lib/gateway.mqtt-client-1.12.jar:./lib/hawtdispatch-1.21.jar:./lib/hawtdispatch-transport-1.21.jar:./bin gateway.Main "$1" "$2" "$3"




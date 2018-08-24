#!/bin/sh

javac -d ./bin -sourcepath ./src/main/java/ -cp ./lib/jssc-2.8.0.jar:./lib/hawtbuf-1.10.jar:./lib/mqtt-client-1.12.jar:./lib/hawtdispatch-1.21.jar:./lib/hawtdispatch-transport-1.21.jar ./src/main/java/gateway/Main.java


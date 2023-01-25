#!/bin/bash

# BSD 3-Clause Licence
# Updated by Pierrick MARIE on 28/11/2018.
#                           on 20/01/2023.

J="java"
CP="-cp ./lib/jssc-2.8.0.jar:./lib/org.eclipse.paho.client.mqttv3-1.2.0.jar:./bin"
MAIN="gateway.Main"

${J} ${CP} ${MAIN} $@




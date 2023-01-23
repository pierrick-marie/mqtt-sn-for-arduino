#!/bin/bash

# BSD 3-Clause Licence
# Updated by Pierrick MARIE on 20/01/2023.

usage() {
	echo "Run the gateway for XBee modules"
	echo " -p, --port [path]      change the path of the XBee module"
	echo " -h, --help             print this message"
}

J="java"
CP="-cp ./lib/jssc-2.8.0.jar:./bin"
MAIN="gateway.Main"

RASPBERRY_PI="NO"
PORT=""

while [[ $# -gt 0 ]]; do
	case $1 in
	    	-p|--port)
			PORT="$2"
			shift # past argument
			shift # past value
			;;
		-h|--help)
			usage
			exit 1;
			;;
    		-*|--*)
			echo "Unknown option $1"
			exit 2
			;;
		?)
			usage
			exit 2;
  	esac
done

${J} ${CP} ${MAIN} ${PORT}

# echo "RASPBERRY_PI	= ${RASPBERRY_PI}"



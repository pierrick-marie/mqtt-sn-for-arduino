MQTT-SN for Arduino with XBee modules
=====================

This is an implementation of MQTT-SN protocol for Arduino with XBee modules for wireless communications.
The implementation is writtrn in C++. The gateway in in Java.

# Get source code

```
	git clone https://framagit.org/pierrick/mqtt-sn.git
```

# Architecture of the project

The source code of this repository is split in two parts: *src/basic-xbee-communication* and *src/mqtt-sn-impl*.

*basic-xbee-communication* is a basic solution to broadcast messages between wireless XBee modules through a gateway. THIS PART DOES NOT IMPLEMENT MQTT-SN!

*mqtt-sn-impl* is an implementation of MQTT-SN for Arduino with wireless XBee modules.

### Folders

 * *src/mqtt-sn-impl*: the implementation of MQTT-SN protocol with the gateway written in Java, Arduino libraries and Arduino client examples 
 * *src/basic-xbee-communication*: a basic program to send and receive messages from a gateway. It does not implement MQTT-SN!
 * *documentation*: all documentations about XBee modules and MQTT-SN protocol
 * *xctu-parameters*: all configurations required for XBee modules with XCTU software 

# Dependencies

The gateway depends on Jssc version 2.8.0 and eclipse Paho MQTT client version 3-1.2.0. Both are provided in source code.

# Installation and usages

## Basic XBee communication

#### Arduino library

To install the Arduino librairy you have to copy the folders *arduino/library* into the default Arduino sketch folder. For Unix like system this folder is *~/Arduino*.

```
	cp src/basic-xbee-communication/arduino/libraries ~/Arduino
```

Now, in *~/Arduino/libraries* you can find the folders *XBee-basic-communications-library*.

You can use that library by including *XBee.h* in your sketch.

```
	#include <XBee.h>
```

#### Arduino client examples

There is a sketch example of Arduino client in *src/basic-xbee-communication/arduino/XBee-basic-communication-example/*. You can copy this folder in your default Arduino sketch folder to play with it.

```
	cp src/basic-xbee-communication/arduino/XBee-basic-communication-example/ ~/Arduino
```

#### Gateway

**Build**

To build the gateway move to the folder and run makefile.

```
	cd src/basic-xbee-communication/gateway
	make
```

**Run**

To run the gateway use the dedicated run file.

```
	./run.sh
```

By default the gateway search the XBee module in */dev/ttyUSB0*. You can specify another path in the argument of run file. For instance, to use */dev/ttyUSB_1_* use the following command.

```
	./run.sh /dev/ttyUSB_1_
```

### MQTT-SN

#### Arduino library

To install the Arduino librairy you have to copy the folders *arduino/library* into the default Arduino sketch folder. For Unix like system this folder is *~/Arduino*.

```
	cp src/mqtt-sn-impl/arduino/libraries ~/Arduino
```

Now, in *~/Arduino/libraries* you can find the folders *mqtt-sn-library*.

You can use that library by including *Mqttsn.h* in your sketch.

```
	#include <XBee.h>
```

#### Arduino client examples

There is a sketch example of Arduino client in *src/mqtt-sn-impl/arduino/mqtt-sn-client-example*. You can copy this folder in your default Arduino sketch folder to play with it.

```
	cp src/mqtt-sn-impl/arduino/mqtt-sn-client-example ~/Arduino
```

#### Gateway

**Build**

To build the gateway move to the folder and run makefile.

```
	cd src/mqtt-sn-impl/gateway
	make
```

**Run**

To run the gateway use the dedicated run file with the following arguments:

* path of XBee module
* IP of MQTT server
* port of MQTT server
* log level option: (ACTIVE || VERBOSE) By default: NONE

Example:

```
	./run.sh /dev/ttyUSB0 127.0.01 8080 ACTIVE
```

# Authors and acknowledgment

Developer: Pierrick MARIE contact at pierrickmarie.info

# License: BSD 3-Clause 

https://framagit.org/pierrick/mqtt-sn/-/raw/master/LICENSE

# Contributing

Do not hesitate to improve to this program. Feel free to send PR or contact me to send comments. You are welcome to fork this project also ;)

# Badges

[![License](https://img.shields.io/badge/License-BSD%203--Clause-green.svg)](https://opensource.org/licenses/BSD-3-Clause) [![made-with-C++](https://img.shields.io/badge/Made%20with-C++-%23E34F26.svg)](https://cpp-lang.net/) [![made-with-Java](https://img.shields.io/badge/Made%20with-Java-%23E34F26.svg)](https://www.java.com/) [![made-for-Arduino](https://img.shields.io/badge/Made%20for-Arduino-%23E34F26.svg)](https://www.arduino.cc/) [![made-for-MQTT](https://img.shields.io/badge/Made%20for-MQTT-blue.svg)](https://mqtt.org/)
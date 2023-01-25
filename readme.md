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

The following instructions are available for both *basic-xbee-communication* and *mqtt-sn-impl*. 

## Arduino libraries

To install Arduino librairies you just have to copy the folders *arduino/libraries* into the default Arduino sketch folder. For Unix like system this folder is *~/Arduino*.

```
	cp src/basic-xbee-communication/arduino/libraries ~/Arduino
	cp src/mqtt-sn-impl/arduino/libraries  ~/Arduino
```

Now, in *~/Arduino/libraries* you can find the folders *XBee-basic-communications-library* and *mqtt-sn-library*. Respectively for basic wireless communication and for MQTT-SN protocol.

## Arduino client examples

# License: BSD 3-Clause 

https://framagit.org/pierrick/mqtt-sn/-/raw/master/LICENSE

# Contributing

Do not hesitate to improve to this program. Feel free to send PR or contact me to send comments. You are welcome to fork this project also ;)

MQTT-SN for Arduino with XBee modules
=====================

This is an implementation of the MQTT-SN protocol for Arduino with XBee modules for wireless communications.
The implementation of MQTT-SN is in C++. The gateway in in Java.

# Get source code

```
git clone https://framagit.org/pierrick/mqtt-sn.git
```

# Architecture of the project

### Folders

 * *src/mqtt-sn-impl*: the implementation of MQTT-SN protocol with the gateway written in Java, Arduino libraries and Arduino client examples 
 * *src/basic-xbee-communication*: a basic program to send and receive messages from a gateway. It does not implement MQTT-SN!
 * *documentation*: all documentations about XBee modules and MQTT-SN protocol
 * *xctu-parameters*: all configurations required for XBee modules with XCTU software 

# License: BSD 3-Clause 

https://framagit.org/pierrick/mqtt-sn/-/raw/master/LICENSE

# Contributing

Do not hesitate to improve to this program. Feel free to send PR or contact me to send comments. You are welcome to fork this project also ;)

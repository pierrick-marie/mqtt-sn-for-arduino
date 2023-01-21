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

 * *documentation*: all documentations about XBee modules and MQTT-SN protocol
 * *implementation*: the source source of the implementatoin of the MQTT-SN protool for Arduino with a gateway. The subfolder "transparent mode" is a demonstration how to send and receive messages between Arduinos and XBEE modules as simple as possible without MQTT-SN.  
 * *xctu-parameters*: all configurations required for XBEE modules with XCTU software 

# License: BSD 3-Clause 

https://framagit.org/pierrick/mqtt-sn/-/raw/master/LICENSE

# Contributing

Do not hesitate to improve to this program. Feel free to send PR or contact me to send comments. You are welcome to fork this project also ;)

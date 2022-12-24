Bibliothèque MQTT-SN pour clients Arduino
=====================

Ce projet est un portage du protocole MQTT-SN sur Arduino. 

Les communications sans fil entre les client Arduino sont effectuées à l'aide de modules XBEE : https://framagit.org/pierrick/mqtt-sn/-/raw/master/mqtt-sn/doc/zigbee-modules.pdf

La gateway utilisée pour coordonner les clients Arduino est développée en Java.
La bibliothèque MQTT-SN pour Arduino est développée en C/C++.

URL publique du projet : https://framagit.org/pierrick/mqtt-sn.git

# Licence BSD 3-Clause 

Texte de la licence : https://framagit.org/pierrick/mqtt-sn/-/raw/master/LICENSE

# Folders

 * *documentation*: all documentations about XBee modules and MQTT-SN protocol
 * *implementation*: the source source of the implementatoin of the MQTT-SN protool for Arduino with a gateway. The subfolder "transparent mode" is a demonstration how to send and receive messages between Arduinos and XBEE modules as simple as possible without MQTT-SN.  
 * *xctu-parameters*: all configurations required for XBEE modules with XCTU software 

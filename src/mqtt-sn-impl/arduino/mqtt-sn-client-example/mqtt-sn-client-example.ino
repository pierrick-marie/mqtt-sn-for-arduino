/**
 * BSD 3-Clause Licence
 *
 * Created by Pierrick MARIE on 28/11/2018.
 * Updated by Pierrick MARIE in 20/01/2023
 */

/**
 * This example send messages on topic "TOPIC_PUB".
 * This program subscribes to the topic "TOPIC_SUB".
 **/

#include <Mqttsn.h>

#define MODULE_NAME "Arduino-Example"
#define TOPIC_SUB "TOPIC_SUB"
#define TOPIC_PUB "TOPIC_PUB"
#define MESSAGE "Message"

int nbReceivedMessages = 0;

Logs logs;			   	// Write logs in the console
SoftwareSerial XBee(5, 4);	// Serial XBee module
Mqttsn mqttsn(&XBee);	   	// MQTT-SN object

void setup() {

	Serial.begin(9600);

	mqttsn.start();		// Run the client
}

void loop() {

	mqttsn.connect(MODULE_NAME);	// Connect module to the gateway

	if (mqttsn.subscribeTopic(TOPIC_SUB)) {	// Subscribe to the topic

		nbReceivedMessages = mqttsn.requestMessages();	// Get messages on TOPIC_SUB

		Serial.print("Nb messages recu ");
		Serial.println(nbReceivedMessages);

		Message *msg = mqttsn.getReceivedMessages();	// Array of received messages

		while (nbReceivedMessages > 0) {	// Print received messages
			nbReceivedMessages--;
			Serial.print("Received message ");
			Serial.print(nbReceivedMessages);
			Serial.print(" = ");
			Serial.println(msg[nbReceivedMessages].data);
		}
	}
	else {
		Serial.println("\n!!! Subscribe KO !!!");
	}

	mqttsn.publish(TOPIC_PUB, MESSAGE);	// Publish MESSAGE TO TOPIC_PUB

	mqttsn.disconnect();	// Disconnect client to the gateway
}

/**
 * BSD 3-Clause Licence
 *
 * Created by Pierrick MARIE on 28/11/2018.
 */

#include <XBee.h>

#define ID 2	// The unique ID of XBee module 

XBee xbee(ID);

String message;	// Used to get message from the gateway

void setup() {

	Serial.begin(9600);
}

void loop() {

	message = xbee.getMessage();	// Get message from the gateway
	while (!message.equals("")) {	// Loop until get message
		Serial.print("Get: ");
		Serial.println(message);
		message = xbee.getMessage();
	}
	Serial.println("");

	Serial.println("Send Hello");
	xbee.sendMessage("Hello");	// Send Hello to the gateway
}

/**
 * BSD 3-Clause Licence
 *
 * Created by Pierrick MARIE on 28/11/2018.
 */

#include "XBee.h"

XBee::XBee(const short id) {

	xbeeId = id;

	xBeeModule = new SoftwareSerial(XBEE_RX, XBEE_TX);
	xBeeModule->begin(XBEE_BAUD_RATE);
}

XBee::~XBee() {

	free(xBeeModule);
}

String XBee::getMessage() {

	char temp = ' ';
	short nbTry = 0;
	String message = "";

	xBeeModule->listen();
	while (!xBeeModule->available() && nbTry <= MAX_TRY) {	// Wait new message
		delay(DELAY_MILLIS);
		nbTry++;
	}

	if (MAX_TRY == nbTry) {
		return "";
	}

	temp = xBeeModule->read();
	while (xBeeModule->available() && isPrintable(temp) && '\n' != temp) {	// Get message char by char until '\n'
		message += temp;
		temp = xBeeModule->read();
	}

	return message;
}

void XBee::sendMessage(String messageOut) {

	String message = String(xbeeId);
	message += "*";
	message += messageOut;
	message += '\n';

#ifdef DEBUG
	Serial.print("Sending message: ");
	Serial.println(message);
#endif

	xBeeModule->println(message);

	delay(6 * DELAY_MILLIS);	// 6 * 0.5 seconds -> 3 seconds
}

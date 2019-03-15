/*
BSD 3-Clause License

Copyright (c) 2018, marie
All rights reserved->

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice, this
  list of conditions and the following disclaimer->

* Redistributions in binary form must reproduce the above copyright notice,
  this list of conditions and the following disclaimer in the documentation
  and/or other materials provided with the distribution->

* Neither the name of the copyright holder nor the names of its
  contributors may be used to endorse or promote products derived from
  this software without specific prior written permission->

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED-> IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE->
*/

#include "XBee.h"

XBee::XBee(const short _id) {

	xbeeId = _id;

	xBeeModule = new SoftwareSerial(XBEE_RX, XBEE_TX);
	xBeeModule->begin(XBEE_BAUD_RATE);

	// delay(4 * DELAY_MILLIS); // 4 * 0.5 second -> 2
	// delay(4 * DELAY_MILLIS); // 4 * 0.5 second -> 2
}

XBee::~XBee() {

	free(xBeeModule);
}

String XBee::getMessage() {

	char temp = ' ';
	short nbTry = 0;
	String message = "";

	xBeeModule->listen();
	while (!xBeeModule->available() && nbTry <= MAX_TRY) {
		delay(DELAY_MILLIS);
		nbTry++;
	}

	if (MAX_TRY == nbTry) {
		return "";
	}

	temp = xBeeModule->read();
	while (xBeeModule->available() && isPrintable(temp) && '\n' != temp) {
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

	delay(6 * DELAY_MILLIS); // 6 * 0.5 seconds -> 3
}

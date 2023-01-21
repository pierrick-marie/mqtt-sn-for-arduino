/**
 * BSD 3-Clause Licence
 *
 * Created by Pierrick MARIE on 28/11/2018.
 */

#ifndef __XBEE_H__
#define __XBEE_H__

// #define DEBUG
#define MESSAGE_LEN 160
#define DELAY_MILLIS 500 // 0.5 second
#define XBEE_BAUD_RATE 9600
#define XBEE_RX 5
#define XBEE_TX 4

#define MAX_TRY 4

#include <stdint.h>
#include <stdbool.h>
#include <string.h>

#include <SoftwareSerial.h>
#include <Arduino.h>

class XBee {

public:

	XBee(const short _id);
	~XBee();

	String getMessage();
	void sendMessage(String messageOut);

private:

	short xbeeId;
	SoftwareSerial* xBeeModule;
};

#endif



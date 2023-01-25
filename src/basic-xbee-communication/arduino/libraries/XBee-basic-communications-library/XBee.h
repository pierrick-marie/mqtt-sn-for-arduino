/**
 * BSD 3-Clause Licence
 *
 * Created by Pierrick MARIE on 28/11/2018.
 */

#ifndef __XBEE_H__
#define __XBEE_H__

// #define DEBUG
#define MESSAGE_LEN 160		// Size of messages
#define DELAY_MILLIS 500	// 0.5 second
#define XBEE_BAUD_RATE 9600
#define XBEE_RX 5			// Receive port
#define XBEE_TX 4			// Send port

#define MAX_TRY 4			// Number of try to get message before exit

#include <stdint.h>
#include <stdbool.h>
#include <string.h>

#include <SoftwareSerial.h>
#include <Arduino.h>

class XBee {

public:

	XBee(const short id);
	~XBee();

	/**
	 * Get message from the gateway
	 */
	String getMessage();

	/**
	 * Send message to the gateway. Messages use the following format: ID*MESSAGE
	 * ID is the ID of the module -> the argument of the constructor
	 * MESSAGE is the argument of that function
	 */
	void sendMessage(String messageOut);

private:

	/**
	 * The id of the XBee module 
	 */
	short xbeeId;

	/**
	 * Pointer to xbee module 
	 */
	SoftwareSerial* xBeeModule;
};

#endif



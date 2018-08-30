/*
mqttsn-messages.cpp

The MIT License (MIT)

Copyright (C) 2014 John Donovan

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

#include <Arduino.h>

#include "mqttsn-messages.h"
#include "Mqttsn.h"
#include "Logs.h"

/**
 *
 * ****************************
 * ############################
 * ####################
 * #############
 * ######
 * ##
 * ****************************
 *
 * PROTECTED FUNCTIONS
 *
 * ****************************
 * ##
 * ######
 * #############
 * ####################
 * ############################
 * ****************************
 *
 **/

extern void MQTTSN_regack_handler(const msg_regack* msg);
void MQTTSN::regack_handler(const msg_regack* msg) {

	logs.debug("Response to register message is received");

	if (msg->return_code == 0 && TopicCount < MAX_TOPICS && bswap(msg->message_id) == MessageId) {
		TopicTable[TopicCount].id = bswap(msg->topic_id);

		logs.debug("The topic id is ");
		Serial.print("regack_handler - Test 0: ");
		Serial.println(msg->topic_id);
		Serial.print("regack_handler - Test 1: ");
		Serial.println(TopicTable[TopicCount].id);

		TopicCount++;
		MQTTSN_regack_handler(msg);
	}
}

extern void MQTTSN_reregister_handler(const msg_reregister* msg);
void MQTTSN::reregister_handler(const msg_reregister* msg) {
	MQTTSN_reregister_handler(msg);
}

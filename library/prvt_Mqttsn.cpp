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
 * PRIVATE FUNCTIONS
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

void MQTTSN::regack(const uint16_t topic_id, const uint16_t message_id, const return_code_t return_code) {
	msg_regack* msg = reinterpret_cast<msg_regack*>(messageBuffer);

	msg->length = sizeof(msg_regack);
	msg->type = REGACK;
	msg->topic_id = bswap(topic_id);
	msg->message_id = bswap(message_id);
	msg->return_code = return_code;

	sendMessage();
}

void MQTTSN::reregister(const uint16_t topic_id, const uint16_t message_id, const return_code_t return_code) {
	msg_reregister* msg = reinterpret_cast<msg_reregister*>(messageBuffer);

	msg->length = sizeof(msg_reregister);
	msg->type = REREGISTER;
	msg->topic_id = bswap(topic_id);
	msg->message_id = bswap(message_id);
	msg->return_code = return_code;

	sendMessage();
}

void MQTTSN::puback(const uint16_t topic_id, const uint16_t message_id, const return_code_t return_code) {
	msg_puback* msg = reinterpret_cast<msg_puback*>(messageBuffer);

	msg->length = sizeof(msg_puback);
	msg->type = PUBACK;
	msg->topic_id = bswap(topic_id);
	msg->message_id = bswap(message_id);
	msg->return_code = return_code;
	sendMessage();
}

/**
 * @brief MQTTSN::register_handler
 * @param message
 *
 * @todo BUG?
 **/
void MQTTSN::register_handler(const msg_register* message) {

	return_code_t ret = REJECTED_INVALID_TOPIC_ID;
	short topic_id = find_topic_id(message->topic_name);

	logs.debug("MQTTSN", "register_handler", "received topic_name: ", message->topic_name);
	logs.debug("MQTTSN", "register_handler", "found topic id: ", (int)topic_id);

	if (topic_id != DEFAULT_TOPIC_ID) {
		topicTable[topic_id].id = bswap(message->topic_id);
		ret = ACCEPTED;
	}

	regack(message->topic_id, message->message_id, ret);
}

void MQTTSN::willtopicresp_handler(const msg_willtopicresp* msg) {
}

void MQTTSN::willmsgresp_handler(const msg_willmsgresp* msg) {
}

void MQTTSN::unsuback_handler(const msg_unsuback* msg) {
}

void MQTTSN::pingreq_handler(const msg_pingreq* msg) {
	pingresp();
}

/**
 * @brief MQTTSN::bswap Magic formula (big / little indian?).
 * @param val A number to swap.
 * @return The swaped number.
 **/
uint16_t MQTTSN::bswap(const uint16_t value) {
	return (value << 8) | (value >> 8);
}

/**
 * @brief MQTTSN::dispatch The function is called at the end of the function parse_stream.
 * It calls the corresponding function according to the message type.
 **/
void MQTTSN::dispatch() {

	logs.debug("Mqttsn", "dispatch", "");

	message_header* response_message = (message_header*)responseBuffer;
	switch (response_message->type) {
	case ADVERTISE:
		advertise_handler((msg_advertise*)responseBuffer);
		break;

	case GWINFO:
		gatewayInfoHandler((msg_gwinfo*)responseBuffer);
		break;

	case CONNACK:
		connack_handler((msg_connack*)responseBuffer);
		break;

	case WILLTOPICREQ:
		willtopicreq_handler(response_message);
		break;

	case WILLMSGREQ:
		willmsgreq_handler(response_message);
		break;

	case REGISTER:
		register_handler((msg_register*)responseBuffer);
		break;

	case REGACK:
		regack_handler((msg_regack*)responseBuffer);
		break;

	case REREGISTER:
		reregister_handler((msg_reregister*)responseBuffer);
		break;

	case PUBLISH:
		publish_handler((msg_publish*)responseBuffer);
		break;

	case PUBACK:
		puback_handler((msg_puback*)responseBuffer);
		break;

	case SUBACK:
		suback_handler((msg_suback*)responseBuffer);
		break;

	case UNSUBACK:
		unsuback_handler((msg_unsuback*)responseBuffer);
		break;

	case PINGREQ:
		pingreq_handler((msg_pingreq*)responseBuffer);
		break;

	case PINGRESP:
		pingresp_handler();
		break;

	case DISCONNECT:
		disconnect_handler((msg_disconnect*)responseBuffer);
		break;

	case WILLTOPICRESP:
		willtopicresp_handler((msg_willtopicresp*)responseBuffer);
		break;

	case WILLMSGRESP:
		willmsgresp_handler((msg_willmsgresp*)responseBuffer);
		break;

	default:
		return;
	}
}

void MQTTSN::sendMessage() {
	message_header* hdr = reinterpret_cast<message_header*>(messageBuffer);

#ifdef USE_SERIAL
	// Sending the message stored into @messageBuffer through @MB_serial_send function
	// extern void MB_serial_send(uint8_t* messageBuffer, int length);
	serialSend(messageBuffer, hdr->length);
#endif

	/**
	* @todo BEGIN: DEBUG
	**/
	if (!waitingForResponse) {
		responseTimer = millis();
		responseRetries = N_RETRY;
	}
	if (!waitingForSubAck) {
		subAckTimer = millis();
		subAckRetries = N_RETRY;
	}
	if (!waitingForPubAck) {
		pubAckTimer = millis();
		pubAckRetries = N_RETRY;
	}
	/*
    if (!waiting_for_pingresp) {
	  _pingresp_timer = millis();
	  _pingresp_retries = N_RETRY;
    }
     */
	/**
	* @todo END: DEBUG
	**/
}

void MQTTSN::advertise_handler(const msg_advertise* msg) {
	gatewayId = msg->gw_id;
}

/**
 * @brief MQTTSN::find_topic_id The function search the index of a @topicName within @topicTable list.
 * @param topicName The name of the topic to search.
 * @return The index of the topic or -1 if not found.
 */
short MQTTSN::find_topic_id(const char* topic_name) {
	for (short i = 0; i < topicCount; i++) {
		if (topicTable[i].id != DEFAULT_TOPIC_ID && strcmp(topicTable[i].name, topic_name) == 0) {
			logs.debug("Mqttsn", "find_topic_id", topicTable[i].id);
			return topicTable[i].id;
		}
	}

	return -1;
}

bool MQTTSN::multiCheckSerial(const int nb_max_try) {

	int nb_try = 0;

	logs.debug("Mqttsn", "multiCheckSerial", "check serial iteration: ", nb_try);
	while( !checkSerial() && nb_try < nb_max_try ) {
		nb_try++;
		logs.debug("Mqttsn", "multiCheckSerial", "check serial iteration: ", nb_try);
	}

	return nb_try != nb_max_try;
}

void MQTTSN::searchGateway(const uint8_t radius) {

	msg_searchgw* msg = reinterpret_cast<msg_searchgw*>(messageBuffer);

	msg->length = sizeof(msg_searchgw);
	msg->type = SEARCHGW;
	msg->radius = radius;

	sendMessage();

	// Waiting a response, set to false in @MQTTSN_parse_stream()
	waitingForResponse = true;
}


































bool MQTTSN::wait_data() {

	int i = 1;

	// waiting for incoming data during 1 second (10x100ms)
	while( xBee->available() <= 0 && i <= 10 ) {
		delay(100);
		i++;
	}
	if( i == 20 ) {
		// timeout -> return false
		return false;
	}

	return true;
}

bool MQTTSN::verify_checksum(uint8_t frame_buffer[], int frame_size) {

	int i;
	uint16_t checksum = 0x00;

	for(i=0; i < frame_size; i++) {
		checksum += frame_buffer[i];
	}
	checksum = checksum & 0xFF;

	return checksum == 0xFF ;
}

bool MQTTSN::is_transmit_status() {
	return frameBufferIn[0] == 139;
}

bool MQTTSN::is_data_packet() {
	return frameBufferIn[0] == 144;
}

int MQTTSN::create_frame(uint8_t* data, int data_lenght, uint8_t* destination_address, uint8_t* frame, int frame_max_lenght, bool broadcast) {

	uint8_t checksum = 0;
	int i = 0;

	// frame buffer is big enough?
	if ( frame_max_lenght < API_FRAME_LEN ) {
		return -1;
	}

	// data is too long?
	// TODO: Split in multiple packets?
	if (data_lenght > API_DATA_LEN) {
		Serial.println("TOO LONG");
		return -2;
	}

	// frame buffer is fine, clear it
	memset (frame, 0, frame_max_lenght);

	/* The header */

	// delimiter
	frame[0] = API_START_DELIMITER;

	// length of the payload
	if(API_PAY_LEN < 256) {
		frame[1] = 0;
		frame[2] = 14 + data_lenght;
	}else{
		frame[1] = API_PAY_LEN / 256;
		frame[2] = API_PAY_LEN - (256 * frame[1]);
	}

	// frame Type: Transmit Request
	checksum = 0;
	checksum += frame[3] = 16;

	// frame id
	checksum += frame[4] = frameId++;

	// 64-bit address
	checksum += frame[5] = destination_address[0];
	checksum += frame[6] = destination_address[1];
	checksum += frame[7] = destination_address[2];
	checksum += frame[8] = destination_address[3];
	checksum += frame[9] = destination_address[4];
	checksum += frame[10] = destination_address[5];
	checksum += frame[11] = destination_address[6];
	checksum += frame[12] = destination_address[7];

	// 16-bit address
	checksum += frame[13] = 0;
	checksum += frame[14] = 0;
	checksum += frame[15] = 0;
	checksum += frame[16] = 0;


	/* The data */
	for (i = 0; i < data_lenght; i++) {
		checksum += frame[17 + i] = data[i];
		Serial.print(" ");
	}

	checksum = 0XFF - checksum;
	frame[17 + data_lenght] = checksum;

	return 17 + data_lenght + 1;
}

void MQTTSN::parseData() {

	logs.debug("Mqttsn", "parseData", "");

	int i;
	int payload_lenght = frameBufferIn[12];
	uint8_t payload[payload_lenght];

	for(i = 0; i < payload_lenght; i++){
		payload[i] = frameBufferIn[12+i];
	}
	parseStream(payload, payload_lenght);

	memset(frameBufferIn, 0, sizeof(frameBufferIn));
}

char* MQTTSN::stringFromReturnCode(const uint8_t return_code) {

	/*
	static char string_code[30] = {'\0'};

	switch(return_code) {
	case 0:
		strncpy(string_code, "ACCEPTED", 8);
		break;
	case 1:
		strncpy(string_code, "REJECTED_CONGESTION", 19);
		break;
	case 2:
		strncpy(string_code, "REJECTED_INVALID_TOPIC_ID", 25);
		break;
	case 3:
		strncpy(string_code, "REJECTED", 8);
		break;
	}
	return string_code;
	*/

	static const char *strings[] = { "ACCEPTED", "REJECTED_CONGESTION", "REJECTED_INVALID_TOPIC_ID", "REJECTED"};
	return strings[return_code];
}

void MQTTSN::serialSend(uint8_t* message_buffer, int length) {

	int _length = create_frame(message_buffer, length, gatewayAddress, frameBufferOut, sizeof(frameBufferOut), false);
	if (_length > 0) {
		xBee->write(frameBufferOut, _length);
		xBee->flush();
	}
}























#ifdef USE_QOS2
void MQTTSN::pubrec_handler(const msg_pubqos2* msg) {
}

void MQTTSN::pubrel_handler(const msg_pubqos2* msg) {
}

void MQTTSN::pubcomp_handler(const msg_pubqos2* msg) {
}
#endif



extern void MQTTSN_regack_handler(const msg_regack* msg);
void MQTTSN::regack_handler(const msg_regack* msg) {

	logs.debug("MQTTSN", "regack_handler", "Response to register message is received");

	if (msg->return_code == 0 && topicCount < MAX_TOPICS && bswap(msg->message_id) == messageId) {
		topicTable[topicCount].id = bswap(msg->topic_id);

		logs.debug("MQTTSN", "regack_handler", "The topic id is ", msg->topic_id);
		logs.debug("MQTTSN", "regack_handler", "The topic table id is ", topicTable[topicCount].id);

		topicCount++;
		MQTTSN_regack_handler(msg);
	}
}

extern void MQTTSN_reregister_handler(const msg_reregister* msg);
void MQTTSN::reregister_handler(const msg_reregister* msg) {
	MQTTSN_reregister_handler(msg);
}

/*
Mqttsn-messages.cpp

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

#include "Mqttsn.h"

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

void Mqttsn::regack(const uint16_t topic_id, const uint16_t message_id, const return_code_t return_code) {
	msg_regack* msg = reinterpret_cast<msg_regack*>(messageBuffer);

	msg->length = sizeof(msg_regack);
	msg->type = REGACK;
	msg->topic_id = bitSwap(topic_id);
	msg->message_id = bitSwap(message_id);
	msg->return_code = return_code;

	sendMessage();
}

void Mqttsn::reregister(const uint16_t topic_id, const uint16_t message_id, const return_code_t return_code) {
	msg_reregister* msg = reinterpret_cast<msg_reregister*>(messageBuffer);

	msg->length = sizeof(msg_reregister);
	msg->type = REREGISTER;
	msg->topic_id = bitSwap(topic_id);
	msg->message_id = bitSwap(message_id);
	msg->return_code = return_code;

	sendMessage();
}

void Mqttsn::puback(const uint16_t topic_id, const uint16_t message_id, const return_code_t return_code) {
	msg_puback* msg = reinterpret_cast<msg_puback*>(messageBuffer);

	msg->length = sizeof(msg_puback);
	msg->type = PUBACK;
	msg->topic_id = bitSwap(topic_id);
	msg->message_id = bitSwap(message_id);
	msg->return_code = return_code;
	sendMessage();
}

/**
 * @brief Mqttsn::register_handler
 * @param message
 *
 * @todo BUG?
 **/
void Mqttsn::register_handler(const msg_register* message) {

	return_code_t ret = REJECTED_INVALID_TOPIC_ID;
	short topic_id = findTopicId(message->topic_name);

	logs.debug("Mqttsn", "register_handler", "received topic_name: ", message->topic_name);
	logs.debug("Mqttsn", "register_handler", "found topic id: ", (int)topic_id);

	if (topic_id != DEFAULT_TOPIC_ID) {
		topicTable[topic_id].id = bitSwap(message->topic_id);
		ret = ACCEPTED;
	}

	regack(message->topic_id, message->message_id, ret);
}

void Mqttsn::willtopicresp_handler(const msg_willtopicresp* msg) {
}

void Mqttsn::willmsgresp_handler(const msg_willmsgresp* msg) {
}

void Mqttsn::unsuback_handler(const msg_unsuback* msg) {
}

void Mqttsn::pingreq_handler(const msg_pingreq* msg) {
	pingresp();
}

/**
 * @brief Mqttsn::bitSwap Magic formula (big / little indian?).
 * @param val A number to swap.
 * @return The swaped number.
 **/
uint16_t Mqttsn::bitSwap(const uint16_t value) {
	return (value << 8) | (value >> 8);
}

/**
 * @brief Mqttsn::dispatch The function is called at the end of the function parse_stream.
 * It calls the corresponding function according to the message type.
 **/
void Mqttsn::dispatch() {

	logs.debug("Mqttsn", "dispatch");

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
		regAckHandler((msg_regack*)responseBuffer);
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

void Mqttsn::sendMessage() {

	logs.debug("Mqttsn", "sendMessage");

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

	logs.debug("Mqttsn", "sendMessage", "Message is sent");
}

void Mqttsn::advertise_handler(const msg_advertise* msg) {
	gatewayId = msg->gw_id;
}

bool Mqttsn::multiCheckSerial(const int nb_max_try) {

	int nb_try = 0;

	logs.debug("Mqttsn", "multiCheckSerial", "check serial iteration: ", nb_try);
	while( !checkSerial() && nb_try < nb_max_try ) {
		nb_try++;
		logs.debug("Mqttsn", "multiCheckSerial", "check serial iteration: ", nb_try);
	}

	return nb_try != nb_max_try;
}

void Mqttsn::searchGateway(const uint8_t radius) {

	logs.debug("MqttsnApi", "searchGateway");

	msg_searchgw* msg = reinterpret_cast<msg_searchgw*>(messageBuffer);

	msg->length = sizeof(msg_searchgw);
	msg->type = SEARCHGW;
	msg->radius = radius;

	sendMessage();

	// Waiting a response, set to false in @Mqttsn_parse_stream()
	waitingForResponse = true;
}


































bool Mqttsn::waitData() {

	logs.debug("MqttsnApi", "waitData");

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

bool Mqttsn::verifyChecksum(uint8_t frame_buffer[], int frame_size) {

	logs.debug("MqttsnApi", "verifyChecksum");

	int i;
	uint16_t checksum = 0x00;

	for(i=0; i < frame_size; i++) {
		checksum += frame_buffer[i];
	}
	checksum = checksum & 0xFF;

	return checksum == 0xFF ;
}

bool Mqttsn::is_transmit_status() {
	return frameBufferIn[0] == 139;
}

bool Mqttsn::is_data_packet() {
	return frameBufferIn[0] == 144;
}

int Mqttsn::createFrame(const uint8_t* data, const int data_lenght, const uint8_t* destination_address, uint8_t* frame, const int frame_max_lenght, const bool broadcast) {

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

void Mqttsn::parseData() {

	logs.debug("Mqttsn", "parseData");

	int i;
	int payload_lenght = frameBufferIn[12];
	uint8_t payload[payload_lenght];

	for(i = 0; i < payload_lenght; i++){
		payload[i] = frameBufferIn[12+i];
	}
	parseStream(payload, payload_lenght);

	memset(frameBufferIn, 0, sizeof(frameBufferIn));
}

char const* Mqttsn::stringFromReturnCode(const uint8_t return_code) {

	switch(return_code) {
	case ACCEPTED:
		return "ACCEPTED" ;
	case REJECTED_CONGESTION:
		return "REJECTED_CONGESTION" ;
	case REJECTED_INVALID_TOPIC_ID:
		return "REJECTED_INVALID_TOPIC_ID" ;
	case REJECTED_NOT_SUPPORTED:
		return "REJECTED_NOT_SUPPORTED";
	}
}

void Mqttsn::serialSend(const uint8_t* messageBuffer, const int length) {

	logs.debug("Mqttsn", "serialSend");

	int _length = createFrame(messageBuffer, length, gatewayAddress, frameBufferOut, sizeof(frameBufferOut), false);
	if (_length > 0) {
		xBee->write(frameBufferOut, _length);
		xBee->flush();
	}

	logs.debug("Mqttsn", "serialSend", "Message is sent");
}























#ifdef USE_QOS2
void Mqttsn::pubrec_handler(const msg_pubqos2* msg) {
}

void Mqttsn::pubrel_handler(const msg_pubqos2* msg) {
}

void Mqttsn::pubcomp_handler(const msg_pubqos2* msg) {
}
#endif


void Mqttsn::unsubscribeById(const uint8_t flags, const uint16_t topic_id) {
	++messageId;

	msg_unsubscribe* msg = reinterpret_cast<msg_unsubscribe*>(messageBuffer);

	msg->length = sizeof(msg_unsubscribe);
	msg->type = UNSUBSCRIBE;
	msg->flags = (flags & QOS_MASK) | FLAG_TOPIC_PREDEFINED_ID;
	msg->message_id = bitSwap(messageId);
	msg->topic_id = bitSwap(topic_id);

	sendMessage();

	if ((flags & QOS_MASK) == FLAG_QOS_1 || (flags & QOS_MASK) == FLAG_QOS_2) {
		waitingForResponse = true;
	}
}

void Mqttsn::unsubscribeByName(const uint8_t flags, const char* topic_name) {
	++messageId;

	msg_unsubscribe* msg = reinterpret_cast<msg_unsubscribe*>(messageBuffer);

	// The -2 here is because we're unioning a 0-length member (topic_name)
	// with a uint16_t in the msg_unsubscribe struct.
	msg->length = sizeof(msg_unsubscribe) + strlen(topic_name) - 2;
	msg->type = UNSUBSCRIBE;
	msg->flags = (flags & QOS_MASK) | FLAG_TOPIC_NAME;
	msg->message_id = bitSwap(messageId);
	strcpy(msg->topic_name, topic_name);

	sendMessage();

	if ((flags & QOS_MASK) == FLAG_QOS_1 || (flags & QOS_MASK) == FLAG_QOS_2) {
		waitingForResponse = true;
	}
}

void Mqttsn::subscribeByName(const uint8_t flags, const char* topic_name) {

	++messageId;
	msg_subscribe* msg = reinterpret_cast<msg_subscribe*>(messageBuffer);

	// The -2 here is because we're unioning a 0-length member (topic_name)
	// with a uint16_t in the msg_subscribe struct.
	msg->length = sizeof(msg_subscribe) + strlen(topic_name) - 2;
	msg->type = SUBSCRIBE;
	msg->flags = (flags & QOS_MASK) | FLAG_TOPIC_NAME;
	msg->message_id = bitSwap(messageId);
	strcpy(msg->topic_name, topic_name);

	sendMessage();

	//if ((flags & QOS_MASK) == FLAG_QOS_1 || (flags & QOS_MASK) == FLAG_QOS_2) {
	waitingForSubAck = true;
	//}
}

void Mqttsn::subscribeById(const uint8_t flags, const uint16_t topic_id) {
	++messageId;

	msg_subscribe* msg = reinterpret_cast<msg_subscribe*>(messageBuffer);

	msg->length = sizeof(msg_subscribe);
	msg->type = SUBSCRIBE;
	msg->flags = (flags & QOS_MASK) | FLAG_TOPIC_PREDEFINED_ID;
	msg->message_id = bitSwap(messageId);
	msg->topic_id = bitSwap(topic_id);

	sendMessage();

	if ((flags & QOS_MASK) == FLAG_QOS_1 || (flags & QOS_MASK) == FLAG_QOS_2) {
		waitingForResponse = true;
	}
}

// extern void Mqttsn_regAckHandler(const msg_regack* msg);
void Mqttsn::regAckHandler(const msg_regack* msg) {

	logs.debug("Mqttsn", "regAckHandler");

	if (msg->return_code == 0 && nbRegisteredTopic < MAX_TOPICS && bitSwap(msg->message_id) == messageId) {
		topicTable[nbRegisteredTopic].id = bitSwap(msg->topic_id);

		logs.debug("Mqttsn", "regAckHandler", "The topic id is ", msg->topic_id);
		logs.debug("Mqttsn", "regAckHandler", "The topic table id is ", topicTable[nbRegisteredTopic].id);

		nbRegisteredTopic++;
		regAckReturnCode = ACCEPTED;
	}
}

// extern void Mqttsn_reregister_handler(const msg_reregister* msg);
void Mqttsn::reregister_handler(const msg_reregister* msg) {
	// Mqttsn_reregister_handler(msg);
}

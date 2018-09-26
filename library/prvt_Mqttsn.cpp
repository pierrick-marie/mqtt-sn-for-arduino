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
  * ****************************
 *
 * PRIVATE FUNCTIONS
 *
 * ****************************
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

	// logs.debug("register_handler", "received topic_name: ", message->topic_name);
	// logs.debug("register_handler", "found topic id: ", (int)topic_id);

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

	waitingForResponse = false;
	message_header* response_message = (message_header*)responseBuffer;

	switch (response_message->type) {
	case ADVERTISE:
		// logs.debug("dispatch", "ADVERTISE");
		advertise_handler((msg_advertise*)responseBuffer);
		break;

	case GWINFO:
		// logs.debug("dispatch", "GWINFO");
		gatewayInfoHandler((msg_gwinfo*)responseBuffer);
		break;

	case CONNACK:
		// logs.debug("dispatch", "CONNACK");
		connackHandler((msg_connack*)responseBuffer);
		break;

	case WILLTOPICREQ:
		// logs.debug("dispatch", "WILLTOPICREQ");
		willtopicreq_handler(response_message);
		break;

	case WILLMSGREQ:
		// logs.debug("dispatch", "WILLMSGREQ");
		willmsgreq_handler(response_message);
		break;

	case REGISTER:
		// logs.debug("dispatch", "REGISTER");
		register_handler((msg_register*)responseBuffer);
		break;

	case REGACK:
		// logs.debug("dispatch", "REGACK");
		regAckHandler((msg_regack*)responseBuffer);
		break;

	case REREGISTER:
		// logs.debug("dispatch", "RE-REGISTER");
		reRegisterHandler((msg_reregister*)responseBuffer);
		break;

	case PUBLISH:
		// logs.debug("dispatch", "PUBLISH");
		publish_handler((msg_publish*)responseBuffer);
		break;

	case PUBACK:
		// logs.debug("dispatch", "PUBACK");
		pubAckHandler((msg_puback*)responseBuffer);
		break;

	case SUBACK:
		// logs.debug("dispatch", "SUBACK");
		suback_handler((msg_suback*)responseBuffer);
		break;

	case UNSUBACK:
		// logs.debug("dispatch", "UNSUBACK");
		unsuback_handler((msg_unsuback*)responseBuffer);
		break;

	case PINGREQ:
		// logs.debug("dispatch", "PINGREQ");
		pingreq_handler((msg_pingreq*)responseBuffer);
		break;

	case PINGRESP:
		// logs.debug("dispatch", "PINGRESP");
		pingresp_handler();
		break;

	case DISCONNECT:
		// logs.debug("dispatch", "DISCONNECT");
		disconnect_handler((msg_disconnect*)responseBuffer);
		break;

	case WILLTOPICRESP:
		// logs.debug("dispatch", "WILLTOPICRESP");
		willtopicresp_handler((msg_willtopicresp*)responseBuffer);
		break;

	case WILLMSGRESP:
		// logs.debug("dispatch", "WILLMSGRESP");
		willmsgresp_handler((msg_willmsgresp*)responseBuffer);
		break;

	default:
		// logs.debug("dispatch", "DEFAULT");
		return;
	}
}

void Mqttsn::pubAckHandler(const msg_puback* msg) {
	pubAckReturnCode = msg->return_code;
}

void Mqttsn::advertise_handler(const msg_advertise* msg) {
	gatewayId = msg->gw_id;
}

bool Mqttsn::multiCheckSerial(const int nb_max_try) {

	int nb_try = 0;

	// logs.debug("multiCheckSerial", "check serial iteration: ", nb_try);
	while( !checkSerial() && nb_try < nb_max_try ) {
		nb_try++;
		// logs.debug("multiCheckSerial", "check serial iteration: ", nb_try);
	}

	return nb_try != nb_max_try;
}

void Mqttsn::searchGateway(const uint8_t radius) {

	// logs.debug("searchGateway", "");

	msg_searchgw* msg = reinterpret_cast<msg_searchgw*>(messageBuffer);

	msg->length = sizeof(msg_searchgw);
	msg->type = SEARCHGW;
	msg->radius = radius;

	waitingForResponse = false;
	sendMessage();
}

void Mqttsn::sendMessage() {

	if(waitingForResponse) {
		logs.debug("sendMessage", "the module is already waiting for a response");
		return;
	}

#ifdef USE_SERIAL
	// logs.debug("sendMessage", "");

	// Sending the message stored into @messageBuffer through @MB_serial_send function
	// extern void MB_serial_send(uint8_t* messageBuffer, int length);
	waitingForResponse = true;
	message_header* header = reinterpret_cast<message_header*>(messageBuffer);

	int length = createFrame(messageBuffer, header->length, gatewayAddress, frameBufferOut, sizeof(frameBufferOut), false);

	if (length > 0) {
		xBee->write(frameBufferOut, length);
		xBee->flush();
		// logs.debug("sendMessage", "message sent");
	} else {
		// logs.debug("sendMessage", "message not sent");
	}
#endif
}

bool Mqttsn::waitData() {


	int i = 1;

	// waiting for incoming data during 1 second (10x100ms)
	while( xBee->available() <= 0 && i <= 10 ) {
		delay(1000);
		i++;
	}
	if( i == 20 ) {
		// timeout -> return false
		// logs.debug("waitData", "No data received");
		return false;
	}

	// logs.debug("waitData", "Data are received");
	return true;
}

bool Mqttsn::verifyChecksum(uint8_t frame_buffer[], int frame_size) {

	int i;
	uint16_t checksum = 0x00;

	for(i=0; i < frame_size; i++) {
		checksum += frame_buffer[i];
	}
	checksum = checksum & 0xFF;

	// logs.debug("verifyChecksum", "checksum is : ", (int) checksum);

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

void Mqttsn::connect(const uint8_t flags, const uint16_t duration, const char* client_id) {

	msg_connect* msg = reinterpret_cast<msg_connect*>(messageBuffer);

	msg->length = sizeof(msg_connect) + strlen(client_id);
	msg->type = CONNECT;
	msg->flags = flags;
	msg->protocol_id = PROTOCOL_ID;
	msg->duration = bitSwap(duration);
	strcpy(msg->client_id, client_id);

	sendMessage();
	connected = false;
}

void Mqttsn::parseData() {

	int i;
	int payload_lenght = frameBufferIn[12];
	uint8_t payload[payload_lenght];

	for(i = 0; i < payload_lenght; i++){
		payload[i] = frameBufferIn[12+i];
	}

	// logs.debug("parseData", "data have been parsed");

	if(waitingForPingResp){
		pingRespTimer = millis();
	}
	memset(responseBuffer, 0, MAX_BUFFER_SIZE);
	memcpy(responseBuffer, (const void*)payload, payload_lenght);

	// logs.debug( "parseStream", "Stream is ready -> dispatch");

	dispatch();

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
	case REJECTED:
		return "REJECTED";
	default:
		return "UNKNOWN";
	}
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
}

void Mqttsn::subscribeByName(const uint8_t flags, const char* topic_name) {

	// logs.debug("subscribeByName", "topic :", topic_name);

	++messageId;
	msg_subscribe* msg = reinterpret_cast<msg_subscribe*>(messageBuffer);

	// The -2 here is because we're unioning a 0-length member (topic_name)
	// with a uint16_t in the msg_subscribe struct.
	msg->length = sizeof(msg_subscribe) + strlen(topic_name) - 2;
	msg->type = SUBSCRIBE;
	msg->flags = (flags & QOS_MASK) | FLAG_TOPIC_NAME;
	msg->message_id = bitSwap(messageId);
	strcpy(msg->topic_name, topic_name);

	// logs.debug("subscribeByName", "sending message 'subscribe topic'");

	sendMessage();

	if ((flags & QOS_MASK) == FLAG_QOS_1 || (flags & QOS_MASK) == FLAG_QOS_2) {
		waitingForSubAck = true;
	}
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
}

// extern void Mqttsn_regAckHandler(const msg_regack* msg);
void Mqttsn::regAckHandler(const msg_regack* msg) {

	if (msg->return_code == ACCEPTED && nbRegisteredTopic < MAX_TOPICS && bitSwap(msg->message_id) == messageId) {
		topicTable[nbRegisteredTopic].id = bitSwap(msg->topic_id);


		nbRegisteredTopic++;
		regAckReturnCode = ACCEPTED;

		// logs.debug("regAckHandler", "Topic registered, it's id is ", msg->topic_id);
		// logs.debug("regAckHandler", "Topic registered, it's table id is ", topicTable[nbRegisteredTopic].id);
	} else {
		// logs.debug("regAckHandler", "Topic NOT registered");
		regAckReturnCode = REJECTED;
	}
}

void Mqttsn::reRegisterHandler(const msg_reregister* msg) {

	// logs.debug("reregisterTopic", "topic id:", msg->topic_id);
	short topicId = bitSwap(msg->topic_id);
	// logs.debug("reregisterTopic", "bitSwap topic id:", topicId);

	const char* topicName = findTopicName(msg->topic_id);

	if( strcmp("", topicName) != 0 ) {
		// logs.debug("reregisterTopic", "topic name found -> register:", topicName);
		registerTopic(topicName);
	}
}

void Mqttsn::publishMessage(const uint8_t flags, const uint16_t topic_id, const void* data, const uint8_t data_len) {

	++messageId;

	msg_publish* msg = reinterpret_cast<msg_publish*>(messageBuffer);

	msg->length = sizeof(msg_publish) + data_len;
	msg->type = PUBLISH;
	msg->flags = flags;
	// @BUG: the code of the original version
	// msg->topic_id = bitSwap(topic_id);
	msg->topic_id = topic_id;
	msg->message_id = bitSwap(messageId);
	memcpy(msg->data, data, data_len);

	// logs.debug("publishMessage", "publish data on topic id", msg->topic_id);

	sendMessage();
}

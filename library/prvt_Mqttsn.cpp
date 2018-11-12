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

bool Mqttsn::waitData() {

	int i = 1;

	while( xBee->available() <= 0 && i <= MAX_TRY ) {
		// waiting for incoming data longer during 1 second (1000ms)
		delay(1000);
		i++;
	}
	if( i >= MAX_TRY ) {
		// logs.debug("waitData", "no data received");
		return false;
	}

	// logs.debug("waitData", "data received");
	return true;
}

void Mqttsn::parseData() {

	int i;
	int payload_lenght = frameBufferIn[12];
	uint8_t payload[payload_lenght];

	// for(int i = 0; i <= 12; i++) {
	// Serial.print(i);
	// Serial.print(" : ");
	// Serial.println(frameBufferIn[12+i]);
	// }
	// Serial.println("--------");

	for(i = 0; i < payload_lenght; i++){
		payload[i] = frameBufferIn[12+i];
		// Serial.print(i + 12);
		// Serial.print(" : ");
		// Serial.println(payload[i]);
	}

	// logs.debug("parseData", "data have been parsed");

	memset(responseBuffer, 0, MAX_BUFFER_SIZE);
	memcpy(responseBuffer, (const void*)payload, payload_lenght);

	// logs.debug( "parseStream", "Stream is ready -> dispatch");

	dispatch();

	memset(frameBufferIn, 0, sizeof(frameBufferIn));
}

void Mqttsn::dispatch() {

	waitingForResponse = false;
	message_header* response_message = (message_header*)responseBuffer;

	// logs.debug("dispatch", "response type:", response_message->type);
	// logs.debug("dispatch", "response length:", response_message->length);

	switch (response_message->type) {
	case ADVERTISE:
		// logs.debug("dispatch", "ADVERTISE");
		advertiseHandler((msg_advertise*)responseBuffer);
		break;

	case GWINFO:
		// logs.debug("dispatch", "GWINFO");
		gatewayInfoHandler((msg_gwinfo*)responseBuffer);
		break;

	case CONNACK:
		// logs.debug("dispatch", "CONNACK");
		connAckHandler((msg_connack*)responseBuffer);
		break;

	case REGACK:
		// logs.debug("dispatch", "REGACK");
		regAckHandler((msg_regack*)responseBuffer);
		break;

	case REGISTER:
		// logs.debug("dispatch", "REGISTER");
		registerHandler((msg_register*)responseBuffer);
		break;

	case REREGISTER:
		// logs.debug("dispatch", "RE-REGISTER");
		reRegisterHandler((msg_reregister*)responseBuffer);
		break;

	case PUBLISH:
		// logs.debug("dispatch", "PUBLISH");
		publishHandler((msg_publish*)responseBuffer);
		break;

	case SUBACK:
		// logs.debug("dispatch", "SUBACK");
		subAckHandler((msg_suback*)responseBuffer);
		break;

	case UNSUBACK:
		// logs.debug("dispatch", "UNSUBACK");
		unsuback_handler((msg_unsuback*)responseBuffer);
		break;

	case PINGREQ:
		// logs.debug("dispatch", "PINGREQ");
		pingReqHandler((msg_pingreq*)responseBuffer);
		break;

	case PINGRESP:
		// logs.debug("dispatch", "PINGRESP");
		pingRespHandler();
		break;

	case DISCONNECT:
		// logs.debug("dispatch", "DISCONNECT");
		disconnect_handler((msg_disconnect*)responseBuffer);
		break;

	default:
		// logs.debug("dispatch", "DEFAULT");
		return;

		// @TODO not implemented yet - QoS level 1 or 2
		// case PUBACK:
		// logs.debug("dispatch", "PUBACK");
		// pubAckHandler((msg_puback*)responseBuffer);
		// break;

		// @TODO not implemented yet
		// case WILLTOPICRESP:
		// logs.debug("dispatch", "WILLTOPICRESP");
		// willTopicRespHandler((msg_willtopicresp*)responseBuffer);
		// break;

		// @TODO not implemented yet
		// case WILLMSGRESP:
		// logs.debug("dispatch", "WILLMSGRESP");
		// willMsgRespHandler((msg_willmsgresp*)responseBuffer);
		// break;

		// @TODO not implemented yet
		// case WILLTOPICREQ:
		// logs.debug("dispatch", "WILLTOPICREQ");
		// willTopicReqHandler(response_message);
		// break;

		// @TODO not implemented yet
		// case WILLMSGREQ:
		// logs.debug("dispatch", "WILLMSGREQ");
		// willmsgreq_handler(response_message);
		// break;
	}
}

void Mqttsn::regAck(const uint16_t topic_id, const uint16_t message_id, const return_code_t return_code) {
	msg_regack* msg = reinterpret_cast<msg_regack*>(messageBuffer);

	msg->length = sizeof(msg_regack);
	msg->type = REGACK;
	msg->topic_id = bitSwap(topic_id);
	msg->message_id = bitSwap(message_id);
	msg->return_code = return_code;

	sendMessage();
}

void Mqttsn::reRegister(const uint16_t topic_id, const uint16_t message_id, const return_code_t return_code) {
	msg_reregister* msg = reinterpret_cast<msg_reregister*>(messageBuffer);

	msg->length = sizeof(msg_reregister);
	msg->type = REREGISTER;
	msg->topic_id = bitSwap(topic_id);
	msg->message_id = bitSwap(message_id);
	msg->return_code = return_code;

	sendMessage();
}

void Mqttsn::unsuback_handler(const msg_unsuback* msg) {
}

void Mqttsn::pingReqHandler(const msg_pingreq* msg) {
	pingResp();
}

void Mqttsn::pingRespHandler() {
	// do nothing
	// logs.debug("pingResphandler", "end of published messages");
}

uint16_t Mqttsn::bitSwap(const uint16_t value) {
	return (value << 8) | (value >> 8);
}

void Mqttsn::publishHandler(const msg_publish* msg) {

	return_code_t ret = REJECTED_INVALID_TOPIC_ID;
	const uint16_t topic_id = bitSwap(msg->topic_id);

	// logs.debug("publishHandler", "");

	for (uint8_t i = 0; i < nbRegisteredTopic; ++i) {
		if (topicTable[i].id == topic_id) {
			ret = ACCEPTED;
			// logs.debug("publishHandler", "message accepted");
			// logs.debug("publishHandler", msg->data);
			memcpy(receivedMessages[nbReceivedMessages], msg->data, strlen(msg->data));
			nbReceivedMessages++;
			break;
		}
	}

	// @TODO not implemented yet - QoS level 1 or 2
	// logs.debug("publishHandler", "send pub ack");
	// logs.debug("publishHandler", "message id:", msg->message_id);
	// pubAck(msg->topic_id, msg->message_id, ret);

	// waiting next message
	if( !checkSerial() ) {
		return;
	}
	parseData();
}

/**
 * The function waits a response from the gateway (@waitData). If a response is available, the function analyse and store the message if necessary.
 *
 * Returns:
 * True if a correct message have been received, else false.
 **/
bool Mqttsn::checkSerial() {

	int i, frame_size;
	uint8_t delimiter, length1, length2;

	// no data is available
	if(!waitData()) {
		// logs.debug("checkSerial", "no data available -> timeout");
		return false;
	}
	delimiter = xBee->read();

	if(delimiter != 0x7E) {
		// logs.debug("checkSerial", "delimiter KO!");
		return false;
	}

	if(xBee->available() > 0) {
		length1 = xBee->read();
		length2 = xBee->read();
		frame_size = (length1*16)+length2+1;

		// store the data in @frameBuffer
		for(i = 0; i < frame_size; i++){
			delay(10);
			frameBufferIn[i] = xBee->read();
		}

		if(!verifyChecksum(frameBufferIn, frame_size)) {
			// logs.debug("checkSerial", "checksum KO!");
			return false;
		}

		if(isTransmitStatus()) {
			// logs.debug("checkSerial", "a transmit status (XBEE acquitall) -> get next message!");
			return checkSerial();
		}

		if(isDataPacket()) {
			// this is a data packet, copy the gateway address
			if(gatewayAddress[0]==0 && gatewayAddress[1]==0 && gatewayAddress[2]==0 && gatewayAddress[3]==0){
				gatewayAddress[0] = frameBufferIn[1];
				gatewayAddress[1] = frameBufferIn[2];
				gatewayAddress[2] = frameBufferIn[3];
				gatewayAddress[3] = frameBufferIn[4];
				gatewayAddress[4] = frameBufferIn[5];
				gatewayAddress[5] = frameBufferIn[6];
				gatewayAddress[6] = frameBufferIn[7];
				gatewayAddress[7] = frameBufferIn[8];
			}
			// all data have been store in @frameBufferIn
			// logs.debug("checkSerial", "correct data received");
			return true;
		}
	}
	// not data available, clear the buffer and return false
	memset(frameBufferIn, 0, sizeof(frameBufferIn));
	// logs.debug("checkSerial", "default KO!");
	return false;
}

void Mqttsn::subAckHandler(const msg_suback* msg) {
	subAckReturnCode = msg->return_code;
}

void Mqttsn::advertiseHandler(const msg_advertise* msg) {
	gatewayId = msg->gw_id;
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
		// logs.debug("sendMessage", "the module is already waiting for a response");
		return;
	}

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

bool Mqttsn::isTransmitStatus() {
	return frameBufferIn[0] == 139;
}

bool Mqttsn::isDataPacket() {
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
	memset(frame, 0, frame_max_lenght);

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

void Mqttsn::connect(const uint8_t flags, const uint16_t duration, const char* module_name) {

	msg_connect* msg = reinterpret_cast<msg_connect*>(messageBuffer);

	msg->length = sizeof(msg_connect) + strlen(module_name);
	msg->type = CONNECT;
	msg->flags = flags;
	msg->protocol_id = PROTOCOL_ID;
	msg->duration = bitSwap(duration);
	strcpy(msg->client_id, module_name);

	sendMessage();
	connected = false;
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

void Mqttsn::connAckHandler(const msg_connack* msg) {

	// logs.debug("connackHandler", "return code is: ", stringFromReturnCode(msg->return_code));

	connected = msg->return_code;
}

void Mqttsn::disconnect_handler(const msg_disconnect* msg) {
	connected = false;
	// Mqttsn_disconnect_handler(msg);
}

void Mqttsn::gatewayInfoHandler(const msg_gwinfo* message) {

	// logs.debug( "gatewayInfoHandler");

	if(message->gw_id == 1) {
		initOk = true;
	} else {
		initOk = false;
	}
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

void Mqttsn::registerHandler(const msg_register* message) {

	return_code_t ret = REJECTED_INVALID_TOPIC_ID;
	short topic_id = findTopicId(message->topic_name);

	// logs.debug("registerHandler", "received topic_name: ", message->topic_name);
	// logs.debug("registerHandler", "found topic id: ", (int)topic_id);

	if (topic_id != DEFAULT_TOPIC_ID) {
		topicTable[topic_id].id = bitSwap(message->topic_id);
		ret = ACCEPTED;
	}

	regAck(message->topic_id, message->message_id, ret);
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
	msg->topic_id = bitSwap(topic_id);

	msg->topic_id = topic_id;
	msg->message_id = bitSwap(messageId);
	memcpy(msg->data, data, data_len);

	// logs.debug("publishMessage", "publish data on topic id", msg->topic_id);
	sendMessage();
}

/**
 *
 * @TODO not implemented yet
 *
 * @brief Mqttsn::willTopic
 * @param flags
 * @param will_topic
 * @param update
 */
/*
void Mqttsn::willTopic(const uint8_t flags, const char* will_topic, const bool update) {
	if (will_topic == NULL) {
		message_header* msg = reinterpret_cast<message_header*>(messageBuffer);

		msg->type = update ? WILLTOPICUPD : WILLTOPIC;
		msg->length = sizeof(message_header);
	} else {
		msg_willtopic* msg = reinterpret_cast<msg_willtopic*>(messageBuffer);

		msg->type = update ? WILLTOPICUPD : WILLTOPIC;
		msg->flags = flags;
		strcpy(msg->will_topic, will_topic);
	}

	sendMessage();
}
*/

/**
 *
 * @TODO not implemented yet
 *
 * @brief Mqttsn::willMesssage
 * @param will_msg
 * @param will_msg_len
 * @param update
 */
/*
void Mqttsn::willMesssage(const void* will_msg, const uint8_t will_msg_len, const bool update) {
	msg_willmsg* msg = reinterpret_cast<msg_willmsg*>(messageBuffer);

	msg->length = sizeof(msg_willmsg) + will_msg_len;
	msg->type = update ? WILLMSGUPD : WILLMSG;
	memcpy(msg->willmsg, will_msg, will_msg_len);

	sendMessage();
}
*/

/**
 *
 * @TODO not implemented yet
 *
 * @brief Mqttsn::willTopicRespHandler
 * @param msg
 */
/*
void Mqttsn::willTopicRespHandler(const msg_willtopicresp* msg) {
}
*/

/**
 *
 * @TODO not implemented yet
 *
 * @brief Mqttsn::willMsgRespHandler
 * @param msg
 */
/*
void Mqttsn::willMsgRespHandler(const msg_willmsgresp* msg) {
}
*/

/**
 *
 * @TODO not implemented yet
 *
 * @brief Mqttsn::willTopicReqHandler
 * @param msg
 */
/*
void Mqttsn::willTopicReqHandler(const message_header* msg) {
}
*/

/**
 *
 * @TODO not implemented yet
 *
 * @brief Mqttsn::willMsgReqHandler
 * @param msg
 */
/*
void Mqttsn::willMsgReqHandler(const message_header* msg) {
}
*/

/**
 * ****************************
 *
 * QoS
 * @TODO not implemented yet - QoS level 1 or 2
 *
 * ****************************
 **/

/*
void Mqttsn::pubAckHandler(const msg_puback* msg) {
	pubAckReturnCode = msg->return_code;
}

void Mqttsn::pubRecHandler(const msg_pubqos2* msg) {
}

void Mqttsn::pubRelHandler(const msg_pubqos2* msg) {
}

void Mqttsn::pubCompHandler(const msg_pubqos2* msg) {
}

void Mqttsn::pubAck(const uint16_t topic_id, const uint16_t message_id, const return_code_t return_code) {
	msg_puback* msg = reinterpret_cast<msg_puback*>(messageBuffer);

	msg->length = sizeof(msg_puback);
	msg->type = PUBACK;
	msg->topic_id = bitSwap(topic_id);
	msg->message_id = bitSwap(message_id);
	msg->return_code = return_code;
	sendMessage();
}

void Mqttsn::pubRec() {
	msg_pubqos2* msg = reinterpret_cast<msg_pubqos2*>(message_buffer);
	msg->length = sizeof(msg_pubqos2);
	msg->type = PUBREC;
	msg->message_id = bitSwap(_message_id);

	sendMessage();
}

void Mqttsn::pubRel() {
	msg_pubqos2* msg = reinterpret_cast<msg_pubqos2*>(message_buffer);
	msg->length = sizeof(msg_pubqos2);
	msg->type = PUBREL;
	msg->message_id = bitSwap(_message_id);

	sendMessage();
}

void Mqttsn::pubComp() {
	msg_pubqos2* msg = reinterpret_cast<msg_pubqos2*>(message_buffer);
	msg->length = sizeof(msg_pubqos2);
	msg->type = PUBCOMP;
	msg->message_id = bitSwap(_message_id);

	sendMessage();
}
*/

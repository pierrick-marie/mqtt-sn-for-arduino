/*
BSD 3-Clause License

Copyright (c) 2018, marie
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice, this
  list of conditions and the following disclaimer.

* Redistributions in binary form must reproduce the above copyright notice,
  this list of conditions and the following disclaimer in the documentation
  and/or other materials provided with the distribution.

* Neither the name of the copyright holder nor the names of its
  contributors may be used to endorse or promote products derived from
  this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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

	// @BUG waits 500ms seconds otherwise the module miss some answers from the gateway
	delay(int_WAIT);

	int i = 1;
	while( xBee->available() <= 0 && i <= MAX_TRY ) {
		// waiting for incoming data longer during 1 second (1000ms)
		delay(LONG_WAIT);
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
	memcpy(responseBuffer, (void*)payload, payload_lenght);

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

	case GWINFO:
		// logs.debug("dispatch", "GWINFO");
		searchGatewayHandler((msg_gwinfo*)responseBuffer);
		break;

	case CONNACK:
		// logs.debug("dispatch", "CONNACK");
		connAckHandler((msg_connack*)responseBuffer);
		break;

	case REGACK:
		// logs.debug("dispatch", "REGACK");
		regAckHandler((msg_regack*)responseBuffer);
		break;

	case PUBLISH:
		// logs.debug("dispatch", "PUBLISH");
		publishHandler((msg_publish*)responseBuffer);
		break;

	case SUBACK:
		// logs.debug("dispatch", "SUBACK");
		subAckHandler((msg_suback*)responseBuffer);
		break;

	case PINGRESP:
		// logs.debug("dispatch", "PINGRESP");
		pingRespHandler();
		break;

	case DISCONNECT:
		// logs.debug("dispatch", "DISCONNECT");
		disconnectHandler((msg_disconnect*)responseBuffer);
		break;

	case REREGISTER:
		// logs.debug("dispatch", "RE-REGISTER");
		reRegisterHandler((msg_reregister*)responseBuffer);
		break;

	default:
		// logs.debug("dispatch", "DEFAULT");
		return;

		// @TODO not implemented yet
		// case ADVERTISE:
		// logs.debug("dispatch", "ADVERTISE");
		// advertiseHandler((msg_advertise*)responseBuffer);
		// break;

		// @TODO not implemented yet
		// case PINGREQ:
		// logs.debug("dispatch", "PINGREQ");
		// pingReqHandler((msg_pingreq*)responseBuffer);
		// break;

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
		// case UNSUBACK:
		// logs.debug("dispatch", "UNSUBACK");
		// unsuback_handler((msg_unsuback*)responseBuffer);
		// break;

		// @TODO not implemented yet
		// case REGISTER:
		// logs.debug("dispatch", "REGISTER");
		// registerHandler((msg_register*)responseBuffer);
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

void Mqttsn::pingRespHandler() {
	// do nothing
	// logs.debug("pingRespHandler", "end of published messages");
}

uint16_t Mqttsn::bitSwap(uint16_t value) {
	return (value << 8) | (value >> 8);
}

void Mqttsn::publishHandler(msg_publish* msg) {

	if(nbReceivedMessage < MAX_MESSAGES) {
		logs.info("msg received");
		receivedMessages[nbReceivedMessage].topic_id = bitSwap(msg->topic_id);
		strcpy(receivedMessages[nbReceivedMessage].data, msg->data);
		nbReceivedMessage++;
	} else {
		// logs.debug("publishHandler", "too many received messages");
	}
	// logs.debug("publishHandler", "nb received messages: ", nbReceivedMessage);
	// logs.debug("publishHandler", "topic: ", receivedMessages[nbReceivedMessage].topic_id);
	// logs.debug("publishHandler", "message: ", receivedMessages[nbReceivedMessage].data);

	// @TODO not implemented yet - QoS level 1 or 2
	// logs.debug("publishHandler", "send pub ack");
	// logs.debug("publishHandler", "message id:", msg->message_id);
	// pubAck(msg->topic_id, msg->message_id, ret);

	// waiting next message
	if( !checkSerial() ) {
		connected = REJECTED;
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
		return checkSerial();
	}

	xBee->listen();
	if(xBee->isListening()) {
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
				return checkSerial();
			}

			if(frameBufferIn[0] == 139) {
				// logs.debug("checkSerial", "a transmit status (XBEE acquitall) -> get next message!");
				return checkSerial();
			}

			if(frameBufferIn[0] == 144) {
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
	}
	// not data available, clear the buffer and return false
	memset(frameBufferIn, 0, sizeof(frameBufferIn));
	// logs.debug("checkSerial", "default KO!");
	return false;
}

void Mqttsn::subAckHandler(msg_suback* msg) {

	if (msg->return_code == ACCEPTED && nbRegisteredTopic < MAX_TOPICS && bitSwap(msg->message_id) == messageId) {
		logs.info("subscibe ok");

		topicTable[lastSubscribedTopic].id = msg->topic_id;
		regAckReturnCode = ACCEPTED;

		logs.debug("subAckHandler", "Topic subscribed, id: ", msg->topic_id);
		logs.debug("subAckHandler", "Topic subscribed, table id: ", topicTable[nbRegisteredTopic].id);
		logs.debug("subAckHandler", "Topic subscribed, table name: ", topicTable[nbRegisteredTopic].name);

		nbRegisteredTopic++;
	} else {
		logs.error("subscibe KO");
		regAckReturnCode = REJECTED;
	}

}

void Mqttsn::sendMessage() {

	// logs.debug("sendMessage");
	delay(int_WAIT);

	if(waitingForResponse) {
		// logs.debug("sendMessage", "the module is already waiting for a response");
		return;
	}
	waitingForResponse = true;

	// Sending the message stored into @messageBuffer through @MB_serial_send function
	// extern void MB_serial_send(uint8_t* messageBuffer, int length);
	message_header* header = reinterpret_cast<message_header*>(messageBuffer);

	int length = createFrame(header->length);

	if (length > 0) {

		xBee->listen();
		while(!xBee->isListening()) { } // infinite loop to wait xBee module

		xBee->write(frameBufferOut, length);
		xBee->flush();
		// logs.debug("sendMessage", "message sent");
	} else {
		// logs.debug("sendMessage", "message not sent");
	}

	// @BUG waits 500ms seconds otherwise the module miss some answers from the gateway
	delay(int_WAIT);
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

int Mqttsn::createFrame(int header_lenght) {

	// logs.debug("createFrame");

	uint8_t checksum = 0;
	int i = 0;

	// data is too long?
	// TODO: Split in multiple packets?
	if (header_lenght > API_DATA_LEN) {
		// logs.debug("createFrame", "ERROR - header_lenght too long: ", header_lenght);
		return -2;
	}

	// frame buffer is fine, clear it
	memset(frameBufferOut, 0, sizeof(frameBufferOut));

	/* The header */

	// delimiter
	frameBufferOut[0] = API_START_DELIMITER;

	// length of the payload
	frameBufferOut[1] = 0;
	frameBufferOut[2] = 14 + header_lenght;

	// frame Type: Transmit Request
	checksum = 0;
	checksum += frameBufferOut[3] = 16;

	// frame id
	checksum += frameBufferOut[4] = frameId++;

	// 64-bit address
	checksum += frameBufferOut[5] = gatewayAddress[0];
	checksum += frameBufferOut[6] = gatewayAddress[1];
	checksum += frameBufferOut[7] = gatewayAddress[2];
	checksum += frameBufferOut[8] = gatewayAddress[3];
	checksum += frameBufferOut[9] = gatewayAddress[4];
	checksum += frameBufferOut[10] = gatewayAddress[5];
	checksum += frameBufferOut[11] = gatewayAddress[6];
	checksum += frameBufferOut[12] = gatewayAddress[7];

	// 16-bit address
	checksum += frameBufferOut[13] = 0;
	checksum += frameBufferOut[14] = 0;
	checksum += frameBufferOut[15] = 0;
	checksum += frameBufferOut[16] = 0;


	/* The data */
	for (i = 0; i < header_lenght; i++) {
		checksum += frameBufferOut[17 + i] = messageBuffer[i];
		Serial.print(" ");
	}

	checksum = 0XFF - checksum;
	frameBufferOut[17 + header_lenght] = checksum;

	return 17 + header_lenght + 1;
}

void Mqttsn::connAckHandler(msg_connack* msg) {

	if(msg->return_code == ACCEPTED) {
		connected = ACCEPTED;
		logs.info("connected");
	} else {
		connected = REJECTED;
		logs.notConnected();
		while(1);
	}
}

void Mqttsn::disconnectHandler(msg_disconnect* msg) {

	connected = REJECTED;

	logs.info("slepping");

	delay(TIME_TO_SLEEP*1000); // time in seconds to milliseconds

	logs.info("awake");
}

void Mqttsn::searchGatewayHandler(msg_gwinfo* message) {

	if(message->gw_id == GATEWAY_ID) {
		logs.info("started");
		initOk = true;
	} else {
		logs.error("not started: stop");
		initOk = false;
		while(1);
	}
}

void Mqttsn::regAckHandler(msg_regack* msg) {

	if (msg->return_code == ACCEPTED && nbRegisteredTopic < MAX_TOPICS && bitSwap(msg->message_id) == messageId) {
		logs.info("register ok");

		topicTable[nbRegisteredTopic].id = msg->topic_id;
		regAckReturnCode = ACCEPTED;

		// logs.debug("regAckHandler", "Topic registered, id: ", msg->topic_id);
		// logs.debug("regAckHandler", "Topic registered, table id: ", topicTable[nbRegisteredTopic].id);
		// logs.debug("regAckHandler", "Topic registered, table name: ", topicTable[nbRegisteredTopic].name);

		nbRegisteredTopic++;
	} else {
		logs.error("register KO");
		regAckReturnCode = REJECTED;
	}
}

void Mqttsn::resetRegisteredTopicId(int topicId) {

	for (int i = 0; i < nbRegisteredTopic; i++) {
		if (topicTable[i].id == topicId) {
			// logs.debug("resetRegisteredTopicId", "topic id found");
			topicTable[i].id = DEFAULT_TOPIC_ID;
		}
	}

	// logs.debug("resetRegisteredTopicId", "topic id NOT found");
}

/**
 * @brief Mqttsn::reRegisterHandler
 * @param msg
 **/
void Mqttsn::reRegisterHandler(msg_reregister* msg) {

	// logs.debug("reregisterTopic", "topic id:", msg->topic_id);
	const char* topicName = findTopicName(msg->topic_id);

	resetRegisteredTopicId(msg->topic_id);

	if( strcmp("", topicName) != 0 ) {
		// logs.debug("reregisterTopic", "topic name found -> register:", topicName);
		registerTopic(topicName);
	}
}

/**
 * @TODO never used
 *
 * @brief Mqttsn::unsubscribeById
 * @param flags
 * @param topic_id
 *
void Mqttsn::unsubscribeById(uint8_t flags, uint16_t topic_id) {
	++messageId;

	msg_unsubscribe* msg = reinterpret_cast<msg_unsubscribe*>(messageBuffer);

	msg->length = sizeof(msg_unsubscribe);
	msg->type = UNSUBSCRIBE;
	msg->flags = (flags & QOS_MASK) | FLAG_TOPIC_PREDEFINED_ID;
	msg->message_id = bitSwap(messageId);
	msg->topic_id = bitSwap(topic_id);

	sendMessage();
}
*/

/**
 * @TODO not implemented yet
 *
 * @brief Mqttsn::unsuback_handler
 * @param msg
 *
void Mqttsn::unsuback_handler(msg_unsuback* msg) {
}
*/

/**
 * @TODO never used
 *
 * @brief Mqttsn::unsubscribeByName
 * @param flags
 * @param topic_name
 *
void Mqttsn::unsubscribeByName(uint8_t flags, char* topic_name) {
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
*/

/**
 * @TODO not implemented yet
 *
 * @brief Mqttsn::advertiseHandler
 * @param msg
 *
void Mqttsn::advertiseHandler(msg_advertise* msg) {

	// logs.debug("advertiseHandler");

	gatewayId = msg->gw_id;
}
*/

/**
 * @TODO never used
 *
 * @brief Mqttsn::subscribeById
 * @param flags
 * @param topic_id
 *
void Mqttsn::subscribeById(uint8_t flags, uint16_t topic_id) {
	++messageId;

	msg_subscribe* msg = reinterpret_cast<msg_subscribe*>(messageBuffer);

	msg->length = sizeof(msg_subscribe);
	msg->type = SUBSCRIBE;
	msg->flags = (flags & QOS_MASK) | FLAG_TOPIC_PREDEFINED_ID;
	msg->message_id = bitSwap(messageId);
	msg->topic_id = bitSwap(topic_id);

	sendMessage();
}
*/

/**
 * @TODO not implemented yet
 *
 * @brief Mqttsn::willTopic
 * @param flags
 * @param will_topic
 * @param update
 *
void Mqttsn::willTopic(uint8_t flags, char* will_topic, bool update) {
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
 * @TODO not implemented yet
 *
 * @brief Mqttsn::willMesssage
 * @param will_msg
 * @param will_msg_len
 * @param update
 *
void Mqttsn::willMesssage(void* will_msg, uint8_t will_msg_len, bool update) {
	msg_willmsg* msg = reinterpret_cast<msg_willmsg*>(messageBuffer);

	msg->length = sizeof(msg_willmsg) + will_msg_len;
	msg->type = update ? WILLMSGUPD : WILLMSG;
	memcpy(msg->willmsg, will_msg, will_msg_len);

	sendMessage();
}
*/

/**
 * @TODO not implemented yet
 *
 * @brief Mqttsn::willTopicRespHandler
 * @param msg
 *
void Mqttsn::willTopicRespHandler(msg_willtopicresp* msg) {
}
*/

/**
 * @TODO not implemented yet
 *
 * @brief Mqttsn::willMsgRespHandler
 * @param msg
 *
void Mqttsn::willMsgRespHandler(msg_willmsgresp* msg) {
}
*/

/**
 * @TODO not implemented yet
 *
 * @brief Mqttsn::willTopicReqHandler
 * @param msg
 */
/*
void Mqttsn::willTopicReqHandler(message_header* msg) {
}
*/

/**
 * @TODO not implemented yet
 *
 * @brief Mqttsn::willMsgReqHandler
 * @param msg
 *
void Mqttsn::willMsgReqHandler(message_header* msg) {
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
void Mqttsn::pubAckHandler(msg_puback* msg) {
	pubAckReturnCode = msg->return_code;
}

void Mqttsn::pubRecHandler(msg_pubqos2* msg) {
}

void Mqttsn::pubRelHandler(msg_pubqos2* msg) {
}

void Mqttsn::pubCompHandler(msg_pubqos2* msg) {
}

void Mqttsn::pubAck(uint16_t topic_id, uint16_t message_id, return_code_t return_code) {
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

/**
 * @TODO not implemented yet
 *
 * @brief Mqttsn::registerHandler
 * @param message
 *
void Mqttsn::registerHandler(msg_register* message) {

	return_code_t ret = REJECTED_INVALID_TOPIC_ID;
	int topic_id = findTopicId(message->topic_name);

	// logs.debug("registerHandler", "received topic_name: ", message->topic_name);
	// logs.debug("registerHandler", "found topic id: ", (int)topic_id);

	if (topic_id != DEFAULT_TOPIC_ID) {
		topicTable[topic_id].id = bitSwap(message->topic_id);
		ret = ACCEPTED;
	}

	regAck(message->topic_id, message->message_id, ret);
}
*/

/**
 * * @TODO not implemented yet
 *
 * @brief Mqttsn::regAck
 * @param topic_id
 * @param message_id
 * @param return_code
 *
void Mqttsn::regAck(uint16_t topic_id, uint16_t message_id, return_code_t return_code) {
	msg_regack* msg = reinterpret_cast<msg_regack*>(messageBuffer);

	msg->length = sizeof(msg_regack);
	msg->type = REGACK;
	msg->topic_id = bitSwap(topic_id);
	msg->message_id = bitSwap(message_id);
	msg->return_code = return_code;

	sendMessage();
}
*/

/**
 * @TODO not implemented yet
 *
 * @brief Mqttsn::reRegister
 * @param topic_id
 * @param message_id
 * @param return_code
 *
void Mqttsn::reRegister(uint16_t topic_id, uint16_t message_id, return_code_t return_code) {
	msg_reregister* msg = reinterpret_cast<msg_reregister*>(messageBuffer);

	msg->length = sizeof(msg_reregister);
	msg->type = REREGISTER;
	msg->topic_id = bitSwap(topic_id);
	msg->message_id = bitSwap(message_id);
	msg->return_code = return_code;

	sendMessage();
}
*/

/**
 * @TODO not implemented yet
 *
 * @brief Mqttsn::pingReqHandler
 * @param msg
 *
void Mqttsn::pingReqHandler(msg_pingreq* msg) {
	pingResp();
}
*/

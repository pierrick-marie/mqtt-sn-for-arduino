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

	delay(200);

	int i = 0;
	XBEE->listen();
	while(XBEE->isListening() && XBEE->available() <= 0 && i <= MAX_TRY ) {
		// waiting for incoming data longer during 1 second (1000ms)
		delay(200);
		i++;
	}
	if( i >= MAX_TRY ) {
		// LOGS.debugln("waitData", "KO");
		return false;
	}

	// LOGS.debugln("waitData", "OK");
	return true;
}

void Mqttsn::parseData() {

	int i;
	int payloadLenght = FrameBufferIn[12];
	uint8_t payload[payloadLenght];

	for(i = 0; i < payloadLenght; i++){
		payload[i] = FrameBufferIn[12+i];
	}

	// LOGS.debugln("parseData", "OK");

	memset(ResponseBuffer, 0, MAX_BUFFER_SIZE);
	memcpy(ResponseBuffer, (void*)payload, payloadLenght);

	// LOGS.debug("parseData", "-> dispatch");

	dispatch();

	memset(FrameBufferIn, 0, sizeof(FrameBufferIn));
}

void Mqttsn::dispatch() {

	// WaitingForResponse = false;
	MessageHeader* responseMessage = (MessageHeader*)ResponseBuffer;

	// LOGS.debug("dispatch", "response type:", responseMessage->type);
	// LOGS.debug("dispatch", "response length:", responseMessage->length);

	if(false == SearchGatewayOk && GWINFO == responseMessage->type) {
		// LOGS.debugln("dispatch", "x2");
		searchGatewayHandler((MsgGwinfo*)ResponseBuffer);
	} else {

		switch (responseMessage->type) {

		case CONNACK:
			// LOGS.debugln("dispatch", "x5");
			connAckHandler((MsgConnAck*)ResponseBuffer);
			break;

		case REGACK:
			// LOGS.debugln("dispatch", "xB");
			regAckHandler((MsgRegAck*)ResponseBuffer);
			break;

		case PUBLISH:
			// LOGS.debugln("dispatch", "xC");
			publishHandler((MsgPublish*)ResponseBuffer);
			break;

		case SUBACK:
			// LOGS.debugln("dispatch", "x13");
			subAckHandler((MsgSubAck*)ResponseBuffer);
			break;

		case PINGRESP:
			// LOGS.debugln("dispatch", "x17");
			pingRespHandler();
			break;

		case DISCONNECT:
			// LOGS.debugln("dispatch", "x18");
			disconnectHandler((MsgDisconnect*)ResponseBuffer);
			break;

		case REREGISTER:
			// LOGS.debugln("dispatch", "x1E");
			reRegisterHandler((MsgReRegister*)ResponseBuffer);
			break;

		default:
			// LOGS.debugln("dispatch", "xx");
			return;

			// @TODO not implemented yet
			// case ADVERTISE:
			// LOGS.debug("dispatch", "ADVERTISE");
			// advertiseHandler((msg_advertise*)ResponseBuffer);
			// break;

			// @TODO not implemented yet
			// case PINGREQ:
			// LOGS.debug("dispatch", "PINGREQ");
			// pingReqHandler((msg_pingreq*)ResponseBuffer);
			// break;

			// @TODO not implemented yet - QoS level 1 or 2
			// case PUBACK:
			// LOGS.debug("dispatch", "PUBACK");
			// pubAckHandler((msg_puback*)ResponseBuffer);
			// break;

			// @TODO not implemented yet
			// case WILLTOPICRESP:
			// LOGS.debug("dispatch", "WILLTOPICRESP");
			// willTopicRespHandler((msg_willtopicresp*)ResponseBuffer);
			// break;

			// @TODO not implemented yet
			// case UNSUBACK:
			// LOGS.debug("dispatch", "UNSUBACK");
			// unsuback_handler((msg_unsuback*)ResponseBuffer);
			// break;

			// @TODO not implemented yet
			// case REGISTER:
			// LOGS.debug("dispatch", "REGISTER");
			// registerHandler((msg_register*)ResponseBuffer);
			// break;

			// @TODO not implemented yet
			// case WILLMSGRESP:
			// LOGS.debug("dispatch", "WILLMSGRESP");
			// willMsgRespHandler((msg_willmsgresp*)ResponseBuffer);
			// break;

			// @TODO not implemented yet
			// case WILLTOPICREQ:
			// LOGS.debug("dispatch", "WILLTOPICREQ");
			// willTopicReqHandler(responseMessage);
			// break;

			// @TODO not implemented yet
			// case WILLMSGREQ:
			// LOGS.debug("dispatch", "WILLMSGREQ");
			// willmsgreq_handler(responseMessage);
			// break;
		}
	}
}

void Mqttsn::pingRespHandler() {
	// do nothing
	// LOGS.debug("pingRespHandler", "end of published messages");
}

uint16_t Mqttsn::bitSwap(uint16_t value) {
	return (value << 8) | (value >> 8);
}

void Mqttsn::publishHandler(MsgPublish* msg) {

	if(NbReceivedMessage < MAX_MESSAGES) {
		LOGS.info("msg received");
		ReceivedMessages[NbReceivedMessage].topicId = bitSwap(msg->topicId);
		strcpy(ReceivedMessages[NbReceivedMessage].data, msg->data);
		NbReceivedMessage++;
	} else {
		// LOGS.debug("publishHandler", "too many received messages");
	}
	// LOGS.debug("publishHandler", "nb received messages: ", NbReceivedMessage);
	// LOGS.debug("publishHandler", "topic: ", ReceivedMessages[NbReceivedMessage].topicId);
	// LOGS.debug("publishHandler", "message: ", ReceivedMessages[NbReceivedMessage].data);

	// @TODO not implemented yet - QoS level 1 or 2
	// LOGS.debug("publishHandler", "send pub ack");
	// LOGS.debug("publishHandler", "message id:", msg->messageId);
	// pubAck(msg->topicId, msg->messageId, ret);

	// waiting next message
	if( !checkSerial() ) {
		Connected = REJECTED;
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

	// LOGS.debug("checkSerial", "");

	int i, frameSize;
	uint8_t delimiter, length1, length2;

	// no data is available
	if(!waitData()) {
		// LOGS.debugln("checkSerial", "KO 0");
		return false;
	}

	delimiter = XBEE->read();

	if(delimiter != 0x7E) {
		// LOGS.debug("checkSerial", "KO 1", delimiter);
		// return checkSerial();
		return false;
	}

	length1 = XBEE->read();
	length2 = XBEE->read();
	frameSize = (length1*16)+length2+1;

	// store the data in @frameBuffer
	for(i = 0; i < frameSize; i++){
		delay(10);
		FrameBufferIn[i] = XBEE->read();
	}

	if(!verifyChecksum(FrameBufferIn, frameSize)) {
		// LOGS.debug("checkSerial", "KO 2", delimiter);
		// return checkSerial();
		return false;
	}

	if(FrameBufferIn[0] == 139 || FrameBufferIn[0] == 161) {
		// LOGS.debug("checkSerial", "KO 3" , FrameBufferIn[0]);
		// return checkSerial();
		return false;
	}

	// printFrameBufferIn();

	if(FrameBufferIn[0] == 144) {
		// this is a data packet, copy the gateway address
		if(0 == GatewayAddress[0] && 0 == GatewayAddress[1] && 0 == GatewayAddress[2] && 0 == GatewayAddress[3]){
			GatewayAddress[0] = FrameBufferIn[1];
			GatewayAddress[1] = FrameBufferIn[2];
			GatewayAddress[2] = FrameBufferIn[3];
			GatewayAddress[3] = FrameBufferIn[4];
			GatewayAddress[4] = FrameBufferIn[5];
			GatewayAddress[5] = FrameBufferIn[6];
			GatewayAddress[6] = FrameBufferIn[7];
			GatewayAddress[7] = FrameBufferIn[8];
		}
		// all data have been store in @FrameBufferIn
		// LOGS.debugln("checkSerial", "OK");
		return true;
	}

	// not data available, clear the buffer and return false
	memset(FrameBufferIn, 0, sizeof(FrameBufferIn));
	// LOGS.debugln("checkSerial", "KO 4");
	return false;
}

void Mqttsn::subAckHandler(MsgSubAck* msg) {

	if (msg->returnCode == ACCEPTED && NbRegisteredTopic < MAX_TOPICS) {
		TopicTable[LastSubscribedTopic].id = msg->topicId;
		RegAckReturnCode = ACCEPTED;

		// LOGS.debug("subAckHandler", "msg->id: ", msg->topicId);
		// LOGS.debug("subAckHandler", "table[id]: ", TopicTable[LastSubscribedTopic].id);
		// LOGS.debug("subAckHandler", "table[name]: ", TopicTable[LastSubscribedTopic].name);

		LOGS.info("subscribe OK");
	} else {
		LOGS.error("subscribe KO");
		RegAckReturnCode = REJECTED;
	}
}

void Mqttsn::sendMessage() {

	// LOGS.debug("sendMessage");
	delay(getRandomTime());

	/*
	if(WaitingForResponse) {
		// LOGS.debug("sendMessage", "the module is already waiting for a response");
		return;
	}
	WaitingForResponse = true;
	*/

	// Sending the message stored into @MessageBuffer through @MB_serial_send function
	// extern void MB_serial_send(uint8_t* MessageBuffer, int length);
	MessageHeader* header = reinterpret_cast<MessageHeader*>(MessageBuffer);

	int length = createFrame(header->length);

	if (length > 0) {

		XBEE->listen();
		while(!XBEE->isListening()) { } // infinite loop to wait XBEE module

		// displayFrameBufferOut();
		XBEE->write(FrameBufferOut, length);
		XBEE->flush();
		// LOGS.debug("sendMessage", "message sent");
	} else {
		// LOGS.debug("sendMessage", "message not sent");
	}

	// @BUG waits 500ms seconds otherwise the module miss some answers from the gateway
	delay(getRandomTime());
}

bool Mqttsn::verifyChecksum(uint8_t _frameBuffer[], int _frameSize) {

	int i;
	uint16_t checksum = 0x00;

	for(i=0; i < _frameSize; i++) {
		checksum += _frameBuffer[i];
	}
	checksum = checksum & 0xFF;

	// LOGS.debug("verifyChecksum", "checksum is : ", (int) checksum);

	return checksum == 0xFF ;
}

int Mqttsn::createFrame(int _headerLenght) {

	// LOGS.debug("createFrame");

	uint8_t checksum = 0;
	int i = 0;

	// data is too long?
	// TODO: Split in multiple packets?
	if (_headerLenght > API_DATA_LEN) {
		// LOGS.debug("createFrame", "ERROR - _headerLenght too long: ", _headerLenght);
		return -2;
	}

	// frame buffer is fine, clear it
	memset(FrameBufferOut, '\0', sizeof(FrameBufferOut));

	/* The header */

	// delimiter
	FrameBufferOut[0] = API_START_DELIMITER;

	// length of the payload
	FrameBufferOut[1] = 0;
	FrameBufferOut[2] = 14 + _headerLenght;

	// frame Type: Transmit Request
	checksum = 0;
	checksum += FrameBufferOut[3] = FRAME_TYPE_TRANSMIT_REQUEST;

	// frame id
	checksum += FrameBufferOut[4] = FRAME_ID_WITHOUT_ACK;

	// 64-bit address
	checksum += FrameBufferOut[5] = GatewayAddress[0];
	checksum += FrameBufferOut[6] = GatewayAddress[1];
	checksum += FrameBufferOut[7] = GatewayAddress[2];
	checksum += FrameBufferOut[8] = GatewayAddress[3];
	checksum += FrameBufferOut[9] = GatewayAddress[4];
	checksum += FrameBufferOut[10] = GatewayAddress[5];
	checksum += FrameBufferOut[11] = GatewayAddress[6];
	checksum += FrameBufferOut[12] = GatewayAddress[7];

	// 16-bit address
	checksum += FrameBufferOut[13] = 0;
	checksum += FrameBufferOut[14] = 0;

	// broadcast radius
	checksum += FrameBufferOut[15] = BROADCAST_RADIUS_ZERO;

	// options
	checksum += FrameBufferOut[16] = OPTION_DISABLE_RETRIES;


	/* The data */
	for (i = 0; i < _headerLenght; i++) {
		checksum += FrameBufferOut[17 + i] = MessageBuffer[i];
	}

	checksum = 0XFF - checksum;
	FrameBufferOut[17 + _headerLenght] = checksum;

	return 17 + _headerLenght + 1;
}

void Mqttsn::connAckHandler(MsgConnAck* msg) {

	if(msg->returnCode == ACCEPTED) {
		Connected = ACCEPTED;
		LOGS.info("connected");
	} else {
		Connected = REJECTED;
		LOGS.error("not connected");
		while(1);
	}

	delay(getRandomTime());
}

void Mqttsn::disconnectHandler(MsgDisconnect* msg) {

	Connected = REJECTED;

	LOGS.info("disconnected");

	delay(SLEEP_TIME * 1500); // 10 ms * 1500 = 15000 ms = 15 s

	LOGS.info("awake");
}

void Mqttsn::searchGatewayHandler(MsgGwinfo* message) {

	if(message->gatewayId == GATEWAY_ID) {
		LOGS.info("started");
		SearchGatewayOk = true;
	} else {
		LOGS.error("not started");
		SearchGatewayOk = false;
		while(1);
	}
}

void Mqttsn::regAckHandler(MsgRegAck* msg) {

	if (msg->returnCode == ACCEPTED && NbRegisteredTopic < MAX_TOPICS) {

		TopicTable[NbRegisteredTopic].id = msg->topicId;
		RegAckReturnCode = ACCEPTED;

		// LOGS.debug("regAckHandler", "Topic registered, id: ", msg->topicId);
		// LOGS.debug("regAckHandler", "Topic registered, table id: ", TopicTable[NbRegisteredTopic].id);
		// LOGS.debug("regAckHandler", "Topic registered, table name: ", TopicTable[NbRegisteredTopic].name);
		LOGS.info("register OK");

		NbRegisteredTopic++;
	} else {
		LOGS.error("register KO");
		RegAckReturnCode = REJECTED;
	}

	delay(getRandomTime());
}

void Mqttsn::resetRegisteredTopicId(int topicId) {

	for (int i = 0; i < NbRegisteredTopic; i++) {
		if (TopicTable[i].id == topicId) {
			LOGS.debug("resetRegisteredTopicId", "id = ", topicId);
			TopicTable[i].id = DEFAULT_TOPIC_ID;
			return;
		}
	}

	LOGS.debugln("resetRegisteredTopicId", "topic id NOT found");
}

/**
 * @brief Mqttsn::reRegisterHandler
 * @param msg
 **/
void Mqttsn::reRegisterHandler(MsgReRegister* msg) {

	// LOGS.debug("reregisterTopic", "topic id:", msg->topicId);
	const char* topicName = findTopicName(msg->topicId);

	resetRegisteredTopicId(msg->topicId);

	if( strcmp("", topicName) != 0 ) {
		// LOGS.debug("reregisterTopic", "topic name found -> register:", topicName);
		registerTopic(topicName);
	}

	delay(getRandomTime());
}

int Mqttsn::getRandomTime() {

	return random(MIN_WAIT, MAX_WAIT) * 100;
}

/*
void Mqttsn::printFrameBufferOut() {
	int i = 0;
	for(; i < sizeof(FrameBufferOut); i++) {
		Serial.print(FrameBufferOut[i], HEX);
		Serial.print(" ");
	}
	Serial.println("");
}
*/

/*
void Mqttsn::printFrameBufferIn() {
	int i = 0;
	for(; i < sizeof(FrameBufferIn); i++) {
		Serial.print(FrameBufferIn[i], HEX);
		Serial.print(" ");
	}
	Serial.println("");
}
*/

/**
 * @TODO never used
 *
 * @brief Mqttsn::unsubscribeById
 * @param flags
 * @param topicId
 *
void Mqttsn::unsubscribeById(uint8_t flags, uint16_t topicId) {
	++messageId;

	msg_unsubscribe* msg = reinterpret_cast<msg_unsubscribe*>(MessageBuffer);

	msg->length = sizeof(msg_unsubscribe);
	msg->type = UNSUBSCRIBE;
	msg->flags = (flags & QOS_MASK) | FLAG_TOPIC_PREDEFINED_ID;
	msg->messageId = bitSwap(messageId);
	msg->topicId = bitSwap(topicId);

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

	msg_unsubscribe* msg = reinterpret_cast<msg_unsubscribe*>(MessageBuffer);

	// The -2 here is because we're unioning a 0-length member (topic_name)
	// with a uint16_t in the msg_unsubscribe struct.
	msg->length = sizeof(msg_unsubscribe) + strlen(topic_name) - 2;
	msg->type = UNSUBSCRIBE;
	msg->flags = (flags & QOS_MASK) | FLAG_TOPIC_NAME;
	msg->messageId = bitSwap(messageId);
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

	// LOGS.debug("advertiseHandler");

	gatewayId = msg->gw_id;
}
*/

/**
 * @TODO never used
 *
 * @brief Mqttsn::subscribeById
 * @param flags
 * @param topicId
 *
void Mqttsn::subscribeById(uint8_t flags, uint16_t topicId) {
	++messageId;

	msg_subscribe* msg = reinterpret_cast<msg_subscribe*>(MessageBuffer);

	msg->length = sizeof(msg_subscribe);
	msg->type = SUBSCRIBE;
	msg->flags = (flags & QOS_MASK) | FLAG_TOPIC_PREDEFINED_ID;
	msg->messageId = bitSwap(messageId);
	msg->topicId = bitSwap(topicId);

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
		MessageHeader* msg = reinterpret_cast<MessageHeader*>(MessageBuffer);

		msg->type = update ? WILLTOPICUPD : WILLTOPIC;
		msg->length = sizeof(MessageHeader);
	} else {
		msg_willtopic* msg = reinterpret_cast<msg_willtopic*>(MessageBuffer);

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
	msg_willmsg* msg = reinterpret_cast<msg_willmsg*>(MessageBuffer);

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
void Mqttsn::willTopicReqHandler(MessageHeader* msg) {
}
*/

/**
 * @TODO not implemented yet
 *
 * @brief Mqttsn::willMsgReqHandler
 * @param msg
 *
void Mqttsn::willMsgReqHandler(MessageHeader* msg) {
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
	pubAckReturnCode = msg->returnCode;
}

void Mqttsn::pubRecHandler(msg_pubqos2* msg) {
}

void Mqttsn::pubRelHandler(msg_pubqos2* msg) {
}

void Mqttsn::pubCompHandler(msg_pubqos2* msg) {
}

void Mqttsn::pubAck(uint16_t topicId, uint16_t messageId, returnCode_t returnCode) {
	msg_puback* msg = reinterpret_cast<msg_puback*>(MessageBuffer);

	msg->length = sizeof(msg_puback);
	msg->type = PUBACK;
	msg->topicId = bitSwap(topicId);
	msg->messageId = bitSwap(messageId);
	msg->returnCode = returnCode;
	sendMessage();
}

void Mqttsn::pubRec() {
	msg_pubqos2* msg = reinterpret_cast<msg_pubqos2*>(message_buffer);
	msg->length = sizeof(msg_pubqos2);
	msg->type = PUBREC;
	msg->messageId = bitSwap(_messageId);

	sendMessage();
}

void Mqttsn::pubRel() {
	msg_pubqos2* msg = reinterpret_cast<msg_pubqos2*>(message_buffer);
	msg->length = sizeof(msg_pubqos2);
	msg->type = PUBREL;
	msg->messageId = bitSwap(_messageId);

	sendMessage();
}

void Mqttsn::pubComp() {
	msg_pubqos2* msg = reinterpret_cast<msg_pubqos2*>(message_buffer);
	msg->length = sizeof(msg_pubqos2);
	msg->type = PUBCOMP;
	msg->messageId = bitSwap(_messageId);

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

	returnCode_t ret = REJECTED_INVALID_topicId;
	int topicId = findTopicId(message->topic_name);

	// LOGS.debug("registerHandler", "received topic_name: ", message->topic_name);
	// LOGS.debug("registerHandler", "found topic id: ", (int)topicId);

	if (topicId != DEFAULT_topicId) {
		TopicTable[topicId].id = bitSwap(message->topicId);
		ret = ACCEPTED;
	}

	regAck(message->topicId, message->messageId, ret);
}
*/

/**
 * * @TODO not implemented yet
 *
 * @brief Mqttsn::regAck
 * @param topicId
 * @param messageId
 * @param returnCode
 *
void Mqttsn::regAck(uint16_t topicId, uint16_t messageId, returnCode_t returnCode) {
	msg_regack* msg = reinterpret_cast<msg_regack*>(MessageBuffer);

	msg->length = sizeof(msg_regack);
	msg->type = REGACK;
	msg->topicId = bitSwap(topicId);
	msg->messageId = bitSwap(messageId);
	msg->returnCode = returnCode;

	sendMessage();
}
*/

/**
 * @TODO not implemented yet
 *
 * @brief Mqttsn::reRegister
 * @param topicId
 * @param messageId
 * @param returnCode
 *
void Mqttsn::reRegister(uint16_t topicId, uint16_t messageId, returnCode_t returnCode) {
	MsgReRegister* msg = reinterpret_cast<MsgReRegister*>(MessageBuffer);

	msg->length = sizeof(MsgReRegister);
	msg->type = REREGISTER;
	msg->topicId = bitSwap(topicId);
	msg->messageId = bitSwap(messageId);
	msg->returnCode = returnCode;

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

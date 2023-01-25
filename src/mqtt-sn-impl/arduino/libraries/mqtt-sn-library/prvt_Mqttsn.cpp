/**
 * BSD 3-Clause Licence
 *
 * Created by Pierrick MARIE on 28/11/2018.
 * Updated by Pierrick MARIE on 20/01/2023
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
	int payloadLenght = frameBufferIn[12];
	uint8_t payload[payloadLenght];

	for(i = 0; i < payloadLenght; i++){
		payload[i] = frameBufferIn[12+i];
	}

	// LOGS.debugln("parseData", "OK");

	memset(responseBuffer, 0, MAX_BUFFER_SIZE);
	memcpy(responseBuffer, (void*)payload, payloadLenght);

	// LOGS.debug("parseData", "-> dispatch");

	dispatch();

	memset(frameBufferIn, 0, sizeof(frameBufferIn));
}

void Mqttsn::dispatch() {

	// WaitingForResponse = false;
	Header* responseMessage = (Header*)responseBuffer;

	// LOGS.debug("dispatch", "response type:", responseMessage->type);
	// LOGS.debug("dispatch", "response length:", responseMessage->length);

	if(false == searchGatewayOk && GATEWAY_INFO == responseMessage->type) {
		// LOGS.debugln("dispatch", "x2");
		searchGatewayHandler((GatewayInfo*)responseBuffer);
	} else {

		switch (responseMessage->type) {

		case CONN_ACK:
			// LOGS.debugln("dispatch", "x5");
			connAckHandler((ConnAck*)responseBuffer);
			break;

		case REG_ACK:
			// LOGS.debugln("dispatch", "xB");
			regAckHandler((RegAck*)responseBuffer);
			break;

		case PUBLISH:
			// LOGS.debugln("dispatch", "xC");
			publishHandler((Message*)responseBuffer);
			break;

		case SUB_ACK:
			// LOGS.debugln("dispatch", "x13");
			subAckHandler((SubAck*)responseBuffer);
			break;

		case PING_RESP:
			// LOGS.debugln("dispatch", "x17");
			pingRespHandler();
			break;

		case DISCONNECT:
			// LOGS.debugln("dispatch", "x18");
			disconnectHandler((Disconnect*)responseBuffer);
			break;

		case RE_REGISTER:
			// LOGS.debugln("dispatch", "x1E");
			reRegisterHandler((ReRegister*)responseBuffer);
			break;

		default:
			// LOGS.debugln("dispatch", "xx");
			return;

			// @TODO not implemented yet
			// case ADVERTISE:
			// LOGS.debug("dispatch", "ADVERTISE");
			// advertiseHandler((Advertise*)responseBuffer);
			// break;

			// @TODO not implemented yet
			// case PING_REQ:
			// LOGS.debug("dispatch", "PING_REQ");
			// pingReqHandler((MsgPingReq*)responseBuffer);
			// break;

			// @TODO not implemented yet - QoS level 1 or 2
			// case PUB_ACK:
			// LOGS.debug("dispatch", "PUB_ACK");
			// pubAckHandler((PubAck*)responseBuffer);
			// break;

			// @TODO not implemented yet
			// case WILL_TOPIC_RESP:
			// LOGS.debug("dispatch", "WILL_TOPIC_RESP");
			// willTopicRespHandler((MsgWillTopicResp*)responseBuffer);
			// break;

			// @TODO not implemented yet
			// case UNSUB_ACK:
			// LOGS.debug("dispatch", "UNSUB_ACK");
			// unsuback_handler((MsgUnsubAck*)responseBuffer);
			// break;

			// @TODO not implemented yet
			// case REGISTER:
			// LOGS.debug("dispatch", "REGISTER");
			// registerHandler((MsgRegister*)responseBuffer);
			// break;

			// @TODO not implemented yet
			// case WILL_MSG_RESP:
			// LOGS.debug("dispatch", "WILL_MSG_RESP");
			// willMsgRespHandler((MsgWillMsgResp*)responseBuffer);
			// break;

			// @TODO not implemented yet
			// case WILL_TOPIC_REQ:
			// LOGS.debug("dispatch", "WILL_TOPIC_REQ");
			// willTopicReqHandler(responseMessage);
			// break;

			// @TODO not implemented yet
			// case WILL_MSG_REQ:
			// LOGS.debug("dispatch", "WILL_MSG_REQ");
			// willmsgreq_handler(responseMessage);
			// break;
		}
	}
}

void Mqttsn::pingRespHandler() {
	// @TODO not implemented yet 
	// LOGS.debug("pingRespHandler", "end of published messages");
}

uint16_t Mqttsn::bitSwap(uint16_t value) {
	return (value << 8) | (value >> 8);
}

void Mqttsn::publishHandler(Message* msg) {

	if(nbReceivedMessage < MAX_MESSAGES) {
		LOGS.info("msg received");
		receivedMessages[nbReceivedMessage].topicId = bitSwap(msg->topicId);
		strcpy(receivedMessages[nbReceivedMessage].data, msg->data);
		nbReceivedMessage++;
	} else {
		// LOGS.debug("publishHandler", "too many received messages");
	}
	// LOGS.debug("publishHandler", "nb received messages: ", nbReceivedMessage);
	// LOGS.debug("publishHandler", "topic: ", receivedMessages[nbReceivedMessage].topicId);
	// LOGS.debug("publishHandler", "message: ", receivedMessages[nbReceivedMessage].data);

	// @TODO not implemented yet - QoS level 1 or 2
	// LOGS.debug("publishHandler", "send pub ack");
	// LOGS.debug("publishHandler", "message id:", msg->messageId);
	// pubAck(msg->topicId, msg->messageId, ret);

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
		frameBufferIn[i] = XBEE->read();
	}

	if(!verifyChecksum(frameBufferIn, frameSize)) {
		// LOGS.debug("checkSerial", "KO 2", delimiter);
		// return checkSerial();
		return false;
	}

	if(frameBufferIn[0] == 139 || frameBufferIn[0] == 161) {
		// LOGS.debug("checkSerial", "KO 3" , frameBufferIn[0]);
		// return checkSerial();
		return false;
	}

	// printFrameBufferIn();

	if(frameBufferIn[0] == 144) {
		// this is a data packet, copy the gateway address
		if(0 == gatewayAddress[0] && 0 == gatewayAddress[1] && 0 == gatewayAddress[2] && 0 == gatewayAddress[3]){
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
		// LOGS.debugln("checkSerial", "OK");
		return true;
	}

	// not data available, clear the buffer and return false
	memset(frameBufferIn, 0, sizeof(frameBufferIn));
	// LOGS.debugln("checkSerial", "KO 4");
	return false;
}

void Mqttsn::subAckHandler(SubAck* msg) {

	if (msg->returnCode == ACCEPTED && nbRegisteredTopic < MAX_TOPICS) {
		topicTable[lastSubscribedTopic].id = msg->topicId;
		regAckReturnCode = ACCEPTED;

		// LOGS.debug("subAckHandler", "msg->id: ", msg->topicId);
		// LOGS.debug("subAckHandler", "table[id]: ", topicTable[lastSubscribedTopic].id);
		// LOGS.debug("subAckHandler", "table[name]: ", topicTable[lastSubscribedTopic].name);

		LOGS.info("subscribe OK");
	} else {
		LOGS.error("subscribe KO");
		regAckReturnCode = REJECTED;
	}
}

void Mqttsn::sendMessage() {

	// LOGS.debug("sendMessage");
	delay(getRandomTime());

	// Sending the message stored into @messageBuffer through @MB_serial_send function
	// extern void MB_serial_send(uint8_t* messageBuffer, int length);
	Header* header = reinterpret_cast<Header*>(messageBuffer);

	int length = createFrame(header->length);

	if (length > 0) {

		XBEE->listen();
		while(!XBEE->isListening()) { } // infinite loop to wait XBEE module

		// displayFrameBufferOut();
		XBEE->write(frameBufferOut, length);
		XBEE->flush();
		// LOGS.debug("sendMessage", "message sent");
	} else {
		// LOGS.debug("sendMessage", "message not sent");
	}

	// @BUG waits 500ms seconds otherwise the module miss some answers from the gateway
	delay(getRandomTime());
}

bool Mqttsn::verifyChecksum(uint8_t frameBuffer[], int frameSize) {

	int i;
	uint16_t checksum = 0x00;

	for(i=0; i < frameSize; i++) {
		checksum += frameBuffer[i];
	}
	checksum = checksum & 0xFF;

	// LOGS.debug("verifyChecksum", "checksum is : ", (int) checksum);

	return checksum == 0xFF ;
}

int Mqttsn::createFrame(int headerLength) {

	// LOGS.debug("createFrame");

	uint8_t checksum = 0;
	int i = 0;

	// data is too long?
	// TODO: Split in multiple packets?
	if (headerLength > API_DATA_LEN) {
		// LOGS.debug("createFrame", "ERROR - headerLength too long: ", headerLength);
		return -2;
	}

	// frame buffer is fine, clear it
	memset(frameBufferOut, '\0', sizeof(frameBufferOut));

	/* The header */

	// delimiter
	frameBufferOut[0] = API_START_DELIMITER;

	// length of the payload
	frameBufferOut[1] = 0;
	frameBufferOut[2] = 14 + headerLength;

	// frame Type: Transmit Request
	checksum = 0;
	checksum += frameBufferOut[3] = FRAME_TYPE_TRANSMIT_REQUEST;

	// frame id
	checksum += frameBufferOut[4] = FRAME_ID_WITHOUT_ACK;

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

	// broadcast radius
	checksum += frameBufferOut[15] = BROADCAST_RADIUS_ZERO;

	// options
	checksum += frameBufferOut[16] = OPTION_DISABLE_RETRIES;


	/* The data */
	for (i = 0; i < headerLength; i++) {
		checksum += frameBufferOut[17 + i] = messageBuffer[i];
	}

	checksum = 0XFF - checksum;
	frameBufferOut[17 + headerLength] = checksum;

	return 17 + headerLength + 1;
}

void Mqttsn::connAckHandler(ConnAck* msg) {

	if(msg->returnCode == ACCEPTED) {
		connected = ACCEPTED;
		LOGS.info("connected");
	} else {
		connected = REJECTED;
		LOGS.error("not connected");
		while(1);
	}

	delay(getRandomTime());
}

void Mqttsn::disconnectHandler(Disconnect* msg) {

	connected = REJECTED;

	LOGS.info("disconnected");

	delay(SLEEP_TIME * 1500); // 10 ms * 1500 = 15000 ms = 15 s

	LOGS.info("awake");
}

void Mqttsn::searchGatewayHandler(GatewayInfo* message) {

	if(message->gatewayId == GATEWAY_ID) {
		LOGS.info("started");
		searchGatewayOk = true;
	} else {
		LOGS.error("not started");
		searchGatewayOk = false;
		while(1);
	}
}

void Mqttsn::regAckHandler(RegAck* msg) {

	if (msg->returnCode == ACCEPTED && nbRegisteredTopic < MAX_TOPICS) {

		topicTable[nbRegisteredTopic].id = msg->topicId;
		regAckReturnCode = ACCEPTED;

		// LOGS.debug("regAckHandler", "Topic registered, id: ", msg->topicId);
		// LOGS.debug("regAckHandler", "Topic registered, table id: ", topicTable[nbRegisteredTopic].id);
		// LOGS.debug("regAckHandler", "Topic registered, table name: ", topicTable[nbRegisteredTopic].name);
		LOGS.info("register OK");

		nbRegisteredTopic++;
	} else {
		LOGS.error("register KO");
		regAckReturnCode = REJECTED;
	}

	delay(getRandomTime());
}

void Mqttsn::resetRegisteredTopicId(int topicId) {

	for (int i = 0; i < nbRegisteredTopic; i++) {
		if (topicTable[i].id == topicId) {
			LOGS.debug("resetRegisteredTopicId", "id = ", topicId);
			topicTable[i].id = DEFAULT_TOPIC_ID;
			return;
		}
	}

	LOGS.debugln("resetRegisteredTopicId", "topic id NOT found");
}

/**
 * @brief Mqttsn::reRegisterHandler
 * @param msg
 **/
void Mqttsn::reRegisterHandler(ReRegister* msg) {

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


/**
 * @TODO never used
 *
 * @brief Mqttsn::unsubscribeById
 * @param flags
 * @param topicId
 **/
void Mqttsn::unsubscribeById(uint8_t flags, uint16_t topicId) {
	++messageId;

	Unsubscribe* msg = reinterpret_cast<Unsubscribe*>(messageBuffer);

	msg->length = sizeof(Unsubscribe);
	msg->type = UNSUBSCRIBE;
	msg->flags = (flags & QOS_MASK) | FLAG_TOPIC_PREDEFINED_ID;
	msg->messageId = bitSwap(messageId);
	msg->topicId = bitSwap(topicId);

	sendMessage();
}

/**
 * @TODO not implemented yet
 *
 * @brief Mqttsn::unsuback_handler
 * @param msg
 **/
void Mqttsn::unsuback_handler(UnsubAck* msg) {
}

/**
 * @TODO never used
 *
 * @brief Mqttsn::unsubscribeByName
 * @param flags
 * @param topicName
 **/
void Mqttsn::unsubscribeByName(uint8_t flags, char* topicName) {
	++messageId;

	Unsubscribe* msg = reinterpret_cast<Unsubscribe*>(messageBuffer);

	// The -2 here is because we're unioning a 0-length member (topicName)
	// with a uint16_t in the MsgUnsubscribe struct.
	msg->length = sizeof(Unsubscribe) + strlen(topicName) - 2;
	msg->type = UNSUBSCRIBE;
	msg->flags = (flags & QOS_MASK) | FLAG_TOPIC_NAME;
	msg->messageId = bitSwap(messageId);
	strcpy(msg->topicName, topicName);

	sendMessage();
}

/**
 * @TODO not implemented yet
 *
 * @brief Mqttsn::advertiseHandler
 * @param msg
 **/
void Mqttsn::advertiseHandler(Advertise* msg) {

	// LOGS.debug("advertiseHandler");
	gatewayId = msg->gatewayId;
}

/**
 * @TODO never used
 *
 * @brief Mqttsn::subscribeById
 * @param flags
 * @param topicId
 **/
void Mqttsn::subscribeById(uint8_t flags, uint16_t topicId) {
	++messageId;

	Subscribe* msg = reinterpret_cast<Subscribe*>(messageBuffer);

	msg->length = sizeof(Subscribe);
	msg->type = SUBSCRIBE;
	msg->flags = (flags & QOS_MASK) | FLAG_TOPIC_PREDEFINED_ID;
	msg->messageId = bitSwap(messageId);
	msg->topicId = bitSwap(topicId);

	sendMessage();
}

/**
 * @TODO not implemented yet
 *
 * @brief Mqttsn::willTopic
 * @param flags
 * @param willTopic
 * @param update
 **/
void Mqttsn::willTopic(uint8_t flags, char* willTopic, bool update) {
	if (willTopic == NULL) {
		Header* msg = reinterpret_cast<Header*>(messageBuffer);

		msg->type = update ? WILL_TOPIC_UPD : WILL_TOPIC;
		msg->length = sizeof(Header);
	} else {
		WillTopic* msg = reinterpret_cast<WillTopic*>(messageBuffer);

		msg->type = update ? WILL_TOPIC_UPD : WILL_TOPIC;
		msg->flags = flags;
		strcpy(msg->willTopic, willTopic);
	}

	sendMessage();
}

/**
 * @TODO not implemented yet
 *
 * @brief Mqttsn::willMesssage
 * @param willMsg
 * @param willMsgLength
 * @param update
 **/
void Mqttsn::willMesssage(void* willMsg, uint8_t willMsgLength, bool update) {
	WillMsg* msg = reinterpret_cast<WillMsg*>(messageBuffer);

	msg->length = sizeof(WillMsg) + willMsgLength;
	msg->type = update ? WILL_MSG_UPD : WILL_MSG;
	memcpy(msg->willMsg, willMsg, willMsgLength);

	sendMessage();
}

/**
 * @TODO not implemented yet
 *
 * @brief Mqttsn::willTopicRespHandler
 * @param msg
 **/
void Mqttsn::willTopicRespHandler(WillTopicResp* msg) {
}

/**
 * @TODO not implemented yet
 *
 * @brief Mqttsn::willMsgRespHandler
 * @param msg
 **/
void Mqttsn::willMsgRespHandler(WillMsgResp* msg) {
}

/**
 * @TODO not implemented yet
 *
 * @brief Mqttsn::willTopicReqHandler
 * @param msg
 **/
void Mqttsn::willTopicReqHandler(WillTopic* msg) {
}

/**
 * @TODO not implemented yet
 *
 * @brief Mqttsn::willMsgReqHandler
 * @param msg
 **/
void Mqttsn::willMsgReqHandler(WillMsg* msg) {
}

/**
 * ****************************
 * QoS
 * @TODO not implemented yet - QoS level 1 or 2
 * ****************************
 **/

void Mqttsn::pubAckHandler(PubAck* msg) {
}

void Mqttsn::pubRecHandler(PubQoS2* msg) {
}

void Mqttsn::pubRelHandler(PubQoS2* msg) {
}

void Mqttsn::pubCompHandler(PubQoS2* msg) {
}

void Mqttsn::pubAck(uint16_t topicId, uint16_t messageId, ReturnCode returnCode) {
	PubAck* msg = reinterpret_cast<PubAck*>(messageBuffer);

	msg->length = sizeof(PubAck);
	msg->type = PUB_ACK;
	msg->topicId = bitSwap(topicId);
	msg->messageId = bitSwap(messageId);
	msg->returnCode = returnCode;
	sendMessage();
}

void Mqttsn::pubRec() {
	PubQoS2* msg = reinterpret_cast<PubQoS2*>(messageBuffer);
	msg->length = sizeof(PubQoS2);
	msg->type = PUB_REC;
	msg->messageId = bitSwap(messageId);

	sendMessage();
}

void Mqttsn::pubRel() {
	PubQoS2* msg = reinterpret_cast<PubQoS2*>(messageBuffer);
	msg->length = sizeof(PubQoS2);
	msg->type = PUB_REL;
	msg->messageId = bitSwap(messageId);

	sendMessage();
}

void Mqttsn::pubComp() {
	PubQoS2* msg = reinterpret_cast<PubQoS2*>(messageBuffer);
	msg->length = sizeof(PubQoS2);
	msg->type = PUB_COMP;
	msg->messageId = bitSwap(messageId);

	sendMessage();
}

/**
 * @TODO not implemented yet
 *
 * @brief Mqttsn::registerHandler
 * @param message
 **/
void Mqttsn::registerHandler(Register* message) {

	ReturnCode ret = REJECTED_INVALID_topicId;
	int topicId = findTopicId(message->topicName);

	// LOGS.debug("registerHandler", "received topicName: ", message->topicName);
	// LOGS.debug("registerHandler", "found topic id: ", (int)topicId);

	if (topicId != DEFAULT_TOPIC_ID) {
		topicTable[topicId].id = bitSwap(message->topicId);
		ret = ACCEPTED;
	}

	regAck(message->topicId, message->messageId, ret);
}

/**
 * * @TODO not implemented yet
 *
 * @brief Mqttsn::regAck
 * @param topicId
 * @param messageId
 * @param returnCode
 **/
void Mqttsn::regAck(uint16_t topicId, uint16_t messageId, ReturnCode returnCode) {
	RegAck* msg = reinterpret_cast<RegAck*>(messageBuffer);

	msg->length = sizeof(RegAck);
	msg->type = REG_ACK;
	msg->topicId = bitSwap(topicId);
	msg->messageId = bitSwap(messageId);
	msg->returnCode = returnCode;

	sendMessage();
}

/**
 * @TODO not implemented yet
 *
 * @brief Mqttsn::reRegister
 * @param topicId
 * @param messageId
 * @param returnCode
 **/
void Mqttsn::reRegister(uint16_t topicId, uint16_t messageId, ReturnCode returnCode) {
	ReRegister* msg = reinterpret_cast<ReRegister*>(messageBuffer);

	msg->length = sizeof(ReRegister);
	msg->type = RE_REGISTER;
	msg->topicId = bitSwap(topicId);
	msg->messageId = bitSwap(messageId);
	msg->returnCode = returnCode;

	sendMessage();
}

/**
 * @TODO not implemented yet
 *
 * @brief Mqttsn::pingReqHandler
 * @param msg
 **/
void Mqttsn::pingReqHandler(Advertise* msg) {
}

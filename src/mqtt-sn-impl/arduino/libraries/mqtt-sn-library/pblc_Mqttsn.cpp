/**
 * BSD 3-Clause Licence
 *
 * Created by Pierrick MARIE on 28/11/2018.
 * Updated by Pierrick MARIE on 20/01/2023
 */

#include "Mqttsn.h"

/**
 * ****************************
 *
 * PUBLIC FUNCTIONS
 *
 * ****************************
 **/

Mqttsn::Mqttsn(SoftwareSerial* xBee) {

	XBEE = xBee;

	XBEE->begin(BAUD_RATE);

	gatewayId = 0;
	lastSubscribedTopic = 0;
	nbRegisteredTopic = 0;
	nbReceivedMessage = 0;
	connected = REJECTED;
	searchGatewayOk = false;

	memset(topicTable, 0, sizeof(Topic) * MAX_TOPICS);
	memset(messageBuffer, 0, MAX_BUFFER_SIZE);
	memset(responseBuffer, 0, MAX_BUFFER_SIZE);

	// Init random seed -> getRandomTime()
	randomSeed(analogRead(0));
}

Mqttsn::~Mqttsn() {
}

bool Mqttsn::isConnected() {

	return connected == ACCEPTED;
}

int Mqttsn::requestMessages() {

	delay(getRandomTime());

	LOGS.info("request msg");

	PingReq* msg = reinterpret_cast<PingReq*>(messageBuffer);
	msg->length = sizeof(PingReq) + strlen(moduleName);
	msg->type = PING_REQ;
	strcpy(msg->clientId, moduleName);

	sendMessage();

	// LOGS.debug("pinqReq", "clean received messages");

	while(nbReceivedMessage > 0) {
		nbReceivedMessage--;
		memset(&receivedMessages[nbReceivedMessage], 0, sizeof(Message));
		// LOGS.debug("pinqReq", "clean message ", nbReceivedMessage);
	}

	if( !checkSerial() ) {
		// LOGS.debug("pingReq", "check serial rejected");
		// Connected = REJECTED;
		LOGS.connectionLost();
		start();
		connect(moduleName);
		requestMessages();
	}

	// LOGS.debug("pingReq", "parsing published messages");
	parseData();

	return nbReceivedMessage;
}

void Mqttsn::disconnect() {

	delay(getRandomTime());

	Disconnect* msg = reinterpret_cast<Disconnect*>(messageBuffer);

	msg->length = sizeof(Header);
	msg->type = DISCONNECT;

	msg->length += sizeof(Disconnect);
	msg->duration = bitSwap(DURATION_TIME);
	// LOGS.debug("diconnect", "sleep duration: ", sleepDuration);

	sendMessage();

	if( !checkSerial() ) {
		// LOGS.debug("disconnect", "check serial rejected");
		// Connected = REJECTED;
		LOGS.connectionLost();
		disconnect();
	}

	// LOGS.debug("disconnect", "parsing published messages");
	parseData();
}

void Mqttsn::publish(const char* topicName, String message){

	delay(getRandomTime());

	if(connected != ACCEPTED) {
		LOGS.connectionLost();
		start();
		connect(moduleName);
		publish(topicName, message);
	}

	int topicId = findTopicId(topicName);

	if(-1 == topicId) {
		// LOGS.debug("publish", "unknown->register", topicName);
		if(! registerTopic(topicName)) {
			// LOGS.debug("publish", "can't register topic");
			return;
		}
		// LOGS.debug("publish", "call again");
		// LOGS.debug("publish", "name: ", topicTable[nbRegisteredTopic].name);
		publish(topicName, message);
	} else {

		Message* msg = reinterpret_cast<Message*>(messageBuffer);

		// get sizeof (Message + message) - sizeof Message.data (array of 40 char)
		// get "sizeof(header of Message)" + sizeof(message)
		msg->length = sizeof(Message) - sizeof(msg->data) + message.length();
		msg->type = PUBLISH;
		msg->flags = QOS_FLAG;
		// @BUG msg->topic_id = bitSwap(topic_id);
		msg->topicId = ( topicId );
		msg->messageId = bitSwap(MESSAGE_ID);
		strcpy(msg->data, message.c_str());

		LOGS.info("publish msg");

		// LOGS.debug("publish", "id:", msg->topic_id);
		// LOGS.debug("publish", "msg: ", msg->data);

		sendMessage();

		// @TODO not implemented yet - QoS level 1 or 2
		// if( !checkSerial() ) {
		// 	return;
		// }
		// parseData();
		// @TODO do not wait a response
		// WaitingForResponse = false;
	}
}

bool Mqttsn::start() {

	delay(getRandomTime()*2);

	// WaitingForResponse = false;
	searchGatewayOk = false;

	SearchGateway* msg = reinterpret_cast<SearchGateway*>(messageBuffer);

	msg->length = sizeof(SearchGateway);
	msg->type = SEARCH_GATEWAY;
	msg->radius = RADIUS;

	// LOGS.debug("start", "sending message");
	sendMessage();

	// LOGS.debug("start", "checking the response from the gateway");

	if(false == searchGatewayOk && !checkSerial() ) {
		start();
	}
	parseData();

	return searchGatewayOk;
}

void Mqttsn::connect(const char* moduleName) {

	delay(getRandomTime());

	strcpy(moduleName, moduleName);

	Connect* msg = reinterpret_cast<Connect*>(messageBuffer);

	msg->length = sizeof(Connect) + strlen(moduleName);
	msg->type = CONNECT;
	msg->flags = QOS_FLAG;
	msg->protocolId = PROTOCOL_ID;
	msg->duration = bitSwap(DURATION_TIME);
	strcpy(msg->clientId, moduleName);

	// LOGS.debug("connect", "name", msg->clientId);

	sendMessage();

	if( !checkSerial() ) {
		// LOGS.debugln("connect", "KO");
		// Connected = REJECTED;
		LOGS.connectionLost();
		connect(moduleName);
	}
	parseData();
}

int Mqttsn::findTopicId(const char* topicName) {

	for (int i = 0; i < nbRegisteredTopic; i++) {
		// LOGS.debug( "findTopicId", "id = ", (int)topicTable[i].id);
		// LOGS.debug( "findTopicId", "name = ", topicTable[i].name);
		if (topicTable[i].id != DEFAULT_TOPIC_ID && strcmp(topicTable[i].name, topicName) == 0) {
			// LOGS.debug( "findTopicId", "return = ", topicTable[i].id);
			return topicTable[i].id;
		}
	}

	// LOGS.debugln("findTopicId", "topicName not found");
	return -1;
}

const char* Mqttsn::findTopicName(int topicId) {

	for (int i = 0; i < nbRegisteredTopic; i++) {
		if (topicTable[i].id != DEFAULT_TOPIC_ID && topicTable[i].id == topicId) {
			return topicTable[i].name;
		}
	}

	// LOGS.debug("findTopicName", "name not found");
	return NULL;
}

bool Mqttsn::subscribeTopic(const char* topicName) {

	delay(getRandomTime());

	// LOGS.debug("subscribeTopic", "topic: ", topicName);

	if(nbRegisteredTopic >= MAX_TOPICS) {
		// LOGS.debug("subscribeTopic", "nb > MAX_TOPICS");
		return false;
	}

	int topicId = findTopicId(topicName);
	if(topicId != -1) {
		topicTable[topicId].id = DEFAULT_TOPIC_ID;
		// LOGS.debug("subscribeTopic", "reset topic id:", topicId);
		lastSubscribedTopic = topicId;
	} else {
		// LOGS.debug("subscribeTopic", "id:", topicId);

		// Fill in the next table entry, but we only increment the counter to
		// the next topic when we get a REG_ACK from the broker. So don't issue
		// another REGISTER until we have resolved this one.
		// @name is save now because it will be lost at the end of this function.
		strcpy(topicTable[nbRegisteredTopic].name, topicName);
		// LOGS.debug("subscribeTopic", "name:", topicTable[nbRegisteredTopic].name);

		// A magic number while the gateway respond: @see:regAckHandler()
		topicTable[nbRegisteredTopic].id = DEFAULT_TOPIC_ID;
		lastSubscribedTopic = nbRegisteredTopic;

		nbRegisteredTopic++;
	}

	// LOGS.debug("subscribeTopic", topicName);

	Subscribe* msg = reinterpret_cast<Subscribe*>(messageBuffer);

	// The -2 here is because we're unioning a 0-length member (topicName)
	// with a uint16_t in the Subscribe struct.
	msg->length = sizeof(Subscribe) + strlen(topicName) - 2;
	msg->type = SUBSCRIBE;
	msg->flags = (QOS_MASK & QOS_MASK) | FLAG_TOPIC_NAME;
	msg->messageId = bitSwap(MESSAGE_ID);
	strcpy(msg->topicName, topicName);

	// LOGS.debug("subscribeTopic", "sending message 'subscribe topic'");

	sendMessage();

	if( !checkSerial() ) {
		// LOGS.debug("subscribe", "check serial rejected");
		// Connected = REJECTED;
		LOGS.connectionLost();
		subscribeTopic(topicName);
	}

	// LOGS.debug("subscribe", "parsing response 'subscribe topic'");
	parseData();

	// LOGS.debug("subscribeTopic", "response from the gateway", subAckReturnCode);
	return subAckReturnCode == ACCEPTED;
}

bool Mqttsn::registerTopic(const char* topicName) {

	delay(getRandomTime());

	// LOGS.debug("register", "topic: ", topicName);

	if(nbRegisteredTopic >= MAX_TOPICS) {
		// LOGS.debug("register", "nb > MAX_TOPICS");
		return false;
	}

	int topicId = findTopicId(topicName);
	if(topicId != -1) {
		// LOGS.debug("register", "already registered");
		return true;
	}
	// LOGS.debug("register", "id:", topicId);

	// Fill in the next table entry, but we only increment the counter to
	// the next topic when we get a REG_ACK from the broker. So don't issue
	// another REGISTER until we have resolved this one.
	// @name is save now because it will be lost at the end of this function.
	strcpy(topicTable[nbRegisteredTopic].name, topicName);
	// LOGS.debug("register", "name:", topicTable[nbRegisteredTopic].name);

	// A magic number while the gateway respond: @see:regAckHandler()
	topicTable[nbRegisteredTopic].id = DEFAULT_TOPIC_ID;

	Register* msg = reinterpret_cast<Register*>(messageBuffer);

	msg->length = sizeof(Register) + strlen(topicName);
	msg->type = REGISTER;
	msg->topicId = 0;
	msg->messageId = bitSwap(MESSAGE_ID);
	strcpy(msg->topicName, topicName);

	sendMessage();

	if( !checkSerial() ) {
		// LOGS.debug("register", "rejected");
		// Connected = REJECTED;
		LOGS.connectionLost();
		registerTopic(topicName);
	}

	parseData();

	// LOGS.debug("register", "response:", regAckReturnCode);
	// LOGS.debug("register", "id:", findTopicId(topicName));

	return regAckReturnCode == ACCEPTED;
}

Message* Mqttsn::getReceivedMessages() {

	delay(getRandomTime());

	return receivedMessages;
}

/**
 * @TODO not implemented yet
 *
 * @brief Mqttsn::pingResp
 */
void Mqttsn::pingResp() {
	// LOGS.debug("pingResp");
}

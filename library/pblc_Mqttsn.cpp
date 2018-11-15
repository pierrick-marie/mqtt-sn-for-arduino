/*
Mqttsn-messages.cpp

The MIT License (MIT)

Copyright (C) 2014 John Donovan

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is

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
 * ****************************
 *
 * PUBLIC FUNCTIONS
 *
 * ****************************
 **/

Mqttsn::Mqttsn(SoftwareSerial* _xBee) {

	xBee = _xBee;

	xBee->begin(BAUD_RATE);

	messageId = 0;
	gatewayId = 0;
	nbRegisteredTopic = 0;
	nbReceivedMessage = 0;
	connected = REJECTED;
	initOk = false;

	moduleName = "";

	memset(topicTable, 0, sizeof(topic) * MAX_TOPICS);
	memset(messageBuffer, 0, MAX_BUFFER_SIZE);
	memset(responseBuffer, 0, MAX_BUFFER_SIZE);
}

Mqttsn::~Mqttsn() {
}

bool Mqttsn::isConnected() {

	return connected == ACCEPTED;
}

void Mqttsn::requestMessages() {

	if(connected != ACCEPTED) {
		logs.notConnected();
		while(1);
	}

	// logs.debug("pingReq", "building message");

	msg_pingreq* msg = reinterpret_cast<msg_pingreq*>(messageBuffer);
	msg->length = sizeof(msg_pingreq) + strlen(moduleName.c_str());
	msg->type = PINGREQ;
	strcpy(msg->client_id, moduleName.c_str());

	sendMessage();

	// logs.debug("pinqReq", "clean received messages");

	while(nbReceivedMessage > 0) {
		nbReceivedMessage--;
		memset(&receivedMessages[nbReceivedMessage], 0, sizeof(msg_publish));
		// logs.debug("pinqReq", "clean message ", nbReceivedMessage);
	}

	if( !checkSerial() ) {
		// logs.debug("pingReq", "check serial rejected");
		connected = REJECTED;
		return;
	}

	// logs.debug("pingReq", "parsing published messages");
	parseData();
}

void Mqttsn::disconnect() {

	if(connected != ACCEPTED) {
		logs.notConnected();
		while(1);
	}

	msg_disconnect* msg = reinterpret_cast<msg_disconnect*>(messageBuffer);

	msg->length = sizeof(message_header);
	msg->type = DISCONNECT;

	msg->length += sizeof(msg_disconnect);
	msg->duration = bitSwap(TIME_TO_SLEEP);
	// logs.debug("diconnect", "sleep duration: ", sleepDuration);

	sendMessage();

	if( !checkSerial() ) {
		// logs.debug("disconnect", "check serial rejected");
		connected = REJECTED;
		return;
	}

	// logs.debug("disconnect", "parsing published messages");
	parseData();
}

void Mqttsn::publish(String topicName, String message){

	if(connected != ACCEPTED) {
		logs.notConnected();
		while(1);
	}

	int topic_id = findTopicId(topicName);

	if(-1 == topic_id) {
		logs.debug("publish", "topic name unknown -> registerTopic()", topicName.c_str());
		if(registerTopic(topicName) != ACCEPTED) {
			logs.debug("publish", "impossible to register topic, return REJECTED");
			return;
		}
		logs.debug("publish", "call publish again with right topic id");
		logs.debug("publish", "TEST topic name: ", topicTable[nbRegisteredTopic].name);
		publish(topicName, message);
	} else {


		msg_publish* msg = reinterpret_cast<msg_publish*>(messageBuffer);
		++messageId;

		// get sizeof (msg_publish + message) - sizeof msg_publish.data (array of 40 char)
		// get "sizeof(header of msg_publish)" + sizeof(message)
		msg->length = sizeof(msg_publish) - sizeof(msg->data) + strlen(message.c_str());
		msg->type = PUBLISH;
		msg->flags = QOS_FLAG;
		// @BUG msg->topic_id = bitSwap(topic_id);
		msg->topic_id = ( topic_id );
		msg->message_id = bitSwap(messageId);
		strcpy(msg->data, message.c_str());

		logs.info("publishing message");

		logs.debug("publish", "publish data on topic id", msg->topic_id);
		logs.debug("publish", "preparing message: ", msg->data);

		sendMessage();

		// @TODO not implemented yet - QoS level 1 or 2
		// if( !checkSerial() ) {
		// 	return;
		// }
		// parseData();
		// @TODO do not wait a response
		waitingForResponse = false;
	}
}

void Mqttsn::start() {

	// logs.debug("searchGateway");

	waitingForResponse = false;
	initOk = false;

	msg_searchgw* msg = reinterpret_cast<msg_searchgw*>(messageBuffer);

	msg->length = sizeof(msg_searchgw);
	msg->type = SEARCHGW;
	msg->radius = RADIUS;

	// logs.debug("searchGateway", "sending message");
	sendMessage();

	// logs.debug("searchGateway", "checking the response from the gateway");

	// waiting next message
	if( !checkSerial() ) {
		logs.error("gateway not available: stop");
		while(1);
	}
	parseData();
}

void Mqttsn::connect(String _moduleName) {

	if(!initOk) {
		logs.notConnected();
		while(1);
	}

	moduleName = _moduleName;

	// logs.debug( "connect", "send a connect message");

	msg_connect* msg = reinterpret_cast<msg_connect*>(messageBuffer);

	msg->length = sizeof(msg_connect) + strlen(moduleName.c_str());
	msg->type = CONNECT;
	msg->flags = QOS_FLAG;
	msg->protocol_id = PROTOCOL_ID;
	msg->duration = bitSwap(KEEP_ALIVE);
	strcpy(msg->client_id, moduleName.c_str());

	sendMessage();

	if( !checkSerial() ) {
		// logs.debug( "connect", "check serial rejected");
		connected = REJECTED;
		return;
	}
	parseData();
}

short Mqttsn::findTopicId(String topicName) {

	for (short i = 0; i < nbRegisteredTopic; i++) {
		logs.debug( "findTopicid", "id = ", (int)topicTable[i].id);
		logs.debug( "findTopicid", "name = ", topicTable[i].name);
		if (topicTable[i].id != DEFAULT_TOPIC_ID && strcmp(topicTable[i].name, topicName.c_str()) == 0) {
			return topicTable[i].id;
		}
	}

	logs.debug("findTopicId", "topicName not found");
	return -1;
}

const char* Mqttsn::findTopicName(const short topicId) {

	for (short i = 0; i < nbRegisteredTopic; i++) {
		if (topicTable[i].id != DEFAULT_TOPIC_ID && topicTable[i].id == topicId) {
			return topicTable[i].name;
		}
	}

	// logs.debug("findTopicName", "name not found");
	return "";
}

int Mqttsn::subscribeTopic(String topicName) {

	if(connected != ACCEPTED) {
		logs.notConnected();
		while(1);
	}

	// logs.debug("subscribeTopic", "searching topic id");

	if(-1 == findTopicId(topicName)){
		// logs.debug( "subscribeTopic", "topic is not already registered -> registerTopic()");
		if(registerTopic(topicName) != ACCEPTED){
			// logs.debug( "subscribeTopic", "registerTopic() not accepted");
			return REJECTED_NOT_SUPPORTED;
		}
	}

	// logs.debug("subscribeTopic", topicName);

	++messageId;
	msg_subscribe* msg = reinterpret_cast<msg_subscribe*>(messageBuffer);

	// The -2 here is because we're unioning a 0-length member (topicName)
	// with a uint16_t in the msg_subscribe struct.
	msg->length = sizeof(msg_subscribe) + strlen(topicName.c_str()) - 2;
	msg->type = SUBSCRIBE;
	msg->flags = (QOS_MASK & QOS_MASK) | FLAG_TOPIC_NAME;
	msg->message_id = bitSwap(messageId);
	strcpy(msg->topic_name, topicName.c_str());

	// logs.debug("subscribeTopic", "sending message 'subscribe topic'");

	sendMessage();

	if( !checkSerial() ) {
		// logs.debug("subscribe", "check serial rejected");
		connected = REJECTED;
		return REJECTED;
	}

	// logs.debug("subscribe", "parsing response 'subscribe topic'");
	parseData();

	// logs.debug("subscribeTopic", "response from the gateway", subAckReturnCode);
	return subAckReturnCode;
}

int Mqttsn::registerTopic(String topicName) {

	if(connected != ACCEPTED) {
		logs.notConnected();
		while(1);
	}

	logs.debug("registerTopic", "register topic: ", topicName.c_str());

	if(nbRegisteredTopic >= MAX_TOPICS) {
		// logs.debug( "registerTopic", "nbRegisteredTopic > MAX_TOPICS");
		return -2;
	}

	int topicId = findTopicId(topicName);
	if(topicId != -1) {
		// logs.debug( "registerTopic", "name is already registered");
		return ACCEPTED;
	}
	logs.debug("registerTopic", "found topic id: ", topicId);

	// Fill in the next table entry, but we only increment the counter to
	// the next topic when we get a REGACK from the broker. So don't issue
	// another REGISTER until we have resolved this one.
	// @name is save now because it will be lost at the end of this function.
	memset(topicTable[nbRegisteredTopic].name, 0, strlen(topicName.c_str()));
	strcpy(topicTable[nbRegisteredTopic].name, topicName.c_str());
	logs.debug("registerTopic", "topic name: ", topicTable[nbRegisteredTopic].name);

	// A magic number while the gateway respond: @see:regAckHandler()
	topicTable[nbRegisteredTopic].id = DEFAULT_TOPIC_ID;

	messageId++;

	msg_register* msg = reinterpret_cast<msg_register*>(messageBuffer);

	msg->length = sizeof(msg_register) + strlen(topicName.c_str());
	msg->type = REGISTER;
	msg->topic_id = 0;
	msg->message_id = bitSwap(messageId);
	strcpy(msg->topic_name, topicName.c_str());

	// logs.debug( "registerTopic", "sending message: register topic ", topicName);
	sendMessage();

	if( !checkSerial() ) {
		// logs.debug( "registerTopic", "check serial rejected");
		connected = REJECTED;
		return REJECTED;
	}

	parseData();

	// logs.debug("registerTopic", "response from the gateway - ", regAckReturnCode);
	// logs.debug("registerTopic", "topic id is: ", findTopicId(topicName));
	return regAckReturnCode;
}

char* Mqttsn::getReceivedData(String topicName) {

	int i = 0;
	short topicId = findTopicId(topicName);

	if(-1 != topicId) {
		// logs.debug("getReceivedData", "topic name:", topicName);
		// logs.debug("getReceivedData", "topic id:", topicId);

		for(; i < nbReceivedMessage; i++) {
			if(receivedMessages[i].topic_id == topicId) {
				// logs.debug("getReceivedData", "message found: ", receivedMessages[i].data);
				receivedMessages[i].topic_id = -1;
				return receivedMessages[i].data;
			}
		}
		// logs.debug("getReceivedData", "topic id not found: ", topicId);
		return NULL;
	}

	// logs.debug("getReceivedData", "topic name not found: ", topicName);
	return NULL;
}

msg_publish* Mqttsn::getReceivedMessages() {
	return receivedMessages;
}

int Mqttsn::getNbReceivedMessages() {
	return nbReceivedMessage;
}

/**
 * @TODO not implemented yet
 *
 * @brief Mqttsn::pingResp
 *
void Mqttsn::pingResp() {

	// logs.debug("pingResp");

	message_header* msg = reinterpret_cast<message_header*>(messageBuffer);
	msg->length = sizeof(message_header);
	msg->type = PINGRESP;
	sendMessage();
}
*/

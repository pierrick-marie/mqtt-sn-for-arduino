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

	connected = false ;
	messageId = 0 ;
	gatewayId = 0 ;
	nbRegisteredTopic = 0;
	nbReceivedMessages = 0;

	memset(topicTable, 0, sizeof(topic) * MAX_TOPICS);
	memset(messageBuffer, 0, MAX_BUFFER_SIZE);
	memset(responseBuffer, 0, MAX_BUFFER_SIZE);
}

Mqttsn::~Mqttsn() {
}

bool Mqttsn::isConnected() {

	return connected == ACCEPTED;
}

void Mqttsn::pingReq(const char* module_name) {

	// logs.debug("pingReq", "building message");

	msg_pingreq* msg = reinterpret_cast<msg_pingreq*>(messageBuffer);
	msg->length = sizeof(msg_pingreq) + strlen(module_name);
	msg->type = PINGREQ;
	strcpy(msg->client_id, module_name);

	sendMessage();

	nbReceivedMessages = 0;

	if( !checkSerial() ) {
		// logs.debug("pingReq", "check serial rejected");
		return;
	}

	// logs.debug("pingReq", "parsing published messages");
	parseData();
}

void Mqttsn::pingResp() {

	// logs.debug("pingResp", "building message");

	message_header* msg = reinterpret_cast<message_header*>(messageBuffer);
	msg->length = sizeof(message_header);
	msg->type = PINGRESP;
	sendMessage();
}

void Mqttsn::disconnect(const uint16_t duration) {
	msg_disconnect* msg = reinterpret_cast<msg_disconnect*>(messageBuffer);

	msg->length = sizeof(message_header);
	msg->type = DISCONNECT;

	if (duration > 0) {
		msg->length += sizeof(msg_disconnect);
		msg->duration = bitSwap(duration);
	}

	sendMessage();
}

void Mqttsn::publish(const char* topic_name, const String message){

	int topic_id = findTopicId(topic_name);
	char* messageToSend = new char [message.length()+1];
	strcpy (messageToSend, message.c_str());

	if(-1 == topic_id) {

		// logs.debug("publish", "topic name unknown -> registerTopic()", topic_name);
		if(registerTopic(topic_name) != ACCEPTED) {
			// logs.debug("publish", "impossible to register topic, return REJECTED");
			return;
		}

		// Have to recall publish function to get the good value of topic_id
		publish(topic_name, messageToSend);
	} else {
		// logs.debug("publish", "send message");

		publishMessage(QOS_FLAG, topic_id, messageToSend, message.length());

		// @TODO not implemented yet - QoS level 1 or 2
		// if( !checkSerial() ) {
		// 	return;
		// }
		// parseData();
		// @TODO do not wait a response
		waitingForResponse = false;
	}
}

int Mqttsn::init() {

	// logs.debug("init", "");

	searchGateway(RADIUS);

	// logs.debug( "init", "checking the response from the gateway");

	// waiting next message
	if( !checkSerial() ) {
		return REJECTED;
	}
	parseData();

	if(!initOk) {
		return REJECTED;
	}

	return ACCEPTED;
}

int Mqttsn::connect(const char* module_name) {

	// logs.debug( "connect", "send a connect message");
	connect(QOS_FLAG, KEEP_ALIVE, module_name);

	if( !checkSerial() ) {
		// logs.debug( "connect", "check serial rejected");
		return REJECTED;
	}

	parseData();

	// logs.debug( "connect", "response from the gateway:", connected);
	return connected;
}

short Mqttsn::findTopicId(const char* topic_name) {

	for (short i = 0; i < nbRegisteredTopic; i++) {
		if (topicTable[i].id != DEFAULT_TOPIC_ID && strcmp(topicTable[i].name, topic_name) == 0) {
			// logs.debug( "findTopicid", "id = ", (int)topicTable[i].id);
			// logs.debug( "findTopicid", "name = ", topicTable[i].name);
			return topicTable[i].id;
		}
	}

	// logs.debug("findTopicId", "id not found");
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

int Mqttsn::subscribeTopic(const char* name) {

	// logs.debug("subscribe", "searching topic id");

	if(-1 == findTopicId(name)){
		// logs.debug( "subscribe", "topic is not already registered -> registerTopic()");
		if(registerTopic(name) != ACCEPTED){
			// logs.debug( "subscribe", "registerTopic() not accepted");
			return REJECTED_NOT_SUPPORTED;
		}
	}

	// logs.debug("subscribe", "subscribing topic");

	subscribeByName(QOS_FLAG, name);

	if( !checkSerial() ) {
		// logs.debug("subscribe", "check serial rejected");
		return REJECTED_NOT_SUPPORTED;
	}

	// logs.debug("subscribe", "parsing response 'subscribe topic'");
	parseData();

	// logs.debug("subscribe", "response from the gateway - ", subAckReturnCode);
	return subAckReturnCode;
}

int Mqttsn::registerTopic(const char* topicName) {

	// logs.debug("registerTopic", "register topic: ", topicName);

	if(nbRegisteredTopic >= MAX_TOPICS) {
		// logs.debug( "registerTopic", "nbRegisteredTopic > MAX_TOPICS");
		return -2;
	}

	int topic_id = findTopicId(topicName);
	if(topic_id != -1) {
		// logs.debug( "registerTopic", "name is already registered");
		return topic_id;
	}

	// Fill in the next table entry, but we only increment the counter to
	// the next topic when we get a REGACK from the broker. So don't issue
	// another REGISTER until we have resolved this one.
	// @name is save now because it will be lost at the end of this function.
	topicTable[nbRegisteredTopic].name = topicName;
	// A magic number while the gateway respond: @see:regAckHandler()
	topicTable[nbRegisteredTopic].id = DEFAULT_TOPIC_ID;

	messageId++;

	// logs.debug( "registerTopic", "topic is registered localy: ", topicName);

	msg_register* msg = reinterpret_cast<msg_register*>(messageBuffer);

	msg->length = sizeof(msg_register) + strlen(topicName);
	msg->type = REGISTER;
	msg->topic_id = 0;
	msg->message_id = bitSwap(messageId);
	strcpy(msg->topic_name, topicName);

	// logs.debug( "registerTopic", "sending message: register topic ", topicName);
	sendMessage();

	if( !checkSerial() ) {
		// logs.debug( "registerTopic", "check serial rejected");
		return REJECTED;
	}

	// logs.debug( "registerTopic", "parsing response 'topic registered'");
	parseData();

	// logs.debug("registerTopic", "response from the gateway - ", regAckReturnCode);
	// logs.debug("registerTopic", "topic id is: ", findTopicId(topicName));
	return regAckReturnCode;
}

short Mqttsn::getNbReceivedMessages() {
	return nbReceivedMessages;
}

String Mqttsn::getReceivedMessage(const short number) {
	return String(receivedMessages[number]);
}

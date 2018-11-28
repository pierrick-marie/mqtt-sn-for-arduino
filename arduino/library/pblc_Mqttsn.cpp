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

	memset(topicTable, 0, sizeof(topic) * MAX_TOPICS);
	memset(messageBuffer, 0, MAX_BUFFER_SIZE);
	memset(responseBuffer, 0, MAX_BUFFER_SIZE);
}

Mqttsn::~Mqttsn() {
}

bool Mqttsn::isConnected() {

	return connected == ACCEPTED;
}

int Mqttsn::requestMessages() {

	if(connected != ACCEPTED) {
		logs.notConnected();
		while(1);
	}

	logs.info("request");

	msg_pingreq* msg = reinterpret_cast<msg_pingreq*>(messageBuffer);
	msg->length = sizeof(msg_pingreq) + strlen(moduleName);
	msg->type = PINGREQ;
	strcpy(msg->client_id, moduleName);

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
		return 0;
	}

	// logs.debug("pingReq", "parsing published messages");
	parseData();

	return nbReceivedMessage;
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

void Mqttsn::publish(const char* topicName, String message){

	if(connected != ACCEPTED) {
		logs.notConnected();
		while(1);
	}

	int topic_id = findTopicId(topicName);

	if(-1 == topic_id) {
		// logs.debug("publish", "unknown->register", topicName);
		if( ! registerTopic(topicName)) {
			// logs.debug("publish", "can't register topic");
			return;
		}
		// logs.debug("publish", "call again");
		// logs.debug("publish", "name: ", topicTable[nbRegisteredTopic].name);
		publish(topicName, message);
	} else {


		msg_publish* msg = reinterpret_cast<msg_publish*>(messageBuffer);
		++messageId;

		// get sizeof (msg_publish + message) - sizeof msg_publish.data (array of 40 char)
		// get "sizeof(header of msg_publish)" + sizeof(message)
		msg->length = sizeof(msg_publish) - sizeof(msg->data) + message.length();
		msg->type = PUBLISH;
		msg->flags = QOS_FLAG;
		// @BUG msg->topic_id = bitSwap(topic_id);
		msg->topic_id = ( topic_id );
		msg->message_id = bitSwap(messageId);
		strcpy(msg->data, message.c_str());

		logs.info("publish msg");

		// logs.debug("publish", "id:", msg->topic_id);
		// logs.debug("publish", "msg: ", msg->data);

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
		logs.error("not started: stop");
		while(1);
	}
	parseData();
}

void Mqttsn::connect(const char* _moduleName) {

	if(!initOk) {
		logs.notConnected();
		while(1);
	}

	// logs.debug( "connect", "save module name", strlen(_moduleName));
	strcpy(moduleName, _moduleName);
	// logs.debug( "connect", "send a connect message", moduleName);

	msg_connect* msg = reinterpret_cast<msg_connect*>(messageBuffer);

	msg->length = sizeof(msg_connect) + strlen(moduleName);
	msg->type = CONNECT;
	msg->flags = QOS_FLAG;
	msg->protocol_id = PROTOCOL_ID;
	msg->duration = bitSwap(KEEP_ALIVE);
	strcpy(msg->client_id, moduleName);

	// logs.debug( "connect", "send a connect message", msg->client_id);

	sendMessage();

	if( !checkSerial() ) {
		// logs.debug( "connect", "check serial rejected");
		connected = REJECTED;
		return;
	}
	parseData();
}

short Mqttsn::findTopicId(const char* topicName) {

	for (short i = 0; i < nbRegisteredTopic; i++) {
		// logs.debug( "findTopicid", "id = ", (int)topicTable[i].id);
		// logs.debug( "findTopicid", "name = ", topicTable[i].name);
		if (topicTable[i].id != DEFAULT_TOPIC_ID && strcmp(topicTable[i].name, topicName) == 0) {
			return topicTable[i].id;
		}
	}

	// logs.debug("findTopicId", "topicName not found");
	return -1;
}

const char* Mqttsn::findTopicName(short topicId) {

	for (short i = 0; i < nbRegisteredTopic; i++) {
		if (topicTable[i].id != DEFAULT_TOPIC_ID && topicTable[i].id == topicId) {
			return topicTable[i].name;
		}
	}

	// logs.debug("findTopicName", "name not found");
	return NULL;
}

bool Mqttsn::subscribeTopic(const char* topicName) {

	if(connected != ACCEPTED) {
		logs.notConnected();
		while(1);
	}

	// logs.debug("subscribeTopic", "searching topic id");

	if(-1 == findTopicId(topicName)){
		// logs.debug( "subscribeTopic", "topic is not already registered -> registerTopic()");
		if( registerTopic(topicName) ){
			// logs.debug("subscribeTopic", topicName);

			++messageId;
			msg_subscribe* msg = reinterpret_cast<msg_subscribe*>(messageBuffer);

			// The -2 here is because we're unioning a 0-length member (topicName)
			// with a uint16_t in the msg_subscribe struct.
			msg->length = sizeof(msg_subscribe) + strlen(topicName) - 2;
			msg->type = SUBSCRIBE;
			msg->flags = (QOS_MASK & QOS_MASK) | FLAG_TOPIC_NAME;
			msg->message_id = bitSwap(messageId);
			strcpy(msg->topic_name, topicName);

			// logs.debug("subscribeTopic", "sending message 'subscribe topic'");

			sendMessage();

			if( !checkSerial() ) {
				// logs.debug("subscribe", "check serial rejected");
				connected = REJECTED;
				return false;
			}

			// logs.debug("subscribe", "parsing response 'subscribe topic'");
			parseData();

			// logs.debug("subscribeTopic", "response from the gateway", subAckReturnCode);
			return subAckReturnCode == ACCEPTED;
		} else {
			// logs.debug( "subscribeTopic", "registerTopic() not accepted");
			return false;
		}
	}

	// logs.debug("subscribeTopic", "topic is already subscribed");
	return true;
}

bool Mqttsn::registerTopic(const char* topicName) {

	if(connected != ACCEPTED) {
		logs.notConnected();
		while(1);
	}

	// logs.debug("register", "topic: ", topicName);

	if(nbRegisteredTopic >= MAX_TOPICS) {
		// logs.debug("register", "nb > MAX_TOPICS");
		return false;
	}

	int topicId = findTopicId(topicName);
	if(topicId != -1) {
		// logs.debug("register", "already registered");
		return true;
	}
	// logs.debug("register", "id:", topicId);

	// Fill in the next table entry, but we only increment the counter to
	// the next topic when we get a REGACK from the broker. So don't issue
	// another REGISTER until we have resolved this one.
	// @name is save now because it will be lost at the end of this function.
	strcpy(topicTable[nbRegisteredTopic].name, topicName);
	// logs.debug("register", "name:", topicTable[nbRegisteredTopic].name);

	// A magic number while the gateway respond: @see:regAckHandler()
	topicTable[nbRegisteredTopic].id = DEFAULT_TOPIC_ID;

	messageId++;

	msg_register* msg = reinterpret_cast<msg_register*>(messageBuffer);

	msg->length = sizeof(msg_register) + strlen(topicName);
	msg->type = REGISTER;
	msg->topic_id = 0;
	msg->message_id = bitSwap(messageId);
	strcpy(msg->topic_name, topicName);

	sendMessage();

	if( !checkSerial() ) {
		// logs.debug("register", "rejected");
		connected = REJECTED;
		return false;
	}

	parseData();

	// logs.debug("register", "response:", regAckReturnCode);
	// logs.debug("register", "id:", findTopicId(topicName));

	return regAckReturnCode == ACCEPTED;
}

msg_publish* Mqttsn::getReceivedMessages() {
	return receivedMessages;
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

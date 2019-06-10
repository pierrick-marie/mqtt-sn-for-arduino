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

Mqttsn::Mqttsn(SoftwareSerial* xBee) {

	XBEE = xBee;

	XBEE->begin(BAUD_RATE);

	GatewayId = 0;
	LastSubscribedTopic = 0;
	NbRegisteredTopic = 0;
	NbReceivedMessage = 0;
	Connected = REJECTED;
	SearchGatewayOk = false;

	memset(TopicTable, 0, sizeof(Topic) * MAX_TOPICS);
	memset(MessageBuffer, 0, MAX_BUFFER_SIZE);
	memset(ResponseBuffer, 0, MAX_BUFFER_SIZE);
}

Mqttsn::~Mqttsn() {
}

bool Mqttsn::isConnected() {

	return Connected == ACCEPTED;
}

int Mqttsn::requestMessages() {

	LOGS.info("request msg");

	MsgPingReq* msg = reinterpret_cast<MsgPingReq*>(MessageBuffer);
	msg->length = sizeof(MsgPingReq) + strlen(ModuleName);
	msg->type = PINGREQ;
	strcpy(msg->clientId, ModuleName);

	sendMessage();

	// LOGS.debug("pinqReq", "clean received messages");

	while(NbReceivedMessage > 0) {
		NbReceivedMessage--;
		memset(&ReceivedMessages[NbReceivedMessage], 0, sizeof(MsgPublish));
		// LOGS.debug("pinqReq", "clean message ", nbReceivedMessage);
	}

	if( !checkSerial() ) {
		// LOGS.debug("pingReq", "check serial rejected");
		Connected = REJECTED;
		LOGS.notConnected();
		while(1);
	}

	// LOGS.debug("pingReq", "parsing published messages");
	parseData();

	return NbReceivedMessage;
}

void Mqttsn::disconnect() {

	/*
	if(Connected != ACCEPTED) {
		LOGS.notConnected();
		while(1);
	}
	*/

	MsgDisconnect* msg = reinterpret_cast<MsgDisconnect*>(MessageBuffer);

	msg->length = sizeof(MessageHeader);
	msg->type = DISCONNECT;

	msg->length += sizeof(MsgDisconnect);
	msg->duration = bitSwap(DURATION_TIME);
	// LOGS.debug("diconnect", "sleep duration: ", sleepDuration);

	sendMessage();

	if( !checkSerial() ) {
		// LOGS.debug("disconnect", "check serial rejected");
		Connected = REJECTED;
		LOGS.notConnected();
		while(1);
	}

	// LOGS.debug("disconnect", "parsing published messages");
	parseData();
}

void Mqttsn::publish(const char* _topicName, String _message){

	if(Connected != ACCEPTED) {
		LOGS.notConnected();
		while(1);
	}

	int topicId = findTopicId(_topicName);

	if(-1 == topicId) {
		// LOGS.debug("publish", "unknown->register", topicName);
		if(! registerTopic(_topicName)) {
			// LOGS.debug("publish", "can't register topic");
			return;
		}
		// LOGS.debug("publish", "call again");
		// LOGS.debug("publish", "name: ", TopicTable[NbRegisteredTopic].name);
		publish(_topicName, _message);
	} else {

		MsgPublish* msg = reinterpret_cast<MsgPublish*>(MessageBuffer);

		// get sizeof (MsgPublish + message) - sizeof MsgPublish.data (array of 40 char)
		// get "sizeof(header of MsgPublish)" + sizeof(message)
		msg->length = sizeof(MsgPublish) - sizeof(msg->data) + _message.length();
		msg->type = PUBLISH;
		msg->flags = QOS_FLAG;
		// @BUG msg->topic_id = bitSwap(topic_id);
		msg->topicId = ( topicId );
		msg->messageId = bitSwap(MESSAGE_ID);
		strcpy(msg->data, _message.c_str());

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
		WaitingForResponse = false;
	}
}

bool Mqttsn::start() {

	LOGS.info("start");

	WaitingForResponse = false;
	SearchGatewayOk = false;

	MsgSearchGateway* msg = reinterpret_cast<MsgSearchGateway*>(MessageBuffer);

	msg->length = sizeof(MsgSearchGateway);
	msg->type = SEARCHGW;
	msg->radius = RADIUS;

	// LOGS.debug("start", "sending message");
	sendMessage();

	// LOGS.debug("start", "checking the response from the gateway");

	int i = 0;
	while(false == SearchGatewayOk && i <= MAX_TRY) {

		// waiting next message
		if( !checkSerial() ) {
			LOGS.error("not started: stop");
			while(1);
		}
		parseData();

		i++;
	}

	return SearchGatewayOk;
}

void Mqttsn::connect(const char* _moduleName) {

	strcpy(ModuleName, _moduleName);

	MsgConnect* msg = reinterpret_cast<MsgConnect*>(MessageBuffer);

	msg->length = sizeof(MsgConnect) + strlen(ModuleName);
	msg->type = CONNECT;
	msg->flags = QOS_FLAG;
	msg->protocolId = PROTOCOL_ID;
	msg->duration = bitSwap(DURATION_TIME);
	strcpy(msg->clientId, ModuleName);

	// LOGS.debug("connect", "name", msg->clientId);

	sendMessage();

	if( !checkSerial() ) {
		// LOGS.debugln("connect", "KO");
		Connected = REJECTED;
		LOGS.notConnected();
		while(1);
	}
	parseData();
}

int Mqttsn::findTopicId(const char* _topicName) {

	for (int i = 0; i < NbRegisteredTopic; i++) {
		// LOGS.debug( "findTopicid", "id = ", (int)TopicTable[i].id);
		// LOGS.debug( "findTopicid", "name = ", TopicTable[i].name);
		if (TopicTable[i].id != DEFAULT_TOPIC_ID && strcmp(TopicTable[i].name, _topicName) == 0) {
			return TopicTable[i].id;
		}
	}

	// LOGS.debug("findTopicId", "topicName not found");
	return -1;
}

const char* Mqttsn::findTopicName(int _topicId) {

	for (int i = 0; i < NbRegisteredTopic; i++) {
		if (TopicTable[i].id != DEFAULT_TOPIC_ID && TopicTable[i].id == _topicId) {
			return TopicTable[i].name;
		}
	}

	// LOGS.debug("findTopicName", "name not found");
	return NULL;
}

bool Mqttsn::subscribeTopic(const char* _topicName) {

	// LOGS.debug("subscribeTopic", "topic: ", topicName);

	if(NbRegisteredTopic >= MAX_TOPICS) {
		// LOGS.debug("subscribeTopic", "nb > MAX_TOPICS");
		return false;
	}

	int topicId = findTopicId(_topicName);
	if(topicId != -1) {
		TopicTable[topicId].id = DEFAULT_TOPIC_ID;
		// LOGS.debug("subscribeTopic", "reset topic id:", topicId);
		LastSubscribedTopic = topicId;
	} else {
		// LOGS.debug("subscribeTopic", "id:", topicId);

		// Fill in the next table entry, but we only increment the counter to
		// the next topic when we get a REGACK from the broker. So don't issue
		// another REGISTER until we have resolved this one.
		// @name is save now because it will be lost at the end of this function.
		strcpy(TopicTable[NbRegisteredTopic].name, _topicName);
		// LOGS.debug("subscribeTopic", "name:", TopicTable[NbRegisteredTopic].name);

		// A magic number while the gateway respond: @see:regAckHandler()
		TopicTable[NbRegisteredTopic].id = DEFAULT_TOPIC_ID;
		LastSubscribedTopic = NbRegisteredTopic;

		NbRegisteredTopic++;
	}

	// LOGS.debug("subscribeTopic", topicName);

	MsgSubscribe* msg = reinterpret_cast<MsgSubscribe*>(MessageBuffer);

	// The -2 here is because we're unioning a 0-length member (topicName)
	// with a uint16_t in the MsgSubscribe struct.
	msg->length = sizeof(MsgSubscribe) + strlen(_topicName) - 2;
	msg->type = SUBSCRIBE;
	msg->flags = (QOS_MASK & QOS_MASK) | FLAG_TOPIC_NAME;
	msg->messageId = bitSwap(MESSAGE_ID);
	strcpy(msg->topicName, _topicName);

	// LOGS.debug("subscribeTopic", "sending message 'subscribe topic'");

	sendMessage();

	if( !checkSerial() ) {
		// LOGS.debug("subscribe", "check serial rejected");
		Connected = REJECTED;
		LOGS.notConnected();
		while(1);
	}

	// LOGS.debug("subscribe", "parsing response 'subscribe topic'");
	parseData();

	// LOGS.debug("subscribeTopic", "response from the gateway", subAckReturnCode);
	return SubAckReturnCode == ACCEPTED;
}

bool Mqttsn::registerTopic(const char* _topicName) {

	// LOGS.debug("register", "topic: ", _topicName);

	if(NbRegisteredTopic >= MAX_TOPICS) {
		// LOGS.debug("register", "nb > MAX_TOPICS");
		return false;
	}

	int topicId = findTopicId(_topicName);
	if(topicId != -1) {
		// LOGS.debug("register", "already registered");
		return true;
	}
	// LOGS.debug("register", "id:", topicId);

	// Fill in the next table entry, but we only increment the counter to
	// the next topic when we get a REGACK from the broker. So don't issue
	// another REGISTER until we have resolved this one.
	// @name is save now because it will be lost at the end of this function.
	strcpy(TopicTable[NbRegisteredTopic].name, _topicName);
	// LOGS.debug("register", "name:", TopicTable[NbRegisteredTopic].name);

	// A magic number while the gateway respond: @see:regAckHandler()
	TopicTable[NbRegisteredTopic].id = DEFAULT_TOPIC_ID;

	MsgRegister* msg = reinterpret_cast<MsgRegister*>(MessageBuffer);

	msg->length = sizeof(MsgRegister) + strlen(_topicName);
	msg->type = REGISTER;
	msg->topicId = 0;
	msg->messageId = bitSwap(MESSAGE_ID);
	strcpy(msg->topicName, _topicName);

	sendMessage();

	if( !checkSerial() ) {
		// LOGS.debug("register", "rejected");
		Connected = REJECTED;
		LOGS.notConnected();
		while(1);
	}

	parseData();

	// LOGS.debug("register", "response:", RegAckReturnCode);
	// LOGS.debug("register", "id:", findTopicId(_topicName));

	return RegAckReturnCode == ACCEPTED;
}

MsgPublish* Mqttsn::getReceivedMessages() {
	return ReceivedMessages;
}

/**
 * @TODO not implemented yet
 *
 * @brief Mqttsn::pingResp
 *
void Mqttsn::pingResp() {

	// LOGS.debug("pingResp");

	message_header* msg = reinterpret_cast<message_header*>(MessageBuffer);
	msg->length = sizeof(message_header);
	msg->type = PINGRESP;
	sendMessage();
}
*/

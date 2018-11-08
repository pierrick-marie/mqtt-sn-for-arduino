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
 * PUBLIC FUNCTIONS
 *
 * ****************************
 * ****************************
 *
 **/

Mqttsn::Mqttsn(SoftwareSerial* _xBee) {

	xBee = _xBee;

	waitingForResponse = false ;
	waitingForSubAck = false ;
	waitingForPubAck = false ;
	waitingForPingResp = false ;
	connected = false ;
	messageId = 0 ;
	gatewayId = 0 ;
	responseTimer = 0 ;
	responseRetries = 0 ;
	pingRespTimer = 0 ;
	pingRespRetries = 0 ;
	subAckTimer = 0 ;
	subAckRetries = 0 ;
	pubAckTimer = 0 ;
	pubAckRetries = 0 ;
	nbRegisteredTopic = 0;

	memset(topicTable, 0, sizeof(topic) * MAX_TOPICS);
	memset(messageBuffer, 0, MAX_BUFFER_SIZE);
	memset(responseBuffer, 0, MAX_BUFFER_SIZE);
}

Mqttsn::~Mqttsn() {
}

bool Mqttsn::isConnected() {

	return connected == ACCEPTED;
}

/**
 * @brief
 * @return
 *
 * @todo DEBUG
 **/
bool Mqttsn::waitForSubAck() {
	if (waitingForSubAck) {
		// TODO: Watch out for overflow.
		if ((millis() - subAckTimer) > (T_RETRY * 1000L)) {
			subAckTimer = millis();

			if (subAckRetries == 0) {
				waitingForSubAck = false;
				disconnect_handler(NULL);
			} else {
				sendMessage();
			}
			Serial.print("RESPONSE RETRIES ");
			Serial.println(subAckRetries);
			--subAckRetries;
		}
	}
	return waitingForSubAck;
}

/**
 * @brief
 * @return
 *
 * @todo DEBUG
 **/
bool Mqttsn::wait_for_puback() {
	if (waitingForPubAck) {
		// TODO: Watch out for overflow.
		if ((millis() - pubAckTimer) > (T_RETRY * 1000L)) {
			pubAckTimer = millis();

			if (pubAckRetries == 0) {
				waitingForPubAck = false;
				disconnect_handler(NULL);
			} else {
				sendMessage();
			}
			Serial.print("RESPONSE RETRIES ");
			Serial.println(pubAckRetries);
			--pubAckRetries;
		}
	}
	return waitingForPubAck;
}

void Mqttsn::waitMessage() {

	logs.debug( "waitMessage" );

	if( !multiCheckSerial(MAX_TRY) ) {
		logs.debug( "waitMessage", "check serial rejected");
		return;
	}

	logs.debug( "waitMessage", "message received");

	int i;
	int payload_lenght = frameBufferIn[12];
	uint8_t payload[payload_lenght];

	for(i = 0; i < payload_lenght; i++){
		payload[i] = frameBufferIn[12+i];
	}

	logs.debug("waitMessage", "data have been received");

	memset(responseBuffer, 0, MAX_BUFFER_SIZE);
	memcpy(responseBuffer, (const void*)payload, payload_lenght);

	logs.debug("waitMessage", "Stream is ready");
	String strData;
	for (char c : responseBuffer) strData += c;
	Serial.println(strData);

	logs.debug("waitMessage", "reset frame buffer to 0");
	memset(frameBufferIn, 0, sizeof(frameBufferIn));
}

/**
 * @brief
 * @return
 *
 * @todo DEBUG
 **/
bool Mqttsn::wait_for_pingresp() {
	if (waitingForPingResp) {
		// TODO: Watch out for overflow.
		if ((millis() - pingRespTimer) > (PR_RETRY * 1000L)) {
			pingRespTimer = millis();

			if (pingRespRetries == 0) {
				waitingForPingResp = false;
				disconnect_handler(NULL);
			} else {
				sendMessage();
			}
			Serial.print("PINGRESP RETRIES ");
			Serial.println(pingRespRetries);
			--pingRespRetries;
		}
	}
	return waitingForPingResp;
}

void Mqttsn::pingReq(const char* module_name) {

	logs.debug("pingReq", "building message");

	msg_pingreq* msg = reinterpret_cast<msg_pingreq*>(messageBuffer);
	msg->length = sizeof(msg_pingreq) + strlen(module_name);
	msg->type = PINGREQ;
	strcpy(msg->client_id, module_name);

	sendMessage();

	// @TODO: DEBUG?
	// pingRespTimer = millis();
	// pingRespRetries = N_RETRY;
	// waitingForPingResp = true;
	// Wainting for published messages
	// parseData();

	if( !multiCheckSerial(MAX_TRY) ) {
		// logs.debug("pingReq", "check serial rejected");
		return;
	}

	// logs.debug("pingReq", "parsing published messages");
	parseData();
}

void Mqttsn::pingResp() {

	// ping resp handler()
	// waitingForPingResp=false;

	logs.debug("pingResp", "building message");

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

int Mqttsn::publish(const char* topic_name, const char* message){

	int topic_id = findTopicId(topic_name);

	if(-1 == topic_id) {

		// logs.debug("publish", "topic name unknown -> registerTopic()", topic_name);
		if(registerTopic(topic_name) != ACCEPTED) {
			// logs.debug("publish", "impossible to register topic, return REJECTED");
			return REJECTED;
		}

		// Have to recall publish function to get the good value of topic_id
		publish(topic_name, message);
	} else {
		// logs.debug("publish", "send message", message);

		publishMessage(QOS_FLAG, topic_id, message, strlen(message));

		if( !multiCheckSerial(MAX_TRY) ) {
			// logs.debug("publish", "check serial rejected");
			return REJECTED;
		}

		// logs.debug("publish", "parsing response 'topic registered'");
		parseData();

		// logs.debug("publish", "response from the gateway - ", regAckReturnCode);
		return pubAckReturnCode;
	}
}

void Mqttsn::will_topic(const uint8_t flags, const char* will_topic, const bool update) {
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

void Mqttsn::will_messsage(const void* will_msg, const uint8_t will_msg_len, const bool update) {
	msg_willmsg* msg = reinterpret_cast<msg_willmsg*>(messageBuffer);

	msg->length = sizeof(msg_willmsg) + will_msg_len;
	msg->type = update ? WILLMSGUPD : WILLMSG;
	memcpy(msg->willmsg, will_msg, will_msg_len);

	sendMessage();
}

int Mqttsn::init() {

	// logs.debug("init", "");

	int nb_try = 0;
	int radius = 0;

	searchGateway(radius);

	while( !checkSerial() && nb_try < MAX_TRY) {
		// logs.debug( "init", "the gateway did not respond, iteration: ", nb_try);
		nb_try++;
		delay(5000);
		searchGateway(radius);
	}

	if( nb_try == MAX_TRY ) {
		// logs.debug( "init", "REJECTED");
		return REJECTED;
	}

	// logs.debug( "init", "waiting for a response");
	parseData();

	// logs.debug( "init", "checking the response from the gateway");
	if(!initOk) {
		return REJECTED;
	}

	return ACCEPTED;
}

int Mqttsn::connect(const char* module_name) {

	// logs.debug( "connect", "send a connect message");
	connect(QOS_FLAG, KEEP_ALIVE, module_name);

	if( !multiCheckSerial(MAX_TRY) ) {
		// logs.debug( "connect", "check serial rejected");
		return REJECTED;
	}

	parseData();

	// logs.debug( "connect", "response from the gateway:", connected);
	return connected;
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
	  return false;
    }

    // data available before the timeout
    delimiter = xBee->read();

    // verifiy if the delimiter is OK
    if(delimiter != 0x7E) {
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

	  // verify the checksum
	  if(!verifyChecksum(frameBufferIn, frame_size)) {
		return false;
	  }

	  // check the type of received message
	  if(isTransmitStatus()) {
		return false;
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
		// logs.debug("checkSerial", "there is data in frame buffer");
		return true;
	  }
    }
    // not data is available, clear the buffer and return false
    memset(frameBufferIn, 0, sizeof(frameBufferIn));
    return false;
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

int Mqttsn::subscribe(const char* name) {

	// logs.debug("subscribe", "searching topic id");

	if(-1 == findTopicId(name)){
		// logs.debug( "subscribe", "topic is not already registered -> registerTopic()");
		if(registerTopic(name) != ACCEPTED){
			return REJECTED_NOT_SUPPORTED;
		}
	}

	// logs.debug("subscribe", "subscribing topic");

	subscribeByName(QOS_FLAG, name);

	if( !multiCheckSerial(MAX_TRY) ) {
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

	// logs.debug( "registerTopic", "Topic registered localy: ", topicName);

	msg_register* msg = reinterpret_cast<msg_register*>(messageBuffer);

	msg->length = sizeof(msg_register) + strlen(topicName);
	msg->type = REGISTER;
	msg->topic_id = 0;
	msg->message_id = bitSwap(messageId);
	strcpy(msg->topic_name, topicName);

	// logs.debug( "registerTopic", "sending message: register topic ", topicName);
	sendMessage();

	if( !multiCheckSerial(MAX_TRY) ) {
		// logs.debug( "registerTopic", "check serial rejected");
		return REJECTED;
	}

	// logs.debug( "registerTopic", "parsing response 'topic registered'");
	parseData();

	// logs.debug("registerTopic", "response from the gateway - ", regAckReturnCode);
	// logs.debug("registerTopic", "topic id is: ", findTopicId(topicName));
	return regAckReturnCode;
}

/**
 *
 * ****************************
 * ****************************
 *
 * GETTERS & SETTERS
 *
 * ****************************
 * ****************************
 *
 **/

bool Mqttsn::getInitOk() {
	return initOk;
}

void Mqttsn::setInitOk(const bool init_ok) {
	initOk = init_ok;
}

/**
 *
 * ****************************
 * ****************************
 *
 * QoS
 *
 * ****************************
 * ****************************
 *
 **/

#ifdef USE_QOS2
void Mqttsn::pubrec() {
	msg_pubqos2* msg = reinterpret_cast<msg_pubqos2*>(message_buffer);
	msg->length = sizeof(msg_pubqos2);
	msg->type = PUBREC;
	msg->message_id = bitSwap(_message_id);

	sendMessage();
}

void Mqttsn::pubrel() {
	msg_pubqos2* msg = reinterpret_cast<msg_pubqos2*>(message_buffer);
	msg->length = sizeof(msg_pubqos2);
	msg->type = PUBREL;
	msg->message_id = bitSwap(_message_id);

	sendMessage();
}

void Mqttsn::pubcomp() {
	msg_pubqos2* msg = reinterpret_cast<msg_pubqos2*>(message_buffer);
	msg->length = sizeof(msg_pubqos2);
	msg->type = PUBCOMP;
	msg->message_id = bitSwap(_message_id);

	sendMessage();
}
#endif

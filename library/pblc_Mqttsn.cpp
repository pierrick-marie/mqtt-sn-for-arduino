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
 * ############################
 * ####################
 * #############
 * ######
 * ##
 * ****************************
 *
 * PUBLIC FUNCTIONS
 *
 * ****************************
 * ##
 * ######
 * #############
 * ####################
 * ############################
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

#ifdef USE_SERIAL
/**
 * parseStream(buf, len) is called at the end of the function CheckSerial() in _MeshBee.ino.
 * CheckSerial() gets all incoming data from the gateway and call parseStream with the buffer of data (@buf) and it's size (@len).
 **/
void Mqttsn::parseStream(uint8_t* buf, uint16_t len) {

	logs.debug("Mqttsn", "parseStream");

	if(waitingForPingResp){
		pingRespTimer = millis();
	}
	memset(responseBuffer, 0, MAX_BUFFER_SIZE);
	memcpy(responseBuffer, (const void*)buf, len);

	// waitingForResponse is set to false to allow another request
	waitingForResponse = false;
	dispatch();
}
#endif

void Mqttsn::gatewayInfoHandler(const msg_gwinfo* message) {

	logs.debug("MqttsnApi", "gatewayInfoHandler");

	if(message->gw_id == 1) {
		initOk = true;
	} else {
		initOk = false;
	}
}

bool Mqttsn::isConnected() {
	return connected;
}

// extern void Mqttsn_connack_handler(const msg_connack* msg);
void Mqttsn::connack_handler(const msg_connack* msg) {
	connected = true;
	// Mqttsn_connack_handler(msg);
}

/**
 * @brief Mqttsn::wait_for_response
 * @return
 *
 * @todo DEBUG
 **/
bool Mqttsn::wait_for_response() {

	if (waitingForResponse) {

		// TODO: Watch out for overflow.
		if ((millis() - responseTimer) > (T_RETRY * 1000L)) {
			responseTimer = millis();

			if (responseRetries == 0) {
				waitingForResponse = false;
				disconnect_handler(NULL);
			} else {
				sendMessage();
			}
			Serial.print("RESPONSE RETRIES ");
			Serial.println(responseRetries);
			--responseRetries;
		}
	}
	return waitingForResponse;
}

/**
 * @brief Mqttsn::wait_for_response
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
 * @brief Mqttsn::wait_for_response
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

/**
 * @brief Mqttsn::wait_for_response
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

// extern void Mqttsn_willtopicreq_handler(const message_header* msg);
void Mqttsn::willtopicreq_handler(const message_header* msg) {
	// Mqttsn_willtopicreq_handler(msg);
}

// extern void Mqttsn_willmsgreq_handler(const message_header* msg);
void Mqttsn::willmsgreq_handler(const message_header* msg) {
	// Mqttsn_willmsgreq_handler(msg);
}


// extern void Mqttsn_puback_handler(const msg_puback* msg);
void Mqttsn::puback_handler(const msg_puback* msg) {
	waitingForPubAck=false;
	// Mqttsn_puback_handler(msg);
}

// extern void Mqttsn_suback_handler(const msg_suback* msg);
void Mqttsn::suback_handler(const msg_suback* msg) {
	waitingForSubAck=false;
	// Mqttsn_suback_handler(msg);
}

// extern void Mqttsn_disconnect_handler(const msg_disconnect* msg);
void Mqttsn::disconnect_handler(const msg_disconnect* msg) {
	connected = false;
	// Mqttsn_disconnect_handler(msg);
}

// extern void Mqttsn_pingresp_handler();
void Mqttsn::pingresp_handler() {
	waitingForPingResp=false;
	// Mqttsn_pingresp_handler();
}

// extern void Mqttsn_publish_handler(const msg_publish* msg);
void Mqttsn::publish_handler(const msg_publish* msg) {
	//if (msg->flags & FLAG_QOS_1) {
	return_code_t ret = REJECTED_INVALID_TOPIC_ID;
	const uint16_t topic_id = bitSwap(msg->topic_id);
	for (uint8_t i = 0; i < nbRegisteredTopic; ++i) {
		if (topicTable[i].id == topic_id) {
			ret = ACCEPTED;
			// Mqttsn_publish_handler(msg);
			break;
		}
	}

	puback(msg->topic_id, msg->message_id, ret);
	msg=NULL;
	//}
}

void Mqttsn::pingreq(const char* client_id) {
	msg_pingreq* msg = reinterpret_cast<msg_pingreq*>(messageBuffer);
	msg->length = sizeof(msg_pingreq) + strlen(client_id);
	msg->type = PINGREQ;
	strcpy(msg->client_id, client_id);

	sendMessage();

	pingRespTimer = millis();
	pingRespRetries = N_RETRY;
	waitingForPingResp = true;
}

void Mqttsn::pingresp() {
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
	waitingForResponse = true;
}



void Mqttsn::publish(const uint8_t flags, const uint16_t topic_id, const void* data, const uint8_t data_len) {
	++messageId;

	msg_publish* msg = reinterpret_cast<msg_publish*>(messageBuffer);

	msg->length = sizeof(msg_publish) + data_len;
	msg->type = PUBLISH;
	msg->flags = flags;
	msg->topic_id = bitSwap(topic_id);
	msg->message_id = bitSwap(messageId);
	memcpy(msg->data, data, data_len);

	sendMessage();

	//if ((flags & QOS_MASK) == FLAG_QOS_1 || (flags & QOS_MASK) == FLAG_QOS_2) {
	waitingForPubAck = true;
	//}
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

	if ((flags & QOS_MASK) == FLAG_QOS_1 || (flags & QOS_MASK) == FLAG_QOS_2) {
		waitingForResponse = true;
	}
}

void Mqttsn::will_messsage(const void* will_msg, const uint8_t will_msg_len, const bool update) {
	msg_willmsg* msg = reinterpret_cast<msg_willmsg*>(messageBuffer);

	msg->length = sizeof(msg_willmsg) + will_msg_len;
	msg->type = update ? WILLMSGUPD : WILLMSG;
	memcpy(msg->willmsg, will_msg, will_msg_len);

	sendMessage();
}

void Mqttsn::connect(const uint8_t flags, const uint16_t duration, const char* client_id) {

	msg_connect* msg = reinterpret_cast<msg_connect*>(messageBuffer);

	msg->length = sizeof(msg_connect) + strlen(client_id);
	msg->type = CONNECT;
	msg->flags = flags;
	msg->protocol_id = PROTOCOL_ID;
	msg->duration = bitSwap(duration);
	strcpy(msg->client_id, client_id);

	sendMessage();
	connected = false;
	waitingForResponse = true;
}

int Mqttsn::init() {

	int nb_try = 0;
	int radius = 0;

	logs.debug("MqttsnApi", "init");

	searchGateway(radius);

	while( !checkSerial() && nb_try < MAX_TRY) {
		logs.debug("MqttsnApi", "init", "the gateway did not respond, iteration: ", nb_try);
		nb_try++;
		// delay(1000);
		searchGateway(radius);
	}

	if( nb_try == MAX_TRY) {
		logs.debug("MqttsnApi", "init", "REJECTED");
		return REJECTED_NOT_SUPPORTED;
	}

	parseData();

	logs.debug("MqttsnApi", "init", "checking the response from the gateway");
	if(initOk) {
		return ACCEPTED;
	} else {
		return REJECTED_NOT_SUPPORTED;
	}

	return ACCEPTED;
}

int Mqttsn::connect(const char* moduleName) {

	logs.debug("Mqttsn", "connect", "send a connect message");
	connect(FLAG, KEEP_ALIVE, moduleName);

	if( !multiCheckSerial(MAX_TRY) ) {
		logs.debug("Mqttsn", "connect", "check serial rejected");
		return REJECTED_NOT_SUPPORTED;
	}

	parseData();

	logs.debug("Mqttsn", "connect", "response from the gateway:", connAckReturnCode);
	return connAckReturnCode;
}

/**
 * The function waits a response from the gateway (@waitData). If a response is available, the function analyse and store the message if necessary.
 *
 * Returns:
 * True if a correct message have been received, else false.
 **/
bool Mqttsn::checkSerial() {

    int i, frame_size, payload_lenght;
    uint8_t delimiter, length1, length2, frame_buffer;
    bool checksum;

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
	  length1=xBee->read();
	  length2=xBee->read();
	  frame_size=(length1*16)+length2+1;

	  // store the data in @frameBuffer
	  for(i = 0; i < frame_size; i++){
		delay(10);
		frameBufferIn[i]=xBee->read();
	  }

	  // verify the checksum
	  if(!verifyChecksum(frameBufferIn, frame_size)) {
		return false;
	  }

	  // check the type of received message
	  if(is_transmit_status()) {
		return false;
	  }

	  if(is_data_packet()) {
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
			logs.debug("Mqttsn", "findTopicid", "id = ", (int)topicTable[i].id);
			logs.debug("Mqttsn", "findTopicid", "name = ", (int)topicTable[i].name);
			return topicTable[i].id;
		}
	}

	return -1;
}

int Mqttsn::subscribe(const char* name) {

	if(-1 == findTopicId(name)){
		if(registerByName(name) != ACCEPTED){
			return REJECTED_NOT_SUPPORTED;
		}
	}
	subscribeByName(FLAG, name);
	while(waitForSubAck()){
		checkSerial();
	}

	// @DEBUG
	delay(1000);
	return subAckReturnCode;
}

int Mqttsn::registerByName(const char* name) {

	logs.debug("Mqttsn", "registerByName");

	// waitingForResponse is set to false in function @parseStream.
	// As a consequence, in nominal case, it should be equals to false.
	if (waitingForResponse) {
		logs.debug("Mqttsn", "registerTopic", "waitingForResponse is true");
		return -2;
	}

	if(nbRegisteredTopic >= MAX_TOPICS) {
		logs.debug("Mqttsn", "registerTopic", "nbRegisteredTopic > MAX_TOPICS");
		return -2;
	}

	int topic_id = findTopicId(name);
	if(topic_id != -1) {
		logs.debug("Mqttsn", "registerTopic", "name is already registered");
		return topic_id;
	}

	// Fill in the next table entry, but we only increment the counter to
	// the next topic when we get a REGACK from the broker. So don't issue
	// another REGISTER until we have resolved this one.
	// @name is save now because it will be lost at the end of this function.
	topicTable[nbRegisteredTopic].name = name;
	// A magic number while the gateway respond: @see:regAckHandler()
	topicTable[nbRegisteredTopic].id = DEFAULT_TOPIC_ID;

	messageId++;
	msg_register* msg = reinterpret_cast<msg_register*>(messageBuffer);

	msg->length = sizeof(msg_register) + strlen(name);
	msg->type = REGISTER;
	msg->topic_id = 0;
	msg->message_id = bitSwap(messageId);
	strcpy(msg->topic_name, name);
	sendMessage();

	if( !multiCheckSerial(MAX_TRY) ) {
		logs.debug("Mqttsn", "registerByName", "check serial rejected");
		return REJECTED_NOT_SUPPORTED;
	}

	parseData();

	logs.debug("Mqttsn", "registerByName", "response from the gateway:", regAckReturnCode);
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

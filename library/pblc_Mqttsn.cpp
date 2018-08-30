/*
mqttsn-messages.cpp

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

#include <Arduino.h>

#include "mqttsn-messages.h"
#include "Mqttsn.h"
#include "Logs.h"

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

MQTTSN::MQTTSN() :
      WaitingForResponse(false),
      WaitingForSuback(false),
      WaitingForPuback(false),
      WaitingForPingresp(false),
      Connected(false),
      MessageId(0),
      TopicCount(0),
      GatewayId(0),
      ResponseTimer(0),
      ResponseRetries(0),
      PingrespTimer(0),
      PingrespRetries(0),
      SubackTimer(0),
      SubackRetries(0),
      PubackTimer(0),
      PubackRetries(0)
{
	memset(TopicTable, 0, sizeof(topic) * MAX_TOPICS);
	memset(MessageBuffer, 0, MAX_BUFFER_SIZE);
	memset(ResponseBuffer, 0, MAX_BUFFER_SIZE);
}

MQTTSN::~MQTTSN() {
}

#ifdef USE_QOS2
void MQTTSN::pubrec() {
	msg_pubqos2* msg = reinterpret_cast<msg_pubqos2*>(message_buffer);
	msg->length = sizeof(msg_pubqos2);
	msg->type = PUBREC;
	msg->message_id = bswap(_message_id);

	send_message();
}

void MQTTSN::pubrel() {
	msg_pubqos2* msg = reinterpret_cast<msg_pubqos2*>(message_buffer);
	msg->length = sizeof(msg_pubqos2);
	msg->type = PUBREL;
	msg->message_id = bswap(_message_id);

	send_message();
}

void MQTTSN::pubcomp() {
	msg_pubqos2* msg = reinterpret_cast<msg_pubqos2*>(message_buffer);
	msg->length = sizeof(msg_pubqos2);
	msg->type = PUBCOMP;
	msg->message_id = bswap(_message_id);

	send_message();
}
#endif

#ifdef USE_SERIAL
/**
 * parse_stream(buf, len) is called at the end of the function CheckSerial() in _MeshBee.ino.
 * CheckSerial() gets all incoming data from the gateway and call parse_stream with the buffer of data (@buf) and it's size (@len).
 **/
void MQTTSN::parse_stream(uint8_t* buf, uint16_t len) {

	if(WaitingForPingresp){
		PingrespTimer = millis();
	}
	memset(ResponseBuffer, 0, MAX_BUFFER_SIZE);
	memcpy(ResponseBuffer, (const void*)buf, len);

	// WaitingForResponse is set to false to allow another request
	WaitingForResponse = false;
	dispatch();
}
#endif

/**
 * @brief MQTTSN::searchgw The function sends a message to the search the closest gateway arround @radius scale.
 * @param radius The max hop to search an available gateway.
 **/
void MQTTSN::searchgw(const uint8_t radius) {

	msg_searchgw* msg = reinterpret_cast<msg_searchgw*>(MessageBuffer);

	msg->length = sizeof(msg_searchgw);
	msg->type = SEARCHGW;
	msg->radius = radius;

	send_message();

	// Waiting a response, set to false in @MQTTSN_parse_stream()
	WaitingForResponse = true;
}

extern void MQTTSN_gwinfo_handler(const msg_gwinfo* msg);
void MQTTSN::gwinfo_handler(const msg_gwinfo* msg) {
	MQTTSN_gwinfo_handler(msg);
}

bool MQTTSN::connected() {
	return Connected;
}

extern void MQTTSN_connack_handler(const msg_connack* msg);
void MQTTSN::connack_handler(const msg_connack* msg) {
	Connected = true;
	MQTTSN_connack_handler(msg);
}

/**
 * @brief MQTTSN::register_topic The function asks to the gateway to register a @topic_name.
 * @param topicName The topic name to register.
 *
 * @return
 *      -2 if it is not possible to register the @topic_name.
 *      -1 if the message to register @topic_name is sent.
 *      >= 0 the id of the @topic_name already registered.
 *
 **/
int MQTTSN::register_topic(const char* topic_name) {

	// WaitingForResponse is set to false in function @MQTTSN_parse_stream.
	// As a consequence, in nominal case, it should be equals to false.
	if (WaitingForResponse) {
		logs.debug("MQTTSN", "register_topic", "waitingForResponse is true");
		return -2;
	}

	if(TopicCount >= MAX_TOPICS) {
		logs.debug("MQTTSN", "register_topic", "topicCount > MAX_TOPICS");
		return -2;
	}

	int topic_id = find_topic_id(topic_name);
	if(topic_id != -1) {
		logs.debug("MQTTSN", "register_topic", "topic_name is already regitered");
		return topic_id;
	}

	// Fill in the next table entry, but we only increment the counter to
	// the next topic when we get a REGACK from the broker. So don't issue
	// another REGISTER until we have resolved this one.
	// @topic_name is save now because it will be lost at the end of this function.
	TopicTable[TopicCount].name = topic_name;
	// A magic number while the gateway respond: @see:regack_handler()
	TopicTable[TopicCount].id = DEFAULT_TOPIC_ID;

	msg_register* msg = reinterpret_cast<msg_register*>(MessageBuffer);
	MessageId++;
	msg->length = sizeof(msg_register) + strlen(topic_name);
	msg->type = REGISTER;
	msg->topic_id = 0;
	msg->message_id = bswap(MessageId);
	strcpy(msg->topic_name, topic_name);
	send_message();

	logs.debug("MQTTSN", "register_topic", "Register topic message is sent");

	// Waiting a response, set to false in @MQTTSN_parse_stream()
	WaitingForResponse = true;

	return -1;
}

/**
 * @brief MQTTSN::wait_for_response
 * @return
 *
 * @todo DEBUG
 **/
bool MQTTSN::wait_for_response() {

	if (WaitingForResponse) {

		// TODO: Watch out for overflow.
		if ((millis() - ResponseTimer) > (T_RETRY * 1000L)) {
			ResponseTimer = millis();

			if (ResponseRetries == 0) {
				WaitingForResponse = false;
				disconnect_handler(NULL);
			} else {
				send_message();
			}
			Serial.print("RESPONSE RETRIES ");
			Serial.println(ResponseRetries);
			--ResponseRetries;
		}
	}
	return WaitingForResponse;
}

/**
 * @brief MQTTSN::wait_for_response
 * @return
 *
 * @todo DEBUG
 **/
bool MQTTSN::wait_for_suback() {
	if (WaitingForSuback) {
		// TODO: Watch out for overflow.
		if ((millis() - SubackTimer) > (T_RETRY * 1000L)) {
			SubackTimer = millis();

			if (SubackRetries == 0) {
				WaitingForSuback = false;
				disconnect_handler(NULL);
			} else {
				send_message();
			}
			Serial.print("RESPONSE RETRIES ");
			Serial.println(SubackRetries);
			--SubackRetries;
		}
	}
	return WaitingForSuback;
}

/**
 * @brief MQTTSN::wait_for_response
 * @return
 *
 * @todo DEBUG
 **/
bool MQTTSN::wait_for_puback() {
	if (WaitingForPuback) {
		// TODO: Watch out for overflow.
		if ((millis() - PubackTimer) > (T_RETRY * 1000L)) {
			PubackTimer = millis();

			if (PubackRetries == 0) {
				WaitingForPuback = false;
				disconnect_handler(NULL);
			} else {
				send_message();
			}
			Serial.print("RESPONSE RETRIES ");
			Serial.println(PubackRetries);
			--PubackRetries;
		}
	}
	return WaitingForPuback;
}

/**
 * @brief MQTTSN::wait_for_response
 * @return
 *
 * @todo DEBUG
 **/
bool MQTTSN::wait_for_pingresp() {
	if (WaitingForPingresp) {
		// TODO: Watch out for overflow.
		if ((millis() - PingrespTimer) > (PR_RETRY * 1000L)) {
			PingrespTimer = millis();

			if (PingrespRetries == 0) {
				WaitingForPingresp = false;
				disconnect_handler(NULL);
			} else {
				send_message();
			}
			Serial.print("PINGRESP RETRIES ");
			Serial.println(PingrespRetries);
			--PingrespRetries;
		}
	}
	return WaitingForPingresp;
}

extern void MQTTSN_willtopicreq_handler(const message_header* msg);
void MQTTSN::willtopicreq_handler(const message_header* msg) {
	MQTTSN_willtopicreq_handler(msg);
}

extern void MQTTSN_willmsgreq_handler(const message_header* msg);
void MQTTSN::willmsgreq_handler(const message_header* msg) {
	MQTTSN_willmsgreq_handler(msg);
}


extern void MQTTSN_puback_handler(const msg_puback* msg);
void MQTTSN::puback_handler(const msg_puback* msg) {
	WaitingForPuback=false;
	MQTTSN_puback_handler(msg);
}

extern void MQTTSN_suback_handler(const msg_suback* msg);
void MQTTSN::suback_handler(const msg_suback* msg) {
	WaitingForSuback=false;
	MQTTSN_suback_handler(msg);
}

extern void MQTTSN_disconnect_handler(const msg_disconnect* msg);
void MQTTSN::disconnect_handler(const msg_disconnect* msg) {
	Connected = false;
	MQTTSN_disconnect_handler(msg);
}

extern void MQTTSN_pingresp_handler(); 
void MQTTSN::pingresp_handler() {
	WaitingForPingresp=false;
	MQTTSN_pingresp_handler();
}

extern void MQTTSN_publish_handler(const msg_publish* msg);
void MQTTSN::publish_handler(const msg_publish* msg) {
	//if (msg->flags & FLAG_QOS_1) {
	return_code_t ret = REJECTED_INVALID_TOPIC_ID;
	const uint16_t topic_id = bswap(msg->topic_id);
	for (uint8_t i = 0; i < TopicCount; ++i) {
		if (TopicTable[i].id == topic_id) {
			ret = ACCEPTED;
			MQTTSN_publish_handler(msg);
			break;
		}
	}

	puback(msg->topic_id, msg->message_id, ret);
	msg=NULL;
	//}
}

void MQTTSN::pingreq(const char* client_id) {
	msg_pingreq* msg = reinterpret_cast<msg_pingreq*>(MessageBuffer);
	msg->length = sizeof(msg_pingreq) + strlen(client_id);
	msg->type = PINGREQ;
	strcpy(msg->client_id, client_id);

	send_message();

	PingrespTimer = millis();
	PingrespRetries = N_RETRY;
	WaitingForPingresp = true;
}

void MQTTSN::pingresp() {
	message_header* msg = reinterpret_cast<message_header*>(MessageBuffer);
	msg->length = sizeof(message_header);
	msg->type = PINGRESP;
	send_message();
}

void MQTTSN::unsubscribe_by_id(const uint8_t flags, const uint16_t topic_id) {
	++MessageId;

	msg_unsubscribe* msg = reinterpret_cast<msg_unsubscribe*>(MessageBuffer);

	msg->length = sizeof(msg_unsubscribe);
	msg->type = UNSUBSCRIBE;
	msg->flags = (flags & QOS_MASK) | FLAG_TOPIC_PREDEFINED_ID;
	msg->message_id = bswap(MessageId);
	msg->topic_id = bswap(topic_id);

	send_message();

	if ((flags & QOS_MASK) == FLAG_QOS_1 || (flags & QOS_MASK) == FLAG_QOS_2) {
		WaitingForResponse = true;
	}
}

void MQTTSN::unsubscribe_by_name(const uint8_t flags, const char* topic_name) {
	++MessageId;

	msg_unsubscribe* msg = reinterpret_cast<msg_unsubscribe*>(MessageBuffer);

	// The -2 here is because we're unioning a 0-length member (topic_name)
	// with a uint16_t in the msg_unsubscribe struct.
	msg->length = sizeof(msg_unsubscribe) + strlen(topic_name) - 2;
	msg->type = UNSUBSCRIBE;
	msg->flags = (flags & QOS_MASK) | FLAG_TOPIC_NAME;
	msg->message_id = bswap(MessageId);
	strcpy(msg->topic_name, topic_name);

	send_message();

	if ((flags & QOS_MASK) == FLAG_QOS_1 || (flags & QOS_MASK) == FLAG_QOS_2) {
		WaitingForResponse = true;
	}
}

void MQTTSN::disconnect(const uint16_t duration) {
	msg_disconnect* msg = reinterpret_cast<msg_disconnect*>(MessageBuffer);

	msg->length = sizeof(message_header);
	msg->type = DISCONNECT;

	if (duration > 0) {
		msg->length += sizeof(msg_disconnect);
		msg->duration = bswap(duration);
	}

	send_message();
	WaitingForResponse = true;
}

void MQTTSN::subscribe_by_name(const uint8_t flags, const char* topic_name) {
	++MessageId;

	msg_subscribe* msg = reinterpret_cast<msg_subscribe*>(MessageBuffer);

	// The -2 here is because we're unioning a 0-length member (topic_name)
	// with a uint16_t in the msg_subscribe struct.
	msg->length = sizeof(msg_subscribe) + strlen(topic_name) - 2;
	msg->type = SUBSCRIBE;
	msg->flags = (flags & QOS_MASK) | FLAG_TOPIC_NAME;
	msg->message_id = bswap(MessageId);
	strcpy(msg->topic_name, topic_name);

	send_message();

	//if ((flags & QOS_MASK) == FLAG_QOS_1 || (flags & QOS_MASK) == FLAG_QOS_2) {
	WaitingForSuback = true;
	//}
}

void MQTTSN::publish(const uint8_t flags, const uint16_t topic_id, const void* data, const uint8_t data_len) {
	++MessageId;

	msg_publish* msg = reinterpret_cast<msg_publish*>(MessageBuffer);

	msg->length = sizeof(msg_publish) + data_len;
	msg->type = PUBLISH;
	msg->flags = flags;
	msg->topic_id = bswap(topic_id);
	msg->message_id = bswap(MessageId);
	memcpy(msg->data, data, data_len);

	send_message();

	//if ((flags & QOS_MASK) == FLAG_QOS_1 || (flags & QOS_MASK) == FLAG_QOS_2) {
	WaitingForPuback = true;
	//}
}

void MQTTSN::subscribe_by_id(const uint8_t flags, const uint16_t topic_id) {
	++MessageId;

	msg_subscribe* msg = reinterpret_cast<msg_subscribe*>(MessageBuffer);

	msg->length = sizeof(msg_subscribe);
	msg->type = SUBSCRIBE;
	msg->flags = (flags & QOS_MASK) | FLAG_TOPIC_PREDEFINED_ID;
	msg->message_id = bswap(MessageId);
	msg->topic_id = bswap(topic_id);

	send_message();

	if ((flags & QOS_MASK) == FLAG_QOS_1 || (flags & QOS_MASK) == FLAG_QOS_2) {
		WaitingForResponse = true;
	}
}

void MQTTSN::will_topic(const uint8_t flags, const char* will_topic, const bool update) {
	if (will_topic == NULL) {
		message_header* msg = reinterpret_cast<message_header*>(MessageBuffer);

		msg->type = update ? WILLTOPICUPD : WILLTOPIC;
		msg->length = sizeof(message_header);
	} else {
		msg_willtopic* msg = reinterpret_cast<msg_willtopic*>(MessageBuffer);

		msg->type = update ? WILLTOPICUPD : WILLTOPIC;
		msg->flags = flags;
		strcpy(msg->will_topic, will_topic);
	}

	send_message();

	if ((flags & QOS_MASK) == FLAG_QOS_1 || (flags & QOS_MASK) == FLAG_QOS_2) {
		WaitingForResponse = true;
	}
}

void MQTTSN::will_messsage(const void* will_msg, const uint8_t will_msg_len, const bool update) {
	msg_willmsg* msg = reinterpret_cast<msg_willmsg*>(MessageBuffer);

	msg->length = sizeof(msg_willmsg) + will_msg_len;
	msg->type = update ? WILLMSGUPD : WILLMSG;
	memcpy(msg->willmsg, will_msg, will_msg_len);

	send_message();
}

void MQTTSN::connect(const uint8_t flags, const uint16_t duration, const char* client_id) {

	msg_connect* msg = reinterpret_cast<msg_connect*>(MessageBuffer);

	msg->length = sizeof(msg_connect) + strlen(client_id);
	msg->type = CONNECT;
	msg->flags = flags;
	msg->protocol_id = PROTOCOL_ID;
	msg->duration = bswap(duration);
	strcpy(msg->client_id, client_id);

	send_message();
	Connected = false;
	WaitingForResponse = true;
}

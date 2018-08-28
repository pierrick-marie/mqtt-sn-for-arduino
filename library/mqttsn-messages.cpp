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
#include "mqttsn.h"


MQTTSN::MQTTSN() :
waiting_for_response(false),
waiting_for_suback(false),
waiting_for_puback(false),
waiting_for_pingresp(false),
_connected(false),
_message_id(0),
topic_count(0),
_gateway_id(0),
_response_timer(0),
_response_retries(0),
_pingresp_timer(0),
_pingresp_retries(0),
_suback_timer(0),
_suback_retries(0),
_puback_timer(0),
_puback_retries(0)
{
    memset(topic_table, 0, sizeof(topic) * MAX_TOPICS);
    memset(message_buffer, 0, MAX_BUFFER_SIZE);
    memset(response_buffer, 0, MAX_BUFFER_SIZE);
}

MQTTSN::~MQTTSN() {
}

void MQTTSN::searchgw(const uint8_t radius) {
    
    msg_searchgw* msg = reinterpret_cast<msg_searchgw*>(message_buffer);

    msg->length = sizeof(msg_searchgw);
    msg->type = SEARCHGW;
    msg->radius = radius;

    send_message();
    
    waiting_for_response = true;
}

/**
 * @ORIGINAL
 **
 */
bool MQTTSN::wait_for_response() {
    
    if (waiting_for_response) {
        
        // TODO: Watch out for overflow.
        if ((millis() - _response_timer) > (T_RETRY * 1000L)) {
            _response_timer = millis();

            if (_response_retries == 0) {
                waiting_for_response = false;
                disconnect_handler(NULL);
            } else {
                send_message();
            }
            Serial.print("RESPONSE RETRIES ");
            Serial.println(_response_retries);
            --_response_retries;
        }
    }
    return waiting_for_response;
}

bool MQTTSN::wait_for_suback() {
    if (waiting_for_suback) {
        // TODO: Watch out for overflow.
        if ((millis() - _suback_timer) > (T_RETRY * 1000L)) {
            _suback_timer = millis();

            if (_suback_retries == 0) {
                waiting_for_suback = false;
                disconnect_handler(NULL);
            } else {
                send_message();
            }
            Serial.print("RESPONSE RETRIES ");
            Serial.println(_suback_retries);
            --_suback_retries;
        }
    }
    return waiting_for_suback;
}

bool MQTTSN::wait_for_puback() {
    if (waiting_for_puback) {
        // TODO: Watch out for overflow.
        if ((millis() - _puback_timer) > (T_RETRY * 1000L)) {
            _puback_timer = millis();

            if (_puback_retries == 0) {
                waiting_for_puback = false;
                disconnect_handler(NULL);
            } else {
                send_message();
            }
            Serial.print("RESPONSE RETRIES ");
            Serial.println(_puback_retries);
            --_puback_retries;
        }
    }
    return waiting_for_puback;
}

bool MQTTSN::wait_for_pingresp() {
    if (waiting_for_pingresp) {
        // TODO: Watch out for overflow.
        if ((millis() - _pingresp_timer) > (PR_RETRY * 1000L)) {
            _pingresp_timer = millis();

            if (_pingresp_retries == 0) {
                waiting_for_pingresp = false;
                disconnect_handler(NULL);
            } else {
                send_message();
            }
            Serial.print("PINGRESP RETRIES ");
            Serial.println(_pingresp_retries);
            --_pingresp_retries;
        }
    }
    return waiting_for_pingresp;
}

bool MQTTSN::connected() {
	return _connected;
}

uint16_t MQTTSN::bswap(const uint16_t val) {
    return (val << 8) | (val >> 8);
}

uint16_t MQTTSN::find_topic_id(const char* name, uint8_t* index) {
    for (uint8_t i = 0; i < topic_count; ++i) {
        if (strcmp(topic_table[i].name, name) == 0 && topic_table[i].id != 0xffff) {
            *index = i;
            return topic_table[i].id;
        }
    }

    return 0xffff;
}

#ifdef USE_SERIAL
/**
 * parse_stream(buf, len) is called at the end of the function CheckSerial() in _MeshBee.ino.
 * CheckSerial() gets all incoming data from the gateway and call parse_stream with the buffer of data (@buf) and it's size (@len).
 * 
 * 
 **/
void MQTTSN::parse_stream(uint8_t* buf, uint16_t len) {
   
    if(waiting_for_pingresp){
        _pingresp_timer = millis();
    }
    memset(response_buffer, 0, MAX_BUFFER_SIZE);
	memcpy(response_buffer, (const void*)buf, len);
    waiting_for_response = false;
	dispatch();
}
#endif

/**
 * dispatch() is called at the end of the function parse_stream.
 * It calls the appropriate function according to the type of the current buffer (@response_buffer).
 **/
void MQTTSN::dispatch() {
    
    message_header* response_message = (message_header*)response_buffer;
    switch (response_message->type) {
    case ADVERTISE:
        advertise_handler((msg_advertise*)response_buffer);
        break;

    case GWINFO:
        gwinfo_handler((msg_gwinfo*)response_buffer);
        break;

    case CONNACK:
        connack_handler((msg_connack*)response_buffer);
        break;

    case WILLTOPICREQ:
        willtopicreq_handler(response_message);
        break;

    case WILLMSGREQ:
        willmsgreq_handler(response_message);
        break;

    case REGISTER:
        register_handler((msg_register*)response_buffer);
        break;

    case REGACK:
        regack_handler((msg_regack*)response_buffer);
        break;

    case REREGISTER:
        reregister_handler((msg_reregister*)response_buffer);
        break;

    case PUBLISH:
        publish_handler((msg_publish*)response_buffer);
        break;

    case PUBACK:
        puback_handler((msg_puback*)response_buffer);
        break;

    case SUBACK:
        suback_handler((msg_suback*)response_buffer);
        break;

    case UNSUBACK:
        unsuback_handler((msg_unsuback*)response_buffer);
        break;

    case PINGREQ:
        pingreq_handler((msg_pingreq*)response_buffer);
        break;

    case PINGRESP:
        pingresp_handler();
        break;

    case DISCONNECT:
        disconnect_handler((msg_disconnect*)response_buffer);
        break;

    case WILLTOPICRESP:
        willtopicresp_handler((msg_willtopicresp*)response_buffer);
        break;

    case WILLMSGRESP:
        willmsgresp_handler((msg_willmsgresp*)response_buffer);
        break;

    default:
        return;
    }
}

void MQTTSN::send_message() {
    message_header* hdr = reinterpret_cast<message_header*>(message_buffer);

#ifdef USE_SERIAL
	extern void MQTTSN_serial_send(uint8_t* message_buffer, int length);
    MQTTSN_serial_send(message_buffer, hdr->length);
#endif

    if (!waiting_for_response) {
        _response_timer = millis();
        _response_retries = N_RETRY;
    }
    if (!waiting_for_suback) {
        _suback_timer = millis();
        _suback_retries = N_RETRY;
    }
    if (!waiting_for_puback) {
        _puback_timer = millis();
        _puback_retries = N_RETRY;
    }
    /*if (!waiting_for_pingresp) {
        _pingresp_timer = millis();
        _pingresp_retries = N_RETRY;
    }*/
}

void MQTTSN::advertise_handler(const msg_advertise* msg) {
    _gateway_id = msg->gw_id;
}

extern void MQTTSN_gwinfo_handler(const msg_gwinfo* msg);
void MQTTSN::gwinfo_handler(const msg_gwinfo* msg) {
	MQTTSN_gwinfo_handler(msg);
}

extern void MQTTSN_connack_handler(const msg_connack* msg);
void MQTTSN::connack_handler(const msg_connack* msg) {
	_connected = 1;
    MQTTSN_connack_handler(msg);
}

extern void MQTTSN_willtopicreq_handler(const message_header* msg);
void MQTTSN::willtopicreq_handler(const message_header* msg) {
    MQTTSN_willtopicreq_handler(msg);
}

extern void MQTTSN_willmsgreq_handler(const message_header* msg);
void MQTTSN::willmsgreq_handler(const message_header* msg) {
    MQTTSN_willmsgreq_handler(msg);
}

extern void MQTTSN_regack_handler(const msg_regack* msg);
void MQTTSN::regack_handler(const msg_regack* msg) {
    if (msg->return_code == 0 && topic_count < MAX_TOPICS && bswap(msg->message_id) == _message_id) {
        topic_table[topic_count].id = bswap(msg->topic_id);
        ++topic_count;
        MQTTSN_regack_handler(msg);
    }
}

extern void MQTTSN_reregister_handler(const msg_reregister* msg);
void MQTTSN::reregister_handler(const msg_reregister* msg) {
    MQTTSN_reregister_handler(msg);
}

extern void MQTTSN_puback_handler(const msg_puback* msg);
void MQTTSN::puback_handler(const msg_puback* msg) {
    waiting_for_puback=false;
    MQTTSN_puback_handler(msg);
}

#ifdef USE_QOS2
void MQTTSN::pubrec_handler(const msg_pubqos2* msg) {
}

void MQTTSN::pubrel_handler(const msg_pubqos2* msg) {
}

void MQTTSN::pubcomp_handler(const msg_pubqos2* msg) {
}
#endif

void MQTTSN::pingreq_handler(const msg_pingreq* msg) {
    pingresp();
}

extern void MQTTSN_suback_handler(const msg_suback* msg);
void MQTTSN::suback_handler(const msg_suback* msg) {
    waiting_for_suback=false;
    MQTTSN_suback_handler(msg);
}

void MQTTSN::unsuback_handler(const msg_unsuback* msg) {
}

extern void MQTTSN_disconnect_handler(const msg_disconnect* msg);
void MQTTSN::disconnect_handler(const msg_disconnect* msg) {
	_connected = false;
    MQTTSN_disconnect_handler(msg);
}

extern void MQTTSN_pingresp_handler(); 
void MQTTSN::pingresp_handler() {
    waiting_for_pingresp=false;
    MQTTSN_pingresp_handler();
}

extern void MQTTSN_publish_handler(const msg_publish* msg);
void MQTTSN::publish_handler(const msg_publish* msg) {
    //if (msg->flags & FLAG_QOS_1) {
        return_code_t ret = REJECTED_INVALID_TOPIC_ID;
        const uint16_t topic_id = bswap(msg->topic_id);
        for (uint8_t i = 0; i < topic_count; ++i) {
            if (topic_table[i].id == topic_id) {
                ret = ACCEPTED;
				MQTTSN_publish_handler(msg);
                break;
            }
        }

        puback(msg->topic_id, msg->message_id, ret);
        msg=NULL;
    //}
}

void MQTTSN::register_handler(const msg_register* msg) {
    return_code_t ret = REJECTED_INVALID_TOPIC_ID;
    uint8_t index;
    uint16_t topic_id = find_topic_id(msg->topic_name, &index);

    if (topic_id != 0xffff) {
        topic_table[index].id = bswap(msg->topic_id);
        ret = ACCEPTED;
    }

    regack(msg->topic_id, msg->message_id, ret);
}

void MQTTSN::willtopicresp_handler(const msg_willtopicresp* msg) {
}

void MQTTSN::willmsgresp_handler(const msg_willmsgresp* msg) {
}

void MQTTSN::connect(const uint8_t flags, const uint16_t duration, const char* client_id) {
    
    msg_connect* msg = reinterpret_cast<msg_connect*>(message_buffer);

    msg->length = sizeof(msg_connect) + strlen(client_id);
    msg->type = CONNECT;
    msg->flags = flags;
    msg->protocol_id = PROTOCOL_ID;
    msg->duration = bswap(duration);
    strcpy(msg->client_id, client_id);

    send_message();
	_connected = false;
    waiting_for_response = true;
}

void MQTTSN::willtopic(const uint8_t flags, const char* will_topic, const bool update) {
    if (will_topic == NULL) {
        message_header* msg = reinterpret_cast<message_header*>(message_buffer);

        msg->type = update ? WILLTOPICUPD : WILLTOPIC;
        msg->length = sizeof(message_header);
    } else {
        msg_willtopic* msg = reinterpret_cast<msg_willtopic*>(message_buffer);

        msg->type = update ? WILLTOPICUPD : WILLTOPIC;
        msg->flags = flags;
        strcpy(msg->will_topic, will_topic);
    }

    send_message();

    if ((flags & QOS_MASK) == FLAG_QOS_1 || (flags & QOS_MASK) == FLAG_QOS_2) {
        waiting_for_response = true;
    }
}

void MQTTSN::willmsg(const void* will_msg, const uint8_t will_msg_len, const bool update) {
    msg_willmsg* msg = reinterpret_cast<msg_willmsg*>(message_buffer);

    msg->length = sizeof(msg_willmsg) + will_msg_len;
    msg->type = update ? WILLMSGUPD : WILLMSG;
    memcpy(msg->willmsg, will_msg, will_msg_len);

    send_message();
}

void MQTTSN::disconnect(const uint16_t duration) {
    msg_disconnect* msg = reinterpret_cast<msg_disconnect*>(message_buffer);

    msg->length = sizeof(message_header);
    msg->type = DISCONNECT;

    if (duration > 0) {
        msg->length += sizeof(msg_disconnect);
        msg->duration = bswap(duration);
    }

    send_message();
    waiting_for_response = true;
}

bool MQTTSN::register_topic(const char* name) {
    if (!waiting_for_response && topic_count < (MAX_TOPICS - 1)) {
        ++_message_id;

        // Fill in the next table entry, but we only increment the counter to
        // the next topic when we get a REGACK from the broker. So don't issue
        // another REGISTER until we have resolved this one.
        topic_table[topic_count].name = name;
        topic_table[topic_count].id = 0xffff;

        msg_register* msg = reinterpret_cast<msg_register*>(message_buffer);

        msg->length = sizeof(msg_register) + strlen(name);
        msg->type = REGISTER;
        msg->topic_id = 0;
        msg->message_id = bswap(_message_id);
        strcpy(msg->topic_name, name);
        send_message();
        waiting_for_response = true;
        return true;
    }

    return false;
}

void MQTTSN::regack(const uint16_t topic_id, const uint16_t message_id, const return_code_t return_code) {
    msg_regack* msg = reinterpret_cast<msg_regack*>(message_buffer);

    msg->length = sizeof(msg_regack);
    msg->type = REGACK;
    msg->topic_id = bswap(topic_id);
    msg->message_id = bswap(message_id);
    msg->return_code = return_code;

    send_message();
}

void MQTTSN::reregister(const uint16_t topic_id, const uint16_t message_id, const return_code_t return_code) {
    msg_reregister* msg = reinterpret_cast<msg_reregister*>(message_buffer);

    msg->length = sizeof(msg_reregister);
    msg->type = REREGISTER;
    msg->topic_id = bswap(topic_id);
    msg->message_id = bswap(message_id);
    msg->return_code = return_code;

    send_message();
}

void MQTTSN::publish(const uint8_t flags, const uint16_t topic_id, const void* data, const uint8_t data_len) {
    ++_message_id;

    msg_publish* msg = reinterpret_cast<msg_publish*>(message_buffer);

    msg->length = sizeof(msg_publish) + data_len;
    msg->type = PUBLISH;
    msg->flags = flags;
    msg->topic_id = bswap(topic_id);
    msg->message_id = bswap(_message_id);
    memcpy(msg->data, data, data_len);

    send_message();

    //if ((flags & QOS_MASK) == FLAG_QOS_1 || (flags & QOS_MASK) == FLAG_QOS_2) {
        waiting_for_puback = true;
    //}
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

void MQTTSN::puback(const uint16_t topic_id, const uint16_t message_id, const return_code_t return_code) {
    msg_puback* msg = reinterpret_cast<msg_puback*>(message_buffer);

    msg->length = sizeof(msg_puback);
    msg->type = PUBACK;
    msg->topic_id = bswap(topic_id);
    msg->message_id = bswap(message_id);
    msg->return_code = return_code;
    send_message();
}

void MQTTSN::subscribe_by_name(const uint8_t flags, const char* topic_name) {
    ++_message_id;

    msg_subscribe* msg = reinterpret_cast<msg_subscribe*>(message_buffer);

    // The -2 here is because we're unioning a 0-length member (topic_name)
    // with a uint16_t in the msg_subscribe struct.
    msg->length = sizeof(msg_subscribe) + strlen(topic_name) - 2;
    msg->type = SUBSCRIBE;
    msg->flags = (flags & QOS_MASK) | FLAG_TOPIC_NAME;
    msg->message_id = bswap(_message_id);
    strcpy(msg->topic_name, topic_name);

    send_message();

    //if ((flags & QOS_MASK) == FLAG_QOS_1 || (flags & QOS_MASK) == FLAG_QOS_2) {
    waiting_for_suback = true;
    //}
}

void MQTTSN::subscribe_by_id(const uint8_t flags, const uint16_t topic_id) {
    ++_message_id;

    msg_subscribe* msg = reinterpret_cast<msg_subscribe*>(message_buffer);

    msg->length = sizeof(msg_subscribe);
    msg->type = SUBSCRIBE;
    msg->flags = (flags & QOS_MASK) | FLAG_TOPIC_PREDEFINED_ID;
    msg->message_id = bswap(_message_id);
    msg->topic_id = bswap(topic_id);

    send_message();

    if ((flags & QOS_MASK) == FLAG_QOS_1 || (flags & QOS_MASK) == FLAG_QOS_2) {
        waiting_for_response = true;
    }
}

void MQTTSN::unsubscribe_by_name(const uint8_t flags, const char* topic_name) {
    ++_message_id;

    msg_unsubscribe* msg = reinterpret_cast<msg_unsubscribe*>(message_buffer);

    // The -2 here is because we're unioning a 0-length member (topic_name)
    // with a uint16_t in the msg_unsubscribe struct.
    msg->length = sizeof(msg_unsubscribe) + strlen(topic_name) - 2;
    msg->type = UNSUBSCRIBE;
    msg->flags = (flags & QOS_MASK) | FLAG_TOPIC_NAME;
    msg->message_id = bswap(_message_id);
    strcpy(msg->topic_name, topic_name);

    send_message();

    if ((flags & QOS_MASK) == FLAG_QOS_1 || (flags & QOS_MASK) == FLAG_QOS_2) {
        waiting_for_response = true;
    }
}

void MQTTSN::unsubscribe_by_id(const uint8_t flags, const uint16_t topic_id) {
    ++_message_id;

    msg_unsubscribe* msg = reinterpret_cast<msg_unsubscribe*>(message_buffer);

    msg->length = sizeof(msg_unsubscribe);
    msg->type = UNSUBSCRIBE;
    msg->flags = (flags & QOS_MASK) | FLAG_TOPIC_PREDEFINED_ID;
    msg->message_id = bswap(_message_id);
    msg->topic_id = bswap(topic_id);

    send_message();

    if ((flags & QOS_MASK) == FLAG_QOS_1 || (flags & QOS_MASK) == FLAG_QOS_2) {
        waiting_for_response = true;
    }
}

void MQTTSN::pingreq(const char* client_id) {
    msg_pingreq* msg = reinterpret_cast<msg_pingreq*>(message_buffer);
    msg->length = sizeof(msg_pingreq) + strlen(client_id);
    msg->type = PINGREQ;
    strcpy(msg->client_id, client_id);

    send_message();

    _pingresp_timer = millis();
    _pingresp_retries = N_RETRY;
    waiting_for_pingresp = true;
}

void MQTTSN::pingresp() {
    message_header* msg = reinterpret_cast<message_header*>(message_buffer);
    msg->length = sizeof(message_header);
    msg->type = PINGRESP;
    send_message();
}

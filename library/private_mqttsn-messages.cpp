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
 * PRIVATE FUNCTIONS
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

void MQTTSN::regack(const uint16_t topic_id, const uint16_t message_id, const return_code_t return_code) {
    msg_regack* msg = reinterpret_cast<msg_regack*>(MessageBuffer);

    msg->length = sizeof(msg_regack);
    msg->type = REGACK;
    msg->topic_id = bswap(topic_id);
    msg->message_id = bswap(message_id);
    msg->return_code = return_code;

    send_message();
}

void MQTTSN::reregister(const uint16_t topic_id, const uint16_t message_id, const return_code_t return_code) {
    msg_reregister* msg = reinterpret_cast<msg_reregister*>(MessageBuffer);

    msg->length = sizeof(msg_reregister);
    msg->type = REREGISTER;
    msg->topic_id = bswap(topic_id);
    msg->message_id = bswap(message_id);
    msg->return_code = return_code;

    send_message();
}

void MQTTSN::puback(const uint16_t topic_id, const uint16_t message_id, const return_code_t return_code) {
    msg_puback* msg = reinterpret_cast<msg_puback*>(MessageBuffer);

    msg->length = sizeof(msg_puback);
    msg->type = PUBACK;
    msg->topic_id = bswap(topic_id);
    msg->message_id = bswap(message_id);
    msg->return_code = return_code;
    send_message();
}

void MQTTSN::register_handler(const msg_register* message) {

    return_code_t ret = REJECTED_INVALID_TOPIC_ID;
    uint16_t topic_id = MQTTSN_find_topic_id(message->topic_name);

    if (topic_id != 0xffff) {
        TopicTable[index].id = bswap(message->topic_id);
        ret = ACCEPTED;
    }

    regack(message->topic_id, message->message_id, ret);
}

void MQTTSN::willtopicresp_handler(const msg_willtopicresp* msg) {
}

void MQTTSN::willmsgresp_handler(const msg_willmsgresp* msg) {
}

void MQTTSN::unsuback_handler(const msg_unsuback* msg) {
}

void MQTTSN::pingreq_handler(const msg_pingreq* msg) {
    MQTTSN_pingresp();
}

/**
 * @brief MQTTSN::bswap Magic formula (big / little indian?)
 * @param val
 * @return
 */
uint16_t MQTTSN::bswap(const uint16_t value) {
    return (value << 8) | (value >> 8);
}

/**
 * dispatch() is called at the end of the function parse_stream.
 * It calls the appropriate function according to the type of the current buffer (@response_buffer).
 **/
void MQTTSN::dispatch() {

    message_header* response_message = (message_header*)ResponseBuffer;
    switch (response_message->type) {
    case ADVERTISE:
        advertise_handler((msg_advertise*)ResponseBuffer);
        break;

    case GWINFO:
        gwinfo_handler((msg_gwinfo*)ResponseBuffer);
        break;

    case CONNACK:
        connack_handler((msg_connack*)ResponseBuffer);
        break;

    case WILLTOPICREQ:
        willtopicreq_handler(response_message);
        break;

    case WILLMSGREQ:
        willmsgreq_handler(response_message);
        break;

    case REGISTER:
        register_handler((msg_register*)ResponseBuffer);
        break;

    case REGACK:
        regack_handler((msg_regack*)ResponseBuffer);
        break;

    case REREGISTER:
        reregister_handler((msg_reregister*)ResponseBuffer);
        break;

    case PUBLISH:
        publish_handler((msg_publish*)ResponseBuffer);
        break;

    case PUBACK:
        puback_handler((msg_puback*)ResponseBuffer);
        break;

    case SUBACK:
        suback_handler((msg_suback*)ResponseBuffer);
        break;

    case UNSUBACK:
        unsuback_handler((msg_unsuback*)ResponseBuffer);
        break;

    case PINGREQ:
        pingreq_handler((msg_pingreq*)ResponseBuffer);
        break;

    case PINGRESP:
        pingresp_handler();
        break;

    case DISCONNECT:
        disconnect_handler((msg_disconnect*)ResponseBuffer);
        break;

    case WILLTOPICRESP:
        willtopicresp_handler((msg_willtopicresp*)ResponseBuffer);
        break;

    case WILLMSGRESP:
        willmsgresp_handler((msg_willmsgresp*)ResponseBuffer);
        break;

    default:
        return;
    }
}

void MQTTSN::send_message() {
    message_header* hdr = reinterpret_cast<message_header*>(MessageBuffer);

#ifdef USE_SERIAL
    extern void MB_serial_send(uint8_t* MessageBuffer, int length);
    MB_serial_send(MessageBuffer, hdr->length);
#endif

    if (!WaitingForResponse) {
        ResponseTimer = millis();
        ResponseRetries = N_RETRY;
    }
    if (!WaitingForSuback) {
        SubackTimer = millis();
        SubackRetries = N_RETRY;
    }
    if (!WaitingForPuback) {
        PubackTimer = millis();
        PubackRetries = N_RETRY;
    }
    /*if (!waiting_for_pingresp) {
         *        _pingresp_timer = millis();
         *        _pingresp_retries = N_RETRY;
}*/
}

void MQTTSN::advertise_handler(const msg_advertise* msg) {
    GatewayId = msg->gw_id;
}


#ifdef USE_QOS2
void MQTTSN::pubrec_handler(const msg_pubqos2* msg) {
}

void MQTTSN::pubrel_handler(const msg_pubqos2* msg) {
}

void MQTTSN::pubcomp_handler(const msg_pubqos2* msg) {
}
#endif

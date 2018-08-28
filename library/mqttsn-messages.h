/*
mqttsn-messages.h

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

#ifndef __MQTTSN_MESSAGES_H__
#define __MQTTSN_MESSAGES_H__

#include "mqttsn.h"

#define MAX_TOPICS 10
#define MAX_BUFFER_SIZE 66

class MQTTSN {
public:
    MQTTSN();
    virtual ~MQTTSN();

    uint16_t MQTTSN_find_topic_id(const char* name);
    bool MQTTSN_wait_for_response();
    bool MQTTSN_wait_for_suback();
    bool MQTTSN_wait_for_puback();
    bool MQTTSN_wait_for_pingresp();
    bool MQTTSN_connected();

#ifdef USE_SERIAL
    void MQTTSN_parse_stream(uint8_t* buf, uint16_t len);
#endif

    void MQTTSN_searchgw(const uint8_t radius);
    void MQTTSN_connect(const uint8_t flags, const uint16_t duration, const char* client_id);
    void MQTTSN_will_topic(const uint8_t flags, const char* will_topic, const bool update = false);
    void MQTTSN_will_messsage(const void* will_msg, const uint8_t will_msg_len, const bool update = false);
    bool MQTTSN_register_topic(const char* name);
    void MQTTSN_publish(const uint8_t flags, const uint16_t topic_id, const void* data, const uint8_t data_len);

#ifdef USE_QOS2
    void MQTTSN_pubrec();
    void MQTTSN_pubrel();
    void MQTTSN_pubcomp();
#endif

    void MQTTSN_subscribe_by_name(const uint8_t flags, const char* topic_name);
    void MQTTSN_subscribe_by_id(const uint8_t flags, const uint16_t topic_id);
    void MQTTSN_unsubscribe_by_name(const uint8_t flags, const char* topic_name);
    void MQTTSN_unsubscribe_by_id(const uint8_t flags, const uint16_t topic_id);
    void MQTTSN_pingreq(const char* client_id);
    void MQTTSN_pingresp();
    void MQTTSN_disconnect(const uint16_t duration);

protected:
    virtual void advertise_handler(const msg_advertise* msg);
    virtual void gwinfo_handler(const msg_gwinfo* msg);
    virtual void connack_handler(const msg_connack* msg);
    virtual void willtopicreq_handler(const message_header* msg);
    virtual void willmsgreq_handler(const message_header* msg);
    virtual void regack_handler(const msg_regack* msg);
    virtual void reregister_handler(const msg_reregister* msg);
    virtual void publish_handler(const msg_publish* msg);
    virtual void register_handler(const msg_register* msg);
    virtual void puback_handler(const msg_puback* msg);
#ifdef USE_QOS2
    virtual void pubrec_handler(const msg_pubqos2* msg);
    virtual void pubrel_handler(const msg_pubqos2* msg);
    virtual void pubcomp_handler(const msg_pubqos2* msg);
#endif
    virtual void suback_handler(const msg_suback* msg);
    virtual void unsuback_handler(const msg_unsuback* msg);
    virtual void pingreq_handler(const msg_pingreq* msg);
    virtual void pingresp_handler();
    virtual void disconnect_handler(const msg_disconnect* msg);
    virtual void willtopicresp_handler(const msg_willtopicresp* msg);
    virtual void willmsgresp_handler(const msg_willmsgresp* msg);

    void regack(const uint16_t topic_id, const uint16_t message_id, const return_code_t return_code);
    void reregister(const uint16_t topic_id, const uint16_t message_id, const return_code_t return_code);
    void puback(const uint16_t topic_id, const uint16_t message_id, const return_code_t return_code);

private:
    struct topic {
        const char* name;
        uint16_t id;
    };

    void dispatch();
    uint16_t bswap(const uint16_t val);
    void send_message();

    // Set to true when we're waiting for some sort of acknowledgement from the server that will transition our state.
    bool WaitingForResponse;
    bool WaitingForSuback;
    bool WaitingForPuback;
    bool WaitingForPingresp;
    bool Connected;
    uint16_t MessageId;
    uint8_t TopicCount;

    uint8_t MessageBuffer[MAX_BUFFER_SIZE];
    uint8_t ResponseBuffer[MAX_BUFFER_SIZE];
    topic TopicTable[MAX_TOPICS];

    uint8_t GatewayId;
    uint32_t ResponseTimer;
    uint8_t ResponseRetries;

    uint32_t PingrespTimer;
    uint8_t PingrespRetries;

    uint32_t SubackTimer;
    uint8_t SubackRetries;

    uint32_t PubackTimer;
    uint8_t PubackRetries;
};

#endif

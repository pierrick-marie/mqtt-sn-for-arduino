/**
 * BSD 3-Clause Licence
 *
 * Created by Pierrick MARIE on 28/11/2018.
 * Updated by Pierrick MARIE on 20/01/2023
 */

#ifndef __MQTT_SN_H__
#define __MQTT_SN_H__

#define PROTOCOL_ID 0x01

#define FLAG_DUP 0x80
#define FLAG_RETAIN 0x10
#define FLAG_WILL 0x08
#define FLAG_CLEAN 0x04

#define FLAG_QOS_0 0x00
#define FLAG_QOS_1 0x20
#define FLAG_QOS_2 0x40
#define FLAG_QOS_M1 0x60

#define FLAG_TOPIC_NAME 0x00
#define FLAG_TOPIC_PREDEFINED_ID 0x01
#define FLAG_TOPIC_INT_NAME 0x02

#define QOS_MASK (FLAG_QOS_0 | FLAG_QOS_1 | FLAG_QOS_2 | FLAG_QOS_M1)
#define TOPIC_MASK (FLAG_TOPIC_NAME | FLAG_TOPIC_PREDEFINED_ID | FLAG_TOPIC_INT_NAME)

// Recommended values for timers and counters. All timers are in seconds.
#define T_ADV 960
#define N_ADV 3
#define T_SEARCH_GW 5
#define T_GW_INFO 5
#define T_WAIT 360
#define T_RETRY 5
#define PR_RETRY 15
#define N_RETRY 5

#define MAX_BUFFER_SIZE 66
#define API_DATA_LEN  40
#define API_FRAME_LEN  (API_DATA_LEN + 9)

enum ReturnCode {
	ACCEPTED,
	REJECTED_CONGESTION,
	REJECTED_INVALID_topicId,
	REJECTED_NOT_SUPPORTED,
	REJECTED
};

enum MessageType {
	ADVERTISE = 0x00,
	SEARCH_GATEWAY = 0x01,
	GATEWAY_INFO = 0x02,
	CONNECT = 0x04,
	CONN_ACK = 0x05,
	REGISTER = 0xA,
	REG_ACK = 0x0B,
	PUBLISH = 0x0C,
	PUB_ACK = 0x0D,
	SUBSCRIBE = 0x12,
	SUB_ACK = 0x13,
	PING_REQ = 0x16,
	PING_RESP = 0x17,
	DISCONNECT = 0x18,
	RE_REGISTER = 0x1E,
	WILL_TOPIC_REQ = 0x06,
	WILL_TOPIC = 0x07,
	WILL_MSG_REQ = 0x08,
	WILL_MSG = 0x09,
	PUB_COMP = 0x0E,
	PUB_REC = 0x0F,
	PUB_REL = 0x10,
	WILL_TOPIC_UPD = 0x1A,
	WILL_TOPIC_RESP = 0x1B,
	WILL_MSG_UPD = 0x1C,
	WILL_MSG_RESP = 0x1D,
	UNSUBSCRIBE = 0x14,
	UNSUB_ACK = 0x15,
};

typedef struct {
	char name[API_DATA_LEN];
	int id;
} Topic;

struct Header {
	uint8_t length;
	uint8_t type;
};

struct Advertise : public Header {
	uint8_t gatewayId;
	uint16_t duration;
};

struct SearchGateway : public Header {
	uint8_t radius;
};

struct GatewayInfo : public Header {
	uint8_t gatewayId;
	char gatewayAddress[0];
};

struct Connect : public Header {
	uint8_t flags;
	uint8_t protocolId;
	uint16_t duration;
	char clientId[0];
};

struct ConnAck : public Header {
	ReturnCode returnCode;
};

struct RegAck : public Header {
	uint16_t topicId;
	uint16_t messageId;
	uint8_t returnCode;
};

struct Message : public Header {
	uint8_t flags;
	uint16_t topicId;
	uint16_t messageId;
	char data[API_DATA_LEN];
};

struct PubAck : public Header {
	uint16_t topicId;
	uint16_t messageId;
	uint8_t returnCode;
};

struct Subscribe : public Header {
	uint8_t flags;
	uint16_t messageId;
	union {
		char topicName[0];
		uint16_t topicId;
	};
};

struct SubAck : public Header {
	uint8_t flags;
	uint16_t topicId;
	uint16_t messageId;
	uint8_t returnCode;
};

struct PingReq : public Header {
	char clientId[0];
};

struct Disconnect : public Header {
	uint16_t duration;
};

struct Register : public Header {
	uint16_t topicId;
	uint16_t messageId;
	char topicName[0];
};

struct ReRegister : public Header {
	uint16_t topicId;
	uint16_t messageId;
	uint8_t returnCode;
};

struct PubQoS2 : public Header {
	uint16_t messageId;
};

struct WillTopic : public Header {
	uint8_t flags;
	char* willTopic;
};

struct WillMsg : public Header {
	char* willMsg;
};

struct Unsubscribe : public Header {
	uint8_t flags;
	uint16_t messageId;
	union {
		char* topicName;
		uint16_t topicId;
	};
};

struct UnsubAck : public Header {
	uint16_t messageId;
};

struct WillTopicResp : public Header {
	uint8_t returnCode;
};

struct WillMsgResp : public Header {
	uint8_t returnCode;
};

#endif

/**
 * BSD 3-Clause Licence
 *
 * Created by Pierrick MARIE on 28/11/2018.
 */

#ifndef __MQTTSN_H__
#define __MQTTSN_H__

#define PROTOCOL_ID 0x01

// #define FLAG_DUP 0x80
// #define FLAG_RETAIN 0x10
// #define FLAG_WILL 0x08
// #define FLAG_CLEAN 0x04

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
// #define T_ADV 960
// #define N_ADV 3
// #define T_SEARCH_GW 5
// #define T_GW_INFO 5
// #define T_WAIT 360
// #define T_RETRY 5
// #define PR_RETRY 15
// #define N_RETRY 5

#define MAX_BUFFER_SIZE 66
#define API_DATA_LEN  40
#define API_FRAME_LEN  (API_DATA_LEN + 9)

enum ReturnCode {
	ACCEPTED,
	REJECTED_CONGESTION,
	REJECTED_INVALID_TOPIC_ID,
	REJECTED_NOT_SUPPORTED,
	REJECTED
};

enum MessageType {
	ADVERTISE = 0x00,
	SEARCHGW = 0x01,
	GWINFO = 0x02,
	CONNECT = 0x04,
	CONNACK = 0x05,
	REGISTER = 0xA,
	REGACK = 0x0B,
	PUBLISH = 0x0C,
	PUBACK = 0x0D,
	SUBSCRIBE = 0x12,
	SUBACK = 0x13,
	PINGREQ = 0x16,
	PINGRESP = 0x17,
	DISCONNECT = 0x18,
	REREGISTER = 0x1E
	// WILLTOPICREQ = 0x06,
	// WILLTOPIC = 0x07,
	// WILLMSGREQ = 0x08,
	// WILLMSG = 0x09,
	// PUBCOMP = 0x0E,
	// PUBREC = 0x0F,
	// PUBREL = 0x10,
	// WILLTOPICUPD = 0x1A,
	// WILLTOPICRESP = 0x1B,
	// WILLMSGUPD = 0x1C,
	// WILLMSGRESP = 0x1D,
	// UNSUBSCRIBE = 0x14,
	// UNSUBACK = 0x15,
};

typedef struct {
	char name[API_DATA_LEN];
	int id;
} Topic;

struct MessageHeader {
	uint8_t length;
	uint8_t type;
};

struct MsgAdvertise : public MessageHeader {
	uint8_t gatewayId;
	uint16_t duration;
};

struct MsgSearchGateway : public MessageHeader {
	uint8_t radius;
};

struct MsgGwinfo : public MessageHeader {
	uint8_t gatewayId;
	char gatewayAddress[0];
};

struct MsgConnect : public MessageHeader {
	uint8_t flags;
	uint8_t protocolId;
	uint16_t duration;
	char clientId[0];
};

struct MsgConnAck : public MessageHeader {
	ReturnCode returnCode;
};

struct MsgRegAck : public MessageHeader {
	uint16_t topicId;
	uint16_t messageId;
	uint8_t returnCode;
};

struct MsgPublish : public MessageHeader {
	uint8_t flags;
	uint16_t topicId;
	uint16_t messageId;
	char data[API_DATA_LEN];
};

struct MsgPubAck : public MessageHeader {
	uint16_t topicId;
	uint16_t messageId;
	uint8_t returnCode;
};

struct MsgSubscribe : public MessageHeader {
	uint8_t flags;
	uint16_t messageId;
	union {
		char topicName[0];
		uint16_t topicId;
	};
};

struct MsgSubAck : public MessageHeader {
	uint8_t flags;
	uint16_t topicId;
	uint16_t messageId;
	uint8_t returnCode;
};

struct MsgPingReq : public MessageHeader {
	char clientId[0];
};

struct MsgDisconnect : public MessageHeader {
	uint16_t duration;
};

struct MsgRegister : public MessageHeader {
	uint16_t topicId;
	uint16_t messageId;
	char topicName[0];
};

struct MsgReRegister : public MessageHeader {
	uint16_t topicId;
	uint16_t messageId;
	uint8_t returnCode;
};

/**
 * @brief The msg_pubqos2 struct
 *
struct msg_pubqos2 : public MessageHeader {
	uint16_t message_id;
};
*/

/**
 * @brief The msg_willtopic struct
 *
struct msg_willtopic : public MessageHeader {
	uint8_t flags;
	char* will_topic;
};
*/

/**
 * @brief The msg_willmsg struct
 *
struct msg_willmsg : public MessageHeader {
	char* willmsg;
};
*/

/**
 * @brief The msg_unsubscribe struct
 *
struct msg_unsubscribe : public MessageHeader {
	uint8_t flags;
	uint16_t message_id;
	union {
		char* topic_name;
		uint16_t topic_id;
	};
};
*/

/**
 * @brief The msg_unsuback struct
 *
struct msg_unsuback : public MessageHeader {
	uint16_t message_id;
};
*/

/**
 * @brief The msg_willtopicresp struct
 *
struct msg_willtopicresp : public MessageHeader {
	uint8_t return_code;
};
*/

/**
 * @brief The msg_willmsgresp struct
 *
struct msg_willmsgresp : public MessageHeader {
	uint8_t return_code;
};
*/

#endif

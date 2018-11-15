/*
Mqttsn-messages.h

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

#ifndef __Mqttsn_MESSAGES_H__
#define __Mqttsn_MESSAGES_H__

#include <stdint.h>
#include <stdbool.h>
#include <string.h>

#include <SoftwareSerial.h>
#include <Arduino.h>

#include "mqttsn-messages.h"
#include "Logs.h"

#define MAX_TOPICS 10
#define MAX_MESSAGES 5

#define DEFAULT_TOPIC_ID 0xffff

#define API_START_DELIMITER  0x7E

#define QOS_FLAG 0
#define KEEP_ALIVE 60
#define TIME_TO_SLEEP 30 // 30 seconds

#define MAX_TRY 10

#define RADIUS 0

#define GATEWAY_ID 1

#define BAUD_RATE 9600

#define LONG_WAIT 2000 // 2000ms - 2s
#define SHORT_WAIT 500 // 500ms - 0.s

class Mqttsn {

public:

	/**
	 * ****************************
	 *
	 * PUBLIC FUNCTIONS
	 *
	 * ****************************
	 **/

	Mqttsn(SoftwareSerial* _xBee) ;
	~Mqttsn() ;

	bool waitForSubAck();
	bool isConnected();

	void publish(String topic_name, String message);

	int subscribeTopic(String topicName);
	void disconnect();

	/**
	 * @brief ABSTRCT_init The init function searches a gateway with a radius = 0.
	 * @return ACCEPTED if a correct response is received, else REJECTED.
	 **/
	void start() ;

	/**
	 * @brief ABSTRCT_connect The funtion tries to connect the module to the gateway.
	 * @param module_name The name of the module used to make the connection
	 * @return ACCEPTED if a correct response is received, else REJECTED.
	 **/
	void connect(String module_name) ;

	/**
	 * @brief Mqttsn::findTopicId The function search the index of a @topicName within @topicTable list.
	 * @param topicName The name of the topic to search.
	 * @return The index of the topic or -1 if not found.
	 */
	short findTopicId(String name) ;

	const char* findTopicName(const short topicId) ;

	/**
	 * @brief Mqttsn::registerTopic The function asks to the gateway to register a @topic_name.
	 * @param name The topic name to register.
	 *
	 * @return
	 *      -2 if it is not possible to register the @topic_name.
	 *      -1 if the message to register @topic_name is sent.
	 *      >= 0 the id of the @topic_name already registered.f
	 *
	 **/
	int registerTopic(String name) ;

	/**
	 * @brief pingReq send a message to the gateway to request new published messages
	 * @param module_name the id of the client who wants published messages
	 */
	void requestMessages();

	char* getReceivedData(String topicName);

	msg_publish* getReceivedMessages();

	int getNbReceivedMessages();

private:

	/**
	 * ****************************
	 *
	 * PRIVATE FUNCTIONS
	 *
	 * ****************************
	 **/

	bool checkSerial();

	/**
	 * The function waits during one second if data is available. In that case it returns true else returns false.
	 *
	 * Returs:
	 * True if a response is received (xBee.available() > 1), else false.
	 **/
	bool waitData() ;

	/**
	 * The function analyses the incoming data (@FrameBufferIn) and calls the function @Mqttsn.parseStream before cleaning the @FrameBufferIn.
	 **/
	void parseData() ;

	/**
	 * @brief Mqttsn::dispatch The function is called at the end of the function parseStream.
	 * It calls the corresponding function according to the message type.
	 **/
	void dispatch();

	/**
	 * @brief Mqttsn::bitSwap Magic formula (big / little indian?).
	 * @param val A number to swap.
	 * @return The swaped number.
	 **/
	uint16_t bitSwap(const uint16_t val);

	/**
	 * @brief sendMessage send a message to the gateway
	 */
	void sendMessage();

	/**
	 * The function creates a MeshBee frame and returns the frame lenght.
	 *
	 * Arguments:
	 * header_lenght: the lenght of @data
	 *
	 * Returns:
	 * The size of the created frame.
	 **/
	int createFrame(const int header_lenght) ;

	/**
	 * The function verifies the checksum of @frame_buffer according to its @frame_size and returns true if it's OK, else return false.
	 *
	 * Returns:
	 * True if the checksum of @frame_buffer is ok, else false.
	 **/
	bool verifyChecksum(uint8_t frame_buffer[], int frame_size) ;

	/**
	 * @brief gatewayInfoHandler notifies the client with the information of the gateway
	 * @param msg the information
	 */
	void searchGatewayHandler(const msg_gwinfo* msg);

	/**
	 * @brief connackHandler notifies the client a connetion to the gateway is ok
	 * @param msg the notification message
	 */
	void connAckHandler(const msg_connack* msg);

	/**
	 * @brief regAckHandler the gateway notifies the client it have register the topic.
	 * @param msg The notification message.
	 */
	void regAckHandler(const msg_regack* msg);

	void publishHandler(const msg_publish* msg);

	/**
	 * @brief subAckHandler notifies the client a subcription topic have been regisered.
	 * @param msg the notification message.
	 */
	void subAckHandler(const msg_suback* msg);

	/**
	 * @brief pingRespHandler Do nothing.
	 */
	void pingRespHandler();

	void disconnectHandler(const msg_disconnect* msg);

	void resetRegisteredTopicId(const short topicId);

	void reRegisterHandler(const msg_reregister* msg);

	// @TODO not implemented yet
	// void willTopicRespHandler(const msg_willtopicresp* msg);
	// void willMsgRespHandler(const msg_willmsgresp* msg);
	// void willTopic(const uint8_t QOS_FLAGs, const char* will_topic, const bool update = false);
	// void willMesssage(const void* will_msg, const uint8_t will_msg_len, const bool update = false);
	// void willTopicReqHandler(const message_header* msg);
	// void willMsgReqHandler(const message_header* msg);
	// void subscribeByName(const uint8_t flags, const char* topic_name);
	// void subscribeById(const uint8_t flags, const uint16_t topic_id);
	// void unsubscribeByName(const uint8_t flags, const char* topic_name);
	// void unsubscribeById(const uint8_t flags, const uint16_t topic_id);
	// void registerHandler(const msg_register* msg);
	// void regAck(const uint16_t topic_id, const uint16_t message_id, const return_code_t return_code);
	// void unsuback_handler(const msg_unsuback* msg);
	// void reRegister(const uint16_t topic_id, const uint16_t message_id, const return_code_t return_code);
	// void pingReqHandler(const msg_pingreq* msg);
	// void pingResp();
	// void advertiseHandler(const msg_advertise* msg);

	// @TODO not implemented yet - QOS level 1 or 2
	// void pubAckHandler(const msg_puback* msg);
	// void pubAck(const uint16_t topic_id, const uint16_t message_id, const return_code_t return_code);
	// void pubRecHandler(const msg_pubqos2* msg);
	// void pubRelHandler(const msg_pubqos2* msg);
	// void pubCompHandler(const msg_pubqos2* msg);
	// void pubRec();
	// void pubRel();
	// void pubComp();

	/**
	 * ****************************
	 *
	 * ATTRIBUTES
	 *
	 * ****************************
	 **/

	// to print logs
	Logs logs;
	SoftwareSerial* xBee;

	// the status of the connection (first sent message)
	bool initOk = false;
	bool waitingForResponse = false;

	// the code received after a subscribe or register message
	int regAckReturnCode = 0;

	int subAckReturnCode = 0;

	msg_publish receivedMessages[MAX_MESSAGES];
	int nbReceivedMessage;

	int nbRegisteredTopic;
	topic topicTable[MAX_TOPICS];

	short connected;
	int messageId;

	uint8_t messageBuffer[MAX_BUFFER_SIZE];
	uint8_t responseBuffer[MAX_BUFFER_SIZE];

	String moduleName;

	uint8_t gatewayId;
	uint8_t frameId = 0;
	uint8_t frameBufferOut[API_FRAME_LEN] = {0};
	uint8_t frameBufferIn[API_FRAME_LEN] = {0};
	uint8_t gatewayAddress[8] = {0};
};

#endif

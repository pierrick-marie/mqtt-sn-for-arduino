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
#define MAX_BUFFER_SIZE 66

#define DEFAULT_TOPIC_ID 0xffff

#define API_START_DELIMITER  0x7E

#define API_DATA_LEN  40
#define API_PAY_LEN  (API_DATA_LEN + 5)
#define API_FRAME_LEN  (API_DATA_LEN + 9)

#define MAX_MESSAGES 10

#define QOS_FLAG 0
#define KEEP_ALIVE 60

#define MAX_TRY 10

#define RADIUS 0

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

	void publish(const char* topic_name, const String message);

	int subscribeTopic(const char* topic_name);
	void disconnect(const uint16_t duration);

	/**
	 * @brief ABSTRCT_init The init function searches a gateway with a radius = 0.
	 * @return ACCEPTED if a correct response is received, else REJECTED.
	 **/
	int init() ;

	/**
	 * @brief ABSTRCT_connect The funtion tries to connect the module to the gateway.
	 * @param module_name The name of the module used to make the connection
	 * @return ACCEPTED if a correct response is received, else REJECTED.
	 **/
	int connect(const char* module_name) ;

	/**
	 * The function returns the associated string status to corresponding to the given @return_code.
	 **/
	char const* stringFromReturnCode(const uint8_t return_code) ;

	/**
	 * @brief Mqttsn::findTopicId The function search the index of a @topicName within @topicTable list.
	 * @param topicName The name of the topic to search.
	 * @return The index of the topic or -1 if not found.
	 */
	short findTopicId(const char* name) ;

	const char* findTopicName(const short topicId) ;

	/**
	 * @brief Mqttsn::registerTopic The function asks to the gateway to register a @topic_name.
	 * @param name The topic name to register.
	 *
	 * @return
	 *      -2 if it is not possible to register the @topic_name.
	 *      -1 if the message to register @topic_name is sent.
	 *      >= 0 the id of the @topic_name already registered.
	 *
	 **/
	int registerTopic(const char* name) ;

	/**
	 * @brief waitMessage waits a message from subscribed topics
	 */
	void waitMessage();

	/**
	 * @brief pingReq send a message to the gateway to request new published messages
	 * @param module_name the id of the client who wants published messages
	 */
	void pingReq(const char* module_name);

	/**
	 * @brief pingResp Sending a ping response to the gateway. The function is called into pingReqHandler() - a function that seems to be never called.
	 * The message seems to be not interpreted by the gateway.
	 */
	void pingResp();

	short getNbReceivedMessages();

	String getReceivedMessage(const short number);

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

	void publishMessage(const uint8_t flags, const uint16_t topic_id, const void* data, const uint8_t data_len);

	/**
	 * @brief Mqttsn::searchgw The function sends a message to the search the closest gateway arround @radius scale.
	 * @param radius The max hop to search an available gateway.
	 **/
	void searchGateway(const uint8_t radius);

	void subscribeByName(const uint8_t flags, const char* topic_name);
	void subscribeById(const uint8_t flags, const uint16_t topic_id);
	void unsubscribeByName(const uint8_t flags, const char* topic_name);
	void unsubscribeById(const uint8_t flags, const uint16_t topic_id);

	void connect(const uint8_t flags, const uint16_t duration, const char* module_name);

	/**
	 * The function creates a MeshBee frame and returns the frame lenght.
	 *
	 * Arguments:
	 * data: the data used to create the frame
	 * data_lenght: the lenght of @data
	 * destination_address: the address to send the frame
	 * frame: the frame that will be fill with the @data
	 * frame_max_lenght: the maximum lenght of the @frame
	 * broadcast: true if the frame is a broadcast message
	 *
	 * Returns:
	 * The size of the created frame.
	 **/
	int createFrame(const uint8_t* data, const int data_lenght, const uint8_t* destination_address, uint8_t* frame, const int frame_max_lenght, const bool broadcast) ;

	/**
	 * The function verifies if the transmetted message in @FrameBufferIn is a data packet.
	 *
	 * Returns:
	 * True if the message is a packet with data, else false.
	 **/
	bool isDataPacket() ;

	/**
	 * The function verifies if the transmetted message in @FrameBufferIn is a status message.
	 *
	 * Returns:
	 * True if the message is a transmit status, else false.
	 **/
	bool isTransmitStatus() ;

	/**
	 * The function verifies the checksum of @frame_buffer according to its @frame_size and returns true if it's OK, else return false.
	 *
	 * Returns:
	 * True if the checksum of @frame_buffer is ok, else false.
	 **/
	bool verifyChecksum(uint8_t frame_buffer[], int frame_size) ;

	/**
	 * @brief advertiseHandler notifies the client with the gateway id.
	 * @param msg the message from the gateway with it's id.
	 */
	void advertiseHandler(const msg_advertise* msg);

	/**
	 * @brief gatewayInfoHandler notifies the client with the information of the gateway
	 * @param msg the information
	 */
	void gatewayInfoHandler(const msg_gwinfo* msg);

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

	/**
	 * @brief registerHandler the gateway ask the client to register a topic.
	 * @param msg the notification message.
	 */
	void registerHandler(const msg_register* msg);

	/**
	 * @brief reRegisterHandler the gateway ask to re-register a topic.
	 * @param msg the notification message.
	 */
	void reRegisterHandler(const msg_reregister* msg);

	void publishHandler(const msg_publish* msg);

	/**
	 * @brief subAckHandler notifies the client a subcription topic have been regisered.
	 * @param msg the notification message.
	 */
	void subAckHandler(const msg_suback* msg);

	void unsuback_handler(const msg_unsuback* msg);

	/**
	 * @brief pingReqHandler Receive a ping request from the gateway. The function seems to be never used...
	 * @param msg the message sent by the gateway
	 */
	void pingReqHandler(const msg_pingreq* msg);

	/**
	 * @brief pingRespHandler Do nothing.
	 */
	void pingRespHandler();

	void disconnect_handler(const msg_disconnect* msg);

	/**
	 * @brief regAck the client notifies the gateway it have register the topic sent by the gateway.
	 * @param topic_id the topic id.
	 * @param message_id the message that constains the topic name.
	 * @param return_code ACCEPTED | REJECTED.
	 */
	void regAck(const uint16_t topic_id, const uint16_t message_id, const return_code_t return_code);

	/**
	 * The function is never used.
	 *
	 * @brief reRegister the client ask the gateway to re-register a topic
	 * @param topic_id the topic id.
	 * @param message_id the message with the topic name.
	 * @param return_code
	 */
	void reRegister(const uint16_t topic_id, const uint16_t message_id, const return_code_t return_code);

	// @TODO not implemented yet
	// void willTopicRespHandler(const msg_willtopicresp* msg);
	// void willMsgRespHandler(const msg_willmsgresp* msg);
	// void willTopic(const uint8_t QOS_FLAGs, const char* will_topic, const bool update = false);
	// void willMesssage(const void* will_msg, const uint8_t will_msg_len, const bool update = false);
	// void willTopicReqHandler(const message_header* msg);
	// void willMsgReqHandler(const message_header* msg);

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

	// the message to send
	String message = "";
	char** receivedMessages;

	short connected;
	int messageId;

	uint8_t messageBuffer[MAX_BUFFER_SIZE];
	uint8_t responseBuffer[MAX_BUFFER_SIZE];

	short nbRegisteredTopic;
	short nbReceivedMessages;
	topic topicTable[MAX_TOPICS];

	uint8_t gatewayId;
	uint16_t u16TopicPubID;

	uint8_t frameId = 0;
	uint8_t frameBufferOut[API_FRAME_LEN] = {0};
	uint8_t frameBufferIn[API_FRAME_LEN] = {0};
	uint8_t gatewayAddress[8] = {0};
};

#endif

/*
BSD 3-Clause License

Copyright (c) 2018, marie
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice, this
  list of conditions and the following disclaimer.

* Redistributions in binary form must reproduce the above copyright notice,
  this list of conditions and the following disclaimer in the documentation
  and/or other materials provided with the distribution.

* Neither the name of the copyright holder nor the names of its
  contributors may be used to endorse or promote products derived from
  this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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

#define MAX_TOPICS 4
#define MAX_MESSAGES 4

#define DEFAULT_TOPIC_ID 0xffff

#define API_START_DELIMITER  0x7E
#define FRAME_TYPE_TRANSMIT_REQUEST 0x10
#define FRAME_ID_WITHOUT_ACK 0x00
#define BROADCAST_RADIUS_ZERO 0x00
#define OPTION_DISABLE_RETRIES 0x01
#define CHECKSUM_VALUE 0xFF
#define TRANSMIT_STATUS 0x8B
#define MESSAGE_ID 0x00

#define QOS_FLAG 0
#define DURATION_TIME 60 // 60 seconds
#define SLEEP_TIME 10 // 10 ms

#define MAX_TRY 20

#define RADIUS 15

#define GATEWAY_ID 1

#define BAUD_RATE 9600

#define MIN_WAIT 4 // 2 x 100
#define MAX_WAIT 12 // 8 x 100

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

	void publish(const char* topic_name, String message);

	bool subscribeTopic(const char* topicName);

	void disconnect();

	/**
	 * @brief ABSTRCT_init The init function searches a gateway with a radius = 0.
	 * @return ACCEPTED if a correct response is received, else REJECTED.
	 **/
	bool start() ;

	/**
	 * @brief ABSTRCT_connect The funtion tries to connect the module to the gateway.
	 * @param module_name The name of the module used to make the connection
	 * @return ACCEPTED if a correct response is received, else REJECTED.
	 **/
	void connect(const char* module_name) ;

	/**
	 * @brief Mqttsn::findTopicId The function search the index of a @topicName within @topicTable list.
	 * @param topicName The name of the topic to search.
	 * @return The index of the topic or -1 if not found.
	 */
	int findTopicId(const char* topic_name) ;

	const char* findTopicName(int topic_id) ;

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
	bool registerTopic(const char* name) ;

	/**
	 * @brief pingReq send a message to the gateway to request new published messages
	 * @param module_name the id of the client who wants published messages
	 */
	int requestMessages();

	MsgPublish* getReceivedMessages();

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
	uint16_t bitSwap(uint16_t value);

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
	int createFrame(int _headerLenght) ;

	/**
	 * The function verifies the checksum of @frame_buffer according to its @frame_size and returns true if it's OK, else return false.
	 *
	 * Returns:
	 * True if the checksum of @frame_buffer is ok, else false.
	 **/
	bool verifyChecksum(uint8_t _frameBuffer[], int _frameSize) ;

	/**
	 * @brief gatewayInfoHandler notifies the client with the information of the gateway
	 * @param msg the information
	 */
	void searchGatewayHandler(MsgGwinfo* msg);

	/**
	 * @brief connackHandler notifies the client a connetion to the gateway is ok
	 * @param msg the notification message
	 */
	void connAckHandler(MsgConnAck* msg);

	/**
	 * @brief regAckHandler the gateway notifies the client it have register the topic.
	 * @param msg The notification message.
	 */
	void regAckHandler(MsgRegAck* msg);

	void publishHandler(MsgPublish* msg);

	/**
	 * @brief subAckHandler notifies the client a subcription topic have been regisered.
	 * @param msg the notification message.
	 */
	void subAckHandler(MsgSubAck* msg);

	/**
	 * @brief pingRespHandler Do nothing.
	 */
	void pingRespHandler();

	void disconnectHandler(MsgDisconnect* msg);

	void resetRegisteredTopicId(int topicId);

	void reRegisterHandler(MsgReRegister* msg);

	void displayFrameBufferOut();
	void displayFrameBufferIn();

	int getRandomTime();

	// @TODO not implemented yet
	// void willTopicRespHandler(msg_willtopicresp* msg);
	// void willMsgRespHandler(msg_willmsgresp* msg);
	// void willTopic(uint8_t QOS_FLAGs, char* will_topic, bool update = false);
	// void willMesssage(void* will_msg, uint8_t will_msg_len, bool update = false);
	// void willTopicReqHandler(message_header* msg);
	// void willMsgReqHandler(message_header* msg);
	// void subscribeByName(uint8_t flags, char* topic_name);
	// void subscribeById(uint8_t flags, uint16_t topic_id);
	// void unsubscribeByName(uint8_t flags, char* topic_name);
	// void unsubscribeById(uint8_t flags, uint16_t topic_id);
	// void registerHandler(msg_register* msg);
	// void regAck(uint16_t topic_id, uint16_t message_id, return_code_t return_code);
	// void unsuback_handler(msg_unsuback* msg);
	// void reRegister(uint16_t topic_id, uint16_t message_id, return_code_t return_code);
	// void pingReqHandler(msg_pingreq* msg);
	// void pingResp();
	// void advertiseHandler(msg_advertise* msg);

	// @TODO not implemented yet - QOS level 1 or 2
	// void pubAckHandler(msg_puback* msg);
	// void pubAck(uint16_t topic_id, uint16_t message_id, return_code_t return_code);
	// void pubRecHandler(msg_pubqos2* msg);
	// void pubRelHandler(msg_pubqos2* msg);
	// void pubCompHandler(msg_pubqos2* msg);
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
	Logs LOGS;
	SoftwareSerial* XBEE;

	// the status of the connection (first sent message)
	bool SearchGatewayOk = false;
	// bool WaitingForResponse = false;

	// the code received after a subscribe or register message
	int RegAckReturnCode = 0;
	int SubAckReturnCode = 0;

	MsgPublish ReceivedMessages[MAX_MESSAGES];
	int NbReceivedMessage;

	int NbRegisteredTopic;
	Topic TopicTable[MAX_TOPICS];

	int Connected;
	int LastSubscribedTopic;

	uint8_t MessageBuffer[MAX_BUFFER_SIZE];
	uint8_t ResponseBuffer[MAX_BUFFER_SIZE];

	char ModuleName[API_DATA_LEN];

	uint8_t GatewayId;
	uint8_t FrameId = 0;
	uint8_t FrameBufferOut[API_FRAME_LEN] = {0};
	uint8_t FrameBufferIn[API_FRAME_LEN] = {0};
	uint8_t GatewayAddress[8] = {0};
};

#endif

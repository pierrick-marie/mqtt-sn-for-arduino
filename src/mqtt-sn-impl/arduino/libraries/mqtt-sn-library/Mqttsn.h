/**
 * BSD 3-Clause Licence
 *
 * Created by Pierrick MARIE on 28/11/2018.
 * Updated by Pierrick MARIE on 20/01/2023
 */

#ifndef __MQTT_SN_MESSAGES_H__
#define __MQTT_SN_MESSAGES_H__

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

	Mqttsn(SoftwareSerial* xBee) ;
	~Mqttsn() ;

	bool waitForSubAck();
	bool isConnected();

	void publish(const char* topicName, String message);

	bool subscribeTopic(const char* topicName);

	void disconnect();

	/**
	 * @brief ABSTRCT_init The init function searches a gateway with a radius = 0.
	 * @return ACCEPTED if a correct response is received, else REJECTED.
	 **/
	bool start() ;

	/**
	 * @brief ABSTRCT_connect The funtion tries to connect the module to the gateway.
	 * @param moduleName The name of the module used to make the connection
	 * @return ACCEPTED if a correct response is received, else REJECTED.
	 **/
	void connect(const char* moduleName) ;

	/**
	 * @brief Mqttsn::findTopicId The function search the index of a @topicName within @topicTable list.
	 * @param topicName The name of the topic to search.
	 * @return The index of the topic or -1 if not found.
	 */
	int findTopicId(const char* topicName) ;

	const char* findTopicName(int topicId) ;

	/**
	 * @brief Mqttsn::registerTopic The function asks to the gateway to register a @topicName.
	 * @param name The topic name to register.
	 *
	 * @return
	 *      -2 if it is not possible to register the @topicName.
	 *      -1 if the message to register @topicName is sent.
	 *      >= 0 the id of the @topicName already registered.f
	 *
	 **/
	bool registerTopic(const char* name) ;

	/**
	 * @brief pingReq send a message to the gateway to request new published messages
	 * @param moduleName the id of the client who wants published messages
	 */
	int requestMessages();

	Message* getReceivedMessages();

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
	 * The function analyses the incoming data (@frameBufferIn) and calls the function @Mqttsn.parseStream before cleaning the @frameBufferIn.
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
	int createFrame(int headerLenght) ;

	/**
	 * The function verifies the checksum of @frame_buffer according to its @frame_size and returns true if it's OK, else return false.
	 *
	 * Returns:
	 * True if the checksum of @frame_buffer is ok, else false.
	 **/
	bool verifyChecksum(uint8_t frameBuffer[], int frameSize) ;

	/**
	 * @brief gatewayInfoHandler notifies the client with the information of the gateway
	 * @param msg the information
	 */
	void searchGatewayHandler(GatewayInfo* msg);

	/**
	 * @brief connackHandler notifies the client a connetion to the gateway is ok
	 * @param msg the notification message
	 */
	void connAckHandler(ConnAck* msg);

	/**
	 * @brief regAckHandler the gateway notifies the client it have register the topic.
	 * @param msg The notification message.
	 */
	void regAckHandler(RegAck* msg);

	void publishHandler(Message* msg);

	/**
	 * @brief subAckHandler notifies the client a subcription topic have been regisered.
	 * @param msg the notification message.
	 */
	void subAckHandler(SubAck* msg);

	/**
	 * @brief pingRespHandler Do nothing.
	 */
	void pingRespHandler();

	void disconnectHandler(Disconnect* msg);

	void resetRegisteredTopicId(int topicId);

	void reRegisterHandler(ReRegister* msg);

	void displayFrameBufferOut();
	void displayFrameBufferIn();

	int getRandomTime();

	// @TODO not implemented yet
	void willTopicRespHandler(WillTopicResp* msg);
	void willMsgRespHandler(WillMsgResp* msg);
	void willTopic(uint8_t QOS_FLAGs, char* willTopic, bool update = false);
	void willMesssage(void* willMsg, uint8_t willMsgLength, bool update = false);
	void willTopicReqHandler(WillTopic* msg);
	void willMsgReqHandler(WillMsg* msg);
	void subscribeByName(uint8_t flags, char* topicName);
	void subscribeById(uint8_t flags, uint16_t topicId);
	void unsubscribeByName(uint8_t flags, char* topicName);
	void unsubscribeById(uint8_t flags, uint16_t topicId);
	void registerHandler(Register* msg);
	void regAck(uint16_t topicId, uint16_t messageId, ReturnCode returnCode);
	void unsuback_handler(UnsubAck* msg);
	void reRegister(uint16_t topicId, uint16_t messageId, ReturnCode returnCode);
	void pingReqHandler(Advertise* msg);
	void pingResp();
	void advertiseHandler(Advertise* msg);

	// @TODO not implemented yet - QOS level 1 or 2
	void pubAckHandler(PubAck* msg);
	void pubAck(uint16_t topicId, uint16_t messageId, ReturnCode returnCode);
	void pubRecHandler(PubQoS2* msg);
	void pubRelHandler(PubQoS2* msg);
	void pubCompHandler(PubQoS2* msg);
	void pubRec();
	void pubRel();
	void pubComp();

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
	bool searchGatewayOk = false;
	// bool WaitingForResponse = false;

	// the code received after a subscribe or register message
	int regAckReturnCode = 0;
	int subAckReturnCode = 0;

	Message receivedMessages[MAX_MESSAGES];
	int nbReceivedMessage;

	int nbRegisteredTopic;
	Topic topicTable[MAX_TOPICS];

	int connected;
	int lastSubscribedTopic;

	uint8_t messageBuffer[MAX_BUFFER_SIZE];
	uint8_t responseBuffer[MAX_BUFFER_SIZE];

	char moduleName[API_DATA_LEN];

	uint8_t messageId = 0;
	uint8_t gatewayId = 0;

	uint8_t frameBufferOut[API_FRAME_LEN] = {0};
	uint8_t frameBufferIn[API_FRAME_LEN] = {0};
	uint8_t gatewayAddress[8] = {0};
};

#endif

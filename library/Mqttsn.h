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

#define API_DATA_PACKET  0x02
#define API_START_DELIMITER  0x7E
#define OPTION_CAST_MASK  0x40   //option unicast or broadcast MASK
#define OPTION_ACK_MASK  0x80    // option ACK or not MASK

#define API_DATA_LEN  40
#define API_PAY_LEN  (API_DATA_LEN + 5)
#define API_FRAME_LEN  (API_DATA_LEN + 9)

#define FLAG 0
#define KEEP_ALIVE 60
#define _REJECTED 3
#define _ACCEPTED 0

#define TIME_TO_WAIT 2000
#define MAX_TRY 5

class MQTTSN {

public:

	MQTTSN();
	~MQTTSN();

	bool wait_for_response();
	bool wait_for_suback();
	bool wait_for_puback();
	bool wait_for_pingresp();
	bool isConnected();

#ifdef USE_SERIAL
	void parseStream(uint8_t* buf, uint16_t len);
#endif

	void connect(const uint8_t flags, const uint16_t duration, const char* client_id);
	void will_topic(const uint8_t flags, const char* will_topic, const bool update = false);
	void will_messsage(const void* will_msg, const uint8_t will_msg_len, const bool update = false);
	int  register_topic(const char* name);
	void publish(const uint8_t flags, const uint16_t topic_id, const void* data, const uint8_t data_len);

#ifdef USE_QOS2
	void pubrec();
	void pubrel();
	void pubcomp();
#endif

	void subscribe_by_name(const uint8_t flags, const char* topic_name);
	void subscribe_by_id(const uint8_t flags, const uint16_t topic_id);
	void unsubscribe_by_name(const uint8_t flags, const char* topic_name);
	void unsubscribe_by_id(const uint8_t flags, const uint16_t topic_id);
	void pingreq(const char* client_id);
	void pingresp();
	void disconnect(const uint16_t duration);

	/**
	 * @brief ABSTRCT_init The init function searches a gateway with a radius = 0.
	 * @return _ACCEPTED if a correct response is received, else REJECTED.
	 **/
	int init() ;

	/**
	 * @brief ABSTRCT_connect The funtion tries to connect the module to the gateway.
	 * @param module_name The name of the module used to make the connection
	 * @return _ACCEPTED if a correct response is received, else REJECTED.
	 **/
	int connect(const char* moduleName) ;

	bool getInitOk() ;
	void setInitOk(const bool init_ok) ;

	/**
	 * The function returns the associated string status to corresponding to the given @return_code.
	 **/
	char* stringFromReturnCode(const uint8_t return_code) ;

	bool checkSerial() ;

	void setXBee(SoftwareSerial* _xBee) ;

private:

	// prints the logs
	Logs logs;
	SoftwareSerial* xBee;

	void dispatch();
	uint16_t bswap(const uint16_t val);
	void sendMessage();
	short find_topic_id(const char* name);

	/**
	 * @brief multiCheckSerial The function calls @MB_check_serial until @nb_max_try have been reach or a response from the gateway have been received.
	 * @param nb_max_try The maximum number of try @MB_check_serial before the time out.
	 * @return True if a respense is received, else false.
	 **/
	bool multiCheckSerial(const int nb_max_try) ;

	/**
	 * @brief MQTTSN::searchgw The function sends a message to the search the closest gateway arround @radius scale.
	 * @param radius The max hop to search an available gateway.
	 **/
	void searchGateway(const uint8_t radius);










	/**
	 * The function sends the @message_buffer through the xBee module according to its @lenght.
	 **/
	void serialSend(uint8_t* message_buffer, int length) ;

	/**
	 * The function analyses the incoming data (@FrameBufferIn) and calls the function @mqttsn.parseStream before cleaning the @FrameBufferIn.
	 **/
	void parseData() ;

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
	int create_frame(uint8_t* data, int data_lenght, uint8_t* destination_address, uint8_t* frame, int frame_max_lenght, bool broadcast) ;

	/**
	 * The function verifies if the transmetted message in @FrameBufferIn is a data packet.
	 *
	 * Returns:
	 * True if the message is a packet with data, else false.
	 **/
	bool is_data_packet() ;

	/**
	 * The function verifies if the transmetted message in @FrameBufferIn is a status message.
	 *
	 * Returns:
	 * True if the message is a transmit status, else false.
	 **/
	bool is_transmit_status() ;

	/**
	 * The function verifies the checksum of @frame_buffer according to its @frame_size and returns true if it's OK, else return false.
	 *
	 * Returns:
	 * True if the checksum of @frame_buffer is ok, else false.
	 **/
	bool verify_checksum(uint8_t frame_buffer[], int frame_size) ;

	/**
	 * The function waits during one second if data is available. In that case it returns true else returns false.
	 *
	 * Returs:
	 * True if a response is received (xBee.available() > 1), else false.
	 **/
	bool wait_data() ;























	void advertise_handler(const msg_advertise* msg);
	void gatewayInfoHandler(const msg_gwinfo* msg);
	void connack_handler(const msg_connack* msg);
	void willtopicreq_handler(const message_header* msg);
	void willmsgreq_handler(const message_header* msg);
	void regack_handler(const msg_regack* msg);
	void reregister_handler(const msg_reregister* msg);
	void publish_handler(const msg_publish* msg);
	void register_handler(const msg_register* msg);
	void puback_handler(const msg_puback* msg);

#ifdef USE_QOS2
	void pubrec_handler(const msg_pubqos2* msg);
	void pubrel_handler(const msg_pubqos2* msg);
	void pubcomp_handler(const msg_pubqos2* msg);
#endif

	void suback_handler(const msg_suback* msg);
	void unsuback_handler(const msg_unsuback* msg);
	void pingreq_handler(const msg_pingreq* msg);
	void pingresp_handler();
	void disconnect_handler(const msg_disconnect* msg);
	void willtopicresp_handler(const msg_willtopicresp* msg);
	void willmsgresp_handler(const msg_willmsgresp* msg);

	void regack(const uint16_t topic_id, const uint16_t message_id, const return_code_t return_code);
	void reregister(const uint16_t topic_id, const uint16_t message_id, const return_code_t return_code);
	void puback(const uint16_t topic_id, const uint16_t message_id, const return_code_t return_code);























	// the status of the connection (first sent message)
	bool initOk = false;

	// the code received after a connect message
	int connAckReturnCode = 0;

	// the code received after a subscribe or register message
	int regAckReturnCode = 0;

	int subAckReturnCode = 0;

	int pubAckReturnCode = 0;

	// the message to send
	String message = "";

	// Set to true when we're waiting for some sort of acknowledgement from the server that will transition our state.
	bool waitingForResponse;
	bool waitingForSubAck;
	bool waitingForPubAck;
	bool waitingForPingResp;
	bool connected;
	short topicCount;
	int messageId;

	uint8_t messageBuffer[MAX_BUFFER_SIZE];
	uint8_t responseBuffer[MAX_BUFFER_SIZE];
	topic topicTable[MAX_TOPICS];

	uint8_t gatewayId;
	uint32_t responseTimer;
	uint8_t responseRetries;

	uint32_t pingRespTimer;
	uint8_t pingRespRetries;

	uint32_t subAckTimer;
	uint8_t subAckRetries;

	uint32_t pubAckTimer;
	uint8_t pubAckRetries;

	uint8_t frameId = 0;
	uint16_t u16TopicPubID;
	uint8_t u8Counter;
	uint8_t frameBufferOut[API_FRAME_LEN] = {0};
	uint8_t frameBufferIn[API_FRAME_LEN] = {0};
	uint8_t gatewayAddress[8] = {0};
};

#endif

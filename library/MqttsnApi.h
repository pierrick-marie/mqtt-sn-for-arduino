
#ifndef __MQTTSN_API_H__
#define __MQTTSN_API_H__

#include "Logs.h"

class MqttsnApi {
public:
	MqttsnApi();
	~MqttsnApi();

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
	int connect(const char* moduleName) ;

	bool getInitOk() ;
	void setInitOk(const bool init_ok) ;

private:
	// prints the logs
	Logs logs;

	// the status of the connection (first sent message)
	bool initOk = false;

	// the code received after a connect message
	int connack_return_code = 0;

	// the code received after a subscribe or register message
	int regack_return_code = 0;

	int suback_return_code = 0;

	int puback_return_code = 0;

	// the message to send
	String message = "";

	/**
	 * @brief multi_check_serial The function calls @MB_check_serial until @nb_max_try have been reach or a response from the gateway have been received.
	 * @param nb_max_try The maximum number of try @MB_check_serial before the time out.
	 * @return True if a respense is received, else false.
	 **/
	bool multi_check_serial(const int nb_max_try) ;
};

#endif

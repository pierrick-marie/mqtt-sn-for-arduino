#include <Arduino.h>

#include "mqttsn-messages.h"
#include "Mqttsn.h"
#include "Logs.h"
#include "MqttsnApi.h"

#define FLAG 0
#define KEEP_ALIVE 60
#define REJECTED 3
#define ACCEPTED 0

#define TIME_TO_WAIT 2000
#define MAX_TRY 5
#define NB_MAX_DICTIONNARY 10

MqttsnApi::MqttsnApi() {

}

MqttsnApi::~MqttsnApi() {

}

/**
 *
 * ****************************
 * ****************************
 *
 * PRIVATE METHODS
 *
 * ****************************
 * ****************************
 *
 **/

bool MqttsnApi::multi_check_serial(const int nb_max_try) {

	int nb_try = 0;

	logs.debug("MqttsnApi", "multi_check_serial", "check serial iteration: ", nb_try);
	while( !MB_check_serial() && nb_try < nb_max_try ) {
		nb_try++;
		logs.debug("MqttsnApi", "multi_check_serial", "check serial iteration: ", nb_try);
	}

	return nb_try != nb_max_try;
}

/**
 *
 * ****************************
 * ****************************
 *
 * PUBLIC METHODS
 *
 * ****************************
 * ****************************
 *
 **/

int MqttsnApi::init() {

	int nb_try = 0;
	long time = 0;
	int radius = 0;

	logs.debug("MqttsnApi", "init", "first try to search a gateway");
	mqttsn.searchgw(radius);

	while( !MB_check_serial() && nb_try < MAX_TRY) {
		logs.debug("MqttsnApi", "init", "the gateway did not respond, iteration: ", nb_try);
		nb_try++;
		// delay(1000);
		mqttsn.searchgw(radius);
	}

	if( nb_try == MAX_TRY) {
		logs.debug("MqttsnApi", "init", "REJECTED");
		return REJECTED;
	}

	logs.debug("MqttsnApi", "init", "parsing the received data");
	MB_parse_data();

	logs.debug("MqttsnApi", "init", "checking the response from the gateway");
	if(init_ok) {
		return ACCEPTED;
	} else {
		return REJECTED;
	}
}

int MqttsnApi::connect(const char* moduleName) {

	logs.debug("MqttsnApi", "init", "send a connect message");
	mqttsn.connect(FLAG, KEEP_ALIVE, moduleName);

	logs.debug("MqttsnApi", "init", "waiting for a response");
	if( !multi_check_serial(MAX_TRY) ) {
		logs.debug("MqttsnApi", "init", "check serial rejected");
		return REJECTED;
	}

	logs.debug("MqttsnApi", "init", "parsing the received data");
	MB_parse_data();

	logs.debug("MqttsnApi", "init", "the parsed response from the gateway ", connack_return_code);
	return connack_return_code;
}

/**
 *
 * ****************************
 * ****************************
 *
 * GETTERS & SETTERS
 *
 * ****************************
 * ****************************
 *
 **/

bool MqttsnApi::getInitOk() {
	return init_ok;
}

void MqttsnApi::setInitOk(const bool init_ok) {
	initOk = init_ok;
}

#include "Logs.h"

#include <Arduino.h>

#define DEBUG_MESSAGE " # [ DEBUG ] "

Logs::Logs() {}

Logs::~Logs() {}

/**
 * @brief MQTTSN::debug Print a debug @message prefixed with "DEBUG: " text.
 * @param message The message to print.
 */
void Logs::debug(const char* message) {
	if(DEBUG) {
		Serial.print(DEBUG_MESSAGE);
		Serial.println(message);
	}
}

void Logs::debug(const char* message, const int value) {
	if(DEBUG) {
		Serial.print(DEBUG_MESSAGE);
		Serial.print(message);
		Serial.println(value);
	}
}

void Logs::debug(const char* message, const char* value) {
	if(DEBUG) {
		Serial.print(DEBUG_MESSAGE);
		Serial.print(message);
		Serial.println(value);
	}
}


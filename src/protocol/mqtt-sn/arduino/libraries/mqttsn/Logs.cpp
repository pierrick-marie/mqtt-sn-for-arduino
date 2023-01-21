/**
 * BSD 3-Clause Licence
 *
 * Created by Pierrick MARIE on 28/11/2018.
 */

#include "Logs.h"

#include <Arduino.h>

#define DEBUG_MESSAGE "[DEBUG] "

Logs::Logs() {}

Logs::~Logs() {}

void Logs::debug(const char* methodeName, const char* message) {

	Serial.print(DEBUG_MESSAGE);
	Serial.print(methodeName);
	Serial.print(": ");
	Serial.print(message);
}

void Logs::debugln(const char* methodeName, const char* message) {

	debug(methodeName, message);
	Serial.println("");
}

void Logs::debug(const char* methodeName, const char* message, const int value) {

	debug(methodeName, message);
	Serial.print(" ");
	Serial.println(value);
}

void Logs::debug(const char* methodeName, const char* message, const char* value) {

	debug(methodeName, message);
	Serial.print(" ");
	Serial.println(value);
}

void Logs::info(const char* message) {

	Serial.print("\n[MQTT] ");
	Serial.println(message);
}

void Logs::error(const char* message) {

	Serial.print("\n[MQTT-ERR]");
	Serial.println(message);
}

void Logs::connectionLost() {

	error("connection lost");
	info("retry");
}

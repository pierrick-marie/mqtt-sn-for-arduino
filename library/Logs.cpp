#include "Logs.h"

#include <Arduino.h>

#define DEBUG_MESSAGE " # [ DEBUG ] "

Logs::Logs() {}

Logs::~Logs() {}


void Logs::debug(const char* methodeName) {

	Serial.print(DEBUG_MESSAGE);
	Serial.print(methodeName);
	Serial.println("()");
}

void Logs::debug(const char* methodeName, const char* message) {

	Serial.print(DEBUG_MESSAGE);
	Serial.print(methodeName);
	Serial.print("(): ");
	Serial.println(message);
}

void Logs::debug(const char* methodeName, const char* message, const int value) {

	Serial.print(DEBUG_MESSAGE);
	Serial.print(methodeName);
	Serial.print("(): ");
	Serial.print(message);
	Serial.print(" ");
	Serial.println(value);
}

void Logs::debug(const char* methodeName, const char* message, const char* value) {

	Serial.print(DEBUG_MESSAGE);
	Serial.print(methodeName);
	Serial.print("(): ");
	Serial.print(message);
	Serial.print(" ");
	Serial.println(value);
}

void Logs::info(const char* message) {

	Serial.print("\n[MQTT] ");
	Serial.println(message);
}

void Logs::error(const char* message) {

	Serial.print("\n[MQTT] -ERR- ");
	Serial.println(message);
}

void Logs::notConnected() {

	error("not connected: stop");
}

#include "Logs.h"

#include <Arduino.h>

#define DEBUG_MESSAGE " # [ DEBUG ] "

Logs::Logs() {}

Logs::~Logs() {}


void Logs::debug(const char* className, const char* methodeName) {
	if(DEBUG) {
		Serial.print(DEBUG_MESSAGE);
		Serial.print(className);
		Serial.print(".");
		Serial.print(methodeName);
		Serial.println("()");
	}
}

void Logs::debug(const char* className, const char* methodeName, const char* message) {
	if(DEBUG) {
		Serial.print(DEBUG_MESSAGE);
		Serial.print(className);
		Serial.print(".");
		Serial.print(methodeName);
		Serial.print("(): ");
		Serial.println(message);
	}
}

void Logs::debug(const char* className, const char* methodeName, const char* message, const int value) {
	if(DEBUG) {
		Serial.print(DEBUG_MESSAGE);
		Serial.print(className);
		Serial.print(".");
		Serial.print(methodeName);
		Serial.print("(): ");
		Serial.print(message);
		Serial.print(" ");
		Serial.println(value);
	}
}

void Logs::debug(const char* className, const char* methodeName, const char* message, const char* value) {
	if(DEBUG) {
		Serial.print(DEBUG_MESSAGE);
		Serial.print(className);
		Serial.print(".");
		Serial.print(methodeName);
		Serial.print("(): ");
		Serial.print(message);
		Serial.print(" ");
		Serial.println(value);
	}
}

#include "Logs.h"

#include <Arduino.h>

#define ACTIVE_MESSAGE " # [ DEBUG ] "
#define VERBOSE_MESSAGE " # [ VERBOSE ] "

Logs::Logs() {}

Logs::~Logs() {}


void Logs::debug(const log_level level, const char* methodeName) {
	if(LEVEL >= level) {
		if(level == VERBOSE) {
			Serial.print(VERBOSE_MESSAGE);
		} else {
			Serial.print(ACTIVE_MESSAGE);
		}
		Serial.print(methodeName);
		Serial.println("()");
	}
}

void Logs::debug(const log_level level, const char* methodeName, const char* message) {
	if(LEVEL >= level) {
		if(level == VERBOSE) {
			Serial.print(VERBOSE_MESSAGE);
		} else {
			Serial.print(ACTIVE_MESSAGE);
		}
		Serial.print(methodeName);
		Serial.print("(): ");
		Serial.println(message);
	}
}

void Logs::debug(const log_level level, const char* methodeName, const char* message, const int value) {
	if(LEVEL >= level) {
		if(level == VERBOSE) {
			Serial.print(VERBOSE_MESSAGE);
		} else {
			Serial.print(ACTIVE_MESSAGE);
		}
		Serial.print(methodeName);
		Serial.print("(): ");
		Serial.print(message);
		Serial.print(" ");
		Serial.println(value);
	}
}

void Logs::debug(const log_level level, const char* methodeName, const char* message, const char* value) {
	if(LEVEL >= level) {
		if(level == VERBOSE) {
			Serial.print(VERBOSE_MESSAGE);
		} else {
			Serial.print(ACTIVE_MESSAGE);
		}
		Serial.print(methodeName);
		Serial.print("(): ");
		Serial.print(message);
		Serial.print(" ");
		Serial.println(value);
	}
}

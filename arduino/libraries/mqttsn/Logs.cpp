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

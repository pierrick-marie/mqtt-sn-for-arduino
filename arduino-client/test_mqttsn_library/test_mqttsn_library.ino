#include <stdint.h>
#include <stdbool.h>
#include <string.h>

#include <SoftwareSerial.h>

#include <Logs.h>
#include <Mqttsn.h>

SoftwareSerial XBee(5, 4); //objet qui permet d'appeler les méthodes pour envoyer des données via le module XBee

Logs logs ; // objet pour ecrire des logs dans la console
Mqttsn mqttsn(&XBee) ; // objet qui permet d'appeler les methodes de la librairie mqttsn

// SoftwareSerial Rfid(2, 3);

#define MODULE_NAME "Arduino-RFID"

#define TOPIC_SUB "SUB_ObiOne"

#define TOPIC_PUB "PUB_ObiOne"

#define WAIT 5000

String rfidId = "";

String message = "";
int i = 0;

void setup() {

  delay(5000);

  Serial.begin(9600);
  XBee.begin(9600);
  XBee.listen();
  // Rfid.begin(9600);
}

void loop() {

  if (XBee.isListening()) {

    if (mqttsn.init() == ACCEPTED) {
      Serial.println("\nInit OK");

      if (mqttsn.connect(MODULE_NAME) == ACCEPTED) {
        Serial.println("\nConnect OK");

        if (mqttsn.registerTopic(TOPIC_PUB) == ACCEPTED) {
          Serial.println("\n Register ok");

          message += "pub ";
          message += i;
          mqttsn.publish(TOPIC_PUB, message);
          message = "";
          i++;

        } else {
          Serial.println("\n!!! Register KO !!!");
        }

        if (mqttsn.subscribeTopic(TOPIC_SUB) == ACCEPTED) {
          Serial.println("\nSubscribe Ok");

          mqttsn.pingReq(MODULE_NAME);
          for (i = 0; i < mqttsn.getNbReceivedMessages(); i++) {
            Serial.println("\nReceived message:");
            Serial.println(mqttsn.getReceivedMessage(i));
          }
        } else {
          Serial.println("\n!!! Subscribe KO !!!");
        }

      }
      else {
        Serial.println("\n!!! Connect KO !!!");
      }
    }
  } else {
    Serial.println("\n!!! Init KO !!!");
  }

  delay(WAIT);
}

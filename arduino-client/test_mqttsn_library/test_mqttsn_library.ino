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

#define TOPIC_SUB_0 "SUB_ObiOne_0"
#define TOPIC_SUB_1 "SUB_ObiOne_1"

#define TOPIC_PUB_0 "PUB_ObiOne_0"
#define TOPIC_PUB_1 "PUB_ObiOne_1"

#define WAIT 5000

String rfidId = "";

String message = "";
int i = 0;

void setup() {

  delay(WAIT);

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

        /*
        if (mqttsn.registerTopic(TOPIC_PUB_0) == ACCEPTED) {
          Serial.println("\nRegister 0 ok");
        } else {
          Serial.println("\n!!! Register 0 KO !!!");
        }
        */

          message += "pub ";
          message += i;
          mqttsn.publish(TOPIC_PUB_1, message.c_str());
          message = "";
          i++;

        /*
        if (mqttsn.registerTopic(TOPIC_PUB_1) == ACCEPTED) {
          Serial.println("\nRegister 1 ok");

          message += "pub ";
          message += i;
          mqttsn.publish(TOPIC_PUB, message.c_str());
          message = "";
          i++;

        } else {
          Serial.println("\n!!! Register 1 KO !!!");
        }
        */

        /*
        if (mqttsn.subscribeTopic(TOPIC_SUB_0) == ACCEPTED) {
          Serial.println("\nSubscribe 0 Ok");
        } else {
          Serial.println("\n!!! Subscribe 0 KO !!!");
        }

        if (mqttsn.subscribeTopic(TOPIC_SUB_1) == ACCEPTED) {
          Serial.println("\nSubscribe 1 Ok");

          mqttsn.pingReq(MODULE_NAME);
 
          message = mqttsn.getReceivedData(TOPIC_SUB_0);
          while( NULL != message) {
            Serial.print("\nReceived message: ");
            Serial.println(message);
            message = mqttsn.getReceivedData(TOPIC_SUB_0);
          }
          
          message = mqttsn.getReceivedData(TOPIC_SUB_1);
          while( NULL != message) {
            Serial.print("\nReceived message: ");
            Serial.println(message);
            message = mqttsn.getReceivedData(TOPIC_SUB_1);
          }
       
        } else {
          Serial.println("\n!!! Subscribe 1 KO !!!");
        }
        */
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

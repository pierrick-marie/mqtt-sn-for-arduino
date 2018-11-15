#include <stdint.h>
#include <stdbool.h>
#include <string.h>

#include <Logs.h>
#include <Mqttsn.h>

#include <SoftwareSerial.h>

#define MODULE_NAME "Arduino-RFID"

#define TOPIC_SUB_0 "SUB_ObiOne_0"
#define TOPIC_SUB_1 "SUB_ObiOne_1"

#define TOPIC_PUB_0 "PUB_ObiOne_0"
#define TOPIC_PUB_1 "PUB_ObiOne_1"

#define WAIT 5000

String rfidId = "";
String message = "";
int i = 0;

// SoftwareSerial Rfid(2, 3);

Logs logs ; // objet pour ecrire des logs dans la console
SoftwareSerial XBee(5, 4); //objet qui permet d'appeler les méthodes pour envoyer des données via le module XBee
Mqttsn mqttsn(&XBee) ; // objet qui permet d'appeler les methodes de la librairie mqttsn

void setup() {

  delay(WAIT);

  Serial.begin(9600);

  // Rfid.begin(9600);

  mqttsn.start();
}

void loop() {

  mqttsn.connect(MODULE_NAME);

  /*
  if (mqttsn.registerTopic(TOPIC_PUB_0) == ACCEPTED) {
    Serial.println("\nRegister 0 ok");
  } else {
    Serial.println("\n!!! Register 0 KO !!!");
  }
  */

  message += "pub ";
  message += i;
  mqttsn.publish(TOPIC_PUB_1, message);
  message = "";
  i++;

  /*
  if (mqttsn.subscribeTopic(TOPIC_SUB_1) == ACCEPTED) {
    Serial.println("\nSubscribe 1 Ok");

    mqttsn.requestMessages();

    int nbReceivedMessages = mqttsn.getNbReceivedMessages();
    msg_publish* msg = mqttsn.getReceivedMessages();
    while (nbReceivedMessages > 0) {
      Serial.print("RECEIVED MESSAGE ");
      Serial.print(nbReceivedMessages);
      Serial.print(": ");
      Serial.println(msg[nbReceivedMessages].data);
      nbReceivedMessages--;
    }
  } else {
    Serial.println("\n!!! Subscribe 1 KO !!!");
  }
  */

  mqttsn.disconnect();
}

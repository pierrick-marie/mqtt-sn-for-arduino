#include <Mqttsn.h>

#define MODULE_NAME "Arduino-RFID"

#define TOPIC_SUB_0 "SUB_ObiOne_0"
#define TOPIC_SUB_1 "SUB_ObiOne_1"

#define TOPIC_PUB_0 "PUB_ObiOne_0"
#define TOPIC_PUB_1 "PUB_ObiOne_1"

String message = "";
int nbReceivedMessages = 0;
int i = 0;

Logs logs ; // objet pour ecrire des logs dans la console
SoftwareSerial XBee(5, 4); //objet qui permet d'appeler les méthodes pour envoyer des données via le module XBee
Mqttsn mqttsn(&XBee) ; // objet qui permet d'appeler les methodes de la librairie mqttsn

void setup() {

  Serial.begin(9600);

  mqttsn.start();
}

void loop() {

  mqttsn.connect(MODULE_NAME);

  message += "pub ";
  message += i;
  mqttsn.publish(TOPIC_PUB_1, message);
  message = "";
  i++;

  if (mqttsn.subscribeTopic(TOPIC_SUB_1)) {

    nbReceivedMessages = mqttsn.requestMessages();

    msg_publish* msg = mqttsn.getReceivedMessages();
    
    while (nbReceivedMessages > 0) {
      nbReceivedMessages--;
      Serial.print("RECEIVED MESSAGE ");
      Serial.print(nbReceivedMessages);
      Serial.print(": ");
      Serial.println(msg[nbReceivedMessages].data);
    }
    
  } else {
    Serial.println("\n!!! Subscribe 1 KO !!!");
  }

  mqttsn.disconnect();
}

/**
   Il envoie sur le topic "TOPIC_PUB" l'identifiant du tag à chaque fois qu'il est approché du lecteur.
   Dans l'exemple le programme est abonné au topic "TOPIC_SUB" et affiche régulièrement les messages reçus sur ce topic.
 **/

#include <Mqttsn.h>

#define MODULE_NAME "Arduino-6"
#define TOPIC_SUB "TOPIC_TEST-6"
#define TOPIC_PUB "TOPIC_TEST-6"
#define MESSAGE "MESSAGE-6"

int nbReceivedMessages = 0;

Logs logs; 	                // objet pour ecrire des logs dans la console
SoftwareSerial XBee(5, 4); 	// objet qui permet d'appeler les méthodes pour envoyer des données via le module XBee
Mqttsn mqttsn(&XBee); 	    // objet qui permet d'appeler les methodes de la librairie mqttsn

void setup() {

  Serial.begin(9600);

  mqttsn.start();

}

void loop() {

  mqttsn.connect(MODULE_NAME);

  if (mqttsn.subscribeTopic(TOPIC_SUB)) {

    nbReceivedMessages = mqttsn.requestMessages();

    Serial.print("Nb messages recu ");
    Serial.println(nbReceivedMessages);

    MsgPublish* msg = mqttsn.getReceivedMessages();

    while (nbReceivedMessages > 0) {
      nbReceivedMessages--;
      Serial.print("Received message ");
      Serial.print(nbReceivedMessages);
      Serial.print(" = ");
      Serial.println(msg[nbReceivedMessages].data);
    }
  } else {
    Serial.println("\n!!! Subscribe KO !!!");
  }

  mqttsn.publish(TOPIC_SUB, MESSAGE);

  mqttsn.disconnect();

}

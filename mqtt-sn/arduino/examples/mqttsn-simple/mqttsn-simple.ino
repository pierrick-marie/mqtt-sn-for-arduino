/**
 * Il envoie sur le topic "TOPIC_PUB" l'identifiant du tag à chaque fois qu'il est approché du lecteur.
 * Dans l'exemple le programme est abonné au topic "TOPIC_SUB" et affiche régulièrement les messages reçus sur ce topic.
 **/

#include <Mqttsn.h>

#define MODULE_NAME "Arduino"

#define TOPIC_SUB "TOPIC_SUB"

#define TOPIC_PUB "TOPIC_PUB"

String rfid = "";
int nbReceivedMessages = 0;
long time = 0;

Logs logs; 	                // objet pour ecrire des logs dans la console
SoftwareSerial XBee(5, 4); 	//objet qui permet d'appeler les méthodes pour envoyer des données via le module XBee
Mqttsn mqttsn(&XBee); 	    // objet qui permet d'appeler les methodes de la librairie mqttsn

void setup() {

  Serial.begin(9600);

  mqttsn.start();
}

void loop() {

/*
  Serial.println("Connect");

  mqttsn.connect(MODULE_NAME);

  if (mqttsn.subscribeTopic(TOPIC_SUB)) {

    Serial.println("Get messages");

    nbReceivedMessages = mqttsn.requestMessages();

    Serial.print(nbReceivedMessages);
    Serial.println(" messages received");

    msg_publish* msg = mqttsn.getReceivedMessages();

    while (nbReceivedMessages > 0) {
      nbReceivedMessages--;
      Serial.print("Received messages: ");
      Serial.print(nbReceivedMessages);
      Serial.print(": ");
      Serial.println(msg[nbReceivedMessages].data);
    }
  } else {
    Serial.println("\n!!! Subscribe KO !!!");
  }

  Serial.println("Publish messages");
  mqttsn.publish(TOPIC_PUB, "rfid");

  Serial.println("Disconnect");

  mqttsn.disconnect();

  */

}

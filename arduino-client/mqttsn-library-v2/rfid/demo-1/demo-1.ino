#include <Mqttsn.h>

#define MODULE_NAME "Arduino-RFID_1"

#define TOPIC_SUB "RFID_1"

#define TOPIC_PUB "RFID_2"

String rfid = "";
int nbReceivedMessages = 0;
long time = 0;

Logs logs ; // objet pour ecrire des logs dans la console
SoftwareSerial XBee(5, 4); //objet qui permet d'appeler les méthodes pour envoyer des données via le module XBee
Mqttsn mqttsn(&XBee) ; // objet qui permet d'appeler les methodes de la librairie mqttsn

SoftwareSerial Rfid(2, 3);

void setup() {

  Serial.begin(9600);
  Rfid.begin(9600);

  mqttsn.start();
}

void loop() {

  mqttsn.connect(MODULE_NAME);

  if (mqttsn.subscribeTopic(TOPIC_SUB)) {

    nbReceivedMessages = mqttsn.requestMessages();

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

  time = millis();
  Rfid.listen();
  if (Rfid.isListening()) {

    Serial.println("Waiting RFID");
    while (!Rfid.available() && time + 10000 >= millis()) {
      delay(100);
    }

    if (Rfid.available()) {
      rfid = Rfid.readString();
      rfid.remove(rfid.length() - 1);
      Serial.print("Publish rfid: ");
      Serial.println(rfid);

      mqttsn.publish(TOPIC_PUB, rfid);
    }
  }

  mqttsn.disconnect();

}

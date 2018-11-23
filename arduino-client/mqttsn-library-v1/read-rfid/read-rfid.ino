#include <stdint.h>
#include <stdbool.h>
#include <string.h>
#include <mqttsn-messages.h>
#include <SoftwareSerial.h>

MQTTSN mqttsn; //objet qui permet d'appeler les methodes de la librairie mqttsn
SoftwareSerial XBee(5, 4); //objet qui permet d'appeler les méthodes pour envoyer des données via le module XBee

#define TOPIC_PUB "ObiOne-RFID-2"
#define TOPIC_SUB "ObiOne-RFID"
#define MODULE_NAME "Arduin-ObiOne-RFID-2"

String rfidId = "";

void setup() {
  Serial.begin(9600);
  XBee.begin(9600);
  XBee.listen();
  if(sn_init() == ACCEPTED){
    Serial.println("Sn_init Ok");
  }
}

void loop() {

  if(sn_connect(MODULE_NAME) == ACCEPTED){
      Serial.println("Sn_connect Sub Ok");
      
      if(!is_topic_registered(TOPIC_SUB)){
        if(sn_subscribe(TOPIC_SUB) == ACCEPTED){
          Serial.println("Sn_subscribe Ok");
        }
      }

      String received = sn_check_subscribed_topic_for_message();
      if(received != ""){
        Serial.print("Message: ");
        Serial.println(received);
      }
  }

  delay(10000);
}


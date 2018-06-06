#include <stdint.h>
#include <stdbool.h>
#include <string.h>
#include <mqttsn-messages.h>
#include <SoftwareSerial.h>
//#include "config.h"
//#include "decl.h"

MQTTSN mqttsn; //objet qui permet d'appeler les methodes de la librairie mqttsn
SoftwareSerial XBee(5, 4); //objet qui permet d'appeler les méthodes pour envoyer des données via le module XBee

#define TOPIC_PUB "t2"
#define TOPIC_SUB "t1"
#define MODULE_NAME "Arduino_Test"

void setup() {
  Serial.begin(9600);
  XBee.begin(9600);
  if(sn_init() == ACCEPTED){
    Serial.println("Sn_init Ok");
  }
}

void loop() {
  if(sn_connect(MODULE_NAME) == ACCEPTED){
    Serial.println("Sn_connect Ok");
    if(!is_topic_registered(TOPIC_PUB)){
      if(sn_register(TOPIC_PUB) == ACCEPTED){
        Serial.println("Sn_register Ok");
      }
    }
    if(!is_topic_registered(TOPIC_SUB)){
      if(sn_subscribe(TOPIC_SUB) == ACCEPTED){
        Serial.println("Sn_subscribe Ok");
      }
    }
    String received = sn_check_subscribed_topic_for_message();
    if(received !=""){
    Serial.print("Message: ");
    Serial.println(received);
    }
    if(sn_publish("toto", TOPIC_PUB) == ACCEPTED){
      Serial.println("Sn_publish Ok");
      sn_disconnect();
    }
  }
  delay(10000);
}


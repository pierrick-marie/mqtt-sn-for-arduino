#include <stdint.h>
#include <stdbool.h>
#include <string.h>
#include <Logs.h>
#include <Mqttsn.h>
#include <MqttsnApi.h>
#include <SoftwareSerial.h>

Logs logs; // objet pour ecrire des logs dans la console
MQTTSN mqttsn; // objet qui permet d'appeler les methodes de la librairie mqttsn
MqttsnApi api; // API de manipulation du protocole MQTT-SN

SoftwareSerial XBee(5, 4); //objet qui permet d'appeler les méthodes pour envoyer des données via le module XBee

SoftwareSerial Rfid(2, 3);

#define TOPIC_PUB "ObiOne-RFID"
#define TOPIC_SUB "ObiPop"
#define MODULE_NAME "Arduino-RFID"

String rfidId = "";
long time = millis();

void setup() {

<<<<<<< HEAD
  delay(500);
=======
  delay(1000);
>>>>>>> d3d4d8dbd37a9f86fe94a54d2b26f800e173be7f
  
  Serial.begin(9600);
  XBee.begin(9600);
  // Rfid.begin(9600);
  XBee.listen();
<<<<<<< HEAD
  // if(ABSTRCT_init() == ACCEPTED){
  if(api.init() == ACCEPTED){
    Serial.println("\nINIT OK");
  } else {
    Serial.println("\nINIT KO!");
    // delay(500);
    // exit(-1);
=======
  if(sn_init() == ACCEPTED){
    Serial.println("Sn_init Ok");
  } else {
    Serial.println("Sn_init KO");
>>>>>>> d3d4d8dbd37a9f86fe94a54d2b26f800e173be7f
  }

/*
  // if(time + 10000 <= millis()) {
      XBee.listen();
      if(XBee.isListening()){
        if(ABSTRCT_connect(MODULE_NAME) == ACCEPTED){
          Serial.println("\nCONNECT OK");
        }
        else {
          Serial.println("\nCONNECT KO!");
        }
      }
    // }

/*

    // if(!ABSTRCT_is_topic_registered(TOPIC_SUB)){
    //  Serial.println("TOPIC NOT REGISTERED");
      if(ABSTRCT_subscribe(TOPIC_SUB) == ACCEPTED){
        Serial.println("\nSn_subscribe Ok - TOPIC IS REGISTERED");
      } else {
        Serial.println("\nTOPIC NOT REGISTERED");
      }

      if(ABSTRCT_subscribe("POW POW POW") == ACCEPTED){
        Serial.println("\nSn_subscribe Ok - TOPIC IS REGISTERED");
      } else {
        Serial.println("\nTOPIC NOT REGISTERED");
      }
    /*  
    } else {
      Serial.println("TOPIC REGISTERED");
    }
    */
}

void loop() {

<<<<<<< HEAD
    

/*
=======
    /*
    if(time + 10000 <= millis()) {
      XBee.listen();
      if(XBee.isListening()){
        if(sn_connect(MODULE_NAME) == ACCEPTED){
          Serial.println("Sn_connect Sub Ok");
          
          if(!is_topic_registered(TOPIC_SUB)){
            if(sn_subscribe(TOPIC_SUB) == ACCEPTED){
              Serial.println("Sn_subscribe Ok");
            }
          }

>>>>>>> d3d4d8dbd37a9f86fe94a54d2b26f800e173be7f
          Serial.println("# GET MESSAGES 0");
          String received = sn_get_message_from_subscribed_topics();
          Serial.println("# GET MESSAGES 1");
          
          time = millis();
          if(received != ""){
            Serial.print("Message: ");
            Serial.println(received);
          }
        }
      }
    }
    */

    /* 
    Rfid.listen();
    if(Rfid.isListening()) {
       if (Rfid.available()) {
  
        rfidId = Rfid.readString();
        rfidId.remove(rfidId.length() - 1);
        Serial.println(rfidId);
  
        XBee.listen();
        if(XBee.isListening()){
          if(sn_connect(MODULE_NAME) == ACCEPTED){
            Serial.println("Sn_connect Pub Ok");
            
            if(!is_topic_registered(TOPIC_PUB)){
              if(sn_register(TOPIC_PUB) == ACCEPTED){
                Serial.println("Sn_register Ok");
              }
            }
      
            if(sn_publish(rfidId, TOPIC_PUB) == ACCEPTED){
              Serial.println("Sn_publish Ok");
              sn_disconnect();
            }
          }
        }
      }
    }
    */
}

#include <stdint.h>
#include <stdbool.h>
#include <string.h>
#include <mqttsn-messages.h>
#include <SoftwareSerial.h>

MQTTSN mqttsn; //objet qui permet d'appeler les methodes de la librairie mqttsn
SoftwareSerial XBee(5, 4); //objet qui permet d'appeler les méthodes pour envoyer des données via le module XBee

SoftwareSerial Rfid(2, 3);

#define TOPIC_PUB "ObiOne-RFID"
#define TOPIC_SUB "ObiPop"
#define MODULE_NAME "Arduin-ObiOne-RFID"

String rfidId = "";
long time = millis();

void setup() {
  Serial.begin(9600);
  XBee.begin(9600);
  Rfid.begin(9600);
  XBee.listen();
  if(sn_init() == ACCEPTED){
    Serial.println("Sn_init Ok");
  }
}

void loop() {

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
    
          String received = sn_check_subscribed_topic_for_message();
          time = millis();
          if(received != ""){
            Serial.print("Message: ");
            Serial.println(received);
          }
        }
      }
    }
  
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
}


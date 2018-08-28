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
#define MODULE_NAME "Arduino-RFID"

String rfidId = "";
long time = millis();

void setup() {

  delay(500);
  
  Serial.begin(9600);
  XBee.begin(9600);
  Rfid.begin(9600);
  XBee.listen();
  if(ABSTRCT_init() == ACCEPTED){
    Serial.println("Sn_init Ok");
  } else {
    Serial.println("Sn_init KO");
  }


  // if(time + 10000 <= millis()) {
      XBee.listen();
      if(XBee.isListening()){
        if(ABSTRCT_connect(MODULE_NAME) == ACCEPTED){
          Serial.println("Sn_connect Sub Ok");
        }
        else {
          Serial.println("Sn_connect Sub KO!");
        }
      }
    // }

    // if(!ABSTRCT_is_topic_registered(TOPIC_SUB)){
    //  Serial.println("TOPIC NOT REGISTERED");
      if(ABSTRCT_subscribe(TOPIC_SUB) == ACCEPTED){
        Serial.println("Sn_subscribe Ok");
      } else {
        Serial.println("TOPIC NOT REGISTERED");
      }
    /*  
    } else {
      Serial.println("TOPIC REGISTERED");
    }
    */
}

void loop() {

    

/*
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

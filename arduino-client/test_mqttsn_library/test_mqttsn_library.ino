#include <stdint.h>
#include <stdbool.h>
#include <string.h>

#include <SoftwareSerial.h>

#include <Logs.h>
#include <Mqttsn.h>

SoftwareSerial XBee(5, 4); //objet qui permet d'appeler les méthodes pour envoyer des données via le module XBee

Logs logs ; // objet pour ecrire des logs dans la console
Mqttsn mqttsn(&XBee) ; // objet qui permet d'appeler les methodes de la librairie mqttsn

// SoftwareSerial Rfid(2, 3);

#define TOPIC_SUB "SUB_ObiOne-RFID"

#define TOPIC_PUB_0 "PUB_ObiPop_0"
#define TOPIC_PUB_1 "PUB_ObiPop_1"
#define TOPIC_PUB_2 "PUB_ObiPop_2"

#define MODULE_NAME "Arduino-RFID"

String rfidId = "";
long time = millis();

void setup() {

  delay(500);

  Serial.begin(9600);
  XBee.begin(9600);
  XBee.listen();
  // Rfid.begin(9600);

  if (mqttsn.init() == ACCEPTED) {
    Serial.println("\nINIT OK");

    if (XBee.isListening()) {

      if (mqttsn.connect(MODULE_NAME) == ACCEPTED) {
        Serial.println("\nCONNECT OK");

        /*
        if (-1 == mqttsn.findTopicId(TOPIC_PUB_0)) {
          Serial.println("\nNOT REGISTERED");

          if (ACCEPTED == mqttsn.registerTopic(TOPIC_PUB_0)) {
            Serial.print("topic ");
            Serial.print(TOPIC_PUB_0);
            Serial.print(" REGISTERED ");
            Serial.print("id ");
            Serial.print(mqttsn.findTopicId(TOPIC_PUB_0));
          }

          if (ACCEPTED == mqttsn.registerTopic(TOPIC_PUB_1)) {
            Serial.print("topic ");
            Serial.print(TOPIC_PUB_1);
            Serial.print(" REGISTERED ");
            Serial.print("id ");
            Serial.print(mqttsn.findTopicId(TOPIC_PUB_1));
          }

          if (ACCEPTED == mqttsn.publish(TOPIC_PUB_0, "coucou 0")) {
            Serial.println("\ncoucou 0 PUBLISHED");
          } else {
            Serial.println("\ncoucou 0 NOT PUBLISHED");
          }

          if (ACCEPTED == mqttsn.publish(TOPIC_PUB_1, "coucou 1")) {
            Serial.println("\ncoucou 1 PUBLISHED");
          } else {
            Serial.println("\ncoucou 1 NOT PUBLISHED");
          }

          if (ACCEPTED == mqttsn.publish(TOPIC_PUB_2, "coucou 2")) {
            Serial.println("\ncoucou 2 PUBLISHED");
          } else {
            Serial.println("\ncoucou 2 NOT PUBLISHED");
          }
          Serial.println(mqttsn.findTopicName(2));
          */


          /*
                  if (ACCEPTED == mqttsn.publish(TOPIC_PUB_0, "coucou 2")) {
                    Serial.println("\nPUBLISHED");
                  } else {
                    Serial.println("\nNOT PUBLISHED");
                  }
          */

          /*
            if (ACCEPTED == mqttsn.publish("TOPIC_PUB_0", "coucou")) {
            Serial.println("\nPUBLISHED");
            } else {
            Serial.println("\nNOT PUBLISHED");
            }
            Serial.println(mqttsn.findTopicId("TOPIC_PUB_0"));
          */

        /*
        }
        */
          if (mqttsn.subscribe(TOPIC_SUB) == ACCEPTED) {
            Serial.println("\nSubscribe Ok");
          } else {
            Serial.println("\nSubscribe NOT ok");
          }

          delay(2000);
          
          mqttsn.pingReq(MODULE_NAME);

          while(1);

      }
      else {
        Serial.println("\nCONNECT KO!");
      }
    }
  } else {
    Serial.println("\nINIT KO!");
  }


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

#include <Mqttsn.h>

#define MODULE_NAME "SIMPLE_DEMO"

#define TOPIC_SUB_1 "ARDUINO_SUB_1"
#define TOPIC_SUB_2 "ARDUINO_SUB_2"
#define TOPIC_SUB_3 "ARDUINO_SUB_3"
#define TOPIC_SUB_4 "ARDUINO_SUB_4"

#define TOPIC_PUB_1 "ARDUINO_PUB_1"
#define TOPIC_PUB_2 "ARDUINO_PUB_2"
#define TOPIC_PUB_3 "ARDUINO_PUB_3"
#define TOPIC_PUB_4 "ARDUINO_PUB_4"

int nbReceivedMessages = 0;
int i = 0;
msg_publish* messages;

Logs logs ;   

SoftwareSerial XBee(5, 4); 

Mqttsn mqttsn(&XBee) ;     

void setup() {

  Serial.begin(9600);

  mqttsn.start();
}

void loop() {

  mqttsn.connect(MODULE_NAME);

  mqttsn.subscribeTopic(TOPIC_SUB_1);
  mqttsn.subscribeTopic(TOPIC_SUB_2);
  mqttsn.subscribeTopic(TOPIC_SUB_3);
  mqttsn.subscribeTopic(TOPIC_SUB_4);

  mqttsn.registerTopic(TOPIC_PUB_1);
  mqttsn.registerTopic(TOPIC_PUB_2);
  mqttsn.registerTopic(TOPIC_PUB_3);
  mqttsn.registerTopic(TOPIC_PUB_4);

  mqttsn.disconnect();


  delay(100000);

  /*
  

  mqttsn.subscribeTopic(TOPIC_SUB_2);
    
  nbReceivedMessages = mqttsn.requestMessages();
  messages = mqttsn.getReceivedMessages();
  
  for(i = 0; i < nbReceivedMessages; i++) {
    Serial.println(messages[i].data);
  }

  mqttsn.publish(TOPIC_SUB_2, "Coucou ARDUINO");
  */

  

}

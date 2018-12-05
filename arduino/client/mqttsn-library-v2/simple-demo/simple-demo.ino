#include <Mqttsn.h>

#define MODULE_NAME "SIMPLE_DEMO"

#define TOPIC_SUB_1 "ARDUINO_SUB_1"
#define TOPIC_SUB_2 "ARDUINO_SUB_2"

#define TOPIC_PUB "ARDUINO_PUB"   

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
    
  nbReceivedMessages = mqttsn.requestMessages();
  messages = mqttsn.getReceivedMessages();
  
  for(i = 0; i < nbReceivedMessages; i++) {
    Serial.println(messages[i].data);
  }

  mqttsn.publish(TOPIC_SUB_2, "Coucou ARDUINO");

  mqttsn.disconnect();

}

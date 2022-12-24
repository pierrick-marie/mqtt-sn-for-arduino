#include <XBee.h>

#define ID 2

XBee xbee(ID);

String message;

void setup() {

  Serial.begin(9600);
}

void loop() {

  message = xbee.getMessage();
  while(!message.equals("")){
    Serial.print("Get: ");
    Serial.println(message);
    message = xbee.getMessage();
  }
  Serial.println("");
 

  Serial.println("Envois: BOUJOUR");
  xbee.sendMessage("BONJOUR");
}

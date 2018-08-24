/** debut bloc include **/
#include <stdint.h>
#include <stdbool.h>
#include <string.h>
#include <mqttsn-messages.h>
#include <SoftwareSerial.h>
#include "config.h"
#include "decl.h"
/** fin bloc include **/

/* debut declaration constantes */
//#define LIGHT_PIN
//#define SOUND_PIN
//#define MOISTURE_PIN
//#define TEMPERATURE_PIN

// nom du topic de publication
#define TOPIC_NAME "" 

// nom du topic de testament
#define TOPIC_TESTAMENT ""
#define MODULE_NAME "" 
#define MAX_NUMBER_MSG_TO_SENT 4
#define TIME_TO_SLEEP 40
#define REJECTED 3
#define ACCEPTED 0
#define KEEP_ALIVE 60
#define FLAG 0
#define TIME_BETWEEN_DISCONNECT_CONNECT 30
/* fin declaration constantes */

// objet pour appeler les methodes de la librairie mqttsn
MQTTSN mqttsn; 

// objet pour appeler les methodes pour envoyer des donnees via le module XBee
SoftwareSerial XBee(5, 4);

// variable pour savoir si une deconnection a ete appele ou non (cf cycle de vie dans sujet)
bool isDeconnectedNormal = false; 

uint16_t topicId = -1;
String message = "";
int numberMsgSent  = 0;

void setup() {

	// initialisation de la liaison serie
	Serial.begin(9600); 
	Serial.println("********Debut du programme********");

	// initialisation de la liaison XBee
	XBee.begin(9600); 
}

void loop() {

	// verification des messages entrant
	CheckSerial();
	if (mqttsn.wait_for_response()){
		return;
	}
}

String getMessage() {

	String message = ""; // eventCaptured.getValue();
	return message;
}

// handler gerant un retour d'info de passerelle
void MQTTSN_gwinfo_handler(const msg_gwinfo* msg){ 
}

// Callback de la methode subscribe_by_name
void MQTTSN_suback_handler(const msg_suback* msg){ 
}

// Callback de la methode pingreq
void MQTTSN_pingresp_handler(){ 
}

// Callback de la methode connect
void MQTTSN_connack_handler(const msg_connack* msg){
}

// Callback de la methode register
void MQTTSN_regack_handler(const msg_regack* msg){ 
}

// Callback de la methode publish
void MQTTSN_puback_handler(const msg_puback* msg){ 
}

// Callback de la methode disconnect
void MQTTSN_disconnect_handler(const msg_disconnect* msg){
}

/******Structure de l'objet msg_public******/
/*
struct msg_publish : public message_header {
    uint8_t flags;
    uint16_t topic_id;
    uint16_t message_id;
    char data[0];
};
*/
/********************************************/

// Callback de la methode publish
void MQTTSN_publish_handler(const msg_publish* msg){ 
}

// Callback de la methode willtopic
void MQTTSN_willtopicreq_handler(const message_header* msg){ 
}

// Callback de la methode willmsg
void MQTTSN_willmsgreq_handler(const message_header* msg){ 
}

void MQTTSN_reregister_handler(msg_reregister const*){
  // RESERVERD !
}

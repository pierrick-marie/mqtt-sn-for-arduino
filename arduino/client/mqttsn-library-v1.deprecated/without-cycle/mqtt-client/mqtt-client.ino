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
// #define RFID_PIN 

// variable a changer en fonction des cartes
#define CHARACTERS_NUMBER_RFID 12

// nom du topic de publication
#define TOPIC_NAME "" 

// nom du topic du testament
#define TOPIC_TESTAMENT ""
#define KEEP_ALIVE 60
#define MODULE_NAME ""
#define ARRAY_SIZE 64 
#define ACCEPTED 0
#define REJECTED 3
#define DISCONNECT_NOW 0
#define FLAG 0
/* fin declaration constantes */

// objet pour appeler les methodes de la librairie mqttsn
MQTTSN mqttsn; 

// objet pour appeler les methodes pour envoyer des donnees via le module XBee
SoftwareSerial XBee(5, 4);

SoftwareSerial rfid(2,3);
uint16_t topicId = -1;
bool scanRFIDCard = false;
String message = "";         
int counterNumberCharactersRFID = 0;
unsigned char RFIDCard[ARRAY_SIZE];

void setup() {

	// initialisation de la liaison serie
	Serial.begin(9600); 
	while (!Serial) {
		// wait for serial port to connect. Needed for native USB port only
	}
	rfid.begin(9600);

	// initialisation de la liaison XBee
	XBee.begin(9600); 
}

void loop() {   
	
	// verification des messages entrants
	CheckSerial();
	if (mqttsn.wait_for_response()){
		return;
	}
}

String getMessage(unsigned char RFIDCard[ARRAY_SIZE]) {
	
	String message = ""; // eventCaptured.getValue();
	return message;
}

// function to clear buffer array
void clearBufferArray() {
}

// handler gerant un retour d'info de passerelle
void MQTTSN_gwinfo_handler(const msg_gwinfo* msg) {
}

// handler gerant un retour de souscription
void MQTTSN_suback_handler(const msg_suback* msg) {
}

// handler gerant une reponse de ping
void MQTTSN_pingresp_handler(){ 
}

// handler gerant un retour de connection
void MQTTSN_connack_handler(const msg_connack* msg){ 
}

// handler gerant un retour d'enregistrement de nom de topic
void MQTTSN_regack_handler(const msg_regack* msg){ 
}

// handler gerant un retour de message publie
void MQTTSN_puback_handler(const msg_puback* msg){
}

// handler gerant une deconnection
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

// handler permettant la creation d'un nom de topic pour le testament
void MQTTSN_willtopicreq_handler(const message_header* msg){ 
}

// handler permettant la creation d'un message testament
void MQTTSN_willmsgreq_handler(const message_header* msg){ 
}

void MQTTSN_reregister_handler(msg_reregister const*){
	// RESERVED !
}

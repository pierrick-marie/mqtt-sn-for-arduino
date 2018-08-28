#define FLAG 0
#define KEEP_ALIVE 60
#define REJECTED 3
#define ACCEPTED 0
#define DEBUG false

#define TIME_TO_WAIT 2000
#define MAX_TRY 5
#define NB_MAX_DICTIONNARY 10

/*
 * The structure of a dictionnary
 * @DEPRECATED @SEE:mqttsn-messages.TopicTable
 *
typedef struct {
    int topic_id;
    char* topic_name;
} topic_dictionnary;
 */

int connack_return_code;
int regack_return_code;
int suback_return_code;
int puback_return_code;
String message;

/*
 * List of registered topics
 * @DEPRECATED @SEE:mqttsn-messages.TopicTable
 *
topic_dictionnary my_topic_dictionnary[NB_MAX_DICTIONNARY];
 */

/*
 * Number of registered topic
 * @DEPRECATED @SEE:mqttsn-messages.TopicTable
 *
int nb_topic_registered = 0;
 */

/* To check if the first message from the gateway have been received */
bool init_ok = false; 

/**
 *
 * ****************************
 * ############################
 * ####################
 * #############
 * ######
 * ##
 * ****************************
 *
 * PRIVATE FUNCTIONS
 *
 * ****************************
 * ##
 * ######
 * #############
 * ####################
 * ############################
 * ****************************
 *
 **/

/**
 * The function calls @MB_check_serial until @nb_max_try have been reach or a response from the gateway have been received.
 *
 * Returns:
 * True if a response is received before @nb_max_try, else false.
 **/
bool multi_check_serial(const int nb_max_try) {

    int nb_try = 1;

    while( !MB_check_serial() && nb_try <= nb_max_try ) {
        nb_try++;
    }

    return nb_try != nb_max_try;
}

/**
 * The function verifies if the @topic_name is already registered.
 *
 * Returns:
 * True if the topic is registered, else false.
 *
 * @deprecated @see:mqqtsn-messages
 *
bool is_topic_registered(const char* topic_name) {

    for(int i=0; i < NB_MAX_DICTIONNARY; i++){
        if(my_topic_dictionnary[i].topic_name == topic_name){
            return true;
        }
    }
    return false;
}
 */

/**
 *
 * ****************************
 * ############################
 * ####################
 * #############
 * ######
 * ##
 * ****************************
 *
 * PUBLIC FUNCTIONS
 *
 * ****************************
 * ##
 * ######
 * #############
 * ####################
 * ############################
 * ****************************
 *
 **/

/**
 * The init function searches a gateway with a radius = 0.
 *
 * Return:
 * ACCEPTED if a correct response is received, else REJECTED.
 **/
int ABSTRCT_init() {
    
    int nb_try = 1;
    long time = 0;
    int radius = 0;

    // first try to search a gateway
    mqttsn.MQTTSN_searchgw(radius);

    while( !MB_check_serial() && nb_try <= MAX_TRY) {
        // the gateway did not respond, let's try again after a delay
        nb_try++;
        delay(1000);
        mqttsn.MQTTSN_searchgw(radius);
    }
    if( nb_try == MAX_TRY) {
        return REJECTED;
    }

    // parsing the received data
    MB_parse_data();

    // checking the response from the gateway
    if(init_ok) {
        return ACCEPTED;
    } else {
        return REJECTED;
    }
}

/**
 * Function called after receiving the first message from the gateway
 * MQTT-SN.searchGW =>
 * <= MQTT-SN.GWinfo
 *
 * Arguments:
 * @message the message sent by the gateway (@see:mqttsn.h->struct msg_gwinfo)
 **/
void MQTTSN_gwinfo_handler(const msg_gwinfo* message) {

    // 1: magic number, get the code of the gateway
    if( message->gw_id == 1 ) {
        init_ok = true;
    } else {
        init_ok = false;
    }
}

/**
 * The funtion tries to connect the module to the gateway. Arguments:
 * @module_name the name of the module used to make the connection
 *
 * Return:
 * ACCEPTED if a correct response is received, else REJECTED.
 **/
int ABSTRCT_connect(const char* module_name) {

    // send a connect message
    mqttsn.MQTTSN_connect(FLAG, KEEP_ALIVE, module_name);

    // waiting for a response
    if( !multi_check_serial(MAX_TRY) ) {
        return REJECTED;
    }
    
    // parsing the received data
    MB_parse_data();

    // the parsed response from the gateway
    return connack_return_code;
}

/**
 * Function called after receiving a connack message.
 * MQTT-SN.connect =>
 * <= MQTT-SN.connack
 *
 * Arguments:
 * @message the message sent by the gateway (@see:mqttsn.h->struct msg_connack)
 **/
void MQTTSN_connack_handler( const msg_connack* message ) {

    debug("Entering connack ");
    debugln(MB_string_from_return_code(message->return_code));
    // save the return code
    connack_return_code = message->return_code;
}

/**
 *
 **/
int ABSTRCT_subscribe(const char* topic_name) {

    // @TODO
    if(mqttsn.MQTTSN_find_topic_id(topic_name) == -1) {
        Serial.println("Topic not registered yet!");
        if(ABSTRCT_register(topic_name) != ACCEPTED) {
            Serial.println("ABSTRACT - REJECTED");
            return REJECTED;
        }
    }
    /*
     * TODO Old code
    mqttsn.subscribe_by_name(FLAG, topic_name);
    while(mqttsn.wait_for_suback()){
        CheckSerial();
    }
    delay(1000);
    return suback_return_code;
    */
    // TODO !
    Serial.println("ABSTRACT - ACCEPTED");
    return ACCEPTED;
}

int ABSTRCT_register(const char* topic_name) {

    // Create and send a message to register the @topic_name
    mqttsn.MQTTSN_register_topic(topic_name);
    // my_topic_dictionnary[nb_topic_registered].topic_name = topic_name;
    // TODO : END OF WORK 28/08/2018
    while(mqttsn.MQTTSN_wait_for_response()){
        CheckSerial();
    }
    //Serial.print("Regack_return_code ");
    //Serial.println(regack_return_code);
    //Serial.print("Regack_topic_id ");
    //Serial.println(my_topic_dictionnary[nb_topic_registered-1].topic_id);
    delay(1000);
    return regack_return_code;
}

int topic_id_for_topic_name(const char* topic_name){
    for(int i=0;i<sizeof(my_topic_dictionnary)/sizeof(topic_dictionnary);i++){
        if(my_topic_dictionnary[i].topic_name == topic_name){
            return my_topic_dictionnary[i].topic_id;
        }
    }
    return -1;
}


int sn_publish(String message, const char* topic_name){
    int topic_id;
    if(!is_topic_registered(topic_name)){
        if(ABSTRCT_register(topic_name) != ACCEPTED){
            return REJECTED;
        }else{
            sn_publish(message, topic_name);
        }
    }
    topic_id = topic_id_for_topic_name(topic_name);
    if(topic_id == -1){
        return REJECTED;
    }
    mqttsn.MQTTSN_publish(FLAG, topic_id, message.c_str(), message.length());
    while(mqttsn.MQTTSN_wait_for_puback()){
        CheckSerial();
    }
    delay(1000);
    return puback_return_code;
}

void sn_disconnect(){
    mqttsn.MQTTSN_disconnect(0);
    delay(1000);
}

String sn_get_message_from_subscribed_topics(){
    message = "";
    mqttsn.MQTTSN_pingreq(MODULE_NAME);
    while(mqttsn.MQTTSN_wait_for_pingresp()){
        CheckSerial();
    }
    delay(1000);
    return message;
}

void debug(String message){
    if(DEBUG){
        Serial.print(message);
    }
}

void debugln(String message){
    if(DEBUG){
        Serial.println(message);
    }
}

void MQTTSN_regack_handler(const msg_regack* msg){ 
    debug("Entering regack ");
    debugln(MB_string_from_return_code(msg->return_code));
    regack_return_code = msg->return_code;
    if(msg->return_code == ACCEPTED){
        my_topic_dictionnary[nb_topic_registered].topic_id = msg->topic_id;
        nb_topic_registered++;
    }
}

void MQTTSN_suback_handler(const msg_suback* msg){
    debug("Entering suback ");
    debugln(MB_string_from_return_code(msg->return_code));
    suback_return_code = msg->return_code;
}

void MQTTSN_puback_handler(const msg_puback* msg){ 
    debug("Entering puback ");
    debugln(MB_string_from_return_code(msg->return_code));
    puback_return_code   = msg->return_code;
}

void MQTTSN_disconnect_handler(const msg_disconnect* msg){
    //handler gérant une déconnection
}

void MQTTSN_publish_handler(const msg_publish* msg){ 
    //handler gérant un message reçu
    message = msg->data;
}

void MQTTSN_pingresp_handler(){ 
    debugln("Entering pingresp");
}

void MQTTSN_reregister_handler(msg_reregister const*){
    //RESERVERD
}

void MQTTSN_willtopicreq_handler(const message_header* msg){ 
    //handler permettant la création d'un nom de topic pour le testament
}

void MQTTSN_willmsgreq_handler(const message_header* msg){ 
    //handler permettant la création d'un message testament
}

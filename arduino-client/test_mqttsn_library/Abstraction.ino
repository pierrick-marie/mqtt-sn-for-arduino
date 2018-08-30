#define FLAG 0
#define KEEP_ALIVE 60
#define REJECTED 3
#define ACCEPTED 0

#define TIME_TO_WAIT 2000
#define MAX_TRY 5
#define NB_MAX_DICTIONNARY 10

/**
 * The structure of a dictionnary
 * @deprecated @see:mqttsn-messages.TopicTable
 *
typedef struct {
    int topic_id;
    char* topic_name;
} topic_dictionnary;
 **/

/* The code received after a connect message */
int connack_return_code;
/* The code received after a subscribe or register message */
int regack_return_code;
int suback_return_code;
int puback_return_code;
String message;

/**
 * List of registered topics
 * @deprecated @see:mqttsn-messages.TopicTable
 *
topic_dictionnary my_topic_dictionnary[NB_MAX_DICTIONNARY];
 **/

/**
 * Number of registered topic
 * @deprecated @see:mqttsn-messages.TopicTable
 *
int nb_topic_registered = 0;
 **/

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
 * @brief multi_check_serial The function calls @MB_check_serial until @nb_max_try have been reach or a response from the gateway have been received.
 * @param nb_max_try The maximum number of try @MB_check_serial before the time out.
 * @return True if a respense is received, else false.
 **/
bool multi_check_serial(const int nb_max_try) {

	int nb_try = 0;

	logs.debug("ABSTRCT.multi_check_serial() -> check serial iteration: ", nb_try);
	while( !MB_check_serial() && nb_try < nb_max_try ) {
		nb_try++;
		logs.debug("ABSTRCT.multi_check_serial(): -> check serial iteration: ", nb_try);
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
 **/

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
 * @brief ABSTRCT_init The init function searches a gateway with a radius = 0.
 * @return ACCEPTED if a correct response is received, else REJECTED.
 **/
int ABSTRCT_init() {

	int nb_try = 0;
	long time = 0;
	int radius = 0;

	logs.debug("ABSTRCT_init() -> first try to search a gateway");
	mqttsn.searchgw(radius);

	while( !MB_check_serial() && nb_try < MAX_TRY) {
		logs.debug("ABSTRCT_init() -> the gateway did not respond, iteration: ", nb_try);
		nb_try++;
		// delay(1000);
		mqttsn.searchgw(radius);
	}

	if( nb_try == MAX_TRY) {
		logs.debug("ABSTRCT_init() -> REJECTED");
		return REJECTED;
	}

	logs.debug("ABSTRCT_init() -> parsing the received data");
	MB_parse_data();

	logs.debug("ABSTRCT_init() -> checking the response from the gateway");
	if(init_ok) {
		return ACCEPTED;
	} else {
		return REJECTED;
	}
}

/**
 * @brief ABSTRCT_connect The funtion tries to connect the module to the gateway.
 * @param module_name The name of the module used to make the connection
 * @return ACCEPTED if a correct response is received, else REJECTED.
 **/
int ABSTRCT_connect(const char* module_name) {

	logs.debug("ABSTRCT_connect() -> send a connect message");
	mqttsn.connect(FLAG, KEEP_ALIVE, module_name);

	logs.debug("ABSTRCT_connect() -> waiting for a response");
	if( !multi_check_serial(MAX_TRY) ) {
		logs.debug("ABSTRCT_connect() -> check serial rejected");
		return REJECTED;
	}

	logs.debug("ABSTRCT_connect() -> parsing the received data");
	MB_parse_data();

	logs.debug("ABSTRCT_connect() -> the parsed response from the gateway ", connack_return_code);
	return connack_return_code;
}

/**
 * @brief ABSTRCT_subscribe Function used to subscribe to a @topic_name.
 *
 *
 *
 * @param topic_name The name of the topic to subscribre.
 * @return ACCEPTED if the sucbribe operation is OK, else REJECTED.
 **/
int ABSTRCT_subscribe(const char* topic_name) {

	/**
     * @deprecated
    if(mqttsn.find_topic_id(topic_name) == -1) {
	  Serial.println("Topic not registered yet!");
     */
	if(ABSTRCT_register(topic_name) != ACCEPTED) {
		Serial.println("ABSTRACT - REJECTED");
		return REJECTED;
	}
	/** } */


	/**
     * @todo BEGIN: DEBUG
    mqttsn.subscribe_by_name(FLAG, topic_name);
    while(mqttsn.wait_for_suback()){
	  CheckSerial();
    }
    delay(1000);
    return suback_return_code;
    * @todo END: DEBUG
    **/
	Serial.println("ABSTRACT_subscribe: ACCEPTED");
	return ACCEPTED;
}

int ABSTRCT_register(const char* topic_name) {

	logs.debug("Request to register the @topic_name: ", topic_name);
	int res_register_topic = mqttsn.register_topic(topic_name);

	if(res_register_topic == -1) {

		logs.debug("Request to register topic is sent, waiting for a response");

		// waiting for a response
		if( !multi_check_serial(MAX_TRY) ) {
			return REJECTED;
		}

		// parsing the received data
		MB_parse_data();

		return regack_return_code;
	}

	if(res_register_topic == -2) {
		logs.debug("It is not possible to register the @topic_name");
		return REJECTED;
	} else {
		logs.debug("ABSTRCT_register()->res_register_topic >= 0, the @topic_name is already registered");
		return ACCEPTED;
	}
}

/**
 * @brief topic_id_for_topic_name
 * @param topic_name
 * @return
 *
 * @deprecated
 *
int topic_id_for_topic_name(const char* topic_name){
    for(int i=0;i<sizeof(my_topic_dictionnary)/sizeof(topic_dictionnary);i++){
	  if(my_topic_dictionnary[i].topic_name == topic_name){
		return my_topic_dictionnary[i].topic_id;
	  }
    }
    return -1;
}
 */

int sn_publish(String message, const char* topic_name){
	/**
     * @todo BEGIN: DEBUG
     **/
	/*
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
    mqttsn.publish(FLAG, topic_id, message.c_str(), message.length());
    while(mqttsn.wait_for_puback()){
	  CheckSerial();
    }
    delay(1000);
    return puback_return_code;
    */
	return ACCEPTED;
	/**
     * @todo BEGIN: DEBUG
     **/
}

void sn_disconnect(){
	mqttsn.disconnect(0);
	delay(1000);
}

String sn_get_message_from_subscribed_topics(){
	message = "";
	mqttsn.pingreq(MODULE_NAME);
	while(mqttsn.wait_for_pingresp()){
		CheckSerial();
	}
	delay(1000);
	return message;
}


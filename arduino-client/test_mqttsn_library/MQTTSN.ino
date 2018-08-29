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
 * @brief MQTTSN_gwinfo_handler Function called after receiving the first message from the gateway
 * MQTT-SN.searchGW =>
 * <= MQTT-SN.GWinfo
 *
 * @param message The message sent by the gateway (@see:mqttsn.h->struct msg_gwinfo)
 */
void MQTTSN_gwinfo_handler(const msg_gwinfo* message) {

    // 1: magic number, get the code of the gateway
    if( message->gw_id == 1 ) {
        init_ok = true;
    } else {
        init_ok = false;
    }
}

/**
 * @brief MQTTSN_connack_handler Function called after receiving a connack message.
 * MQTT-SN.connect =>
 * <= MQTT-SN.connack
 *
 * @param message The message sent by the gateway (@see:mqttsn.h->struct msg_connack)
 */
void MQTTSN_connack_handler( const msg_connack* message ) {

    debug("Entering connack ", MB_string_from_return_code(message->return_code));
    // save the return code
    connack_return_code = message->return_code;
}

/**
 * @brief MQTTSN_regack_handler Function called after receiving a regack message.
 * MQTT-SN.register_topic =>
 * <= MQTT-SN.regack
 *
 * @param msg The message sent by the gateway (@see:mqttsn.h->struct msg_regack)
 */
void MQTTSN_regack_handler(const msg_regack* msg){

    debug("Entering regack: ", MB_string_from_return_code(msg->return_code));
    regack_return_code = msg->return_code;
    /**
     * @deprecated
    if(msg->return_code == ACCEPTED){
        my_topic_dictionnary[nb_topic_registered].topic_id = msg->topic_id;
        nb_topic_registered++;
    }
     */
}

void MQTTSN_suback_handler(const msg_suback* msg){
    debug("Entering suback ", MB_string_from_return_code(msg->return_code));
    suback_return_code = msg->return_code;
}

void MQTTSN_puback_handler(const msg_puback* msg){ 
    debug("Entering puback ", MB_string_from_return_code(msg->return_code));
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
    debug("Entering pingresp");
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

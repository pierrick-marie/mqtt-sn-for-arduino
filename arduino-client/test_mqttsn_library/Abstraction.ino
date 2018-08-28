#define FLAG 0
#define KEEP_ALIVE 60
#define REJECTED 3
#define ACCEPTED 0
#define DEBUG false

#define TIME_TO_WAIT 2000
#define MAX_TRY 5

typedef struct {
  int topic_id;
  char* topic_name;
} topic_dictionnary;

int connack_return_code;
int regack_return_code;
int suback_return_code;
int puback_return_code;
String message;

topic_dictionnary my_topic_dictionnary[10];
int nb_topic_registered=0;

bool init_ok = false;

int sn_init(){

  // Search gateway
  mqttsn.searchgw(0);
  delay(500);

  // Waiting for a response
  int nb_try = 0;
  long time = millis();
  bool test = !XBee.available();
  while(test && nb_try < MAX_TRY){
    Serial.print("CHECK IF - ");
    Serial.println(test);
    if(time + TIME_TO_WAIT < millis()) {
      // Search again the gateway (5 times max @MAX_TRY)
      Serial.println("############ TRY AGAIN!");
      mqttsn.searchgw(0);
      delay(500);
      nb_try++;
      test = !XBee.available();
      time = millis();
    }
  }
  Serial.print("OUT OF WHILE - ");
  Serial.println(test);
  
  // Read the response
  CheckSerial();

  // Wait for checking the response @MQTTSN_gwinfo_handler()
  while(!init_ok && time + TIME_TO_WAIT > millis()) {}

  if(init_ok) {
    return ACCEPTED;
  } else {
    return REJECTED;
  }
}

int sn_connect(const char* module_name){
  mqttsn.connect(FLAG, KEEP_ALIVE, module_name);
  while(mqttsn.wait_for_response()){
    CheckSerial();
  }
  delay(1000);
  return connack_return_code;
}

bool is_topic_registered(const char* topic_name){
  for(int i=0;i<sizeof(my_topic_dictionnary)/sizeof(topic_dictionnary);i++){
    if(my_topic_dictionnary[i].topic_name == topic_name){
      return true;
    }
  }
  return false;
}

int topic_id_for_topic_name(const char* topic_name){
  for(int i=0;i<sizeof(my_topic_dictionnary)/sizeof(topic_dictionnary);i++){
    if(my_topic_dictionnary[i].topic_name == topic_name){
      return my_topic_dictionnary[i].topic_id;
    }
  }
  return -1;
}

int sn_register(const char* topic_name){
  mqttsn.register_topic(topic_name);
  my_topic_dictionnary[nb_topic_registered].topic_name = topic_name;
  while(mqttsn.wait_for_response()){
    CheckSerial();
  }
  //Serial.print("Regack_return_code ");
  //Serial.println(regack_return_code);
  //Serial.print("Regack_topic_id ");
  //Serial.println(my_topic_dictionnary[nb_topic_registered-1].topic_id);
  delay(1000);
  return regack_return_code;
}

int sn_subscribe(const char* topic_name){
  if(!is_topic_registered(topic_name)){
    if(sn_register(topic_name) != ACCEPTED){
      return REJECTED;
    }
  }
  mqttsn.subscribe_by_name(FLAG, topic_name);
  while(mqttsn.wait_for_suback()){
    CheckSerial();
  }
  delay(1000);
  return suback_return_code;
}

int sn_publish(String message, const char* topic_name){
  int topic_id;
  if(!is_topic_registered(topic_name)){
    if(sn_register(topic_name) != ACCEPTED){
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

void MQTTSN_connack_handler(const msg_connack* msg){
  debug("Entering connack ");
  debugln(stringFromReturnCode(msg->return_code));
  connack_return_code = msg->return_code;
}

void MQTTSN_regack_handler(const msg_regack* msg){ 
  debug("Entering regack ");
  debugln(stringFromReturnCode(msg->return_code));
  regack_return_code = msg->return_code;
  if(msg->return_code == ACCEPTED){
    my_topic_dictionnary[nb_topic_registered].topic_id = msg->topic_id;
    nb_topic_registered++;
  }
}

void MQTTSN_suback_handler(const msg_suback* msg){
  debug("Entering suback ");
  debugln(stringFromReturnCode(msg->return_code));
  suback_return_code = msg->return_code;
}

void MQTTSN_puback_handler(const msg_puback* msg){ 
  debug("Entering puback ");
  debugln(stringFromReturnCode(msg->return_code));
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

void MQTTSN_gwinfo_handler(const msg_gwinfo* msg){ 
  if(msg->gw_id == 1) {
    init_ok = true;
  } else {
    init_ok = false;
  }
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

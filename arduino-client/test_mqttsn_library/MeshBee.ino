#include <stdint.h>
#include <stdbool.h>
#include <string.h>

#define API_DATA_PACKET  0x02
#define API_START_DELIMITER  0x7E
#define OPTION_CAST_MASK  0x40   //option unicast or broadcast MASK
#define OPTION_ACK_MASK  0x80   // option ACK or not MASK

#define API_DATA_LEN  40
#define API_PAY_LEN  (API_DATA_LEN + 5)
#define API_FRAME_LEN  (API_DATA_LEN + 9)

uint8_t FrameID = 0;
uint16_t u16TopicPubID;
uint8_t u8Counter;
uint8_t FrameBufferOut[API_FRAME_LEN] = {0};
uint8_t FrameBufferIn[API_FRAME_LEN] = {0};
uint8_t GatewayAddress[8] = {0};

/**
 * ############################
 * Private functions
 * ############################
 **/

/**
 * The function waits during one second if data is available. In that case it returns true else returns false.
 *
 * Returs:
 * True if a response is received (XBee.available() > 1), else false.
 **/
bool wait_data() {

    int i = 1;
    
    // waiting for incoming data during 1 second (10x100ms)
    while( XBee.available() <= 0 && i <= 10 ) {
        delay(100);
        i++;
    }
    if( i == 20 ) {
        // timeout -> return false
        return false;
    }
    
    return true;
}

/**
 * The function verifies the checksum of @frame_buffer according to its @frame_size and returns true if it's OK, else return false.
 *
 * Returns:
 * True if the checksum of @frame_buffer is ok, else false.
 **/
bool verify_checksum(uint8_t frame_buffer[], int frame_size){
    
    int i;
    uint16_t checksum = 0x00;
    
    for(i=0; i < frame_size; i++) {
        checksum += frame_buffer[i];
    }
    checksum = checksum & 0xFF;
    
    return checksum == 0xFF ;
}

/**
 * The function verifies if the transmetted message in @FrameBufferIn is a status message.
 *
 * Returns:
 * True if the message is a transmit status, else false.
 **/
bool is_transmit_status() {
    return FrameBufferIn[0] == 139;
}

/**
 * The function verifies if the transmetted message in @FrameBufferIn is a data packet.
 *
 * Returns:
 * True if the message is a packet with data, else false.
 **/
bool is_data_packet() {
    return FrameBufferIn[0] == 144;
}

/**
 * The function creates a MeshBee frame and returns the frame lenght.
 *
 * Arguments:
 * data: the data used to create the frame
 * data_lenght: the lenght of @data
 * destination_address: the address to send the frame
 * frame: the frame that will be fill with the @data
 * frame_max_lenght: the maximum lenght of the @frame
 * broadcast: true if the frame is a broadcast message
 *
 * Returns:
 * The size of the created frame.
 **/
int create_frame(uint8_t* data, int data_lenght, uint8_t* destination_address, uint8_t* frame, int frame_max_lenght, bool broadcast) {

    uint8_t checksum = 0;
    int i = 0;

    // frame buffer is big enough?
    if ( frame_max_lenght < API_FRAME_LEN ) {
        return -1;
    }

    // data is too long?
    // TODO: Split in multiple packets?
    if (data_lenght > API_DATA_LEN) {
        Serial.println("TOO LONG");
        return -2;
    }

    // frame buffer is fine, clear it
    memset (frame, 0, frame_max_lenght);

    /* The header */

    // delimiter
    frame[0] = API_START_DELIMITER;

    // length of the payload
    if(API_PAY_LEN < 256) {
        frame[1] = 0;
        frame[2] = 14 + data_lenght;
    }else{
        frame[1] = API_PAY_LEN / 256;
        frame[2] = API_PAY_LEN - (256 * frame[1]);
    }

    // frame Type: Transmit Request
    checksum = 0;
    checksum += frame[3] = 16;

    // frame id
    checksum += frame[4] = FrameID++;

    // 64-bit address
    checksum += frame[5] = destination_address[0];
    checksum += frame[6] = destination_address[1];
    checksum += frame[7] = destination_address[2];
    checksum += frame[8] = destination_address[3];
    checksum += frame[9] = destination_address[4];
    checksum += frame[10] = destination_address[5];
    checksum += frame[11] = destination_address[6];
    checksum += frame[12] = destination_address[7];

    // 16-bit address
    checksum += frame[13] = 0;
    checksum += frame[14] = 0;
    checksum += frame[15] = 0;
    checksum += frame[16] = 0;


    /* The data */
    for (i = 0; i < data_lenght; i++) {
        checksum += frame[17 + i] = data[i];
        Serial.print(" ");
    }

    checksum = 0XFF - checksum;
    frame[17 + data_lenght] = checksum;

    return 17 + data_lenght + 1;
}

/**
 * ############################
 * Public functions
 * ############################
 **/

/**
 * The function waits a response from the gateway (@wait_data). If a response is available, the function analyse and store the message if necessary.
 *
 * Returns:
 * True if a correct message have been received, else false.
 **/
bool MB_check_serial() {

    int i, frame_size, payload_lenght;
    uint8_t delimiter, length1, length2, frame_buffer;
    bool checksum;

    // no data is available
    if(!wait_data()) {
        return false;
    }

    // data available before the timeout
    delimiter = XBee.read();

    // verifiy if the delimiter is OK
    if(delimiter != 0x7E) {
        return false;
    }

    if(XBee.available() > 0) {
        length1=XBee.read();
        length2=XBee.read();
        frame_size=(length1*16)+length2+1;

        // store the data in @frameBuffer
        for(i = 0; i < frame_size; i++){
            delay(10);
            FrameBufferIn[i]=XBee.read();
        }
        
        // verify the checksum
        if(!verify_checksum(FrameBufferIn, frame_size)) {
            return false;
        }
        
        // check the type of received message
        if(is_transmit_status()) {
            return false;
        }

        if(is_data_packet()) {
            // this is a data packet, copy the gateway address
            if(GatewayAddress[0]==0 && GatewayAddress[1]==0 && GatewayAddress[2]==0 && GatewayAddress[3]==0){
                GatewayAddress[0] = FrameBufferIn[1];
                GatewayAddress[1] = FrameBufferIn[2];
                GatewayAddress[2] = FrameBufferIn[3];
                GatewayAddress[3] = FrameBufferIn[4];
                GatewayAddress[4] = FrameBufferIn[5];
                GatewayAddress[5] = FrameBufferIn[6];
                GatewayAddress[6] = FrameBufferIn[7];
                GatewayAddress[7] = FrameBufferIn[8];
            }
            // all data have been store in @FrameBufferIn
            return true;
        }
    }
    // not data is available, clear the buffer and return false
    memset(FrameBufferIn, 0, sizeof(FrameBufferIn));
    return false;
}

/**
 * The function analyses the incoming data (@FrameBufferIn) and calls the function @mqttsn.parse_stream before cleaning the @FrameBufferIn.
 **/
void MB_parse_data() {

    int i;
    int payload_lenght = FrameBufferIn[12];
    uint8_t payload[payload_lenght];
    
    for(i = 0; i < payload_lenght; i++){
        payload[i] = FrameBufferIn[12+i];
    }
    mqttsn.MQTTSN_parse_stream(payload, payload_lenght);
    
    memset(FrameBufferIn, 0, sizeof(FrameBufferIn));
}

/**
 * The function returns the associated string status to corresponding to the given @return_code.
 **/
static inline char* MB_string_from_return_code(const uint8_t return_code){

    static char string_code[30] = {'\0'};

    /*
    switch(return_code) {
        case 0:
        */
            strncpy(string_code, "ACCEPTED", 8);
            /*
        break;
    }
    */


    // char strings[][] = { "ACCEPTED", "REJECTED_CONGESTION", "REJECTED_INVALID_TOPIC_ID", "REJECTED"};
    // return strings[return_code];

    Serial.print("\n POW POW POW: ");
    Serial.println(string_code);

    return string_code;
}

/**
 * The function sends the @message_buffer through the XBee module according to its @lenght.
 **/
void MB_serial_send(uint8_t* message_buffer, int length) {

    int _length = create_frame(message_buffer, length, GatewayAddress, FrameBufferOut, sizeof(FrameBufferOut), false);
    if (_length > 0) {
        XBee.write(FrameBufferOut, _length);
        XBee.flush();
    }
}

/**
 * DEPRECATED
 **/
void CheckSerial(){
    /*
         *   if(XBee.available()>0){
         *       delay(10);
         *       // Serial.println("XBee Available");
         *       uint8_t delimiter=XBee.read();
         *       if(delimiter!=0x7E)
         *           return;
         *       if(XBee.available()>0){
         *           uint8_t length1=XBee.read();
         *           uint8_t length2=XBee.read();
         *           //Serial.print(length1);
         *           //Serial.print(" ");
         *           //Serial.print(length2);
         *           int frameSize=(length1*16)+length2+1;
         *           //Serial.print(" ");
         *           //Serial.println(frameSize);
         *           uint8_t frameBuffer[frameSize];
         *           for(int i=0;i<frameSize;i++){
         *               delay(10);
         *               frameBuffer[i]=XBee.read();
         *               //Serial.print(frameBuffer[i],HEX);
         *               //Serial.print(" ");
}
//Serial.println("");
bool cs=verifyChecksum(frameBuffer, frameSize);
if(!cs){
        return -1;
}
switch(frameBuffer[0]){
        case 139:
                // Serial.println("Transmit Status");
                break;
        case 144:
                // Serial.println("Receive Packet");
                if(GatewayAddress[0]==0 && GatewayAddress[1]==0 && GatewayAddress[2]==0 && GatewayAddress[3]==0){
                        GatewayAddress[0]=frameBuffer[1];
                        GatewayAddress[1]=frameBuffer[2];
                        GatewayAddress[2]=frameBuffer[3];
                        GatewayAddress[3]=frameBuffer[4];
                        GatewayAddress[4]=frameBuffer[5];
                        GatewayAddress[5]=frameBuffer[6];
                        GatewayAddress[6]=frameBuffer[7];
                        GatewayAddress[7]=frameBuffer[8];
}
int pay_len=frameBuffer[12];
uint8_t pay[pay_len];
for(int i=0;i<pay_len;i++){
        pay[i]=frameBuffer[12+i];
}
mqttsn.parse_stream(pay, pay_len);
break;   
}
}
}
*/
}

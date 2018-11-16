

/**
 * ############################
 * Private functions
 * ############################
 **/

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

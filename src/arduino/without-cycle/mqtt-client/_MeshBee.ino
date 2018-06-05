#include <stdint.h>
#include <stdbool.h>
#include <string.h>
#include "config.h"

#define API_DATA_PACKET  0x02
#define API_START_DELIMITER  0x7E
#define OPTION_CAST_MASK  0x40   //option unicast or broadcast MASK
#define OPTION_ACK_MASK  0x80   // option ACK or not MASK

uint8_t FrameID = 0;

// Create MeshBee frame (return frame lenght)
int MB_FrameCreate(uint8_t* data, int data_len, uint8_t* dest_addr, uint8_t* frame, int frame_max_len, bool broadcast)
{
  // frame buffer is big enough?
  if ( frame_max_len < API_FRAME_LEN )
  {
    return -1;
  }
  
  // data is too long?
  // TODO: Split in multiple packets?
  Serial.print("length ");
  Serial.println(data_len);
  if (data_len > API_DATA_LEN)
  {
    Serial.println("TOO LONG");
    return -2;
  }
  
  // Frame buffer is fine. Clear it
  memset (frame, 0, frame_max_len); 
  
  // Header
  frame[0] = API_START_DELIMITER;  // Delimiter
  /*Serial.print("API START DELIMITER:" );
  Serial.print(API_START_DELIMITER, HEX);
  Serial.println();*/
  if(API_PAY_LEN<256){    // Length of the payload
    frame[1] = 0;  
    frame[2] = 14+data_len;  
  }else{
    frame[1]=API_PAY_LEN/256;
    frame[2]=API_PAY_LEN-(256*frame[1]);
  }
  /*Serial.print("API PAY LEN:" );
  Serial.print(frame[1], HEX);
  Serial.print(" ");
  Serial.print(frame[2], HEX);
  Serial.println();*/

  // Payload
  uint8_t cs = 0;  // CS=Sum of the payload
  cs+= frame[3] = 16; //Frame Type: Transmit Request
  /*Serial.print("Frame Type:" );
  Serial.print(frame[3], HEX);
  Serial.println();*/
  cs += frame[4] = FrameID++;   // frame id
  /*Serial.print("Frame ID:" );
  Serial.print(frame[4], HEX);
  Serial.println();*/
  cs += frame[5] = dest_addr[0]; //64-bit address
  cs += frame[6] = dest_addr[1];
  cs += frame[7] = dest_addr[2];
  cs += frame[8] = dest_addr[3];
  cs += frame[9] = dest_addr[4];
  cs += frame[10] = dest_addr[5];
  cs += frame[11] = dest_addr[6];
  cs += frame[12] = dest_addr[7];
  /*Serial.print("64-bit address:" );
  Serial.print(frame[5], HEX);
  Serial.print(" ");
  Serial.print(frame[6], HEX);
  Serial.print(" ");
  Serial.print(frame[7], HEX);
  Serial.print(" ");
  Serial.print(frame[8], HEX);
  Serial.print(" ");
  Serial.print(frame[9], HEX);
  Serial.print(" ");
  Serial.print(frame[10], HEX);
  Serial.print(" ");
  Serial.print(frame[11], HEX);
  Serial.print(" ");
  Serial.print(frame[12], HEX);
  Serial.println();*/

  cs += frame[13] = 0; //16-bit address
  cs += frame[14] = 0;
  /*Serial.print("16-bit address:" );
  Serial.print(frame[13], HEX);
  Serial.print(" ");
  Serial.print(frame[14], HEX);
  Serial.println();*/
  cs += frame[15] = 0; //broadcast radius - 0 = unlimited hop
  /*Serial.print("broadcast radius:" );
  Serial.print(frame[15], HEX);
  Serial.println();*/
  cs += frame[16] = 0; //options - 0 = default
  /*Serial.print("options:" );
  Serial.print(frame[16], HEX);
  Serial.println();*/
  // Data
  //Serial.print("data:" );
  for (int i=0; i<data_len; i++){
    cs += frame[17 + i] = data[i];
    /*Serial.print(frame[17+i], HEX); 
    Serial.print(" "); */
  }  
  //Serial.println();
  cs = 0XFF - cs;
  frame[17 + data_len] = cs;
  /*Serial.print("checksum:" );
  Serial.print(frame[17 + data_len], HEX); 
  Serial.println();*/
  
  return 17+data_len+1;
}


// Parse MeshBee frame (return data length)
int MB_FrameParse(uint8_t* frame, int frame_len, uint8_t* data, int data_max_len, uint16_t* src_addr)
{
  if ( frame_len != API_FRAME_LEN )  
  {
    // TODO: May be a valid frame, keep reading?
    return -1;  
  }
  // Delimeter?
  if (frame[0] != API_START_DELIMITER)  // Delimiter
  {
    return -2;
  }
  /*Serial.print("->API_START_DELIMITER: ");
  Serial.print(frame[0], HEX);
  Serial.println();
 
  Serial.print("->API_PAY_LEN: ");
  Serial.print(frame[1], HEX);
  Serial.print(" ");
  Serial.print(frame[2], HEX);
  Serial.println();*/

  
  // Payload
  int len;
  uint8_t cs = 0;  // CS=Sum of the payload
  cs += frame[3];   // frame id
  cs += frame[4]+frame[5]+frame[6]+frame[7]+frame[8]+frame[9]+frame[10]+frame[11]; //64-bit address
  cs += frame[12]+frame[13]; //16-bit address

  cs += frame[14]; //options

  for(int i=15;i<frame_len-1;i++){
    cs += frame[i];
  }
  cs = 0XFF -cs;
  /*Serial.print("CS COMP ");
  Serial.print(cs, HEX);
  Serial.print(" ");
  Serial.println(frame[frame_len-1], HEX);*/
   // Verify checksum
  if (cs != frame[frame_len-1])
  {
    return -6;
  }
  return (frame_len-1-15); 
}

void CheckSerial(){
  if(XBee.available()>0){
    delay(10);
    //Serial.println("XBee Available");
    uint8_t delimiter=XBee.read();
    if(delimiter!=0x7E)
      return;
    if(XBee.available()>0){
      uint8_t length1=XBee.read();
      uint8_t length2=XBee.read();
      //Serial.print(length1);
      //Serial.print(" ");
      //Serial.print(length2);
      int frameSize=(length1*16)+length2+1;
      //Serial.print(" ");
      //Serial.println(frameSize);
      uint8_t frameBuffer[frameSize];
      for(int i=0;i<frameSize;i++){
        delay(10);
        frameBuffer[i]=XBee.read();
        //Serial.print(frameBuffer[i],HEX);
        //Serial.print(" ");
      }
      //Serial.println("");
      bool cs=verifyChecksum(frameBuffer, frameSize);
      if(!cs){
        return -1;
      }
      switch(frameBuffer[0]){
        case 139:
          //Serial.println("Transmit Status");
          break;
        case 144:
          //Serial.println("Receive Packet");
          if(gwAdd[0]==0 && gwAdd[1]==0 && gwAdd[2]==0 && gwAdd[3]==0){
            gwAdd[0]=frameBuffer[1];
            gwAdd[1]=frameBuffer[2];
            gwAdd[2]=frameBuffer[3];
            gwAdd[3]=frameBuffer[4];
            gwAdd[4]=frameBuffer[5];
            gwAdd[5]=frameBuffer[6];
            gwAdd[6]=frameBuffer[7];
            gwAdd[7]=frameBuffer[8];
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
}



void MQTTSN_serial_send(uint8_t* message_buffer, int length)
{
  // Assuming that our gateway is at address 0 (coordinator)
  //Serial.print("SerialSend ");
  int len = MB_FrameCreate (message_buffer, length, gwAdd, FrameBufferOut, sizeof(FrameBufferOut), false);
  //Serial.println(len);
  if (len > 0)
  {
    XBee.write(FrameBufferOut, len);
    XBee.flush();
  }
}

bool verifyChecksum(uint8_t frameBuffer[], int frameSize){
  uint16_t cs=0x00;
  for(int i=0;i<frameSize;i++)
    cs+=frameBuffer[i];
  cs=cs&0xFF;
  if(cs==0xFF)
    return true;
  else
    return false;
}


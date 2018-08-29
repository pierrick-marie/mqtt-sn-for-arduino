package gateway;

import jssc.*;
import mqttsn.*;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.MQTT;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by arnaudoglaza on 03/07/2017.
 */
public class Main {

    public static int GatewayId = 1;

    public final static HashMap<String, MQTT> ClientMap = new HashMap<>();
    public final static HashMap<String, String> AddressClientMap = new HashMap<>();
    public final static HashMap<String, String> ClientState = new HashMap<>();
    public final static HashMap<String, Integer> ClientDuration = new HashMap<>();
    public final static HashMap<String, ArrayList<Message>> ClientBufferedMessage = new HashMap<>();
    public final static HashMap<String, CallbackConnection> AddressConnectiontMap = new HashMap<>();
    public final static HashMap<String, Integer> TopicName = new HashMap<>();
    public final static ArrayList<Integer> MessageIdAck = new ArrayList<>();
    public final static HashMap<String, Boolean> WillTopicAck = new HashMap<>();
    public final static HashMap<String, Boolean> WillMessageAck = new HashMap<>();

    public static SerialPort SerialPort = null;
    private static int MessageId = 0;

    public static void main(String[] args) {
        SerialPort = Serial.getSerial("/dev/ttyUSB0");
    }

    public static class SerialPortReader implements SerialPortEventListener {

        public void serialEvent(SerialPortEvent event) {
            int inputBufferSize;
            int totalInputSize = 0;
            if( event.isRXCHAR() ) {
                try {
                    inputBufferSize = SerialPort.getInputBufferBytesCount();
                    while( inputBufferSize > totalInputSize ){
                        totalInputSize = inputBufferSize;
                        Thread.sleep(100);
                        inputBufferSize = SerialPort.getInputBufferBytesCount();
                    }
                    checkDuplicate(SerialPort.readBytes(totalInputSize));
                } catch (SerialPortException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void checkDuplicate(byte[] buffer) throws URISyntaxException, InterruptedException {

        int indexOfByte = getFirstIndexforByte((byte)0X7E, buffer);

        if(indexOfByte == -1){
            if(verifyData(buffer)) {
                parseData(buffer);
            }
            return;
        }

        // for loop
        int i;

        while(indexOfByte != -1){

            byte[]temp = new byte[indexOfByte];
            byte[]newBuff = new byte[buffer.length-indexOfByte];

            for(i=0; i < temp.length; i++){
                temp[i]=buffer[i];
            }

            if(verifyData(temp)) {
                parseData(temp);
            }

            for(i=0; i < newBuff.length; i++) {
                newBuff[i] = buffer[indexOfByte + i];
            }

            buffer = newBuff;
            indexOfByte = getFirstIndexforByte((byte)0X7E, buffer);
            Thread.sleep(100);
        }
    }

    /**
     * The function returns the index of @searchedByte into @data.
     *
     * @param searchedByte The byte to search.
     * @param data The date to search into the @searchedByte.
     *
     * @return The index of @searedByte or -1 if not found.
     */
    public static int getFirstIndexforByte(byte searchedByte, byte[] data) {

        for(int i=1; i < data.length; i++){
            if(data[i] == searchedByte) {
                return i;
            }
        }

        return -1;
    }

    /**
     * The function checks if the first byte of @data is equals to 0x7E else returns false.
     * If ok, the functions returns the result of @verifyChecksum()
     *
     * @param data The data to verify.
     * @return True is the @data is OK, else false.
     */
    public static boolean verifyData(byte[] data) {

        if(data[0] != (byte)0x7E) {
            return false;
        }

        return verifyChecksum(data);
    }

    /**
     * The function verifies the checksum of the @data.
     *
     * @param data The data to verify the checksum.
     * @return True if the checksum is ok, else false.
     */
    public static boolean verifyChecksum(byte[] data) {

        int checksum = 0;

        // magic number
        for(int i=3; i < data.length; i++) {
            checksum += (data[i]&0xFF);
        }
        checksum = checksum&0xFF;

        if(checksum == 0xFF) {
            return true;
        }
        else {
            return false;
        }
    }

    public static void parseData(byte[] data) throws URISyntaxException, InterruptedException {

        if(data[3] == (byte)0x8B) {
            return;
        }

        byte address64[]=new byte[8];
        byte address16[]=new byte[2];
        int payload_length;
        int data_type;
        byte[] payload;
        byte[] message;

        // for loop
        int i;

        // Read the first 8 bytes
        for(i=0; i < 8; i++) {
            address64[i] = data[4 + i];
        }

        // Read the first 2 bytes
        for(i=0; i < 2; i++) {
            address16[i] = data[12+i];
        }

        // check the type of message
        if(data[15] == 0x01) {
            payload_length = (data[16] * 16) + data[17];
            data_type = data[18];
            payload = new byte[payload_length];
            for(i=19; i < data.length; i++) {
                payload[i] = data[i];
            }
        } else {
            payload_length=data[15];
            data_type = data[16];
            payload = new byte[payload_length];
            for(i=0; i < payload_length; i++) {
                payload[i] = data[15 + i];
            }
        }

        // Compute the message for each case of the following switch except for SEARCHGW
        message = new byte[payload_length-2];
        for(i=0; i < message.length; i++) {
            message[i] = payload[2 + i];
        }

        switch ( data_type ){
            case 0x01:
                //SEARCHGW
                SearchGW searchGW = new SearchGW(address64, address16, payload[2]);
                searchGW.start();
                break;

            case 0x04:
                //CONNECT
                Connect connect = new Connect(address64, address16, message);
                connect.start();
                break;

            case 0x07:
                //WILLTOPIC
                WillTopic willTopic = new WillTopic(address64, address16, message);
                willTopic.start();
                break;

            case 0x09:
                //WILLMESSAGE
                WillMessage willMessage = new WillMessage(address64, address16, message);
                willMessage.start();
                break;

            case 0x0A:
                //REGISTER
                Register register = new Register(address64, address16, message);
                register.start();
                break;

            case 0x12:
                //SUBSCRIBE
                Subscribe subscribe = new Subscribe(address64, address16, message);
                subscribe.start();
                break;

            case 0x18:
                //DISCONNECT
                Disconnect disconnect = new Disconnect(address64, address16, message);
                disconnect.start();
                break;

            case 0x0C:
                //PUBLISH
                Publish publish = new Publish(address64, address16, message);
                publish.start();
                break;

            case 0x0D:
                //PUBACK
                Puback puback = new Puback(address64, address16, message);
                puback.start();
                break;

            case 0x16:
                //PINGREQ
                Pingreq pingreq = new Pingreq(address64, address16, message);
                pingreq.start();
                break;
        }
    }
}


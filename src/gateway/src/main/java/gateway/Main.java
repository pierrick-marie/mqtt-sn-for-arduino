package gateway;

import jssc.*;
import mqttsn.*;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.MQTT;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by arnaudoglaza on 03/07/2017.
 */
public class Main {

    public static int gwId=1;
    public static HashMap<String, MQTT> clientMap;
    public static HashMap<String, String> addressClientMap;
    public static HashMap<String, String> clientState;
    public static HashMap<String, Integer> clientDuration;
    public static HashMap<String, ArrayList<Message>> clientBufferedMessage;
    public static HashMap<String, CallbackConnection> addressConnectiontMap;
    public static HashMap<String, Integer> topicName;
    static int msgID;
    public static ArrayList<Integer> msgIDack;
    public static HashMap<String, Boolean> willTopicAck;
    public static HashMap<String, Boolean> willMessageAck;

    public static SerialPort serialPort;

    public static void main(String[] args) {
        clientMap=new HashMap<>();
        clientState=new HashMap<>();
        addressClientMap=new HashMap<>();
        addressConnectiontMap=new HashMap<>();
        clientDuration=new HashMap<>();
        clientBufferedMessage=new HashMap<>();
        topicName=new HashMap<>();
        msgID=0;
        msgIDack=new ArrayList<>();
        willTopicAck=new HashMap<>();
        willMessageAck=new HashMap<>();
        serialPort=Serial.getSerial("/dev/tty.usbserial-AL008PBO");
    }

    static class SerialPortReader implements SerialPortEventListener {

        public void serialEvent(SerialPortEvent event) {
            int cpt=0;
            int temp;
            if(event.isRXCHAR()){
                try {
                    temp=serialPort.getInputBufferBytesCount();
                    while(temp>cpt){
                        cpt=temp;
                        Thread.sleep(100);
                        temp=serialPort.getInputBufferBytesCount();
                    }
                    byte buffer[] = serialPort.readBytes(cpt);
                    checkDuplicate(buffer);
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

    public static int getFirstIndexforByte(byte b, byte[] data){
        for(int i=1;i<data.length;i++){
            if(data[i]==b)
                return i;
        }
        return -1;
    }

    public static void checkDuplicate(byte[] buffer) throws URISyntaxException, InterruptedException {
        /*System.out.println("----------");
        for(int i=0;i<buffer.length;i++)
            System.out.print(String.format("%02X", buffer[i],StandardCharsets.UTF_8)+" ");
        System.out.println("");
        System.out.println("----------");*/
        int cpt=getFirstIndexforByte((byte)0X7E, buffer);
        if(cpt==-1){
            if(verifyData(buffer))
                parseData(buffer);
            return;
        }
        while(cpt!=-1){
            byte[]temp=new byte[cpt];
            byte[]newBuff=new byte[buffer.length-cpt];
            for(int i=0;i<temp.length;i++){
                temp[i]=buffer[i];
            }
            if(verifyData(temp))
                parseData(temp);
            for(int i=0;i<newBuff.length;i++)
                newBuff[i]=buffer[cpt+i];
            buffer=newBuff;
            cpt=getFirstIndexforByte((byte)0X7E, buffer);
            Thread.sleep(100);
        }
    }

    public static boolean verifyData(byte[] data){
        /*System.out.println("----------");
        for(int i=0;i<data.length;i++)
            System.out.print(String.format("%02X", data[i],StandardCharsets.UTF_8)+" ");
        System.out.println("");
        System.out.println("----------");*/
        if(data[0] != (byte)0x7E) {
            //System.err.println("Wrong delimiter: " + String.format("%02X", data[0]));
            return false;
        }
        return verifyChecksum(data);
    }

    public static boolean verifyChecksum(byte[] data){
        int cs=0;
        for(int i=3;i<data.length;i++) {
            cs += (data[i]&0xFF);
        }
        cs= cs&0xFF;
        if(cs==0xFF)
            return true;
        else{
            /*for(int i=0;i<data.length;i++)
                System.err.print(String.format("%02X", data[i])+" ");
            System.err.println("");
            System.err.println("Wrong Checksum");*/
            return false;
        }
    }

    public static void parseData(byte[] data) throws URISyntaxException, InterruptedException {
        //for(int i=0;i<data.length;i++)
        //    System.out.print(String.format("%02X", data[i])+" ");
        //System.out.println("");
        if(data[3]==(byte)0x8B)
            return;
        byte add64[]=new byte[8];
        byte add16[]=new byte[2];
        int payload_length;
        int data_type;
        byte[] payload;
        for(int i=0;i<8;i++)
            add64[i]=data[4+i];
        for(int i=0;i<2;i++)
            add16[i]=data[12+i];
        if(data[15]==0x01){
            payload_length=data[16]*16+data[17];
            data_type=data[18];
            payload=new byte[payload_length];
            for(int i=19;i<data.length;i++)
                payload[i]=data[i];
        }else{
            payload_length=data[15];
            data_type=data[16];
            payload=new byte[payload_length];
            for(int i=0;i<payload_length;i++)
                payload[i]=data[15+i];
        }
        byte[] msg;
        switch (data_type){
            case 0x01:
                //SEARCHGW
                SearchGW searchGW=new SearchGW(add64, add16, payload[2]);
                searchGW.start();
                //Mqttsn.SearchGW(add64, add16, payload[2]);
                break;
            case 0x04:
                //CONNECT
                msg=new byte[payload_length-2];
                for(int i=0;i<msg.length;i++)
                    msg[i]=payload[2+i];
                Connect connect=new Connect(add64, add16, msg);
                connect.start();
                //Mqttsn.connect(add64, add16, msg);
                break;
            case 0x07:
                //WILLTOPIC
                msg=new byte[payload_length-2];
                for(int i=0;i<msg.length;i++)
                    msg[i]=payload[2+i];
                WillTopic willTopic=new WillTopic(add64, add16, msg);
                willTopic.start();
                //Mqttsn.willtopic(add64, add16, msg);
                break;
            case 0x09:
                //WILLMESSAGE
                msg=new byte[payload_length-2];
                for(int i=0;i<msg.length;i++)
                    msg[i]=payload[2+i];
                WillMessage willMessage=new WillMessage(add64, add16, msg);
                willMessage.start();
                //Mqttsn.willmessage(add64, add16, msg);
                break;
            case 0x0A:
                //REGISTER
                msg=new byte[payload_length-2];
                for(int i=0;i<msg.length;i++){
                    msg[i]=payload[2+i];
                }
                Register register=new Register(add64, add16, msg);
                register.start();
                //Mqttsn.register(add64, add16, msg);
                break;
            case 0x12:
                //SUBSCRIBE
                msg=new byte[payload_length-2];
                for(int i=0;i<msg.length;i++)
                    msg[i]=payload[2+i];
                Subscribe subscribe=new Subscribe(add64, add16, msg);
                subscribe.start();
                //Mqttsn.subscribe(add64, add16, msg);
                break;
            case 0x18:
                //DISCONNECT
                msg=new byte[payload_length-2];
                for(int i=0;i<msg.length;i++)
                    msg[i]=payload[2+i];
                Disconnect disconnect=new Disconnect(add64, add16, msg);
                disconnect.start();
                //Mqttsn.disconnect(add64, add16, msg);
                break;
            case 0x0C:
                //PUBLISH
                msg=new byte[payload_length-2];
                for(int i=0;i<msg.length;i++)
                    msg[i]=payload[2+i];
                Publish publish=new Publish(add64, add16, msg);
                publish.start();
                //Mqttsn.publish(add64, add16, msg);
                break;
            case 0x0D:
                //PUBACK
                msg=new byte[payload_length-2];
                for(int i=0;i<msg.length;i++)
                    msg[i]=payload[2+i];
                Puback puback=new Puback(add64, add16, msg);
                puback.start();
                //Mqttsn.pubackClient(add64, add16, msg);
                break;
            case 0x16:
                //PINGREQ
                msg=new byte[payload_length-2];
                for(int i=0;i<msg.length;i++)
                    msg[i]=payload[2+i];
                Pingreq pingreq=new Pingreq(add64, add16, msg);
                pingreq.start();
                //Mqttsn.pingreq(add64, add16, msg);
                break;
        }
    }

    public static int getMsgID(){
        return msgID++;
    }
}

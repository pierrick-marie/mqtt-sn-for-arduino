package mqttsn;

import gateway.Main;
import gateway.Serial;
import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.CallbackConnection;
import utils.Utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by arnaudoglaza on 07/07/2017.
 */
public class Publish extends Thread {

    byte[] add64;
    byte[] add16;
    byte[] msg;
    private static final DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public Publish(byte[] add64, byte[] add16, byte[] msg) {
        this.add16=add16;
        this.add64=add64;
        this.msg=msg;
    }

    public void publish(){
        Date date = new Date();
        System.out.println(sdf.format(date)+": -> "+Main.AddressClientMap.get(Utils.byteArrayToString(add64))+" Publish");
        /*for(int i=0;i<message.length;i++)
            System.out.print(String.format("%02X", message[i])+" ");
        System.out.println("");*/
        byte flags=msg[0];
        boolean DUP=(flags&0b10000000)==1;
        int qos=flags&0b01100000>>5;
        boolean retain=(flags&0b00010000)==1;
        int topicIDType=flags&0b00000011;
        int topicID=(msg[2]<<8)+(msg[1]&0xFF);
        //System.out.println(topicID);
        byte[] msgID=new byte[2];
        msgID[0]=msg[3];
        msgID[1]=msg[4];
        //System.out.println("MSGID "+String.format("%02X", msgID[0])+" "+String.format("%02X", msgID[1]));
        byte[] data=new byte[msg.length-5];
        for(int i=0;i<data.length;i++) {
            data[i] = msg[5 + i];
        }
        //System.out.println(new String(data, StandardCharsets.UTF_8));
        //System.out.println(Main.TopicName.entrySet());
        //System.out.println(topicID);
        if(Main.TopicName.containsValue(topicID)){
            String topicName=getKeyByValue(Main.TopicName,topicID);
            CallbackConnection connection=Main.AddressConnectionMap.get(Utils.byteArrayToString(add64));
            if(connection!=null){
                connection.publish(topicName, data, Utils.getQoS(qos), retain, new Callback<Void>() {
                    @Override
                    public void onSuccess(Void value) {
                        puback(add64, add16, topicID, msgID, 0x00);
                    }

                    @Override
                    public void onFailure(Throwable value) {
                        Date date = new Date();
                        System.err.println(sdf.format(date)+": Error on publish");
                        value.printStackTrace();
                    }
                });
            }else{
                puback(add64, add16, topicID, msgID, 0x03);
            }
        }else{
            reregister(add64, add16, topicID, msgID);
        }
    }

    public static void reregister(byte[] add64, byte[] add16, int topicId, byte[] msgID){
        Date date = new Date();
        System.out.println(sdf.format(date)+": <- "+Main.AddressClientMap.get(Utils.byteArrayToString(add64))+" Reregister");
        byte[] ret=new byte[7];
        ret[0]=(byte)0x07;
        ret[1]=(byte)0x1E;
        if(topicId>255){
            ret[2]= (byte) (topicId/255);
            ret[3]= (byte) (topicId%255);
        }else{
            ret[2]=(byte)0x00;
            ret[3]= (byte) topicId;
        }
        ret[4]=msgID[0];
        ret[5]=msgID[1];
        ret[6]=(byte)0x00;
        Serial.write(Main.SerialPort, add64, add16, ret);
    }

    public static void puback(byte[] add64, byte[] add16, int topicId, byte[] msgID, int returnCode){
        Date date = new Date();
        System.out.println(sdf.format(date)+": <- "+Main.AddressClientMap.get(Utils.byteArrayToString(add64))+" Puback");
        byte[] ret=new byte[7];
        ret[0]=(byte)0x07;
        ret[1]=(byte)0x0D;
        if(topicId>255){
            ret[2]= (byte) (topicId/255);
            ret[3]= (byte) (topicId%255);
        }else{
            ret[2]=(byte)0x00;
            ret[3]= (byte) topicId;
        }
        ret[4]=msgID[0];
        ret[5]=msgID[1];
        ret[6]=(byte)returnCode;
        Serial.write(Main.SerialPort, add64, add16, ret);
    }

    public void run(){
        publish();
    }

    public String getKeyByValue(Map<String, Integer> map, int value) {
        String key="";
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                key=entry.getKey();
            }
        }
        return key;
    }


}

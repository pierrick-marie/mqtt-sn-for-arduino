package mqttsn;

import gateway.Main;
import gateway.Serial;
import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.Topic;

import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by arnaudoglaza on 07/07/2017.
 */
public class Subscribe extends Thread {

    byte[] add64;
    byte[] add16;
    byte[] msg;
    private static final DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public Subscribe(byte[] add64, byte[] add16, byte[] msg) {
        this.add16=add16;
        this.add64=add64;
        this.msg=msg;
    }

    public void subscribe(){
        Date date = new Date();
        System.out.println(sdf.format(date)+": -> "+Main.AddressClientMap.get(Utils.byteArrayToString(add64))+" Subscribe");
        byte flags=msg[0];
        //System.out.println("FLAGS "+String.format("%8s", Integer.toBinaryString(flags & 0xFF)).replace(' ', '0'));
        boolean DUP=(flags&0b10000000)==1;
        int qos=(flags&0b01100000)>>5;
        int topicIDType=flags&0b00000011;
        byte[] msgID=new byte[2];
        msgID[0]=msg[1];
        msgID[1]=msg[2];
        byte[] name=new byte[msg.length-3];
        for(int i=0;i<msg.length-3;i++)
            name[i]=msg[3+i];
        String topicName=new String(name, StandardCharsets.UTF_8);
        int topicID;
        //System.out.println(TopicName);
        if(Main.TopicName.containsKey(topicName)){
            topicID=Main.TopicName.get(topicName);
            //System.out.println("TOPICID "+topicID);
            CallbackConnection connection=Main.AddressConnectionMap.get(Utils.byteArrayToString(add64));
            Topic[] topics={new Topic(topicName, Utils.getQoS(qos))};
            final int finalTopicID = topicID;
            connection.subscribe(topics, new Callback<byte[]>() {
                @Override
                public void onSuccess(byte[] value) {
                    Main.ClientBufferedMessage.put(Utils.byteArrayToString(add64), new ArrayList<>());
                    suback(add64, add16, value, msgID, finalTopicID);
                }

                @Override
                public void onFailure(Throwable value) {
                    Date date = new Date();
                    System.err.println(sdf.format(date)+": Error on subscribe");
                    value.printStackTrace();
                    return;
                }
            });
        }else{
            date = new Date();
            System.err.println(sdf.format(date)+": Subscribe error: topic not registered");
            return;
        }
    }

    public static void suback(byte[] add64, byte[] add16, byte[] qoses, byte[] msgID, int topicID){
        Date date = new Date();
        System.out.println(sdf.format(date)+": <- "+Main.AddressClientMap.get(Utils.byteArrayToString(add64))+" Suback");
        byte[] ret=new byte[8];
        ret[0]=(byte)0x08;
        ret[1]=(byte)0x13;
        ret[2]=qoses[0];
        if(topicID>255){
            ret[3]= (byte) (topicID/255);
            ret[4]= (byte) (topicID%255);
        }else{
            ret[3]=(byte)0x00;
            ret[4]= (byte) topicID;
        }
        ret[5]=msgID[0];
        ret[6]=msgID[1];
        ret[7]=(byte)0x00;
        Serial.write(Main.SerialPort, add64, add16, ret);
    }

    public void run(){
        subscribe();
    }


}

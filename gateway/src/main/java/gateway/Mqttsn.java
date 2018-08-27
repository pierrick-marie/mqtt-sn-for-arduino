package gateway;

import org.fusesource.mqtt.client.*;

import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * Created by arnaudoglaza on 03/07/2017.
 */
public class Mqttsn {

    /*public static void searchGW(byte[] add64, byte[] add16, int radius){
        System.out.println("-> SearchGW");
        gwinfo(add64, add16);
    }

    public static void gwinfo(byte[] add64, byte[] add16){
        System.out.println("<- GwInfo");
        byte[] ret=new byte[3];
        ret[0]=(byte)0x03;
        ret[1]=(byte)0x02;
        ret[2]=(byte)Main.gwId;
        Serial.write(Main.serialPort, add64, add16, ret);
    }

    public static void connect(byte[] add64, byte[] add16, byte[] msg) throws URISyntaxException, InterruptedException {
        System.out.println("-> "+Main.addressClientMap.get(byteArrayToString(add64))+" Connect");
        byte flags=msg[0];
        //System.out.println("FLAGS "+String.format("%8s", Integer.toBinaryString(flags & 0xFF)).replace(' ', '0'));
        int protocolID=msg[1];
        short duration= (short) (msg[2]*16+msg[3]);
        boolean will=(flags>>3)==1;
        boolean cleansession=(flags>>2)==1;
        byte[] id=new byte[msg.length-4];
        for(int i=0;i<id.length;i++)
            id[i]=msg[4+i];
        String clientID=new String(id, StandardCharsets.UTF_8);
        if(Main.clientMap.containsKey(clientID)){
            if(Main.clientState.get(byteArrayToString(add64)).equals("Asleep")){
                System.out.println(Main.addressClientMap.get(byteArrayToString(add64))+" come back from sleep");
                Main.clientState.put(byteArrayToString(add64), "Active");
                Thread.sleep(1000);
                connack(add64, add16);
            }else{
                connack(add64, add16);
            }
        }else{
            if(will) {
                WillTopicReq willTopicReq=new WillTopicReq(add64, add16);
                willTopicReq.start();
                willTopicReq.join();
            }
            MQTT mqtt = new MQTT();
            mqtt.setHost("localhost", 1883);
            mqtt.setClientId(clientID);
            mqtt.setCleanSession(cleansession);
            mqtt.setKeepAlive(duration);
            CallbackConnection connection = mqtt.callbackConnection();
            Main.addressClientMap.put(byteArrayToString(add64), clientID);
            Main.clientMap.put(clientID, mqtt);
            Main.addressConnectiontMap.put(byteArrayToString(add64), connection);
            Main.clientState.put(byteArrayToString(add64),"Active");
            Mqtt_Listener listener=new Mqtt_Listener(add64);
            connection.listener(listener);
            connection.connect(new Callback<Void>() {
                @Override
                public void onSuccess(Void value) {
                    connack(add64, add16);
                }

                @Override
                public void onFailure(Throwable value) {
                    System.out.println("Failure on connect");
                    value.printStackTrace();
                }
            });
        }
    }

    public static void connack(byte[] add64, byte[] add16){
        System.out.println("<- "+Main.addressClientMap.get(byteArrayToString(add64))+" Connack");
        byte[] ret=new byte[3];
        ret[0]=(byte)0x03;
        ret[1]=(byte)0x05;
        ret[2]=(byte)0x00;
        Serial.write(Main.serialPort, add64, add16, ret);
    }*/

    /*public static void register(byte[] add64, byte[] add16, byte[] msg){
        System.out.println("-> "+Main.addressClientMap.get(byteArrayToString(add64))+" Register");
        //int topicID=msg[0]*16+msg[1];
        byte[] msgID=new byte[2];
        msgID[0]=msg[2];
        msgID[1]=msg[3];
        byte[] name=new byte[msg.length-4];
        for(int i=0;i<name.length;i++){
            name[i]=msg[4+i];
        }
        String topicName=new String(name, StandardCharsets.UTF_8);
        int id=-1;
        if(Main.topicName.contains(topicName)){
            for(int i=0;i<Main.topicName.size();i++)
                if(Main.topicName.get(i).equals(topicName)){
                    id=i;
                    break;
                }
        }else{
            Main.topicName.add(topicName);
            id=Main.topicName.size()-1;
        }
        if(id!=-1)
            regack(add64, add16, msgID, id);
        else
            System.err.println("Register Error: topicName");
    }

    public static void regack(byte[] add64, byte[] add16, byte[] msgID, int id){
        System.out.println("<- "+Main.addressClientMap.get(byteArrayToString(add64))+" Regack");
        byte[] ret=new byte[7];
        ret[0]=(byte)0x07;
        ret[1]=(byte)0x0B;
        if(id>255){
            ret[2]= (byte) (id/255);
            ret[3]= (byte) (id%255);
        }else{
            ret[2]=(byte)0x00;
            ret[3]= (byte) id;
        }
        ret[4]=msgID[0];
        ret[5]=msgID[1];
        ret[6]=(byte)0x00;
        Serial.write(Main.serialPort, add64, add16, ret);
    }*/

    /*public static void subscribe(byte[] add64, byte[] add16, byte[] msg){
        System.out.println("-> "+Main.addressClientMap.get(byteArrayToString(add64))+" Subscribe");
        int flags=msg[0];
        byte[] msgID=new byte[2];
        msgID[0]=msg[1];
        msgID[1]=msg[2];
        byte[] name=new byte[msg.length-3];
        for(int i=0;i<msg.length-3;i++)
            name[i]=msg[3+i];
        String topicName=new String(name, StandardCharsets.UTF_8);
        int topicID=-1;
        if(Main.topicName.contains(topicName)){
            for(int i=0;i<Main.topicName.size();i++)
                if(Main.topicName.get(i).equals(topicName))
                    topicID=i;
            CallbackConnection connection=Main.addressConnectiontMap.get(byteArrayToString(add64));
            Topic[] topics={new Topic(topicName, QoS.AT_LEAST_ONCE)};
            final int finalTopicID = topicID;
            connection.subscribe(topics, new Callback<byte[]>() {
                @Override
                public void onSuccess(byte[] value) {
                    Main.clientBufferedMessage.put(byteArrayToString(add64), new ArrayList<>());
                    suback(add64, add16, value, msgID, finalTopicID);
                }

                @Override
                public void onFailure(Throwable value) {
                    System.err.println("Error on subscribe");
                    value.printStackTrace();
                    return;
                }
            });
        }else{
            System.err.println("Subscribe error: topic not registered");
            return;
        }
    }

    public static void suback(byte[] add64, byte[] add16, byte[] qoses, byte[] msgID, int topicID){
        System.out.println("<- "+Main.addressClientMap.get(byteArrayToString(add64))+" Suback");
        byte[] ret=new byte[8];
        ret[0]=(byte)0x08;
        ret[1]=(byte)0x13;
        ret[2]=(byte)0x00;
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
        Serial.write(Main.serialPort, add64, add16, ret);
    }*/

    /*public static void disconnect(byte[] add64, byte[] add16, byte[] msg){
        System.out.println("-> "+Main.addressClientMap.get(byteArrayToString(add64))+" Disconnect");
        if(msg.length==4){
            int duration=(msg[0]<<8)+(msg[1]&0xFF);
            System.out.println("Duration: "+duration);
            Main.clientState.put(byteArrayToString(add64),"Asleep");
            Main.clientDuration.put(byteArrayToString(add64), duration);
            disconnectack(add64, add16);
            System.out.println(Main.addressClientMap.get(byteArrayToString(add64))+" going into sleep");
            TimeOut timeOut=new TimeOut(duration, add64);
            Threading.thread(timeOut, false);
        }else{
            //TODO REAL DISCONNECT
            Main.clientState.put(byteArrayToString(add64),"Disconnected");
        }
    }

    public static void disconnectack(byte[] add64, byte[] add16){
        System.out.println("<- "+Main.addressClientMap.get(byteArrayToString(add64))+" Disconnectack");
        byte[] ret=new byte[2];
        ret[0]=(byte)0x02;
        ret[1]=(byte)0x18;
        Serial.write(Main.serialPort, add64, add16, ret);
    }*/

    /*public static void publish(byte[] add64, byte[] add16, byte[] msg){
        System.out.println("-> "+Main.addressClientMap.get(byteArrayToString(add64))+" Publish");
        for(int i=0;i<msg.length;i++)
            System.out.print(String.format("%02X", msg[i])+" ");
        System.out.println("");
        int flags=msg[0];
        int topicID=(msg[1]<<8)+(msg[2]&0xFF);
        byte[] msgID=new byte[2];
        msgID[0]=msg[3];
        msgID[1]=msg[4];
        System.out.println("MSGID "+String.format("%02X", msgID[0])+" "+String.format("%02X", msgID[1]));
        byte[] data=new byte[msg.length-5];
        for(int i=0;i<data.length;i++) {
            data[i] = msg[5 + i];
        }
        System.out.println(new String(data, StandardCharsets.UTF_8));
        String topicName=Main.topicName.get(topicID);
        CallbackConnection connection=Main.addressConnectiontMap.get(byteArrayToString(add64));
        connection.publish(topicName, data, QoS.AT_LEAST_ONCE, false, new Callback<Void>() {
            @Override
            public void onSuccess(Void value) {
                puback(add64, add16, topicID, msgID);
            }

            @Override
            public void onFailure(Throwable value) {
                System.err.println("Error on publish");
                value.printStackTrace();
            }
        });
    }

    public static void puback(byte[] add64, byte[] add16, int topicId, byte[] msgID){
        System.out.println("<- "+Main.addressClientMap.get(byteArrayToString(add64))+" Puback");
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
        ret[6]=(byte)0x00;
        Serial.write(Main.serialPort, add64, add16, ret);
    }

    public static void pubackClient(byte[] add64, byte[] add16, byte[] msg){
        System.out.println("-> "+Main.addressClientMap.get(byteArrayToString(add64))+" PubackClient");
        if(msg[4]==(byte)0x00){
            System.out.println(String.format("%02X", msg[2])+" "+String.format("%02X", msg[3]));
            int msgID=(msg[3]<<8)+(msg[2]&0xFF);
            System.out.println("MSGIDRECEIVED "+msgID);
            Main.msgIDack.add(msgID);
        }
    }*/

    /*public static void pingreq(byte[] add64, byte[] add16, byte[] msg){
        String clientId=new String(msg, StandardCharsets.UTF_8);
        System.out.println("-> "+clientId+" Pingreq");
        if(Main.clientState.get(byteArrayToString(add64)).equals("Asleep")){
            Main.clientState.put(byteArrayToString(add64),"Awake");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            sendBufferedMessage(add64, add16);
        }
    }

    public static void sendBufferedMessage(byte[] add64, byte[] add16){
        System.out.println("SendBufferedMessage");
        System.out.println(Main.clientBufferedMessage.get(byteArrayToString(add64)).size()+" messages buffered");
        ArrayList<Message> toSend=Main.clientBufferedMessage.get(byteArrayToString(add64));
        MultipleSender msender=new MultipleSender(add64, add16, toSend);
        System.out.println("Before Msender");
        msender.start();
    }

    public static void pingresp(byte[] add64, byte[] add16){
        System.out.println("<- "+Main.addressClientMap.get(byteArrayToString(add64))+" Pingresp");
        byte[] ret=new byte[2];
        ret[0]=(byte)0x02;
        ret[1]=(byte)0x17;
        Serial.write(Main.serialPort, add64, add16, ret);
        if(Main.clientState.get(byteArrayToString(add64)).equals("Awake")) {
            Main.clientState.put(byteArrayToString(add64), "Asleep");
            System.out.println(Main.addressClientMap.get(byteArrayToString(add64))+" goes to sleep");
            TimeOut timeOut=new TimeOut(Main.clientDuration.get(byteArrayToString(add64)), add64);
            Threading.thread(timeOut, false);
        }
    }*/

    /*public static void willtopicreq(byte[] add64, byte[] add16){
        System.out.println("<- "+Main.addressClientMap.get(byteArrayToString(add64))+" Willtopicreq");
        byte[] ret=new byte[2];
        ret[0]=(byte)0x02;
        ret[1]=(byte)0x06;
        Serial.write(Main.serialPort, add64, add16, ret);
    }

    public static void willtopic(byte[] add64, byte[] add16, byte[] msg){
        System.out.println("-> "+Main.addressClientMap.get(byteArrayToString(add64))+" Willtopic");
        Main.willTopicAck.put(byteArrayToString(add64), false);
        if(msg.length==0){
            String clientID=Main.addressClientMap.get(byteArrayToString(add64));
            MQTT mqtt=Main.clientMap.get(clientID);
            mqtt.setWillTopic("");
            mqtt.setWillMessage("");
        }else{
            byte flags=msg[0];
            int will_QOS=flags&0b01100000;
            boolean will_retain=(flags&0b00010000)==1;
            byte[] data=new byte[msg.length-1];
            for(int i=0;i<msg.length-1;i++)
                data[i]=msg[i+1];
            String willtopic=new String(data, StandardCharsets.UTF_8);
            String clientID=Main.addressClientMap.get(byteArrayToString(add64));
            MQTT mqtt=Main.clientMap.get(clientID);
            mqtt.setWillTopic(willtopic);
            mqtt.setWillQos(getQoS(will_QOS));
            mqtt.setWillRetain(will_retain);
        }
    }

    public static void willmessagereq(byte[] add64, byte[] add16){
        System.out.println("<- "+Main.addressClientMap.get(byteArrayToString(add64))+" Willmessagereq");
        byte[] ret=new byte[2];
        ret[0]=(byte)0x02;
        ret[1]=(byte)0x08;
        Serial.write(Main.serialPort, add64, add16, ret);
    }

    public static void willmessage(byte[] add64, byte[] add16, byte[] msg){
        System.out.println("-> "+Main.addressClientMap.get(byteArrayToString(add64))+" Willmessage");
        String willmessage=new String(msg, StandardCharsets.UTF_8);
        String clientID=Main.addressClientMap.get(byteArrayToString(add64));
        MQTT mqtt=Main.clientMap.get(clientID);
        mqtt.setWillMessage(willmessage);
    }*/

    /*public static String byteArrayToString(byte[] array){
        String ret="";
        for(int i=0;i<array.length;i++)
            ret+=array[i];
        return ret;
    }

    public static byte[] getTopicId(String name){
        byte[] ret=new byte[2];
        int id=-1;
        for(int i=0;i<Main.topicName.size();i++)
            if(Main.topicName.get(i).equals(name))
                id=i;
        if(id!=-1){
            if(id>255){
                ret[0]= (byte) (id/255);
                ret[1]= (byte) (id%255);
            }else{
                ret[0]=(byte)0x00;
                ret[1]= (byte) id;
            }
        }else{
            return null;
        }
        return ret;
    }

    public static QoS getQoS(int qos){
        if(qos==0)
            return QoS.AT_MOST_ONCE;
        else if(qos==1)
            return QoS.AT_LEAST_ONCE;
        else if(qos==2)
            return  QoS.EXACTLY_ONCE;
        else
            return null;
    }*/
}

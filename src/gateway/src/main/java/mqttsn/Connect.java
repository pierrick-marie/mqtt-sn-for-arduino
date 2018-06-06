package mqttsn;

import gateway.Main;
import gateway.Mqtt_Listener;
import gateway.Serial;
import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.MQTT;

import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by arnaudoglaza on 07/07/2017.
 */
public class Connect extends Thread {

    private static final DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    byte[] add64;
    byte[] add16;
    byte[] msg;

    public Connect(byte[] add64, byte[] add16, byte[] msg) {
        this.add16=add16;
        this.add64=add64;
        this.msg=msg;
    }

    public void connect() throws URISyntaxException, InterruptedException {
        Date date = new Date();
        System.out.println(sdf.format(date)+": -> "+Main.addressClientMap.get(Utils.byteArrayToString(add64))+" Connect");
        byte flags=msg[0];
        int protocolID=msg[1];
        short duration= (short) (msg[2]*16+msg[3]);
        boolean will=(flags>>3)==1;
        boolean cleansession=(flags>>2)==1;
        byte[] id=new byte[msg.length-4];
        for(int i=0;i<id.length;i++)
            id[i]=msg[4+i];
        String clientID=new String(id, StandardCharsets.UTF_8);
        if(Main.clientMap.containsKey(clientID)){
            if(Main.clientState.get(Utils.byteArrayToString(add64)).equals("Asleep")){
                //System.out.println(Main.addressClientMap.get(Utils.byteArrayToString(add64))+" come back from sleep");
                Main.clientState.put(Utils.byteArrayToString(add64), "Active");
                Thread.sleep(10);
                connack(add64, add16, true);
            }else if(Main.clientState.get(Utils.byteArrayToString(add64)).equals("Lost")) {
                MQTT mqtt = new MQTT();
                mqtt.setHost("192.168.1.42", 1883);
                //mqtt.setHost("141.115.64.26", 1883);
                mqtt.setClientId(clientID);
                mqtt.setCleanSession(cleansession);
                mqtt.setKeepAlive(duration);
                CallbackConnection connection = mqtt.callbackConnection();
                Main.addressConnectiontMap.put(Utils.byteArrayToString(add64), connection);
                Main.clientState.put(Utils.byteArrayToString(add64),"Active");
                Mqtt_Listener listener=new Mqtt_Listener(add64);
                connection.listener(listener);
                if(will) {
                    //System.out.println("before topicreq");
                    WillTopicReq willTopicReq=new WillTopicReq(add64, add16);
                    willTopicReq.start();
                    willTopicReq.join();
                    //System.out.println("after topicreq");
                    WillMessageReq willMessageReq=new WillMessageReq(add64, add16);
                    willMessageReq.start();
                    willMessageReq.join();
                    //System.out.println("after msgreq");
                }
                connection.connect(new Callback<Void>() {
                    @Override
                    public void onSuccess(Void value) {
                        connack(add64, add16, true);
                    }

                    @Override
                    public void onFailure(Throwable value) {
                        Date date = new Date();
                        System.err.println(sdf.format(date)+": Failure on connect");
                        value.printStackTrace();
                    }
                });
            }else{
                connack(add64, add16, true);
            }
        }else{
            MQTT mqtt = new MQTT();
            mqtt.setHost("192.168.1.42", 1883);
            //mqtt.setHost("141.115.64.26", 1883);
            mqtt.setClientId(clientID);
            mqtt.setCleanSession(cleansession);
            mqtt.setKeepAlive(duration);
            CallbackConnection connection = mqtt.callbackConnection();
            Main.addressClientMap.put(Utils.byteArrayToString(add64), clientID);
            Main.clientMap.put(clientID, mqtt);
            Main.addressConnectiontMap.put(Utils.byteArrayToString(add64), connection);
            Main.clientState.put(Utils.byteArrayToString(add64),"Active");
            Mqtt_Listener listener=new Mqtt_Listener(add64);
            connection.listener(listener);
            if(will) {
                //System.out.println("before topicreq");
                WillTopicReq willTopicReq=new WillTopicReq(add64, add16);
                willTopicReq.start();
                willTopicReq.join();
                //System.out.println("after topicreq");
                WillMessageReq willMessageReq=new WillMessageReq(add64, add16);
                willMessageReq.start();
                willMessageReq.join();
                //System.out.println("after msgreq");
            }
            connection.connect(new Callback<Void>() {
                @Override
                public void onSuccess(Void value) {
                    connack(add64, add16, false);
                }

                @Override
                public void onFailure(Throwable value) {
                    Date date = new Date();
                    System.err.println(sdf.format(date)+": Failure on connect");
                    value.printStackTrace();
                }
            });
        }
    }

    public void connack(byte[] add64, byte[] add16, boolean isValid){
        Date date = new Date();
        System.out.println(sdf.format(date)+": <- "+Main.addressClientMap.get(Utils.byteArrayToString(add64))+" Connack");
        byte[] ret=new byte[3];
        ret[0]=(byte)0x03;
        ret[1]=(byte)0x05;
        if(isValid){
            ret[2]=(byte)0x00;
        }else{
            ret[2]=(byte)0x03;
        }
        Serial.write(Main.serialPort, add64, add16, ret);

    }

    public void run() {
        try {
            connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

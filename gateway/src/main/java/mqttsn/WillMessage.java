package mqttsn;

import gateway.Main;
import org.fusesource.mqtt.client.MQTT;

import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by arnaudoglaza on 07/07/2017.
 */
public class WillMessage extends Thread {

    byte[] add64;
    byte[] add16;
    byte[] msg;
    private static final DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public WillMessage(byte[] add64, byte[] add16, byte[] msg) {
        this.add64=add64;
        this.add16=add16;
        this.msg=msg;
    }

    public void willmessage(){
        Date date = new Date();
        System.out.println(sdf.format(date)+": -> "+Main.AddressClientMap.get(Utils.byteArrayToString(add64))+" Willmessage");
        Main.WillMessageAck.put(Utils.byteArrayToString(add64), false);
        String willmessage=new String(msg, StandardCharsets.UTF_8);
        String clientID=Main.AddressClientMap.get(Utils.byteArrayToString(add64));
        MQTT mqtt=Main.ClientMap.get(clientID);
        mqtt.setWillMessage(willmessage);
    }

    public void run() {
        willmessage();
    }
}

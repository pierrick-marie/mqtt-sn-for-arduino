package mqttsn;

import gateway.*;

import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by arnaudoglaza on 07/07/2017.
 */
public class Pingreq extends Thread {

    byte[] add64;
    byte[] add16;
    byte[] msg;
    private static final DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public Pingreq(byte[] add64, byte[] add16, byte[] msg) {
        this.add16=add16;
        this.add64=add64;
        this.msg=msg;
    }

    public void pingreq(){
        String clientId=new String(msg, StandardCharsets.UTF_8);
        Date date = new Date();
        System.out.println(sdf.format(date)+": -> "+clientId+" Pingreq");
        //if(Main.clientState.get(Utils.byteArrayToString(add64)).equals("Asleep")){
            Main.clientState.put(Utils.byteArrayToString(add64),"Awake");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            sendBufferedMessage(add64, add16);
        //}
    }

    public void sendBufferedMessage(byte[] add64, byte[] add16){
        //System.out.println("SendBufferedMessage");
        //System.out.println(Main.clientBufferedMessage.get(Utils.byteArrayToString(add64)).size()+" messages buffered");
        ArrayList<Message> toSend=Main.clientBufferedMessage.get(Utils.byteArrayToString(add64));
        MultipleSender msender=new MultipleSender(add64, add16, toSend);
        //System.out.println("Before Msender");
        msender.start();
    }

    public void run(){
        pingreq();
    }


}

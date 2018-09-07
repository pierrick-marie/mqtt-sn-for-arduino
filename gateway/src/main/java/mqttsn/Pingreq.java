package mqttsn;

import gateway.*;
import utils.Utils;

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
        //if(Main.ClientState.get(Utils.byteArrayToString(address64)).equals("Asleep")){
            Main.ClientState.put(Utils.byteArrayToString(add64), utils.State.AWAKE);
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
        //System.out.println(Main.ClientBufferedMessage.get(Utils.byteArrayToString(address64)).size()+" messages buffered");
        
        ArrayList<Message> toSend = Main.ClientBufferedMessage.get(Utils.byteArrayToString(add64));
        
        MultipleSender msender=new MultipleSender(add64, add16, toSend);
        //System.out.println("Before Msender");
        msender.start();
    }

    public void run(){
        pingreq();
    }


}

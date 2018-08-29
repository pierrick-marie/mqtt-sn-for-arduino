package mqttsn;

import gateway.Main;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by arnaudoglaza on 07/07/2017.
 */
public class Puback extends Thread {

    byte[] add64;
    byte[] add16;
    byte[] msg;
    private static final DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public Puback(byte[] add64, byte[] add16, byte[] msg) {
        this.add16=add16;
        this.add64=add64;
        this.msg=msg;
    }

    public void puback(){
        Date date = new Date();
        System.out.println(sdf.format(date)+": -> "+Main.AddressClientMap.get(Utils.byteArrayToString(add64))+" PubackClient");
        //for(int i=0;i<msg.length;i++){
        //    System.out.print(String.format("%02X", msg[i]));
        //}
        //System.out.println("");
        if(msg[4]==(byte)0x00){
            int msgID=(msg[3]<<8)+(msg[2]&0xFF);
            //System.out.println("MSGIDRECEIVED "+msgID);
            Main.MessageIdAck.add(msgID);
        }
    }

    public void run(){
        puback();
    }


}

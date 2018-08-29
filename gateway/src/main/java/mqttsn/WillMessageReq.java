package mqttsn;

import gateway.Main;
import gateway.Serial;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by arnaudoglaza on 07/07/2017.
 */
public class WillMessageReq extends Thread {

    byte[] add64;
    byte[] add16;
    private static final DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public WillMessageReq(byte[] add64, byte[] add16) {
        this.add64=add64;
        this.add16=add16;
    }

    public void willmessagereq(){
        Date date = new Date();
        System.out.println(sdf.format(date)+": <- "+ Main.AddressClientMap.get(Utils.byteArrayToString(add64))+" Willmessagereq");
        byte[] ret=new byte[2];
        ret[0]=(byte)0x02;
        ret[1]=(byte)0x08;
        Main.WillMessageAck.put(Utils.byteArrayToString(add64), true);
        Serial.write(Main.SerialPort, add64, add16, ret);
        int cpt=0;
        while(cpt<10 && Main.WillMessageAck.get(Utils.byteArrayToString(add64))){
            try {
                Thread.sleep(1000);
                cpt++;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if(Main.WillMessageAck.get(Utils.byteArrayToString(add64))){
            //System.out.println("Resend willmessagereq");
            willmessagereq();
        }
    }

    public void run() {
        willmessagereq();
    }
}

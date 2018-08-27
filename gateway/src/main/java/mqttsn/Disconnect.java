package mqttsn;

import gateway.Main;
import gateway.Serial;
import gateway.Threading;
import gateway.TimeOut;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by arnaudoglaza on 07/07/2017.
 */
public class Disconnect extends Thread {

    byte[] add64;
    byte[] add16;
    byte[] msg;
    private static final DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public Disconnect(byte[] add64, byte[] add16, byte[] msg) {
        this.add16=add16;
        this.add64=add64;
        this.msg=msg;
    }

    public void disconnect(){
        Date date = new Date();
        System.out.println(sdf.format(date)+": -> "+ Main.addressClientMap.get(Utils.byteArrayToString(add64))+" Disconnect");
        if(msg.length==4){
            int duration=(msg[0]<<8)+(msg[1]&0xFF);
            //System.out.println("Duration: "+duration);
            if(duration>0){
                Main.clientState.put(Utils.byteArrayToString(add64),"Asleep");
                Main.clientDuration.put(Utils.byteArrayToString(add64), duration);
                disconnectack(add64, add16);
                //System.out.println(Main.addressClientMap.get(Utils.byteArrayToString(add64))+" going into sleep");
                TimeOut timeOut=new TimeOut(duration, add64);
                Threading.thread(timeOut, false);
            }
        }else{
            //TODO REAL DISCONNECT
            Main.clientState.put(Utils.byteArrayToString(add64),"Disconnected");
            disconnectack(add64, add16);
        }
    }

    public static void disconnectack(byte[] add64, byte[] add16){
        Date date = new Date();
        System.out.println(sdf.format(date)+": <- "+Main.addressClientMap.get(Utils.byteArrayToString(add64))+" Disconnectack");
        byte[] ret=new byte[2];
        ret[0]=(byte)0x02;
        ret[1]=(byte)0x18;
        Serial.write(Main.serialPort, add64, add16, ret);
    }

    public void run(){
        disconnect();
    }


}

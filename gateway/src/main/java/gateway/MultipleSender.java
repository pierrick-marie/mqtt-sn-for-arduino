package gateway;

import mqttsn.Utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by arnaudoglaza on 04/07/2017.
 */
public class MultipleSender extends Thread {

    byte[] add64;
    byte[] add16;
    ArrayList<Message> messageList;
    private static final DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public MultipleSender(byte[] add64, byte[] add16, ArrayList<Message> messageList) {
        this.add64=add64;
        this.add16=add16;
        this.messageList=messageList;
    }

    public void run() {
        //System.out.println("Begin Multiple Sender");
        for(int i=0;i<messageList.size();i++){
            //System.out.println("Starting to send message n°"+i);
            Sender sender=new Sender(add64, add16, messageList.get(i));
            //System.out.println("Before "+i+" sender");
            sender.start();
            try {
                sender.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //System.out.println("After "+i+" sender");
        }
        for(int i=0;i<messageList.size();i++)
            Main.clientBufferedMessage.put(Utils.byteArrayToString(add64),new ArrayList<>());
        //System.out.println("End of Msender");
        pingresp(add64, add16);
    }

    public  void pingresp(byte[] add64, byte[] add16){
        Date date = new Date();
        System.out.println(sdf.format(date)+": <- "+Main.addressClientMap.get(Utils.byteArrayToString(add64))+" Pingresp");
        byte[] ret=new byte[2];
        ret[0]=(byte)0x02;
        ret[1]=(byte)0x17;
        Serial.write(Main.serialPort, add64, add16, ret);
        if(Main.clientState.get(Utils.byteArrayToString(add64)).equals("Awake")) {
            Main.clientState.put(Utils.byteArrayToString(add64), "Asleep");
            //System.out.println(Main.addressClientMap.get(Utils.byteArrayToString(add64))+" goes to sleep");
            if(Main.clientDuration.get(Utils.byteArrayToString(add64)) != null){
                TimeOut timeOut=new TimeOut(Main.clientDuration.get(Utils.byteArrayToString(add64)), add64);
                Threading.thread(timeOut, false);
            }
        }
    }

}
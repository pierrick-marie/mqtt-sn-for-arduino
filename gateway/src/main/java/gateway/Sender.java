package gateway;

import mqttsn.Utils;

/**
 * Created by arnaudoglaza on 04/07/2017.
 */
public class Sender extends Thread {

    byte[] add64;
    byte[] add16;
    Message message;

    public Sender(byte[] add64, byte[] add16, Message message) {
        this.add64=add64;
        this.add16=add16;
        this.message=message;
    }

    public void sendMessage(Message msg){
        byte[] ret=new byte[7+msg.getBody().length()];
        ret[0]=(byte)ret.length;
        ret[1]=(byte)0x0C;
        ret[2]=(byte)0x00;
        System.out.println(msg.getTopic());
        ret[3]= Utils.getTopicId(msg.getTopic())[0];
        ret[4]=Utils.getTopicId(msg.getTopic())[1];
        int msgID=Main.getMessageId();
        //System.out.println("Send message with MessageId "+MessageId);
        if(msgID>255){
            ret[5]= (byte) (msgID/256);
            ret[6]= (byte) (msgID%256);
        }else{
            ret[5]=(byte)0x00;
            ret[6]= (byte) msgID;
        }
        byte[] data=msg.getBody().getBytes();
        for(int i=0;i<msg.body.length();i++)
            ret[7+i]=data[i];
        Serial.write(Main.SerialPort, add64, add16, ret);
        int cpt=0;
        while(cpt<10 && !Main.MessageIdAck.contains(msgID)){
            try {
                Thread.sleep(1000);
                cpt++;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if(!Main.MessageIdAck.contains(msgID)){
            //System.out.println("Resend message");
            sendMessage(msg);
        }
    }

    public void run() {
        sendMessage(message);
    }
}

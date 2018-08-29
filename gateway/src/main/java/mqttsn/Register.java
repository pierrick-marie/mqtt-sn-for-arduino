package mqttsn;

import gateway.Main;
import gateway.Serial;

import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by arnaudoglaza on 07/07/2017.
 */
public class Register extends Thread {

    byte[] add64;
    byte[] add16;
    byte[] msg;
    private static final DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public Register(byte[] add64, byte[] add16, byte[] msg) {
        this.add16=add16;
        this.add64=add64;
        this.msg=msg;
    }

    public void register() {

        Date date = new Date();

        System.out.println(sdf.format(date)+": -> "+ Main.AddressClientMap.get(Utils.byteArrayToString(add64))+" Register");

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
        if(Main.TopicName.containsKey(topicName)){
            id=Main.TopicName.get(topicName);
            System.out.println(sdf.format(date)+": TOPIC NAME IS CONTAINED -> "+topicName+" "+id);
        }else{
            id=Main.TopicName.size();
            System.out.println(sdf.format(date)+": TOPIC NAME IS NOT CONTAINED -> "+topicName+" "+id);
            Main.TopicName.put(topicName, id);
        }
        if(id!=-1)
            regack(add64, add16, msgID, id);
        else{
            date = new Date();
            System.err.println(sdf.format(date)+": Register Error: TopicName");
        }
    }

    public void regack(byte[] add64, byte[] add16, byte[] msgID, int id){
        Date date = new Date();
        System.out.println(sdf.format(date)+": <- "+Main.AddressClientMap.get(Utils.byteArrayToString(add64))+" Regack");
        byte[] ret=new byte[7];
        ret[0]=(byte)0x07;
        ret[1]=(byte)0x0B;
        if(id>255){
            ret[3]= (byte) (id/255);
            ret[2]= (byte) (id%255);
        }else{
            ret[3]= (byte) id;
            ret[2]=(byte)0x00;
        }
        ret[4]=msgID[0];
        ret[5]=msgID[1];
        ret[6]=(byte)0x00;
        Serial.write(Main.SerialPort, add64, add16, ret);
    }



    public void run(){
        register();
    }


}

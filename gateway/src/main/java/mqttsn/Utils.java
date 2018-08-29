package mqttsn;

import gateway.Main;
import org.fusesource.mqtt.client.QoS;

/**
 * Created by arnaudoglaza on 07/07/2017.
 */
public class Utils {

    public static String byteArrayToString(byte[] array){
        String ret="";
        for(int i=0;i<array.length;i++)
            ret+=array[i];
        return ret;
    }

    public static byte[] getTopicId(String name){
        byte[] ret=new byte[2];
        int id=-1;
        /*for(int i = 0; i< Main.TopicName.size(); i++) {
            System.out.println(i+" "+Main.TopicName.get(i)+" "+name);
            if (Main.TopicName.get(i).equals(name))
                id = i;
        }*/
        id = Main.TopicName.get(name);
        if(id!=-1){
            if(id>255){
                ret[0]= (byte) (id/255);
                ret[1]= (byte) (id%255);
            }else{
                ret[0]=(byte)0x00;
                ret[1]= (byte) id;
            }
        }else{
            return null;
        }
        return ret;
    }

    public static QoS getQoS(int qos){
        if(qos==0)
            return QoS.AT_MOST_ONCE;
        else if(qos==1)
            return QoS.AT_LEAST_ONCE;
        else if(qos==2)
            return  QoS.EXACTLY_ONCE;
        else
            return null;
    }
}

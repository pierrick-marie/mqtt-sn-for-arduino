package mqttsn;

import gateway.Main;
import gateway.Serial;
import utils.Log;
import utils.Utils;

import java.nio.charset.StandardCharsets;

/**
 * Created by arnaudoglaza on 07/07/2017.
 */
public class Register extends Thread {

    byte[] address64;
    byte[] address16;
    byte[] message;

    public Register(byte[] address64, byte[] address16, byte[] message) {

        this.address16 = address16;
        this.address64 = address64;
        this.message = message;
    }

    /**
     * This method registers a topic name into the list of topics @see:Main.TopicName
     * The method does not register the topic name to the bus.
     */
    public void register() {

        Log.print(" -> " + Main.AddressClientMap.get(Utils.byteArrayToString(address64))+" Register");

        byte[] messageId = new byte[2];
        messageId[0]= message[2];
        messageId[1]= message[3];
        byte[] name = new byte[message.length-4];
        String topicName;
        // i: for loop
        int i, topicId = -1;

        for(i=0; i < name.length; i++) {
            name[i]= message[4+i];
        }
        topicName = new String(name, StandardCharsets.UTF_8);

        if( Main.TopicName.containsKey(topicName) ) {
            topicId = Main.TopicName.get( topicName );
            Log.print("TOPIC NAME IS CONTAINED -> " + topicName + " " + topicId);
        } else {
            topicId = Main.TopicName.size();
            Log.print("TOPIC NAME IS NOT CONTAINED -> " + topicName + " " + topicId);
            Main.TopicName.put( topicName, topicId );
        }
        if( topicId != -1)
            regack(address64, address16, messageId, topicId);
        else{
            Log.print("Register Error: TopicName");
        }
    }

    /**
     * The method sends regack message to the XBee module.
     *
     * @param address64
     * @param address16
     * @param messageId
     * @param topicId
     */
    public void regack(byte[] address64, byte[] address16, byte[] messageId, int topicId) {

        Log.print(" <- " + Main.AddressClientMap.get(Utils.byteArrayToString(address64)) + " Regack");

        // the message to send
        byte[] message = new byte[7];

        message[0] = (byte)0x07;
        message[1] = (byte)0x0B;
        if( topicId > 255 ){
            message[3] = (byte) (topicId/255);
            message[2] = (byte) (topicId%255);
        } else {
            message[3] = (byte) topicId;
            message[2] =(byte)0x00;
        }
        message[4] = messageId[0];
        message[5] = messageId[1];
        message[6] = (byte)0x00;

        Serial.write(Main.SerialPort, address64, address16, message);
    }

    public void run(){
        register();
    }


}

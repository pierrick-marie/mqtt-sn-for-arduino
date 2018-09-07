package gateway;

import utils.Utils;
import utils.Log;

/**
 * Created by arnaudoglaza on 04/07/2017.
 */
public class Sender extends Thread {

    private final byte[] address64;
    private final byte[] address16;
    private final Message message;

    public Sender(byte[] address64, byte[] address16, Message message) {

        this.address64 = address64;
        this.address16 = address16;
        this.message = message;
    }

    public void sendMessage(){

        byte[] serialMessage = new byte[7+message.getBody().length()];
        byte[] data = message.getBody().getBytes();
        int nbTry = 0;
        // for loop
        int i;

        // creating the serial message to send

        serialMessage[0] = (byte)serialMessage.length;
        serialMessage[1] = (byte)0x0C;
        serialMessage[2] = (byte)0x00;

        Log.print( "Sender.sendMessage() topic => " + message.getTopic() );

        serialMessage[3] = Utils.getTopicId(message.getTopic())[0];
        serialMessage[4] = Utils.getTopicId(message.getTopic())[1];

        if( Main.MessageId > 255 ) {
            serialMessage[5] = (byte) (Main.MessageId / 256);
            serialMessage[6] = (byte) (Main.MessageId % 256);
        } else {
            serialMessage[5] = (byte)0x00;
            serialMessage[6] = (byte) Main.MessageId;
        }

        for(i=0; i < message.body.length(); i++) {
            serialMessage[7 + i] = data[i];
        }
        // sending the message
        Serial.write(Main.SerialPort, address64, address16, serialMessage);

        // waiting for an acquittal
        while( nbTry < 10 && !Main.MessageIdAck.contains(Main.MessageId) ) {
            try {
                Thread.sleep(1000);
                nbTry++;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // the message has not been acquit -> resend
        if( !Main.MessageIdAck.contains(Main.MessageId) ){
            Log.debug("Sender", "sendMessage", "Resend the message");
            sendMessage();
        }
    }

    public void run() {
        sendMessage();
    }
}

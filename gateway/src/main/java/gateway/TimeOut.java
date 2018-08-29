package gateway;

import mqttsn.Utils;
import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.CallbackConnection;

import java.beans.ExceptionListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by arnaudoglaza on 03/07/2017.
 */
public class TimeOut implements Runnable, ExceptionListener {

    int duration;
    byte[] add64;
    private static final DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public TimeOut(int duration, byte[] add64) {
        this.duration=duration;
        this.add64=add64;
    }

    public void start() {
        for(int i=0;i<duration;i++){
            //System.out.println((duration-i)+" "+Main.AddressClientMap.get(Utils.byteArrayToString(address64))+" "+Main.ClientState.get(Utils.byteArrayToString(address64)));
            if(!Main.ClientState.get(Utils.byteArrayToString(add64)).equals("Asleep")){
                System.out.println(Main.AddressClientMap.get(Utils.byteArrayToString(add64))+" not asleep anymore");
                return;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        clientTimeOut(add64);
    }

    public void clientTimeOut(byte[] add64){
        Main.ClientState.put(Utils.byteArrayToString(add64), "Lost");
        CallbackConnection connection=Main.AddressConnectiontMap.get(Utils.byteArrayToString(add64));
        connection.disconnect(new Callback<Void>() {
            @Override
            public void onSuccess(Void value) {
                //TODO
            }

            @Override
            public void onFailure(Throwable value) {
                Date date = new Date();
                System.err.println(sdf.format(date)+": Error on clientTimeout disconnect");
                value.printStackTrace();
            }
        });
    }

    @Override
    public void run() {
        this.start();
    }

    @Override
    public void exceptionThrown(Exception e) {
        e.printStackTrace();
    }
}

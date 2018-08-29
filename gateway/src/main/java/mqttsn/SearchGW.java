package mqttsn;

import gateway.Main;
import gateway.Serial;
import utils.Log;

/**
 * Created by arnaudoglaza on 07/07/2017.
 */
public class SearchGW extends Thread {

    byte[] add64;
    byte[] add16;
    int radius;

    public SearchGW(byte[] add64, byte[] add16, int radius) {
        this.add16 = add16;
        this.add64 = add64;
        this.radius = radius;
    }

    public void searchGW(){

        Log.print(": -> SearchGW");

        byte[] ret=new byte[3];
        ret[0]=(byte)0x03;
        ret[1]=(byte)0x02;
        ret[2]=(byte) Main.GatewayId;

        Serial.write(Main.SerialPort, add64, add16, ret);
    }

    public void run() {
        searchGW();
    }
}

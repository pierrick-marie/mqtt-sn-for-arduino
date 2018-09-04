package mqttsn;

import gateway.Main;
import gateway.Serial;
import utils.Log;

/**
 * Created by arnaudoglaza on 07/07/2017.
 */
public class SearchGateway extends Thread {

    byte[] address64;
    byte[] address16;
    int radius;

    public SearchGateway(byte[] address64, byte[] address16, int radius) {

        this.address16 = address16;
        this.address64 = address64;
        this.radius = radius;
    }

    public void searchGateway() {

        Log.print(" -> Search gateway");

        byte[] ret=new byte[3];
        ret[0]=(byte)0x03;
        ret[1]=(byte)0x02;
        ret[2]=(byte) Main.GatewayId;

        Serial.write(Main.SerialPort, address64, address16, ret);
    }

    public void run() {
        searchGateway();
    }
}

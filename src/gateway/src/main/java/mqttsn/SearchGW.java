package mqttsn;

import gateway.Main;
import gateway.Serial;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by arnaudoglaza on 07/07/2017.
 */
public class SearchGW extends Thread {

    byte[] add64;
    byte[] add16;
    int radius;
    private static final DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public SearchGW(byte[] add64, byte[] add16, int radius) {
        this.add16=add16;
        this.add64=add64;
        this.radius=radius;
    }

    public void searchGW(){
        Date date = new Date();
        System.out.println(sdf.format(date)+": -> SearchGW");
        gwinfo(add64, add16);
    }

    public static void gwinfo(byte[] add64, byte[] add16){
        Date date = new Date();
        System.out.println(sdf.format(date)+": <- GwInfo");
        byte[] ret=new byte[3];
        ret[0]=(byte)0x03;
        ret[1]=(byte)0x02;
        ret[2]=(byte) Main.gwId;
        Serial.write(Main.serialPort, add64, add16, ret);
    }

    public void run() {
        searchGW();
    }
}

package gateway;

import gateway.utils.Config;

/**
 * Created by arnaudoglaza on 03/07/2017.
 */
public class Main {

	public static final int GATEWAY_ID = 1;

	public static void main(String[] args) {

		Config.instance.parseArgs(args);

		// Log.print("Starting the gateway \n * Serial: " + Config.SERIAL_PORT + "\n * IP server: " + Config.IP_SERVER + " \n * Port server: " + Config.PORT_SERVER);

		// new SerialPortReader();

        /*
        try {
            MqttClient client = new MqttClient();
            client.connect();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        */

	}
}


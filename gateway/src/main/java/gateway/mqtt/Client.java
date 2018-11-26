package gateway.mqtt;

import gateway.mqtt.client.Device;
import gateway.utils.Config;
import gateway.utils.Time;
import gateway.utils.log.Log;
import gateway.utils.log.LogLevel;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.fusesource.mqtt.codec.MessageSupport;

import static gateway.mqtt.sn.Prtcl.PAYLOAD_LENGTH;

public class Client implements MqttCallback, IClient, Runnable {

    private final String PRTCL = "tcp://";
    private final Integer SLEEP_TIME = 5; // seconds

    private final Device device;
    private final Boolean cleanSession;

    private Boolean isStarted = false;
    private Boolean isConnected = false;
    private MqttClient mqttClient;
    private MqttConnectOptions option;

    public Client(final Device device, final Boolean cleanSession) {

        this.device = device;
        this.cleanSession = cleanSession;

        try {
            mqttClient = new MqttClient(PRTCL + Config.IP_SERVER + ":" + Config.PORT_SERVER, device.name(), new MemoryPersistence());
            option = new MqttConnectOptions();
            option.setCleanSession(cleanSession);
        } catch (MqttException e) {
            Log.error("Client", "constructor", e.getMessage());
        }

        /*
        String topic = "MQTT Examples";
        String content = "Message from MqttPublishSample";
        int qos = 2;

        String clientId = "JavaSample";
        MemoryPersistence persistence = ;

        try {



            sampleClient.subscribe(topic);
            sampleClient.setCallback(this);

            DMqMessage message = new DMqMessage(content.getBytes());
            message.setQos(qos);
            sampleClient.publish(topic, message);

            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("Message published");
            sampleClient.disconnect();
            System.out.println("Disconnected");
            System.exit(0);
        } catch (MqttException me) {
            System.out.println("reason " + me.getReasonCode());
            System.out.println("msg " + me.getMessage());
            System.out.println("loc " + me.getLocalizedMessage());
            System.out.println("cause " + me.getCause());
            System.out.println("excep " + me);
            me.printStackTrace();
        }
        */
    }

    @Override
    public void connectionLost(Throwable throwable) {

        Log.error("Client", "connect", "NOT IMPLEMENTED YET");

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {

        if (message.getPayload().length < PAYLOAD_LENGTH) {
            Log.debug(LogLevel.VERBOSE, "Client", "messageArrived", "message: " + message.getPayload() + " on topic: " + topic);
            device.addMqttMessage(new MqMessage(topic, new String(message.getPayload())));
        } else {
            Log.error("Client", "messageArrived", "payload too long");
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

        Log.error("Client", "connect", "NOT IMPLEMENTED YET");

    }

    @Override
    public Boolean connect() {

        Log.debug(LogLevel.VERBOSE, "Client", "connect", "try to connect to the gateway.mqtt broker");

        try {
            mqttClient.connect();
        } catch (MqttException e) {
            Log.error("Client", "connect", e.getMessage());
            isConnected = false;
            return isConnected;
        }

        Log.debug(LogLevel.VERBOSE, "Client", "connect", "connected to the gateway.mqtt broker");
        isConnected = true;
        return isConnected;
    }

    @Override
    public Boolean subscribe(SnTopic topic) {

        if(!isStarted) {
            new Thread(this).start();
        }

        try {
            mqttClient.subscribe(topic.toString());
            mqttClient.setCallback(this);
            topic.setSubscribed();
        } catch (MqttException e) {
            Log.error("Client", "subscribe", e.getMessage());
            return false;
        }

        return true;
    }

    @Override
    public Boolean publish(SnTopic topic, String message, Boolean retain) {

        try {
            mqttClient.publish(topic.name().toString(), new MqttMessage(message.getBytes()));
            Log.debug(LogLevel.VERBOSE, "Client", "publish", "Publish message: " + message + " on the topic: " + topic);
            return true;
        } catch (MqttException e) {
            Log.error("Client", "publish", "Impossible to publish the message: " + message);
            Log.debug(LogLevel.VERBOSE, "Client", "publish", e.getMessage());
            return false;
        }
    }

    @Override
    public Boolean isConnected() {
        return isConnected;
    }

    @Override
    public Boolean disconnect() {

        isConnected = false;

        try {
            mqttClient.disconnect();
        } catch (MqttException e) {
            Log.error("Client", "disconnect", e.getMessage());
            return false;
        }

        return true;
    }

    @Override
    public void run() {
        // wait until receiving messages -> messageArrived
        isStarted = true;
        while(isConnected) {
            try {
                Thread.sleep(SLEEP_TIME * 1000);
            } catch (InterruptedException e) {
                Log.error("Client", "run", e.getMessage());
            }
        }
    }
}

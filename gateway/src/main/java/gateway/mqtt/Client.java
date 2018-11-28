package gateway.mqtt;

import gateway.mqtt.client.Device;
import gateway.mqtt.sn.Prtcl;
import gateway.utils.Config;
import gateway.utils.log.Log;
import gateway.utils.log.LogLevel;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import static gateway.mqtt.sn.Prtcl.PAYLOAD_LENGTH;

public class Client implements MqttCallback, IClient, Runnable {

    private final String PRTCL = "tcp://";
    private final Integer SLEEP_TIME = 5; // seconds

    private final Device device;
    private final Boolean cleanSession;

    private Boolean isStarted = false;
    private MqttClient mqttClient;
    private MqttConnectOptions option;

    public Client(final Device device, final Boolean cleanSession) {

        this.device = device;
        this.cleanSession = cleanSession;

        try {
            mqttClient = new MqttClient(PRTCL + Config.IP_SERVER + ":" + Config.PORT_SERVER, device.getName(), new MemoryPersistence());
            option = new MqttConnectOptions();
            option.setCleanSession(cleanSession);
        } catch (MqttException e) {
            Log.error("Client", "constructor", e.getMessage());
        }
    }

    @Override
    public void connectionLost(Throwable throwable) {

        Log.error("Client", "connectionLost", throwable.getMessage());

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {

        if (message.getPayload().length < PAYLOAD_LENGTH) {
            Log.debug(LogLevel.VERBOSE, "Client", "messageArrived", "message: " + new String(message.getPayload()) + " on topic: " + topic);
            device.addMqttMessage(new MqMessage(topic, new String(message.getPayload())));
        } else {
            Log.error("Client", "messageArrived", "payload too long");
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

        try {
            Log.debug(LogLevel.VERBOSE,"Client", "deliveryComplete", iMqttDeliveryToken.getMessage().toString());
        } catch (MqttException e) {
            Log.error("Client", "deliveryComplete", e.getMessage());
        }
    }

    @Override
    public Boolean connect() {

        Log.debug(LogLevel.VERBOSE, "Client", "connect", "try to connect to the gateway.mqtt broker");

        if(mqttClient.isConnected()) {
            return true;
        }

        try {
            mqttClient.connect();
        } catch (MqttException e) {
            Log.error("Client", "connect", e.getMessage());
            Log.error("Client", "connect", e.getCause().getMessage());
        }

        Log.debug(LogLevel.VERBOSE, "Client", "connect", device.getName() + " connected");
        return mqttClient.isConnected();
    }

    @Override
    public Boolean subscribe(SnTopic topic) {

        if(!isStarted) {
            new Thread(this).start();
        }

        try {
            mqttClient.subscribe(topic.toString(), Prtcl.DEFAULT_QOS);
            mqttClient.setCallback(this);
            topic.setSubscribed();
            Log.debug(LogLevel.VERBOSE, "Client", "subscribe", device.getName() + " subscribed to " + topic.name());
        } catch (MqttException e) {
            Log.error("Client", "subscribe", e.getMessage());
            return false;
        }

        return true;
    }

    @Override
    public Boolean publish(SnTopic topic, String message) {

        try {
            MqttMessage mqttMessage = new MqttMessage(message.getBytes());
            mqttMessage.setQos(Prtcl.DEFAULT_QOS);
            mqttClient.publish(topic.name(), mqttMessage);
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
        return mqttClient.isConnected();
    }

    @Override
    public Boolean disconnect() {

        /*
         * Do nothing (stay connected), otherwise the device will not receive any message.
         *
        isConnected = false;
        try {
            mqttClient.disconnect();
        } catch (MqttException e) {
            return false;
        }
        */

        Log.debug(LogLevel.VERBOSE, "Client", "disconnect", device.getName() + " disconnected");

        return true;
    }

    @Override
    public void run() {
        // wait until receiving messages -> messageArrived
        isStarted = true;
        while(mqttClient.isConnected()) {
            try {
                Thread.sleep(SLEEP_TIME * 1000);
            } catch (InterruptedException e) {
                Log.error("Client", "run", e.getMessage());
            }
        }
        isStarted = false;
    }
}

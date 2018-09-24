package main;

import org.fusesource.mqtt.client.*;
import utils.log.Log;
import utils.mqttclient.MqttClient;

public class TestMain {

	public static final long TIME_TO_WAIT = 10000; // 10 seconds

	public static void main(String[] args) {

		testBlockingConnection();
	}

	private static void testBlockingConnection() {

		Log.activeDebug("Connecting to Broker using MQTT");

		MqttClient mqtt = new MqttClient();
		BlockingConnection connection = null;

		Log.activeDebug("new blocking connection");
		try {
			connection = mqtt.connect(TIME_TO_WAIT);
		} catch (Exception e) {
			Log.activeDebug("not connected");
			Log.activeDebug(e.getMessage());
		}
		Log.activeDebug("connected to broker !!! ");

		// Subscribe to  fidelityAds topic
		Topic[] topics = { new Topic("FIDELITY_ADS_TOPIC", QoS.AT_LEAST_ONCE)};
		try {
			connection.subscribe(topics);
			Log.activeDebug("subscribe");
		} catch (Exception e) {
			Log.activeDebug("Exception: " + e.getMessage());
		}
		// Publish Ads
		String ads1 = "Discount on transfert fees up to -50% with coupon code JBOSSDOCTOR.  www.beosbank.com";
		long index=0;
		while(true){
			try {
				connection.publish("FIDELITY_ADS_TOPIC", (index+":"+ads1).getBytes(), QoS.AT_LEAST_ONCE, false);
				Log.activeDebug("publish");
			} catch (Exception e) {
				Log.activeDebug(e.getMessage());
			}
			Log.activeDebug("Sent messages with index = " + index);
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				Log.activeDebug(e.getMessage());
			}
			index++;
			Log.activeDebug("end of loop");
		}
	}
}
package main;

import org.fusesource.mqtt.client.*;
import utils.log.Log;
import utils.mqttclient.MqttClient;

import java.util.concurrent.TimeoutException;

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
			mqtt.connect();
		} catch (Exception e) {
			Log.activeDebug("not connected");
			Log.activeDebug(e.getMessage());
		}
		Log.activeDebug("connected to broker !!! ");

		try {
			Log.activeDebug("subscribe topic: MON_PETIT_CHAT");
			mqtt.subscribe("MON_PETIT_CHAT");
			Log.activeDebug("subscription OK");

		} catch (TimeoutException e) {
			Log.activeDebug("Exception: " + e.getMessage());
		}

		try {
			Log.activeDebug("subscribe topic: MON_CACA");
			mqtt.subscribe("MON_CACA");
			Log.activeDebug("subscription OK");

		} catch (TimeoutException e) {
			Log.activeDebug("Exception: " + e.getMessage());
		}

			/*
			Message message = connection.receive();
			System.out.println(message.getTopic());
			byte[] payload = message.getPayload();
			// process the message then:
			message.ack();
			Log.activeDebug("A message have been received: " + new String(payload));
			*/



		/*

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

		*/
	}
}
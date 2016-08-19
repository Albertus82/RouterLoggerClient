package it.albertus.router.client.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.google.gson.Gson;

public class ThresholdsMqttMessageListener implements IMqttMessageListener {

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		System.out.println(message);
		final ThresholdsPayload tp = new Gson().fromJson(new String(message.getPayload()), ThresholdsPayload.class);
		System.out.println(tp);
	}

}

package it.albertus.router.client.mqtt;

import it.albertus.router.client.RouterLoggerStatus;
import it.albertus.router.client.gui.RouterLoggerGui;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.google.gson.Gson;

public class StatusMqttMessageListener implements IMqttMessageListener {

	private final RouterLoggerGui gui;

	public StatusMqttMessageListener(RouterLoggerGui gui) {
		this.gui = gui;
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		final StatusPayload sp = new Gson().fromJson(new String(message.getPayload()), StatusPayload.class);
		final RouterLoggerStatus rls = RouterLoggerStatus.valueOf(sp.getStatus());
		gui.setStatus(rls);
	}

}

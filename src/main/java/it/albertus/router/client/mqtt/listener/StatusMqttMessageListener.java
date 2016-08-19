package it.albertus.router.client.mqtt.listener;

import it.albertus.router.client.RouterLoggerStatus;
import it.albertus.router.client.gui.RouterLoggerGui;
import it.albertus.router.client.mqtt.BaseMqttClient;

import java.io.UnsupportedEncodingException;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class StatusMqttMessageListener implements IMqttMessageListener {

	private final RouterLoggerGui gui;

	public StatusMqttMessageListener(RouterLoggerGui gui) {
		this.gui = gui;
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws JsonSyntaxException, UnsupportedEncodingException {
		final StatusPayload sp = new Gson().fromJson(new String(message.getPayload(), BaseMqttClient.PREFERRED_CHARSET), StatusPayload.class);
		final RouterLoggerStatus rls = RouterLoggerStatus.valueOf(sp.getStatus());
		gui.setStatus(rls);
	}

}

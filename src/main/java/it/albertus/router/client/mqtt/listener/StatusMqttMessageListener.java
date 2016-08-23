package it.albertus.router.client.mqtt.listener;

import it.albertus.router.client.dto.StatusDto;
import it.albertus.router.client.engine.RouterLoggerStatus;
import it.albertus.router.client.engine.Status;
import it.albertus.router.client.gui.RouterLoggerGui;
import it.albertus.router.client.mqtt.BaseMqttClient;

import java.io.UnsupportedEncodingException;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class StatusMqttMessageListener implements IMqttMessageListener {

	private final RouterLoggerGui gui;

	public StatusMqttMessageListener(final RouterLoggerGui gui) {
		this.gui = gui;
	}

	@Override
	public void messageArrived(final String topic, final MqttMessage message) throws JsonSyntaxException, UnsupportedEncodingException {
		final StatusDto sp = new Gson().fromJson(new String(message.getPayload(), BaseMqttClient.PREFERRED_CHARSET), StatusDto.class);
		final RouterLoggerStatus rls = new RouterLoggerStatus(Status.valueOf(sp.getStatus()), sp.getTimestamp());
		gui.setStatus(rls);
	}

}

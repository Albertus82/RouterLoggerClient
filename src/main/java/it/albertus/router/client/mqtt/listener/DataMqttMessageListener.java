package it.albertus.router.client.mqtt.listener;

import it.albertus.router.client.engine.RouterData;
import it.albertus.router.client.engine.Threshold;
import it.albertus.router.client.gui.RouterLoggerGui;
import it.albertus.router.client.mqtt.BaseMqttClient;

import java.io.UnsupportedEncodingException;
import java.util.Collections;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class DataMqttMessageListener implements IMqttMessageListener {

	private final RouterLoggerGui gui;

	private int iteration = 0;

	public DataMqttMessageListener(final RouterLoggerGui gui) {
		this.gui = gui;
	}

	@Override
	public void messageArrived(final String topic, final MqttMessage message) throws JsonSyntaxException, UnsupportedEncodingException {
		final RouterData data = new Gson().fromJson(new String(message.getPayload(), BaseMqttClient.PREFERRED_CHARSET), RouterData.class);
		gui.getDataTable().addRow(++iteration, data, Collections.<Threshold, String> emptyMap());
		gui.getTrayIcon().updateTrayItem(gui.getCurrentStatus() != null ? gui.getCurrentStatus().getStatus() : null, data);
	}

}

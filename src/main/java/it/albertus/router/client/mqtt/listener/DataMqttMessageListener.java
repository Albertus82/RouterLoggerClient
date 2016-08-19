package it.albertus.router.client.mqtt.listener;

import it.albertus.router.client.Threshold;
import it.albertus.router.client.gui.DataTable;
import it.albertus.router.client.gui.RouterData;
import it.albertus.router.client.mqtt.BaseMqttClient;

import java.io.UnsupportedEncodingException;
import java.util.Collections;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class DataMqttMessageListener implements IMqttMessageListener {

	private final DataTable dataTable;

	private int iteration = 0;

	public DataMqttMessageListener(DataTable dataTable) {
		this.dataTable = dataTable;
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws JsonSyntaxException, UnsupportedEncodingException {
		RouterData data = new Gson().fromJson(new String(message.getPayload(), BaseMqttClient.PREFERRED_CHARSET), RouterData.class);
		dataTable.addRow(++iteration, data, Collections.<Threshold, String> emptyMap());
	}

}

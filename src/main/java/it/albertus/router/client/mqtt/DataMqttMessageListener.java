package it.albertus.router.client.mqtt;

import it.albertus.router.client.Threshold;
import it.albertus.router.client.gui.DataTable;
import it.albertus.router.client.gui.RouterData;

import java.util.Collections;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.google.gson.Gson;

public class DataMqttMessageListener implements IMqttMessageListener {

	private final DataTable dataTable;

	private int i = 1;

	public DataMqttMessageListener(DataTable dataTable) {
		this.dataTable = dataTable;
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		RouterData data = new Gson().fromJson(new String(message.getPayload()), RouterData.class);
		dataTable.addRow(i++, data, Collections.<Threshold, String> emptyMap());
	}

}

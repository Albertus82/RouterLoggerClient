package it.albertus.router.client.mqtt.listener;

import java.io.UnsupportedEncodingException;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import it.albertus.router.client.dto.RouterDataDto;
import it.albertus.router.client.dto.transformer.DataTransformer;
import it.albertus.router.client.engine.RouterData;
import it.albertus.router.client.gui.DataTable;
import it.albertus.router.client.gui.RouterLoggerClientGui;
import it.albertus.router.client.gui.ThresholdsManager;
import it.albertus.router.client.mqtt.BaseMqttClient;

public class DataMqttMessageListener implements IMqttMessageListener {

	private final RouterLoggerClientGui gui;

	public DataMqttMessageListener(final RouterLoggerClientGui gui) {
		this.gui = gui;
	}

	@Override
	public void messageArrived(final String topic, final MqttMessage message) throws JsonSyntaxException, UnsupportedEncodingException {
		final RouterDataDto dto = new Gson().fromJson(new String(message.getPayload(), BaseMqttClient.PREFERRED_CHARSET), RouterDataDto.class);
		final RouterData data = DataTransformer.fromDto(dto);

		final ThresholdsManager thresholdsManager = gui.getThresholdsManager();
		final DataTable dataTable = gui.getDataTable();
		if (dataTable != null && dataTable.getTableViewer() != null && !dataTable.getTableViewer().getTable().isDisposed()) {
			if (thresholdsManager.getThresholdsBuffer().containsKey(data.getTimestamp())) {
				synchronized (thresholdsManager) {
					dataTable.addRow(data, thresholdsManager.getThresholdsBuffer().get(data.getTimestamp()));
					thresholdsManager.getThresholdsBuffer().remove(data.getTimestamp());
				}
			}
			else {
				dataTable.addRow(data, null);
			}
		}

		gui.getTrayIcon().updateTrayItem(gui.getCurrentStatus() != null ? gui.getCurrentStatus().getStatus() : null, data);
	}

}

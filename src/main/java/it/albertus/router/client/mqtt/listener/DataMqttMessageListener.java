package it.albertus.router.client.mqtt.listener;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.google.gson.Gson;

import it.albertus.mqtt.MqttPayloadDecoder;
import it.albertus.router.client.dto.RouterDataDto;
import it.albertus.router.client.dto.transformer.DataTransformer;
import it.albertus.router.client.engine.RouterData;
import it.albertus.router.client.gui.DataTable;
import it.albertus.router.client.gui.RouterLoggerClientGui;
import it.albertus.router.client.gui.ThresholdsManager;
import it.albertus.router.client.mqtt.BaseMqttClient;
import it.albertus.router.client.resources.Messages;
import it.albertus.util.logging.LoggerFactory;

public class DataMqttMessageListener implements IMqttMessageListener {

	private static final Logger logger = LoggerFactory.getLogger(DataMqttMessageListener.class);

	private final MqttPayloadDecoder decoder = new MqttPayloadDecoder();
	private final RouterLoggerClientGui gui;

	public DataMqttMessageListener(final RouterLoggerClientGui gui) {
		this.gui = gui;
	}

	@Override
	public void messageArrived(final String topic, final MqttMessage message) throws IOException {
		final Level level = Level.FINE;
		if (logger.isLoggable(level)) {
			logger.log(level, Messages.get("msg.mqtt.message.arrived"), new Object[] { topic, message });
		}

		final RouterDataDto dto = new Gson().fromJson(new String(decoder.decode(message.getPayload()), BaseMqttClient.PREFERRED_CHARSET), RouterDataDto.class);
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

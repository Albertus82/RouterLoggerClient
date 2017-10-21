package it.albertus.routerlogger.client.mqtt.listener;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.google.gson.Gson;

import it.albertus.routerlogger.client.dto.RouterDataDto;
import it.albertus.routerlogger.client.dto.transformer.DataTransformer;
import it.albertus.routerlogger.client.dto.transformer.ThresholdsTransformer;
import it.albertus.routerlogger.client.engine.RouterData;
import it.albertus.routerlogger.client.engine.ThresholdsReached;
import it.albertus.routerlogger.client.gui.DataTable;
import it.albertus.routerlogger.client.gui.RouterLoggerClientGui;
import it.albertus.routerlogger.client.resources.Messages;
import it.albertus.util.logging.LoggerFactory;

public class DataMqttMessageListener extends MqttMessageListener {

	private static final Logger logger = LoggerFactory.getLogger(DataMqttMessageListener.class);

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

		final RouterDataDto dto = new Gson().fromJson(decode(message), RouterDataDto.class);
		final RouterData data = DataTransformer.fromDto(dto);
		final ThresholdsReached thresholdsReached = ThresholdsTransformer.fromDto(dto);

		final DataTable dataTable = gui.getDataTable();
		if (dataTable != null && dataTable.getTableViewer() != null && !dataTable.getTableViewer().getTable().isDisposed()) {
			dataTable.addRow(data, thresholdsReached);
			if (!message.isRetained()) {
				gui.getThresholdsManager().printThresholdsReached(thresholdsReached);
			}
		}

		gui.getTrayIcon().updateTrayItem(gui.getCurrentStatus() != null ? gui.getCurrentStatus().getStatus() : null, data);
	}

}

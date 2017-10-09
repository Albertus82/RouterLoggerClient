package it.albertus.router.client.mqtt.listener;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.google.gson.Gson;

import it.albertus.router.client.dto.ThresholdsDto;
import it.albertus.router.client.dto.transformer.ThresholdsTransformer;
import it.albertus.router.client.engine.ThresholdsReached;
import it.albertus.router.client.gui.RouterLoggerClientGui;
import it.albertus.router.client.resources.Messages;
import it.albertus.util.logging.LoggerFactory;

public class ThresholdsMqttMessageListener extends MqttMessageListener {

	private static final Logger logger = LoggerFactory.getLogger(ThresholdsMqttMessageListener.class);

	private final RouterLoggerClientGui gui;

	public ThresholdsMqttMessageListener(final RouterLoggerClientGui gui) {
		this.gui = gui;
	}

	@Override
	public void messageArrived(final String topic, final MqttMessage message) throws IOException {
		final Level level = Level.FINE;
		if (logger.isLoggable(level)) {
			logger.log(level, Messages.get("msg.mqtt.message.arrived"), new Object[] { topic, message });
		}

		final ThresholdsDto dto = new Gson().fromJson(decode(message), ThresholdsDto.class);

		if (dto != null && dto.getReached() != null && !dto.getReached().isEmpty()) {
			final ThresholdsReached thresholdsReached = ThresholdsTransformer.fromDto(dto);

			gui.getThresholdsManager().updateDataTable(thresholdsReached);

			if (!message.isRetained()) {
				gui.getThresholdsManager().printThresholdsReached(thresholdsReached);
			}
		}
	}

}

package it.albertus.routerlogger.client.mqtt.listener;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.google.gson.Gson;

import it.albertus.routerlogger.client.dto.StatusDto;
import it.albertus.routerlogger.client.dto.transformer.StatusTransformer;
import it.albertus.routerlogger.client.engine.RouterLoggerStatus;
import it.albertus.routerlogger.client.gui.RouterLoggerClientGui;
import it.albertus.routerlogger.client.resources.Messages;
import it.albertus.util.logging.LoggerFactory;

public class StatusMqttMessageListener extends MqttMessageListener {

	private static final Logger logger = LoggerFactory.getLogger(StatusMqttMessageListener.class);

	private final RouterLoggerClientGui gui;

	public StatusMqttMessageListener(final RouterLoggerClientGui gui) {
		this.gui = gui;
	}

	@Override
	public void messageArrived(final String topic, final MqttMessage message) throws IOException {
		final Level level = Level.FINE;
		if (logger.isLoggable(level)) {
			logger.log(level, Messages.get("msg.mqtt.message.arrived"), new Object[] { topic, message });
		}

		final StatusDto dto = new Gson().fromJson(decode(message), StatusDto.class);
		final RouterLoggerStatus rls = StatusTransformer.fromDto(dto);
		gui.updateStatus(rls);
	}

}

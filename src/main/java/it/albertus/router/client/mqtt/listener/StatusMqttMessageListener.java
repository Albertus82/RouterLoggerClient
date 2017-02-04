package it.albertus.router.client.mqtt.listener;

import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import it.albertus.router.client.dto.StatusDto;
import it.albertus.router.client.dto.transformer.StatusTransformer;
import it.albertus.router.client.engine.RouterLoggerStatus;
import it.albertus.router.client.gui.RouterLoggerClientGui;
import it.albertus.router.client.mqtt.BaseMqttClient;
import it.albertus.router.client.resources.Messages;
import it.albertus.util.logging.LoggerFactory;

public class StatusMqttMessageListener implements IMqttMessageListener {

	private static final Logger logger = LoggerFactory.getLogger(StatusMqttMessageListener.class);

	private final RouterLoggerClientGui gui;

	public StatusMqttMessageListener(final RouterLoggerClientGui gui) {
		this.gui = gui;
	}

	@Override
	public void messageArrived(final String topic, final MqttMessage message) throws JsonSyntaxException, UnsupportedEncodingException {
		logger.fine(Messages.get("msg.mqtt.message.arrived", topic, message));

		final StatusDto dto = new Gson().fromJson(new String(message.getPayload(), BaseMqttClient.PREFERRED_CHARSET), StatusDto.class);
		final RouterLoggerStatus rls = StatusTransformer.fromDto(dto);
		gui.setStatus(rls);
	}

}

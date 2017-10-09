package it.albertus.router.client.mqtt.listener;

import java.io.IOException;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import it.albertus.mqtt.MqttPayloadDecoder;
import it.albertus.mqtt.MqttUtils;

public abstract class MqttMessageListener implements IMqttMessageListener {

	private final MqttPayloadDecoder decoder = new MqttPayloadDecoder();

	public String decode(final MqttMessage message) throws IOException {
		return new String(decoder.decode(message.getPayload()), MqttUtils.CHARSET_UTF8);
	}

}

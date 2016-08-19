package it.albertus.router.client.mqtt.listener;

import it.albertus.router.client.resources.Resources;

public class MqttCallback extends MqttCallbackAdapter {

	private final String clientId;

	public MqttCallback(final String clientId) {
		this.clientId = clientId;
	}

	@Override
	public void connectionLost(final Throwable cause) {
		cause.printStackTrace();
	}

	@Override
	public void connectComplete(final boolean reconnect, final String serverURI) {
		System.out.println(Resources.get("msg.mqtt.connected", serverURI, clientId));
	}

}

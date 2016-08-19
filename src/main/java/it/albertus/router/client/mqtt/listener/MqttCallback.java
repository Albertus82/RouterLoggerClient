package it.albertus.router.client.mqtt.listener;

import it.albertus.router.client.Logger;
import it.albertus.router.client.resources.Resources;

public class MqttCallback extends MqttCallbackAdapter {

	private final String clientId;

	public MqttCallback(final String clientId) {
		this.clientId = clientId;
	}

	@Override
	public void connectionLost(final Throwable cause) {
		Logger.getInstance().log(cause);
	}

	@Override
	public void connectComplete(final boolean reconnect, final String serverURI) {
		Logger.getInstance().log(Resources.get("msg.mqtt.connected", serverURI, clientId));
	}

}

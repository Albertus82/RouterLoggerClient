package it.albertus.router.client.mqtt.listener;

import it.albertus.jface.SwtThreadExecutor;
import it.albertus.router.client.gui.RouterLoggerGui;
import it.albertus.router.client.resources.Messages;
import it.albertus.router.client.util.Logger;

public class RouterLoggerClientMqttCallback extends MqttCallbackAdapter {

	private final String clientId;
	private final RouterLoggerGui gui;

	private volatile boolean connectionLost = false;

	public RouterLoggerClientMqttCallback(final String clientId, final RouterLoggerGui gui) {
		this.clientId = clientId;
		this.gui = gui;
	}

	@Override
	public void connectionLost(final Throwable cause) {
		connectionLost = true;
		Logger.getInstance().log(cause);
	}

	@Override
	public void connectComplete(final boolean reconnect, final String serverURI) {
		Logger.getInstance().log(Messages.get("msg.mqtt.connected", serverURI, clientId));
		if (connectionLost) {
			new SwtThreadExecutor(gui.getShell()) {
				@Override
				protected void run() {
					gui.reconnectAfterConnectionLoss();
					connectionLost = false;
				}
			}.start();
		}
	}

}

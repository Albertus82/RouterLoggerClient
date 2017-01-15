package it.albertus.router.client.mqtt.listener;

import it.albertus.jface.SwtThreadExecutor;
import it.albertus.router.client.gui.RouterLoggerClientGui;
import it.albertus.router.client.resources.Messages;
import it.albertus.router.client.util.Logger;
import it.albertus.router.client.util.LoggerFactory;

public class RouterLoggerClientMqttCallback extends MqttCallbackAdapter {

	private static final Logger logger = LoggerFactory.getLogger(RouterLoggerClientMqttCallback.class);

	private final String clientId;
	private final RouterLoggerClientGui gui;

	public RouterLoggerClientMqttCallback(final String clientId, final RouterLoggerClientGui gui) {
		this.clientId = clientId;
		this.gui = gui;
	}

	@Override
	public void connectionLost(final Throwable cause) {
		logger.log(cause);
	}

	@Override
	public void connectComplete(final boolean reconnect, final String serverURI) {
		logger.log(Messages.get("msg.mqtt.connected", serverURI, clientId));
		if (reconnect) {
			new SwtThreadExecutor(gui.getShell()) {
				@Override
				protected void run() {
					gui.reconnectAfterConnectionLoss();
				}
			}.start();
		}
	}

}

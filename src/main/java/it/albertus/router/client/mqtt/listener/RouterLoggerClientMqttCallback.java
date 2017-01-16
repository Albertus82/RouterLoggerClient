package it.albertus.router.client.mqtt.listener;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import it.albertus.jface.SwtThreadExecutor;
import it.albertus.router.client.gui.RouterLoggerClientGui;
import it.albertus.router.client.resources.Messages;
import it.albertus.router.client.util.Logger;
import it.albertus.router.client.util.LoggerFactory;

public class RouterLoggerClientMqttCallback implements MqttCallbackExtended {

	private static final Logger logger = LoggerFactory.getLogger(RouterLoggerClientMqttCallback.class);

	private final String clientId;
	private final RouterLoggerClientGui gui;

	public RouterLoggerClientMqttCallback(final String clientId, final RouterLoggerClientGui gui) {
		this.clientId = clientId;
		this.gui = gui;
	}

	@Override
	public void connectionLost(final Throwable cause) {
		logger.error(cause);
	}

	@Override
	public void connectComplete(final boolean reconnect, final String serverURI) {
		logger.info(Messages.get("msg.mqtt.connected", serverURI, clientId));
		if (reconnect) {
			new SwtThreadExecutor(gui.getShell()) {
				@Override
				protected void run() {
					gui.reconnectAfterConnectionLoss();
				}
			}.start();
		}
	}

	@Override
	public void messageArrived(final String topic, final MqttMessage message) throws Exception {
		logger.debug(Messages.get("msg.mqtt.message.arrived", topic, message));
	}

	@Override
	public void deliveryComplete(final IMqttDeliveryToken token) {
		logger.debug(Messages.get("msg.mqtt.message.delivered", token));
	}

}

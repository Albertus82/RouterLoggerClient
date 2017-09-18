package it.albertus.router.client.mqtt.listener;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttToken;

import it.albertus.jface.DisplayThreadExecutor;
import it.albertus.router.client.gui.RouterLoggerClientGui;
import it.albertus.router.client.resources.Messages;
import it.albertus.util.logging.LoggerFactory;

public class MqttCallback implements MqttCallbackExtended {

	private static final Logger logger = LoggerFactory.getLogger(MqttCallback.class);

	private final String clientId;
	private final RouterLoggerClientGui gui;

	public MqttCallback(final String clientId, final RouterLoggerClientGui gui) {
		this.clientId = clientId;
		this.gui = gui;
	}

	@Override
	public void connectionLost(final Throwable cause) {
		logger.log(Level.WARNING, cause.toString(), cause);
	}

	@Override
	public void connectComplete(final boolean reconnect, final String serverURI) {
		logger.log(Level.INFO, Messages.get("msg.mqtt.connected"), new String[] { serverURI, clientId });
		if (reconnect) {
			new DisplayThreadExecutor(gui.getShell()).execute(gui::reconnectAfterConnectionLoss);
		}
	}

	@Override
	public void messageArrived(final String topic, final MqttMessage message) throws Exception {
		if (logger.isLoggable(Level.FINE)) {
			logger.log(Level.FINE, Messages.get("msg.mqtt.message.arrived"), new Object[] { topic, message });
		}
	}

	@Override
	public void deliveryComplete(final IMqttDeliveryToken token) {
		if (logger.isLoggable(Level.FINE)) {
			logger.log(Level.FINE, Messages.get("msg.mqtt.message.delivered"), token instanceof MqttToken ? ((MqttToken) token).internalTok : token);
		}
	}

}

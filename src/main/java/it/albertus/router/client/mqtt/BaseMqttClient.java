package it.albertus.router.client.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import it.albertus.router.client.util.Logger;
import it.albertus.router.client.util.LoggerFactory;

public abstract class BaseMqttClient {

	private static final Logger logger = LoggerFactory.getLogger(BaseMqttClient.class);

	public static final String PREFERRED_CHARSET = "UTF-8";

	private volatile MqttClient client;

	protected class MqttClientStartThread extends Thread {

		private final MqttConnectOptions options;
		private final boolean retry;

		protected MqttClientStartThread(final MqttConnectOptions options, final boolean retry) {
			this.setName("mqttClientStartThread");
			this.setDaemon(true);
			this.options = options;
			this.retry = retry;
		}

		@Override
		public void run() {
			try {
				client.connect(options);
			}
			catch (final Exception e) {
				logger.error(e);
				if (retry) {
					try {
						client.close();
					}
					catch (final MqttException me) {
						logger.error(me);
					}
					client = null;
				}
			}
		}
	}

	protected abstract void connect();

	public void disconnect() {
		try {
			doDisconnect();
		}
		catch (final Exception e) {
			logger.error(e);
		}
	}

	protected synchronized void doConnect(final String clientId, final MqttConnectOptions options, final MqttClientPersistence persistence, final boolean retry) throws MqttException {
		if (client == null) {
			client = new MqttClient(options.getServerURIs()[0], clientId, persistence);
			client.setCallback(createMqttCallback(clientId));
			final Thread starter = new MqttClientStartThread(options, retry);
			starter.start();
			try {
				starter.join();
			}
			catch (final InterruptedException ie) {
				logger.debug(ie);
				Thread.currentThread().interrupt();
			}
		}
	}

	protected abstract MqttCallback createMqttCallback(final String clientId);

	protected synchronized void doSubscribe(final String topic, final int qos, final IMqttMessageListener listener) throws MqttException {
		if (client == null) {
			connect(); // Lazy connection.
		}
		if (client != null && client.isConnected()) {
			client.subscribe(topic, qos, listener);
		}
	}

	protected synchronized boolean doDisconnect() throws MqttException {
		if (client != null) {
			if (client.isConnected()) {
				try {
					client.disconnect();
				}
				catch (final Exception e) {
					logger.error(e);
					client.disconnectForcibly();
				}
			}
			client.close();
			client = null;
			return true;
		}
		else {
			return false;
		}
	}

	public MqttClient getClient() {
		return client;
	}

}

package it.albertus.routerlogger.client.mqtt;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

import it.albertus.jface.preference.field.UriListEditor;
import it.albertus.net.mqtt.MqttQos;
import it.albertus.routerlogger.client.engine.RouterLoggerClientConfig;
import it.albertus.routerlogger.client.gui.RouterLoggerClientGui;
import it.albertus.routerlogger.client.mqtt.listener.DeviceStatusMqttMessageListener;
import it.albertus.routerlogger.client.mqtt.listener.MqttCallback;
import it.albertus.routerlogger.client.mqtt.listener.AppStatusMqttMessageListener;
import it.albertus.routerlogger.client.resources.Messages;
import it.albertus.util.Configuration;
import it.albertus.util.ConfigurationException;
import it.albertus.util.NewLine;
import it.albertus.util.logging.LoggerFactory;

/** @Singleton */
public class MqttClient extends BaseMqttClient {

	private static final Logger logger = LoggerFactory.getLogger(MqttClient.class);

	private static final Configuration configuration = RouterLoggerClientConfig.getInstance();

	private static final String CFG_KEY_MQTT_CLEAN_SESSION = "mqtt.clean.session";
	private static final String CFG_KEY_MQTT_MAX_INFLIGHT = "mqtt.max.inflight";
	private static final String CFG_KEY_MQTT_CONNECTION_TIMEOUT = "mqtt.connection.timeout";
	private static final String CFG_KEY_MQTT_KEEP_ALIVE_INTERVAL = "mqtt.keep.alive.interval";
	private static final String CFG_KEY_MQTT_PASSWORD = "mqtt.password";
	private static final String CFG_KEY_MQTT_USERNAME = "mqtt.username";
	private static final String CFG_KEY_MQTT_CLIENT_ID = "mqtt.client.id";
	private static final String CFG_KEY_MQTT_SERVER_URI = "mqtt.server.uri";
	private static final String CFG_KEY_MQTT_AUTOMATIC_RECONNECT = "mqtt.automatic.reconnect";
	private static final String CFG_KEY_MQTT_CONNECT_RETRY = "mqtt.connect.retry";
	private static final String CFG_KEY_MQTT_VERSION = "mqtt.version";
	private static final String CFG_KEY_MQTT_PERSISTENCE_FILE_ENABLED = "mqtt.persistence.file.enabled";
	private static final String CFG_KEY_MQTT_PERSISTENCE_FILE_CUSTOM = "mqtt.persistence.file.custom";
	private static final String CFG_KEY_MQTT_PERSISTENCE_FILE_PATH = "mqtt.persistence.file.path";

	private static final String CFG_KEY_MQTT_DATA_TOPIC = "mqtt.data.topic";
	private static final String CFG_KEY_MQTT_DATA_QOS = "mqtt.data.qos";

	private static final String CFG_KEY_MQTT_STATUS_TOPIC = "mqtt.status.topic";
	private static final String CFG_KEY_MQTT_STATUS_QOS = "mqtt.status.qos";

	public static class Defaults {
		public static final boolean ENABLED = false;
		public static final String CLIENT_ID = "RouterLoggerClient";
		public static final int KEEP_ALIVE_INTERVAL = MqttConnectOptions.KEEP_ALIVE_INTERVAL_DEFAULT;
		public static final int CONNECTION_TIMEOUT = MqttConnectOptions.CONNECTION_TIMEOUT_DEFAULT;
		public static final int MAX_INFLIGHT = MqttConnectOptions.MAX_INFLIGHT_DEFAULT;
		public static final boolean CLEAN_SESSION = MqttConnectOptions.CLEAN_SESSION_DEFAULT;
		public static final boolean AUTOMATIC_RECONNECT = true;
		public static final boolean CONNECT_RETRY = true;
		public static final byte MQTT_VERSION = MqttConnectOptions.MQTT_VERSION_DEFAULT;
		public static final boolean PERSISTENCE_FILE_ENABLED = false;
		public static final boolean PERSISTENCE_FILE_CUSTOM = false;

		public static final boolean DATA_ENABLED = true;
		public static final String DATA_TOPIC = "routerlogger/status/device";
		public static final byte DATA_QOS = MqttQos.AT_MOST_ONCE.getValue();

		public static final boolean STATUS_ENABLED = true;
		public static final String STATUS_TOPIC = "routerlogger/status/app";
		public static final byte STATUS_QOS = MqttQos.EXACTLY_ONCE.getValue();

		private Defaults() {
			throw new IllegalAccessError("Constants class");
		}
	}

	private RouterLoggerClientGui gui;
	private IMqttMessageListener dataMessageListener;
	private IMqttMessageListener statusMessageListener;

	private MqttClient() {}

	private static class Singleton {
		private static final MqttClient instance = new MqttClient();

		private Singleton() {
			throw new IllegalAccessError();
		}
	}

	public static MqttClient getInstance() {
		return Singleton.instance;
	}

	public void init(final RouterLoggerClientGui gui) {
		this.gui = gui;
		createListeners();
	}

	protected void createListeners() {
		dataMessageListener = new DeviceStatusMqttMessageListener(gui);
		statusMessageListener = new AppStatusMqttMessageListener(gui);
	}

	@Override
	protected void connect() {
		createListeners(); // renew listeners on connection
		try {
			final MqttConnectOptions options = new MqttConnectOptions();
			final String[] serverURIs = configuration.getString(CFG_KEY_MQTT_SERVER_URI, true).split(UriListEditor.URI_SPLIT_REGEX);
			if (serverURIs == null || serverURIs.length == 0 || serverURIs[0].trim().isEmpty()) {
				throw new ConfigurationException(Messages.get("err.mqtt.cfg.error.uri"), CFG_KEY_MQTT_SERVER_URI);
			}
			options.setServerURIs(serverURIs);
			final String username = configuration.getString(CFG_KEY_MQTT_USERNAME);
			if (username != null && !username.isEmpty()) {
				options.setUserName(username);
			}
			final char[] password = configuration.getCharArray(CFG_KEY_MQTT_PASSWORD);
			if (password != null && password.length > 0) {
				options.setPassword(password);
			}
			options.setKeepAliveInterval(configuration.getInt(CFG_KEY_MQTT_KEEP_ALIVE_INTERVAL, Defaults.KEEP_ALIVE_INTERVAL));
			options.setConnectionTimeout(configuration.getInt(CFG_KEY_MQTT_CONNECTION_TIMEOUT, Defaults.CONNECTION_TIMEOUT));
			options.setMaxInflight(configuration.getInt(CFG_KEY_MQTT_MAX_INFLIGHT, Defaults.MAX_INFLIGHT));
			options.setCleanSession(configuration.getBoolean(CFG_KEY_MQTT_CLEAN_SESSION, Defaults.CLEAN_SESSION));
			options.setAutomaticReconnect(configuration.getBoolean(CFG_KEY_MQTT_AUTOMATIC_RECONNECT, Defaults.AUTOMATIC_RECONNECT));
			options.setMqttVersion(configuration.getByte(CFG_KEY_MQTT_VERSION, Defaults.MQTT_VERSION));

			final String clientId = configuration.getString(CFG_KEY_MQTT_CLIENT_ID, Defaults.CLIENT_ID);

			final MqttClientPersistence persistence;
			if (configuration.getBoolean(CFG_KEY_MQTT_PERSISTENCE_FILE_ENABLED, Defaults.PERSISTENCE_FILE_ENABLED)) {
				final String directory = configuration.getString(CFG_KEY_MQTT_PERSISTENCE_FILE_PATH);
				if (configuration.getBoolean(CFG_KEY_MQTT_PERSISTENCE_FILE_CUSTOM, Defaults.PERSISTENCE_FILE_CUSTOM) && directory != null && !directory.isEmpty()) {
					persistence = new MqttDefaultFilePersistence(directory);
				}
				else {
					persistence = new MqttDefaultFilePersistence();
				}
			}
			else {
				persistence = new MemoryPersistence();
			}
			logger.log(Level.INFO, Messages.get("msg.mqtt.connecting"), new String[] { Arrays.toString(serverURIs), NewLine.SYSTEM_LINE_SEPARATOR + options.toString().trim() + "======" });
			doConnect(clientId, options, persistence, configuration.getBoolean(CFG_KEY_MQTT_CONNECT_RETRY, Defaults.CONNECT_RETRY));
		}
		catch (final Exception e) {
			logger.log(Level.SEVERE, e.toString(), e);
		}
	}

	@Override
	public void disconnect() {
		try {
			if (doDisconnect()) {
				logger.info(Messages.get("msg.mqtt.disconnected"));
			}
		}
		catch (final Exception e) {
			logger.log(Level.SEVERE, e.toString(), e);
		}
	}

	public void subscribeDeviceStatus() {
		final String topic = configuration.getString(CFG_KEY_MQTT_DATA_TOPIC, Defaults.DATA_TOPIC);
		final int qos = configuration.getByte(CFG_KEY_MQTT_DATA_QOS, Defaults.DATA_QOS);
		try {
			doSubscribe(topic, qos, dataMessageListener);
		}
		catch (final Exception e) {
			logger.log(Level.SEVERE, e.toString(), e);
		}
	}

	public void subscribeAppStatus() {
		final String topic = configuration.getString(CFG_KEY_MQTT_STATUS_TOPIC, Defaults.STATUS_TOPIC);
		final int qos = configuration.getByte(CFG_KEY_MQTT_STATUS_QOS, Defaults.STATUS_QOS);
		try {
			doSubscribe(topic, qos, statusMessageListener);
		}
		catch (final Exception e) {
			logger.log(Level.SEVERE, e.toString(), e);
		}
	}

	@Override
	protected MqttCallback createMqttCallback(String clientId) {
		return new MqttCallback(clientId, gui);
	}

}

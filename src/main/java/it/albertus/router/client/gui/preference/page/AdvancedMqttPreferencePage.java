package it.albertus.router.client.gui.preference.page;

import it.albertus.jface.preference.LocalizedLabelsAndValues;
import it.albertus.router.client.resources.Messages;
import it.albertus.util.Localized;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

public class AdvancedMqttPreferencePage extends MqttPreferencePage {

	public static LocalizedLabelsAndValues getMqttVersionComboOptions() {
		final LocalizedLabelsAndValues options = new LocalizedLabelsAndValues(3);
		Localized name = new Localized() {
			@Override
			public String getString() {
				return Messages.get("lbl.mqtt.version.default");
			}
		};
		options.put(name, MqttConnectOptions.MQTT_VERSION_DEFAULT);
		name = new Localized() {
			@Override
			public String getString() {
				return "3.1";
			}
		};
		options.put(name, MqttConnectOptions.MQTT_VERSION_3_1);
		name = new Localized() {
			@Override
			public String getString() {
				return "3.1.1";
			}
		};
		options.put(name, MqttConnectOptions.MQTT_VERSION_3_1_1);
		return options;
	}

}

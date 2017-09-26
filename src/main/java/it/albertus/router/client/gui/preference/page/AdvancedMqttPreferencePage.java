package it.albertus.router.client.gui.preference.page;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

import it.albertus.jface.preference.LocalizedLabelsAndValues;
import it.albertus.router.client.resources.Messages;

public class AdvancedMqttPreferencePage extends MqttPreferencePage {

	public static LocalizedLabelsAndValues getMqttVersionComboOptions() {
		final LocalizedLabelsAndValues options = new LocalizedLabelsAndValues(3);
		options.add(() -> Messages.get("lbl.mqtt.version.default"), MqttConnectOptions.MQTT_VERSION_DEFAULT);
		options.add(() -> "3.1", MqttConnectOptions.MQTT_VERSION_3_1);
		options.add(() -> "3.1.1", MqttConnectOptions.MQTT_VERSION_3_1_1);
		return options;
	}

}

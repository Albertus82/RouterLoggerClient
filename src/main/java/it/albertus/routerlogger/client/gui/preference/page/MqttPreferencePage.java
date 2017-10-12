package it.albertus.routerlogger.client.gui.preference.page;

import it.albertus.jface.preference.LocalizedLabelsAndValues;
import it.albertus.jface.preference.page.RestartHeaderPreferencePage;
import it.albertus.mqtt.MqttQos;

public class MqttPreferencePage extends RestartHeaderPreferencePage {

	public static LocalizedLabelsAndValues getMqttQosComboOptions() {
		final MqttQos[] values = MqttQos.values();
		final LocalizedLabelsAndValues options = new LocalizedLabelsAndValues(values.length);
		for (final MqttQos qos : values) {
			options.add(qos::getDescription, qos.getValue());
		}
		return options;
	}

}

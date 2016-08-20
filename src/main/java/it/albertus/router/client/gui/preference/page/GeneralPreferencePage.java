package it.albertus.router.client.gui.preference.page;

import it.albertus.jface.TextFormatter;
import it.albertus.jface.preference.LocalizedNamesAndValues;
import it.albertus.router.client.mqtt.MqttQos;
import it.albertus.router.client.resources.Resources;
import it.albertus.router.client.resources.Resources.Language;
import it.albertus.util.Localized;

import java.util.Locale;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

public class GeneralPreferencePage extends BasePreferencePage {

	@Override
	protected Control createHeader() {
		final Label header = new Label(getFieldEditorParent(), SWT.WRAP);
		TextFormatter.setBoldFontStyle(header);
		header.setText(Resources.get("lbl.preferences.general.header"));
		return header;
	}

	public static LocalizedNamesAndValues getLanguageComboOptions() {
		final Language[] values = Resources.Language.values();
		final LocalizedNamesAndValues options = new LocalizedNamesAndValues(values.length);
		for (final Language language : values) {
			final Locale locale = language.getLocale();
			final String value = locale.getLanguage();
			final Localized name = new Localized() {
				@Override
				public String getString() {
					return locale.getDisplayLanguage(locale);
				}
			};
			options.put(name, value);
		}
		return options;
	}

	public static LocalizedNamesAndValues getMqttQosComboOptions() {
		final MqttQos[] values = MqttQos.values();
		final LocalizedNamesAndValues options = new LocalizedNamesAndValues(values.length);
		for (final MqttQos qos : values) {
			final byte value = qos.getValue();
			final Localized name = new Localized() {
				@Override
				public String getString() {
					return qos.getDescription();
				}
			};
			options.put(name, value);
		}
		return options;
	}

}

package it.albertus.router.client.gui.preference.page;

import java.util.Locale;

import it.albertus.jface.preference.LocalizedLabelsAndValues;
import it.albertus.jface.preference.StaticLabelsAndValues;
import it.albertus.jface.preference.page.RestartHeaderPreferencePage;
import it.albertus.router.client.engine.Protocol;
import it.albertus.router.client.resources.Messages;
import it.albertus.router.client.resources.Messages.Language;
import it.albertus.util.Localized;

public class GeneralPreferencePage extends RestartHeaderPreferencePage {

	public static LocalizedLabelsAndValues getLanguageComboOptions() {
		final Language[] values = Messages.Language.values();
		final LocalizedLabelsAndValues options = new LocalizedLabelsAndValues(values.length);
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

	public static StaticLabelsAndValues getProtocolComboOptions() {
		final StaticLabelsAndValues options = new StaticLabelsAndValues(Protocol.values().length);
		for (final Protocol protocol : Protocol.values()) {
			final String value = protocol.name();
			final String name = protocol.toString();
			options.put(name, value);
		}
		return options;
	}

}

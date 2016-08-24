package it.albertus.router.client.gui.preference.page;

import it.albertus.jface.preference.LocalizedNamesAndValues;
import it.albertus.jface.preference.StaticNamesAndValues;
import it.albertus.router.client.engine.Protocol;
import it.albertus.router.client.resources.Resources;
import it.albertus.router.client.resources.Resources.Language;
import it.albertus.util.Localized;

import java.util.Locale;

public class GeneralPreferencePage extends RestartHeaderPreferencePage {

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

	public static StaticNamesAndValues getProtocolComboOptions() {
		final StaticNamesAndValues options = new StaticNamesAndValues(Protocol.values().length);
		for (final Protocol protocol : Protocol.values()) {
			final String value = protocol.name();
			final String name = protocol.toString();
			options.put(name, value);
		}
		return options;
	}

}

package it.albertus.router.client.engine;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import it.albertus.jface.JFaceMessages;
import it.albertus.router.client.resources.Messages;
import it.albertus.util.Configuration;
import it.albertus.util.StringUtils;

public class RouterLoggerClientConfiguration extends Configuration {

	public static final String CFG_KEY_LANGUAGE = "language";
	public static final String FILE_NAME = "routerlogger-client.cfg";

	private final Set<String> guiImportantKeys = new LinkedHashSet<>();

	private RouterLoggerClientConfiguration() {
		/* Caricamento della configurazione... */
		super(Messages.get("msg.application.name") + File.separator + FILE_NAME, true);
		init();
	}

	public static class Defaults {
		public static final String LANGUAGE = Locale.getDefault().getLanguage();
		public static final String GUI_IMPORTANT_KEYS_SEPARATOR = ",";

		private Defaults() {
			throw new IllegalAccessError("Constants class");
		}
	}

	private static class Singleton {
		private static final RouterLoggerClientConfiguration instance = new RouterLoggerClientConfiguration();

		private Singleton() {
			throw new IllegalAccessError();
		}
	}

	public static RouterLoggerClientConfiguration getInstance() {
		return Singleton.instance;
	}

	public Set<String> getGuiImportantKeys() {
		return guiImportantKeys;
	}

	private void init() {
		/* Impostazione lingua */
		if (this.contains("language")) {
			final String language = getString(CFG_KEY_LANGUAGE, Defaults.LANGUAGE);
			Messages.setLanguage(language);
			JFaceMessages.setLanguage(language);
		}

		/* Caricamento chiavi da evidenziare */
		guiImportantKeys.clear();
		for (final String importantKey : this.getString("gui.important.keys", true).split(this.getString("gui.important.keys.separator", Defaults.GUI_IMPORTANT_KEYS_SEPARATOR).trim())) {
			if (StringUtils.isNotBlank(importantKey)) {
				this.guiImportantKeys.add(importantKey.trim());
			}
		}
	}

	@Override
	public void reload() {
		super.reload();
		init();
	}

}

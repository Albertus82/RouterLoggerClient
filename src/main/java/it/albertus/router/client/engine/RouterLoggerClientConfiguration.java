package it.albertus.router.client.engine;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import it.albertus.jface.JFaceMessages;
import it.albertus.router.client.resources.Messages;
import it.albertus.util.Configuration;
import it.albertus.util.StringUtils;
import it.albertus.util.logging.CustomFormatter;
import it.albertus.util.logging.FileHandlerBuilder;
import it.albertus.util.logging.LoggerFactory;
import it.albertus.util.logging.LoggingSupport;

public class RouterLoggerClientConfiguration extends Configuration {

	private static final Logger logger = LoggerFactory.getLogger(RouterLoggerClientConfiguration.class);

	public static class Defaults {
		public static final String LANGUAGE = Locale.getDefault().getLanguage();
		public static final Level LOGGING_LEVEL = Level.INFO;
		public static final boolean LOGGING_FILES_ENABLED = true;
		public static final String LOGGING_FILES_PATH = getOsSpecificLocalAppDataDir() + File.separator + Messages.get("msg.application.name");
		public static final int LOGGING_FILES_LIMIT = 1024;
		public static final int LOGGING_FILES_COUNT = 5;
		public static final String GUI_IMPORTANT_KEYS_SEPARATOR = ",";

		private Defaults() {
			throw new IllegalAccessError("Constants class");
		}
	}

	private static final String LOG_FORMAT_FILE = "%1$td/%1$tm/%1$tY %1$tH:%1$tM:%1$tS.%tL %4$s %3$s - %5$s%6$s%n";

	public static final String CFG_KEY_LANGUAGE = "language";
	public static final String CFG_KEY_LOGGING_LEVEL = "logging.level";

	public static final String CFG_FILE_NAME = "routerlogger-client.cfg";
	public static final String LOG_FILE_NAME = "routerlogger-client.%g.log";

	private final Set<String> guiImportantKeys = new LinkedHashSet<>();

	private Handler fileHandler;
	private FileHandlerBuilder fileHandlerBuilder;

	public RouterLoggerClientConfiguration() throws IOException {
		super(Messages.get("msg.application.name") + File.separator + CFG_FILE_NAME, true);
		init();
	}

	@Override
	public void reload() throws IOException {
		super.reload();
		init();
	}

	private void init() {
		updateLanguage();
		updateLogging();

		guiImportantKeys.clear();
		for (final String importantKey : this.getString("gui.important.keys", true).split(this.getString("gui.important.keys.separator", Defaults.GUI_IMPORTANT_KEYS_SEPARATOR).trim())) {
			if (StringUtils.isNotBlank(importantKey)) {
				this.guiImportantKeys.add(importantKey.trim());
			}
		}
	}

	private void updateLogging() {
		if (LoggingSupport.getInitialConfigurationProperty() == null) {
			updateLoggingLevel();

			if (this.getBoolean("logging.files.enabled", Defaults.LOGGING_FILES_ENABLED)) {
				enableLoggingFileHandler();
			}
			else {
				disableLoggingFileHandler();
			}
		}
	}

	private void updateLanguage() {
		final String language = this.getString(CFG_KEY_LANGUAGE, Defaults.LANGUAGE);
		Messages.setLanguage(language);
		JFaceMessages.setLanguage(language);
	}

	private void updateLoggingLevel() {
		try {
			LoggingSupport.setLevel(LoggingSupport.getRootLogger().getName(), Level.parse(this.getString(CFG_KEY_LOGGING_LEVEL, Defaults.LOGGING_LEVEL.getName())));
		}
		catch (final IllegalArgumentException iae) {
			logger.log(Level.WARNING, iae.toString(), iae);
		}
	}

	private void enableLoggingFileHandler() {
		final String loggingPath = this.getString("logging.files.path", Defaults.LOGGING_FILES_PATH);
		if (loggingPath != null && !loggingPath.isEmpty()) {
			final FileHandlerBuilder builder = new FileHandlerBuilder().pattern(loggingPath + File.separator + LOG_FILE_NAME).limit(this.getInt("logging.files.limit", Defaults.LOGGING_FILES_LIMIT) * 1024).count(this.getInt("logging.files.count", Defaults.LOGGING_FILES_COUNT)).append(true).formatter(new CustomFormatter(LOG_FORMAT_FILE));
			if (fileHandlerBuilder == null || !builder.equals(fileHandlerBuilder)) {
				if (fileHandler != null) {
					LoggingSupport.getRootLogger().removeHandler(fileHandler);
					fileHandler.close();
					fileHandler = null;
				}
				try {
					new File(loggingPath).mkdirs();
					fileHandlerBuilder = builder;
					fileHandler = builder.build();
					LoggingSupport.getRootLogger().addHandler(fileHandler);
				}
				catch (final IOException ioe) {
					logger.log(Level.SEVERE, ioe.toString(), ioe);
				}
			}
		}
	}

	private void disableLoggingFileHandler() {
		if (fileHandler != null) {
			LoggingSupport.getRootLogger().removeHandler(fileHandler);
			fileHandler.close();
			fileHandler = null;
			fileHandlerBuilder = null;
		}
	}

	public Set<String> getGuiImportantKeys() {
		return guiImportantKeys;
	}

}

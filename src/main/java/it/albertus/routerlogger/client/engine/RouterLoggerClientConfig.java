package it.albertus.routerlogger.client.engine;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import it.albertus.jface.JFaceMessages;
import it.albertus.routerlogger.client.resources.Messages;
import it.albertus.routerlogger.client.util.InitializationException;
import it.albertus.util.Configuration;
import it.albertus.util.StringUtils;
import it.albertus.util.logging.CustomFormatter;
import it.albertus.util.logging.EnhancedFileHandler;
import it.albertus.util.logging.FileHandlerConfig;
import it.albertus.util.logging.LoggerFactory;
import it.albertus.util.logging.LoggingSupport;

public class RouterLoggerClientConfig extends Configuration {

	private static final Logger logger = LoggerFactory.getLogger(RouterLoggerClientConfig.class);

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

	private EnhancedFileHandler fileHandler;

	private static RouterLoggerClientConfig instance;

	private RouterLoggerClientConfig() throws IOException {
		super(Messages.get("msg.application.name") + File.separator + CFG_FILE_NAME, true);
		init();
	}

	public static synchronized RouterLoggerClientConfig getInstance() {
		if (instance == null) {
			try {
				instance = new RouterLoggerClientConfig();
			}
			catch (final IOException e) {
				final String message = Messages.get("err.open.cfg", CFG_FILE_NAME);
				logger.log(Level.SEVERE, message, e);
				throw new InitializationException(message, e);
			}
		}
		return instance;
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
			final FileHandlerConfig newConfig = new FileHandlerConfig();
			newConfig.setPattern(loggingPath + File.separator + LOG_FILE_NAME);
			newConfig.setLimit(getInt("logging.files.limit", Defaults.LOGGING_FILES_LIMIT) * 1024);
			newConfig.setCount(getInt("logging.files.count", Defaults.LOGGING_FILES_COUNT));
			newConfig.setAppend(true);
			newConfig.setFormatter(new CustomFormatter(LOG_FORMAT_FILE));

			if (fileHandler != null) {
				final FileHandlerConfig oldConfig = FileHandlerConfig.fromHandler(fileHandler);
				if (!oldConfig.getPattern().equals(newConfig.getPattern()) || oldConfig.getLimit() != newConfig.getLimit() || oldConfig.getCount() != newConfig.getCount()) {
					logger.log(Level.FINE, "Logging configuration has changed; closing and removing old {0}...", fileHandler.getClass().getSimpleName());
					LoggingSupport.getRootLogger().removeHandler(fileHandler);
					fileHandler.close();
					fileHandler = null;
					logger.log(Level.FINE, "Old FileHandler closed and removed.");
				}
			}

			if (fileHandler == null) {
				logger.log(Level.FINE, "FileHandler not found; creating one...");
				try {
					new File(loggingPath).mkdirs();
					fileHandler = new EnhancedFileHandler(newConfig);
					LoggingSupport.getRootLogger().addHandler(fileHandler);
					logger.log(Level.FINE, "{0} created successfully.", fileHandler.getClass().getSimpleName());
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
			logger.log(Level.FINE, "FileHandler closed and removed.");
		}
	}

	public Set<String> getGuiImportantKeys() {
		return guiImportantKeys;
	}

}

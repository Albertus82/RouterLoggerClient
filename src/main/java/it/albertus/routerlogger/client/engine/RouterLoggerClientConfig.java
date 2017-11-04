package it.albertus.routerlogger.client.engine;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import it.albertus.routerlogger.client.resources.Messages;
import it.albertus.util.InitializationException;
import it.albertus.util.LanguageConfig;
import it.albertus.util.LoggingConfig;
import it.albertus.util.StringUtils;
import it.albertus.util.logging.LoggerFactory;

public class RouterLoggerClientConfig extends LoggingConfig implements LanguageConfig {

	private static final String DIRECTORY_NAME = "RouterLogger" + File.separator + "Client";

	public static final String DEFAULT_LOGGING_FILES_PATH = getOsSpecificLocalAppDataDir() + File.separator + DIRECTORY_NAME;

	public static final String DEFAULT_GUI_IMPORTANT_KEYS_SEPARATOR = ",";

	private static final String CFG_FILE_NAME = "routerlogger-client.cfg";
	private static final String LOG_FILE_NAME_PATTERN = "routerlogger-client.%g.log";

	private static final Logger logger = LoggerFactory.getLogger(RouterLoggerClientConfig.class);

	private static RouterLoggerClientConfig instance;

	private final Set<String> guiImportantKeys = new LinkedHashSet<>();

	private RouterLoggerClientConfig() throws IOException {
		super(DIRECTORY_NAME + File.separator + CFG_FILE_NAME, true);
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
	protected void init() {
		super.init();
		updateLanguage();
		guiImportantKeys.clear();
		for (final String importantKey : getString("gui.important.keys", true).split(getString("gui.important.keys.separator", DEFAULT_GUI_IMPORTANT_KEYS_SEPARATOR).trim())) {
			if (StringUtils.isNotBlank(importantKey)) {
				this.guiImportantKeys.add(importantKey.trim());
			}
		}
	}

	@Override
	public void updateLanguage() {
		final String language = getString("language", Messages.DEFAULT_LANGUAGE);
		Messages.setLanguage(language);
	}

	public Set<String> getGuiImportantKeys() {
		return guiImportantKeys;
	}

	@Override
	protected boolean isFileHandlerEnabled() {
		return getBoolean("logging.files.enabled", super.isFileHandlerEnabled());
	}

	@Override
	protected String getFileHandlerPattern() {
		return getString("logging.files.path", DEFAULT_LOGGING_FILES_PATH) + File.separator + LOG_FILE_NAME_PATTERN;
	}

	@Override
	protected int getFileHandlerLimit() {
		final Integer limit = getInt("logging.files.limit");
		if (limit != null) {
			return limit * 1024;
		}
		else {
			return super.getFileHandlerLimit();
		}
	}

	@Override
	protected int getFileHandlerCount() {
		return getInt("logging.files.count", super.getFileHandlerCount());
	}

	@Override
	protected String getLoggingLevel() {
		return getString("logging.level", super.getLoggingLevel());
	}

}

package it.albertus.router.client;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import it.albertus.router.client.engine.RouterLoggerClientConfiguration;
import it.albertus.router.client.gui.RouterLoggerClientGui;
import it.albertus.router.client.resources.Messages;
import it.albertus.util.logging.LoggerFactory;
import it.albertus.util.logging.LoggingSupport;

public class RouterLoggerClient {

	private static final Logger logger;

	public static class InitializationException extends Exception {
		private static final long serialVersionUID = 6431588267552911987L;

		private InitializationException(final String message, final Throwable cause) {
			super(message, cause);
		}
	}

	private static final String LOG_FORMAT_CONSOLE = "%1$td/%1$tm/%1$tY %1$tH:%1$tM:%1$tS.%tL %4$s: %5$s%6$s%n";

	private static RouterLoggerClientConfiguration configuration;
	private static InitializationException initializationException;

	static {
		if (LoggingSupport.getFormat() == null) {
			LoggingSupport.setFormat(LOG_FORMAT_CONSOLE);
		}
		logger = LoggerFactory.getLogger(RouterLoggerClient.class);
		try {
			configuration = new RouterLoggerClientConfiguration();
		}
		catch (final IOException ioe) {
			final String message = Messages.get("err.open.cfg", RouterLoggerClientConfiguration.CFG_FILE_NAME);
			logger.log(Level.SEVERE, message, ioe);
			initializationException = new InitializationException(message, ioe);
		}
	}

	private RouterLoggerClient() {
		throw new IllegalAccessError();
	}

	public static void main(final String[] args) {
		RouterLoggerClientGui.run(initializationException);
	}

	public static RouterLoggerClientConfiguration getConfiguration() {
		return configuration;
	}

	public static InitializationException getInitializationException() {
		return initializationException;
	}

}

package it.albertus.router.client;

import java.io.IOException;

import it.albertus.router.client.engine.RouterLoggerClientConfiguration;
import it.albertus.router.client.gui.RouterLoggerClientGui;
import it.albertus.router.client.resources.Messages;
import it.albertus.router.client.util.Logger;
import it.albertus.router.client.util.LoggerFactory;

public class RouterLoggerClient {

	private static final Logger logger = LoggerFactory.getLogger(RouterLoggerClient.class);

	public static class InitializationException extends Exception {
		private static final long serialVersionUID = 6431588267552911987L;

		private InitializationException(final String message, final Throwable cause) {
			super(message, cause);
		}
	}

	private static RouterLoggerClientConfiguration configuration = null;

	private static InitializationException initializationException = null;

	static {
		try {
			configuration = new RouterLoggerClientConfiguration();
		}
		catch (final IOException ioe) {
			logger.error(ioe);
			initializationException = new InitializationException(Messages.get("err.open.cfg", RouterLoggerClientConfiguration.FILE_NAME), ioe);
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

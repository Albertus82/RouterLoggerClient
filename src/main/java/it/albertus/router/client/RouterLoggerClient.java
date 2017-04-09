package it.albertus.router.client;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;

import it.albertus.router.client.engine.RouterLoggerClientConfiguration;
import it.albertus.router.client.gui.RouterLoggerClientGui;
import it.albertus.router.client.util.InitializationException;
import it.albertus.util.logging.CustomFormatter;
import it.albertus.util.logging.LoggingSupport;

public class RouterLoggerClient {

	private static final String LOG_FORMAT_CONSOLE = "%1$td/%1$tm/%1$tY %1$tH:%1$tM:%1$tS.%tL %4$s: %5$s%6$s%n";

	private static InitializationException initializationException;

	static {
		if (LoggingSupport.getFormat() == null) {
			for (final Handler handler : LoggingSupport.getRootHandlers()) {
				if (handler instanceof ConsoleHandler) {
					handler.setFormatter(new CustomFormatter(LOG_FORMAT_CONSOLE));
				}
			}
		}
		try {
			RouterLoggerClientConfiguration.getInstance();
		}
		catch (final InitializationException e) {
			initializationException = e;
		}
		catch (final RuntimeException e) {
			initializationException = new InitializationException(e.getMessage(), e);
		}
	}

	private RouterLoggerClient() {
		throw new IllegalAccessError();
	}

	public static void main(final String[] args) {
		RouterLoggerClientGui.run(initializationException);
	}

	public static InitializationException getInitializationException() {
		return initializationException;
	}

}

package it.albertus.router.client;

import it.albertus.util.Configuration;

public class Logger {

	private static class Singleton {
		private static final Logger instance = new Logger();
	}

	public static Logger getInstance() {
		return Singleton.instance;
	}

	private Logger() {
		Configuration configuration;
		try {
			configuration = RouterLoggerConfiguration.getInstance();
		}
		catch (final Throwable t) {
			t.printStackTrace();
			configuration = null;
		}
		this.configuration = configuration;
	}

	public enum Destination {
		CONSOLE,
		FILE,
		EMAIL;
	}

	public interface Defaults {
		boolean DEBUG = false;
	}

	private final Configuration configuration;

	public boolean isDebugEnabled() {
		return configuration != null ? configuration.getBoolean("console.debug", Defaults.DEBUG) : true;
	}

	public void log(final String text, final Destination... destinations) {
		System.out.println(text);
	}

	public void log(final Throwable throwable, Destination... destinations) {
		throwable.printStackTrace();
	}

}

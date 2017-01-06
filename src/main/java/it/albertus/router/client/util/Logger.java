package it.albertus.router.client.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import it.albertus.router.client.engine.RouterLoggerClientConfiguration;
import it.albertus.util.Configuration;

public class Logger {

	private static final ThreadLocal<DateFormat> timestampFormat = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		}
	};

	private final Configuration configuration = getConfiguration();

	private Logger() {}

	private Configuration getConfiguration() {
		try {
			return RouterLoggerClientConfiguration.getInstance();
		}
		catch (final RuntimeException re) {
			log(re);
			return null;
		}
	}

	private static class Singleton {
		private static final Logger instance = new Logger();

		private Singleton() {
			throw new IllegalAccessError();
		}
	}

	public static Logger getInstance() {
		return Singleton.instance;
	}

	public static class Defaults {
		public static final boolean DEBUG = false;

		private Defaults() {
			throw new IllegalAccessError("Constants class");
		}
	}

	public boolean isDebugEnabled() {
		return configuration != null ? configuration.getBoolean("debug", Defaults.DEBUG) : true;
	}

	public void log(final String text) {
		log(text, new Date());
	}

	public void log(final String text, final Date timestamp) {
		System.out.println(timestampFormat.get().format(timestamp) + ' ' + text);
	}

	public void log(final Throwable throwable) {
		final Writer sw = new StringWriter();
		throwable.printStackTrace(new PrintWriter(sw));
		System.err.println(timestampFormat.get().format(new Date()) + ' ' + sw.toString().trim());
	}

}

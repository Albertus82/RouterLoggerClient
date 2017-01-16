package it.albertus.router.client.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import it.albertus.router.client.engine.RouterLoggerClientConfiguration;
import it.albertus.util.Configuration;
import it.albertus.util.ExceptionUtils;

public class Logger {

	static final ThreadLocal<DateFormat> timestampFormat = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		}
	};

	private Logger() {}

	private Configuration getConfiguration() {
		try {
			return RouterLoggerClientConfiguration.getInstance();
		}
		catch (final RuntimeException re) {
			error(re);
			return null;
		}
	}

	private static class Singleton {
		private static final Logger instance = new Logger();

		private Singleton() {
			throw new IllegalAccessError();
		}
	}

	static Logger getInstance() {
		return Singleton.instance;
	}

	public static class Defaults {
		public static final boolean DEBUG = false;

		private Defaults() {
			throw new IllegalAccessError("Constants class");
		}
	}

	public boolean isDebugEnabled() {
		final Configuration configuration = getConfiguration();
		return configuration != null ? configuration.getBoolean("debug", Defaults.DEBUG) : true;
	}

	public void debug(final String message) {
		if (isDebugEnabled()) {
			info(message);
		}
	}

	public void debug(final Throwable throwable) {
		if (isDebugEnabled()) {
			System.out.println(timestampFormat.get().format(new Date()) + ' ' + ExceptionUtils.getStackTrace(throwable).trim());
		}
	}

	public void info(final String message) {
		info(message, new Date());
	}

	public void info(final String message, final Date timestamp) {
		System.out.println(timestampFormat.get().format(timestamp) + ' ' + message);
	}

	public void error(final Throwable throwable) {
		System.err.println(timestampFormat.get().format(new Date()) + ' ' + ExceptionUtils.getStackTrace(throwable).trim());
	}

}

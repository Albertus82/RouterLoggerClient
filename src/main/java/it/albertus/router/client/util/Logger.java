package it.albertus.router.client.util;

import it.albertus.router.client.engine.RouterLoggerClientConfiguration;
import it.albertus.util.Configuration;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

	private static final DateFormat timestampFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

	private static class Singleton {
		private static final Logger instance = new Logger();
	}

	public static Logger getInstance() {
		return Singleton.instance;
	}

	private Logger() {
		Configuration configuration;
		try {
			configuration = RouterLoggerClientConfiguration.getInstance();
		}
		catch (final Throwable t) {
			t.printStackTrace();
			configuration = null;
		}
		this.configuration = configuration;
	}

	public interface Defaults {
		boolean DEBUG = false;
	}

	private final Configuration configuration;

	public boolean isDebugEnabled() {
		return configuration != null ? configuration.getBoolean("console.debug", Defaults.DEBUG) : true;
	}

	public void log(final String text) {
		log(text, new Date());
	}

	public void log(final String text, final Date timestamp) {
		System.out.println(formatTimestamp(timestamp) + ' ' + text);
	}

	public void log(final Throwable throwable) {
		final Writer sw = new StringWriter();
		throwable.printStackTrace(new PrintWriter(sw));
		System.err.println(formatTimestamp(new Date()) + ' ' + sw.toString().trim());
	}

	private synchronized String formatTimestamp(final Date timestamp) {
		return timestampFormat.format(timestamp);
	}

}

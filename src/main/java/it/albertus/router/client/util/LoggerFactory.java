package it.albertus.router.client.util;

public class LoggerFactory {

	private LoggerFactory() {
		throw new IllegalAccessError();
	}

	public static Logger getLogger(final Class<?> clazz) {
		return Logger.getInstance();
	}

}

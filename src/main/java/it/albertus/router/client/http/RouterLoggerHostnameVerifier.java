package it.albertus.router.client.http;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

public class RouterLoggerHostnameVerifier implements HostnameVerifier {

	private final String hostname;

	public RouterLoggerHostnameVerifier(final String hostname) {
		this.hostname = hostname;
	}

	@Override
	public boolean verify(final String hostname, final SSLSession session) {
		return hostname.equals(this.hostname);
	}

}

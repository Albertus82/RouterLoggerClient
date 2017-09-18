package it.albertus.router.client.http;

import java.io.IOException;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

import javax.xml.bind.DatatypeConverter;

import it.albertus.router.client.engine.RouterLoggerClientConfig;
import it.albertus.util.Configuration;
import it.albertus.util.StringUtils;
import it.albertus.util.Version;

public class HttpConnector {

	public static class Defaults {
		public static final int CONNECTION_TIMEOUT = 0;
		public static final int READ_TIMEOUT = 0;
		public static final boolean AUTHENTICATION = true;
		public static final boolean PROXY_ENABLED = false;
		public static final Type PROXY_TYPE = Type.HTTP;
		public static final String PROXY_ADDRESS = "10.0.0.1";
		public static final int PROXY_PORT = 8080;
		public static final boolean PROXY_AUTH_REQUIRED = false;

		private Defaults() {
			throw new IllegalAccessError("Constants class");
		}
	}

	private static final String USER_AGENT = String.format("Mozilla/5.0 (%s; %s; %s) RouterLoggerClient/%s (KHTML, like Gecko)", System.getProperty("os.name"), System.getProperty("os.arch"), System.getProperty("os.version"), Version.getInstance().getNumber());

	private static final String CHARSET = "UTF-8";

	private static final Configuration configuration = RouterLoggerClientConfig.getInstance();

	private HttpConnector() {
		throw new IllegalAccessError();
	}

	public static HttpURLConnection openConnection(final URL url, final String etag) throws IOException {
		final URLConnection connection;
		if (configuration.getBoolean("proxy.enabled", Defaults.PROXY_ENABLED)) {
			if (configuration.getBoolean("proxy.auth.required", Defaults.PROXY_AUTH_REQUIRED)) {
				Authenticator.setDefault(new Authenticator() {
					@Override
					public PasswordAuthentication getPasswordAuthentication() {
						return (new PasswordAuthentication(configuration.getString("proxy.user"), configuration.getCharArray("proxy.password")));
					}
				});
			}
			else {
				Authenticator.setDefault(null);
			}
			final Proxy proxy = new Proxy(Proxy.Type.valueOf(configuration.getString("proxy.type", Defaults.PROXY_TYPE.name())), new InetSocketAddress(configuration.getString("proxy.address", Defaults.PROXY_ADDRESS), configuration.getInt("proxy.port", Defaults.PROXY_PORT)));
			connection = url.openConnection(proxy);
		}
		else {
			Authenticator.setDefault(null);
			connection = url.openConnection(/* DIRECT */);
		}
		if (connection instanceof HttpURLConnection) {
			connection.setConnectTimeout(configuration.getInt("http.connection.timeout", Defaults.CONNECTION_TIMEOUT));
			connection.setReadTimeout(configuration.getInt("http.read.timeout", Defaults.READ_TIMEOUT));
			connection.setRequestProperty("User-Agent", USER_AGENT);
			connection.addRequestProperty("Accept", "application/json");
			connection.addRequestProperty("Accept-Encoding", "gzip");
			if (etag != null && !etag.trim().isEmpty()) {
				connection.addRequestProperty("If-None-Match", etag);
			}
			final String username = configuration.getString("http.username");
			final char[] password = configuration.getCharArray("http.password");
			if (configuration.getBoolean("http.authentication", Defaults.AUTHENTICATION) && StringUtils.isNotEmpty(username) && password != null && password.length > 0) {
				final byte[] un = username.getBytes(CHARSET);
				final byte[] pw = toBytes(password);
				final ByteBuffer buffer = ByteBuffer.allocate(un.length + 1 + pw.length);
				buffer.put(un).put((byte) ':').put(pw);
				connection.addRequestProperty("Authorization", "Basic " + DatatypeConverter.printBase64Binary(buffer.array()));
			}
			return (HttpURLConnection) connection;
		}
		else {
			throw new IllegalArgumentException(String.valueOf(url));
		}
	}

	private static byte[] toBytes(final char[] chars) {
		final ByteBuffer byteBuffer = Charset.forName(CHARSET).encode(CharBuffer.wrap(chars));
		return Arrays.copyOfRange(byteBuffer.array(), byteBuffer.position(), byteBuffer.limit());
	}

}

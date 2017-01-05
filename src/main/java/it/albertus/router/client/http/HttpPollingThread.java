package it.albertus.router.client.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.SecureRandom;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.xml.bind.DatatypeConverter;

import com.google.gson.Gson;

import it.albertus.router.client.dto.RouterDataDto;
import it.albertus.router.client.dto.StatusDto;
import it.albertus.router.client.dto.ThresholdsDto;
import it.albertus.router.client.dto.transformer.DataTransformer;
import it.albertus.router.client.dto.transformer.StatusTransformer;
import it.albertus.router.client.dto.transformer.ThresholdsTransformer;
import it.albertus.router.client.engine.RouterData;
import it.albertus.router.client.engine.RouterLoggerClientConfiguration;
import it.albertus.router.client.engine.RouterLoggerStatus;
import it.albertus.router.client.engine.ThresholdsReached;
import it.albertus.router.client.gui.RouterLoggerClientGui;
import it.albertus.router.client.resources.Messages;
import it.albertus.router.client.util.Logger;
import it.albertus.util.Configuration;
import it.albertus.util.IOUtils;
import it.albertus.util.StringUtils;

public class HttpPollingThread extends Thread {

	private static final String CFG_KEY_CLIENT_PROTOCOL = "client.protocol";
	private static final String CFG_KEY_HTTP_HOST = "http.host";
	private static final String CFG_KEY_HTTP_PORT = "http.port";
	private static final String CFG_KEY_HTTP_AUTHENTICATION = "http.authentication";
	private static final String CFG_KEY_HTTP_USERNAME = "http.username";
	private static final String CFG_KEY_HTTP_PASSWORD = "http.password";
	private static final String CFG_KEY_HTTP_READ_TIMEOUT = "http.read.timeout";
	private static final String CFG_KEY_HTTP_CONNECTION_TIMEOUT = "http.connection.timeout";
	private static final String CFG_KEY_HTTP_REFRESH_SECS = "http.refresh.secs";
	private static final String CFG_KEY_HTTP_IGNORE_CERTIFICATE = "http.ignore.certificate";
	private static final String CFG_KEY_HTTP_CONNECTION_RETRY_INTERVAL_SECS = "http.connection.retry.interval.secs";

	public static class Defaults {
		public static final int REFRESH_SECS = 0;
		public static final boolean AUTHENTICATION = true;
		public static final int PORT = 8080;
		public static final boolean IGNORE_CERTIFICATE = false;
		public static final int CONNECTION_TIMEOUT = 0;
		public static final int READ_TIMEOUT = 0;
		public static final short CONNECTION_RETRY_INTERVAL_SECS = 30;

		private Defaults() {
			throw new IllegalAccessError("Constants class");
		}
	}

	private final Configuration configuration = RouterLoggerClientConfiguration.getInstance();
	private final RouterLoggerClientGui gui;

	private String eTag;

	private volatile boolean exit = false;

	public HttpPollingThread(final RouterLoggerClientGui gui) {
		this.setDaemon(true);
		this.gui = gui;
		if (configuration.getBoolean(CFG_KEY_HTTP_IGNORE_CERTIFICATE, Defaults.IGNORE_CERTIFICATE)) {
			try {
				final SSLContext sslContext = SSLContext.getInstance("SSL");
				sslContext.init(null, new TrustManager[] { new DummyTrustManager() }, new SecureRandom());
				HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
			}
			catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void interrupt() {
		exit = true;
		super.interrupt();
	}

	@Override
	public void run() {
		String scheme = configuration.getString(CFG_KEY_CLIENT_PROTOCOL).trim().toLowerCase();
		if (!scheme.contains("http")) {
			return;
		}
		String host = configuration.getString(CFG_KEY_HTTP_HOST);
		int port = configuration.getInt(CFG_KEY_HTTP_PORT, Defaults.PORT);

		String baseUrl = scheme + "://" + host + ":" + port;

		final Logger logger = Logger.getInstance();
		logger.log(Messages.get("msg.http.polling", scheme.toUpperCase(), baseUrl));

		while (!exit) {
			scheme = configuration.getString(CFG_KEY_CLIENT_PROTOCOL).trim().toLowerCase();
			if (!scheme.contains("http")) {
				break;
			}
			host = configuration.getString(CFG_KEY_HTTP_HOST);
			port = configuration.getInt(CFG_KEY_HTTP_PORT, Defaults.PORT);
			final String username = configuration.getString(CFG_KEY_HTTP_USERNAME);
			final char[] password = configuration.getCharArray(CFG_KEY_HTTP_PASSWORD);
			final int connectionTimeout = configuration.getInt(CFG_KEY_HTTP_CONNECTION_TIMEOUT, Defaults.CONNECTION_TIMEOUT);
			final int readTimeout = configuration.getInt(CFG_KEY_HTTP_READ_TIMEOUT, Defaults.READ_TIMEOUT);

			baseUrl = scheme + "://" + host + ":" + port;

			if (configuration.getBoolean(CFG_KEY_HTTP_IGNORE_CERTIFICATE, Defaults.IGNORE_CERTIFICATE)) {
				HttpsURLConnection.setDefaultHostnameVerifier(new RouterLoggerHostnameVerifier(host));
			}

			final String authenticationHeader = buildAuthenticationHeader(username, password);

			InputStream is = null;
			InputStreamReader isr = null;
			GZIPInputStream gis = null;
			int refresh = configuration.getInt(CFG_KEY_HTTP_REFRESH_SECS, Defaults.REFRESH_SECS);
			try {
				// RouterLoggerStatus
				final RouterLoggerStatus rls = getStatus(baseUrl, authenticationHeader, connectionTimeout, readTimeout);
				gui.setStatus(rls);

				// RouterData
				URL url = new URL(baseUrl + "/json/data");
				HttpURLConnection urlConnection = openConnection(url, authenticationHeader, connectionTimeout, readTimeout);

				if (eTag != null) {
					urlConnection.addRequestProperty("If-None-Match", eTag);
				}

				// Logging
				if (logger.isDebugEnabled()) {
					logger.log(Messages.get("msg.http.response.code", urlConnection.getResponseCode()));
				}

				for (final String header : urlConnection.getHeaderFields().keySet()) {
					if (header != null) {
						if ("Etag".equalsIgnoreCase(header)) {
							eTag = urlConnection.getHeaderField(header);
						}
						else if (refresh <= 0 && "Refresh".equalsIgnoreCase(header)) {
							refresh = Integer.parseInt(urlConnection.getHeaderField(header));
						}
					}
				}
				if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK || urlConnection.getResponseCode() == HttpURLConnection.HTTP_NOT_AUTHORITATIVE) {
					RouterDataDto routerDataDto = null;
					try {
						is = urlConnection.getInputStream();
						if (isResponseCompressed(urlConnection)) {
							gis = new GZIPInputStream(is);
							isr = new InputStreamReader(gis);
						}
						else {
							isr = new InputStreamReader(is);
						}
						routerDataDto = new Gson().fromJson(isr, RouterDataDto.class);
					}
					finally {
						IOUtils.closeQuietly(isr, gis, is);
					}
					final RouterData data = DataTransformer.fromDto(routerDataDto);

					// ThresholdsReached
					url = new URL(baseUrl + "/json/thresholds");
					urlConnection = openConnection(url, authenticationHeader, connectionTimeout, readTimeout);
					ThresholdsDto thresholdsDto = null;
					try {
						is = urlConnection.getInputStream();
						if (isResponseCompressed(urlConnection)) {
							gis = new GZIPInputStream(is);
							isr = new InputStreamReader(gis);
						}
						else {
							isr = new InputStreamReader(is);
						}
						thresholdsDto = new Gson().fromJson(isr, ThresholdsDto.class);
					}
					finally {
						IOUtils.closeQuietly(isr, gis, is);
					}
					final ThresholdsReached thresholdsReached = ThresholdsTransformer.fromDto(thresholdsDto);

					// Update GUI
					if (data != null && thresholdsReached != null) {
						gui.getDataTable().addRow(data, thresholdsReached.getReached());
					}
					if (rls != null) {
						gui.getTrayIcon().updateTrayItem(rls.getStatus(), data);
					}
					gui.getThresholdsManager().printThresholdsReached(thresholdsReached);
				}
			}
			catch (final IOException ioe) {
				ioe.printStackTrace();
				refresh = configuration.getShort(CFG_KEY_HTTP_CONNECTION_RETRY_INTERVAL_SECS, Defaults.CONNECTION_RETRY_INTERVAL_SECS);
			}
			finally {
				IOUtils.closeQuietly(isr, gis, is);
			}
			if (exit) {
				break;
			}
			else {
				if (refresh <= 0) {
					logger.log(Messages.get("err.http.refresh.auto"));
					break;
				}
				try {
					Thread.sleep(refresh * 1000L);
				}
				catch (final InterruptedException ie) {
					break;
				}
			}
		}
	}

	private String buildAuthenticationHeader(final String username, final char[] password) {
		final String authenticationHeader;
		if (configuration.getBoolean(CFG_KEY_HTTP_AUTHENTICATION, Defaults.AUTHENTICATION) && StringUtils.isNotEmpty(username) && password != null && password.length > 0) {
			authenticationHeader = "Basic " + DatatypeConverter.printBase64Binary((username + ":" + String.valueOf(password)).getBytes());
		}
		else {
			authenticationHeader = null;
		}
		return authenticationHeader;
	}

	private RouterLoggerStatus getStatus(final String baseUrl, final String authenticationHeader, final int connectionTimeout, final int readTimeout) throws MalformedURLException, IOException {
		RouterLoggerStatus status = null;
		final URL url = new URL(baseUrl + "/json/status");
		final HttpURLConnection urlConnection = openConnection(url, authenticationHeader, connectionTimeout, readTimeout);
		if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK || urlConnection.getResponseCode() == HttpURLConnection.HTTP_NOT_AUTHORITATIVE) {
			InputStream is = null;
			GZIPInputStream gis = null;
			InputStreamReader isr = null;
			StatusDto statusDto = null;
			try {
				is = urlConnection.getInputStream();
				if (isResponseCompressed(urlConnection)) {
					gis = new GZIPInputStream(is);
					isr = new InputStreamReader(gis);
				}
				else {
					isr = new InputStreamReader(is);
				}
				statusDto = new Gson().fromJson(isr, StatusDto.class);
			}
			finally {
				IOUtils.closeQuietly(isr, gis, is);
			}
			status = StatusTransformer.fromDto(statusDto);
		}
		return status;
	}

	private boolean isResponseCompressed(final URLConnection urlConnection) {
		final String contentEncoding = urlConnection.getHeaderField("Content-Encoding");
		return contentEncoding != null && contentEncoding.toLowerCase().contains("gzip");
	}

	private HttpURLConnection openConnection(final URL url, final String authenticationHeader, final int connectionTimeout, final int readTimeout) throws IOException {
		final HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
		urlConnection.setConnectTimeout(connectionTimeout);
		urlConnection.setReadTimeout(readTimeout);
		urlConnection.addRequestProperty("Accept", "application/json");
		urlConnection.addRequestProperty("Accept-Encoding", "gzip");
		if (authenticationHeader != null) {
			urlConnection.addRequestProperty("Authorization", authenticationHeader);
		}
		return urlConnection;
	}

}

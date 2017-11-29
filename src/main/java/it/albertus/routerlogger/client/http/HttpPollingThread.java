package it.albertus.routerlogger.client.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;

import com.google.gson.Gson;

import it.albertus.routerlogger.client.dto.DeviceStatusDto;
import it.albertus.routerlogger.client.dto.AppStatusDto;
import it.albertus.routerlogger.client.dto.transformer.DataTransformer;
import it.albertus.routerlogger.client.dto.transformer.StatusTransformer;
import it.albertus.routerlogger.client.dto.transformer.ThresholdsTransformer;
import it.albertus.routerlogger.client.engine.RouterData;
import it.albertus.routerlogger.client.engine.RouterLoggerClientConfig;
import it.albertus.routerlogger.client.engine.AppStatus;
import it.albertus.routerlogger.client.engine.ThresholdsReached;
import it.albertus.routerlogger.client.gui.RouterLoggerClientGui;
import it.albertus.routerlogger.client.resources.Messages;
import it.albertus.util.Configuration;
import it.albertus.util.logging.LoggerFactory;

public class HttpPollingThread extends Thread {

	private static final Logger logger = LoggerFactory.getLogger(HttpPollingThread.class);

	private static final Configuration configuration = RouterLoggerClientConfig.getInstance();

	private static final String HDR_KEY_REFRESH = "Refresh";
	private static final String HDR_KEY_ETAG = "ETag";

	private static final String CFG_KEY_CLIENT_PROTOCOL = "client.protocol";
	private static final String CFG_KEY_HTTP_HOST = "http.host";
	private static final String CFG_KEY_HTTP_PORT = "http.port";
	private static final String CFG_KEY_HTTP_REFRESH_SECS = "http.refresh.secs";
	private static final String CFG_KEY_HTTP_IGNORE_CERTIFICATE = "http.ignore.certificate";
	private static final String CFG_KEY_HTTP_CONNECTION_RETRY_INTERVAL_SECS = "http.connection.retry.interval.secs";

	public static class Defaults {
		public static final int REFRESH_SECS = 0;
		public static final int PORT = 8080;
		public static final boolean IGNORE_CERTIFICATE = false;
		public static final short CONNECTION_RETRY_INTERVAL_SECS = 30;

		private Defaults() {
			throw new IllegalAccessError("Constants class");
		}
	}

	private final RouterLoggerClientGui gui;

	private int refresh;
	private String eTagData;
	private String eTagStatus;

	public HttpPollingThread(final RouterLoggerClientGui gui) {
		this.setDaemon(true);
		this.gui = gui;
		if (configuration.getBoolean(CFG_KEY_HTTP_IGNORE_CERTIFICATE, Defaults.IGNORE_CERTIFICATE)) {
			try {
				final SSLContext sslContext = SSLContext.getInstance("TLS");
				sslContext.init(null, new TrustManager[] { new DummyTrustManager() }, new SecureRandom());
				HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
			}
			catch (final Exception e) {
				logger.log(Level.SEVERE, e.toString(), e);
			}
		}
	}

	@Override
	public void run() {
		String scheme = configuration.getString(CFG_KEY_CLIENT_PROTOCOL, true).trim().toLowerCase();

		if (!scheme.contains("http")) {
			return;
		}

		logger.log(Level.INFO, Messages.get("msg.http.polling"), new Object[] { scheme.toUpperCase(), scheme + "://" + configuration.getString(CFG_KEY_HTTP_HOST) + ":" + configuration.getInt(CFG_KEY_HTTP_PORT, Defaults.PORT) });

		while (!Thread.interrupted()) {
			scheme = configuration.getString(CFG_KEY_CLIENT_PROTOCOL, true).trim().toLowerCase();

			if (!scheme.contains("http")) {
				break;
			}

			final String host = configuration.getString(CFG_KEY_HTTP_HOST, true);

			if (configuration.getBoolean(CFG_KEY_HTTP_IGNORE_CERTIFICATE, Defaults.IGNORE_CERTIFICATE)) {
				HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> host.equals(hostname));
			}

			refresh = configuration.getInt(CFG_KEY_HTTP_REFRESH_SECS, Defaults.REFRESH_SECS);
			try {
				final String baseUrl = scheme + "://" + host + ":" + configuration.getInt(CFG_KEY_HTTP_PORT, Defaults.PORT);

				final AppStatus status = getRouterLoggerStatus(baseUrl);

				if (status != null) {
					gui.updateStatus(status);
				}

				final DeviceStatusDto routerDataDto = getRouterDataDto(baseUrl);
				final RouterData routerData = DataTransformer.fromDto(routerDataDto);
				if (status != null) {
					gui.getTrayIcon().updateTrayItem(status.getStatus(), routerData);
				}

				final ThresholdsReached thresholdsReached = ThresholdsTransformer.fromDto(routerDataDto);
				gui.getDataTable().addRow(routerData, thresholdsReached);
				gui.getThresholdsManager().printThresholdsReached(thresholdsReached);
			}
			catch (final SSLException e) {
				logger.log(Level.WARNING, Messages.get("err.http.ssl", host, Messages.get("lbl.preferences.http.ignore.certificate")), e);
				refresh = configuration.getShort(CFG_KEY_HTTP_CONNECTION_RETRY_INTERVAL_SECS, Defaults.CONNECTION_RETRY_INTERVAL_SECS);
			}
			catch (final IOException e) {
				logger.log(Level.SEVERE, e.toString(), e);
				refresh = configuration.getShort(CFG_KEY_HTTP_CONNECTION_RETRY_INTERVAL_SECS, Defaults.CONNECTION_RETRY_INTERVAL_SECS);
			}
			if (Thread.interrupted()) {
				break;
			}
			else {
				if (refresh <= 0) {
					logger.info(Messages.get("err.http.refresh.auto"));
					break;
				}
				try {
					TimeUnit.SECONDS.sleep(refresh);
				}
				catch (final InterruptedException ie) {
					interrupt();
				}
			}
		}
	}

	private AppStatus getRouterLoggerStatus(final String baseUrl) throws IOException {
		final URL url = new URL(baseUrl + "/status/app");
		final HttpURLConnection urlConnection = ConnectionFactory.createHttpConnection(url, eTagStatus);

		urlConnection.connect();
		for (final String header : urlConnection.getHeaderFields().keySet()) {
			if (header != null && HDR_KEY_ETAG.equalsIgnoreCase(header)) {
				eTagStatus = urlConnection.getHeaderField(header);
				break;
			}
		}

		final AppStatusDto dto = getDtoFromHttpResponse(urlConnection, AppStatusDto.class);
		return StatusTransformer.fromDto(dto);
	}

	private DeviceStatusDto getRouterDataDto(final String baseUrl) throws IOException {
		final URL url = new URL(baseUrl + "/status/device");
		final HttpURLConnection urlConnection = ConnectionFactory.createHttpConnection(url, eTagData);

		urlConnection.connect();
		for (final String header : urlConnection.getHeaderFields().keySet()) {
			if (header != null) {
				if (HDR_KEY_ETAG.equalsIgnoreCase(header)) {
					eTagData = urlConnection.getHeaderField(header);
				}
				else if (refresh <= 0 && HDR_KEY_REFRESH.equalsIgnoreCase(header)) {
					refresh = Integer.parseInt(urlConnection.getHeaderField(header));
				}
			}
		}

		return getDtoFromHttpResponse(urlConnection, DeviceStatusDto.class);
	}

	private <T> T getDtoFromHttpResponse(final HttpURLConnection urlConnection, final Class<T> dtoClass) throws IOException {
		if (logger.isLoggable(Level.FINE)) {
			final StringBuilder message = new StringBuilder(urlConnection.getRequestMethod());
			message.append(' ').append(String.valueOf(urlConnection.getURL()));
			message.append(" - ").append(urlConnection.getResponseCode());
			if (urlConnection.getContentLength() != -1) {
				message.append(" - Content-Length: ").append(urlConnection.getContentLength());
			}
			if (urlConnection.getContentEncoding() != null) {
				message.append(" - Content-Encoding: ").append(urlConnection.getContentEncoding());
			}
			logger.fine(message.toString());
		}

		if (urlConnection.getContentLength() > 0 && (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK || urlConnection.getResponseCode() == HttpURLConnection.HTTP_NOT_AUTHORITATIVE)) {
			try (final InputStream httpInputStream = urlConnection.getInputStream(); final GZIPInputStream gzipInputStream = isResponseCompressed(urlConnection) ? new GZIPInputStream(httpInputStream) : null; final InputStreamReader inputStreamReader = new InputStreamReader(gzipInputStream != null ? gzipInputStream : httpInputStream)) {
				return new Gson().fromJson(inputStreamReader, dtoClass);
			}
		}
		else {
			return null;
		}
	}

	private static boolean isResponseCompressed(final URLConnection urlConnection) {
		final String contentEncoding = urlConnection.getContentEncoding();
		return contentEncoding != null && contentEncoding.toLowerCase().contains("gzip");
	}

}

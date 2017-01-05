package it.albertus.router.client.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.Arrays;
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

	private static final String HDR_KEY_AUTHORIZATION = "Authorization";
	private static final String HDR_KEY_ACCEPT = "Accept";
	private static final String HDR_KEY_ACCEPT_ENCODING = "Accept-Encoding";
	private static final String HDR_KEY_REFRESH = "Refresh";
	private static final String HDR_KEY_ETAG = "ETag";
	private static final String HDR_KEY_IF_NONE_MATCH = "If-None-Match";

	private static final String CHARSET = "UTF-8";

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

	private final Logger logger = Logger.getInstance();
	private volatile boolean exit = false;

	private int refresh;
	private String eTagData;
	private String eTagStatus;
	private String eTagThresholds;

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
				logger.log(e);
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
		String scheme = configuration.getString(CFG_KEY_CLIENT_PROTOCOL, true).trim().toLowerCase();

		if (!scheme.contains("http")) {
			return;
		}

		String host = configuration.getString(CFG_KEY_HTTP_HOST);

		logger.log(Messages.get("msg.http.polling", scheme.toUpperCase(), scheme + "://" + host + ":" + configuration.getInt(CFG_KEY_HTTP_PORT, Defaults.PORT)));

		while (!exit) {
			// Prepare connection parameters
			scheme = configuration.getString(CFG_KEY_CLIENT_PROTOCOL, true).trim().toLowerCase();

			if (!scheme.contains("http")) {
				break;
			}

			host = configuration.getString(CFG_KEY_HTTP_HOST, true);

			final HttpConnectionParams params = new HttpConnectionParams(scheme + "://" + host + ":" + configuration.getInt(CFG_KEY_HTTP_PORT, Defaults.PORT), configuration.getInt(CFG_KEY_HTTP_CONNECTION_TIMEOUT, Defaults.CONNECTION_TIMEOUT), configuration.getInt(CFG_KEY_HTTP_READ_TIMEOUT, Defaults.READ_TIMEOUT), configuration.getString(CFG_KEY_HTTP_USERNAME), configuration.getCharArray(CFG_KEY_HTTP_PASSWORD));

			if (configuration.getBoolean(CFG_KEY_HTTP_IGNORE_CERTIFICATE, Defaults.IGNORE_CERTIFICATE)) {
				HttpsURLConnection.setDefaultHostnameVerifier(new RouterLoggerHostnameVerifier(host));
			}

			refresh = configuration.getInt(CFG_KEY_HTTP_REFRESH_SECS, Defaults.REFRESH_SECS);
			try {
				final RouterLoggerStatus status = getRouterLoggerStatus(params);

				if (status != null) {
					gui.setStatus(status);
				}

				final RouterData routerData = getRouterData(params);
				if (routerData != null) {
					final ThresholdsReached thresholdsReached = getThresholdsReached(params);
					gui.getDataTable().addRow(routerData, thresholdsReached);
					gui.getThresholdsManager().printThresholdsReached(thresholdsReached);
				}

				if (status != null) {
					gui.getTrayIcon().updateTrayItem(status.getStatus(), routerData);
				}
			}
			catch (final IOException ioe) {
				logger.log(ioe);
				refresh = configuration.getShort(CFG_KEY_HTTP_CONNECTION_RETRY_INTERVAL_SECS, Defaults.CONNECTION_RETRY_INTERVAL_SECS);
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
					interrupt();
				}
			}
		}
	}

	private RouterLoggerStatus getRouterLoggerStatus(final HttpConnectionParams params) throws IOException {
		final URL url = new URL(params.getBaseUrl() + "/json/status");
		final HttpURLConnection urlConnection = openConnection(url, params.getConnectionTimeout(), params.getReadTimeout(), params.getUsername(), params.getPassword());

		if (eTagStatus != null) {
			urlConnection.addRequestProperty(HDR_KEY_IF_NONE_MATCH, eTagStatus);
		}
		for (final String header : urlConnection.getHeaderFields().keySet()) {
			if (header != null && HDR_KEY_ETAG.equalsIgnoreCase(header)) {
				eTagStatus = urlConnection.getHeaderField(header);
			}
		}

		final StatusDto dto = getDtoFromHttpResponse(urlConnection, StatusDto.class);
		return StatusTransformer.fromDto(dto);
	}

	private RouterData getRouterData(final HttpConnectionParams params) throws IOException {
		final URL url = new URL(params.getBaseUrl() + "/json/data");
		final HttpURLConnection urlConnection = openConnection(url, params.getConnectionTimeout(), params.getReadTimeout(), params.getUsername(), params.getPassword());

		if (eTagData != null) {
			urlConnection.addRequestProperty(HDR_KEY_IF_NONE_MATCH, eTagData);
		}

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

		final RouterDataDto dto = getDtoFromHttpResponse(urlConnection, RouterDataDto.class);
		return DataTransformer.fromDto(dto);
	}

	private ThresholdsReached getThresholdsReached(final HttpConnectionParams params) throws IOException {
		final URL url = new URL(params.getBaseUrl() + "/json/thresholds");
		final HttpURLConnection urlConnection = openConnection(url, params.getConnectionTimeout(), params.getReadTimeout(), params.getUsername(), params.getPassword());

		if (eTagThresholds != null) {
			urlConnection.addRequestProperty(HDR_KEY_IF_NONE_MATCH, eTagThresholds);
		}
		for (final String header : urlConnection.getHeaderFields().keySet()) {
			if (header != null && HDR_KEY_ETAG.equalsIgnoreCase(header)) {
				eTagThresholds = urlConnection.getHeaderField(header);
			}
		}

		final ThresholdsDto dto = getDtoFromHttpResponse(urlConnection, ThresholdsDto.class);
		return ThresholdsTransformer.fromDto(dto);
	}

	private <T> T getDtoFromHttpResponse(final HttpURLConnection urlConnection, final Class<T> dtoClass) throws IOException {
		if (logger.isDebugEnabled()) {
			StringBuilder sb = new StringBuilder(String.valueOf(urlConnection.getURL()));
			sb.append(" - ").append(urlConnection.getResponseCode());
			if (urlConnection.getContentLength() != -1) {
				sb.append(" - Content-Length: ").append(urlConnection.getContentLength());
			}
			if (urlConnection.getContentEncoding() != null) {
				sb.append(" - Content-Encoding: " + urlConnection.getContentEncoding());
			}
			logger.log(sb.toString());
		}

		if (urlConnection.getContentLength() > 0 && (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK || urlConnection.getResponseCode() == HttpURLConnection.HTTP_NOT_AUTHORITATIVE)) {
			InputStream httpInputStream = null;
			GZIPInputStream gzipInputStream = null;
			InputStreamReader inputStreamReader = null;
			try {
				httpInputStream = urlConnection.getInputStream();
				if (isResponseCompressed(urlConnection)) {
					gzipInputStream = new GZIPInputStream(httpInputStream);
				}
				inputStreamReader = new InputStreamReader(gzipInputStream != null ? gzipInputStream : httpInputStream);
				return new Gson().fromJson(inputStreamReader, dtoClass);
			}
			finally {
				IOUtils.closeQuietly(inputStreamReader, gzipInputStream, httpInputStream);
			}
		}
		else {
			return null;
		}
	}

	private boolean isResponseCompressed(final URLConnection urlConnection) {
		final String contentEncoding = urlConnection.getContentEncoding();
		return contentEncoding != null && contentEncoding.toLowerCase().contains("gzip");
	}

	private HttpURLConnection openConnection(final URL url, final int connectionTimeout, final int readTimeout, final String username, final char[] password) throws IOException {
		final HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
		urlConnection.setConnectTimeout(connectionTimeout);
		urlConnection.setReadTimeout(readTimeout);
		urlConnection.addRequestProperty(HDR_KEY_ACCEPT, "application/json");
		urlConnection.addRequestProperty(HDR_KEY_ACCEPT_ENCODING, "gzip");
		if (configuration.getBoolean(CFG_KEY_HTTP_AUTHENTICATION, Defaults.AUTHENTICATION) && StringUtils.isNotEmpty(username) && password != null && password.length > 0) {
			final byte[] un = username.getBytes(CHARSET);
			final byte[] pw = toBytes(password);
			final ByteBuffer buffer = ByteBuffer.allocate(un.length + 1 + pw.length);
			buffer.put(un);
			buffer.put((byte) ':');
			buffer.put(pw);
			urlConnection.addRequestProperty(HDR_KEY_AUTHORIZATION, "Basic " + DatatypeConverter.printBase64Binary(buffer.array()));
		}
		return urlConnection;
	}

	private static byte[] toBytes(final char[] chars) {
		final ByteBuffer byteBuffer = Charset.forName(CHARSET).encode(CharBuffer.wrap(chars));
		return Arrays.copyOfRange(byteBuffer.array(), byteBuffer.position(), byteBuffer.limit());
	}

}

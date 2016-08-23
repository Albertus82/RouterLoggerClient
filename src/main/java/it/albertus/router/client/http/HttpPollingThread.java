package it.albertus.router.client.http;

import it.albertus.router.client.dto.RouterDataDto;
import it.albertus.router.client.dto.StatusDto;
import it.albertus.router.client.dto.ThresholdDto;
import it.albertus.router.client.dto.ThresholdsDto;
import it.albertus.router.client.engine.RouterData;
import it.albertus.router.client.engine.RouterLoggerStatus;
import it.albertus.router.client.engine.Status;
import it.albertus.router.client.engine.Threshold;
import it.albertus.router.client.engine.Threshold.Type;
import it.albertus.router.client.engine.ThresholdsReached;
import it.albertus.router.client.gui.RouterLoggerGui;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.bind.DatatypeConverter;

import com.google.gson.Gson;

public class HttpPollingThread extends Thread {

	private final RouterLoggerGui gui;

	private int iteration = 0;
	private String eTag;
	private int refresh;

	public HttpPollingThread(final RouterLoggerGui gui) {
		this.setDaemon(true);
		this.gui = gui;
	}

	@Override
	public void run() {
		while (true) {
			if (refresh > 0) {
				try {
					Thread.sleep(refresh * 1000);
				}
				catch (InterruptedException e) {
					break;
				}
			}


			
			final String scheme = "https"; // TODO config
			final String host = "localhost"; // TODO config
			final String username = "admin"; // TODO config
			final String password = "admin"; // TODO config

			if (true) {
				HttpsURLConnection.setDefaultHostnameVerifier(new RouterLoggerHostnameVerifier(host));
			}

			final String authenticationHeader;
			if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
				authenticationHeader = "Basic " + DatatypeConverter.printBase64Binary((username + ":" + password).getBytes());
			}
			else {
				authenticationHeader = null;
			}

			// RouterLoggerStatus
			Reader httpReader = null;
			try {
				URL url = new URL(scheme + "://" + host + "/json/status");
				HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.addRequestProperty("Accept", "application/json");
				if (authenticationHeader != null) {
					urlConnection.addRequestProperty("Authorization", authenticationHeader);
				}
				InputStream is = urlConnection.getInputStream();
				httpReader = new InputStreamReader(is);
				final StatusDto statusDto = new Gson().fromJson(httpReader, StatusDto.class);
				httpReader.close();
				final RouterLoggerStatus rls = new RouterLoggerStatus(Status.valueOf(statusDto.getStatus()), statusDto.getTimestamp());
				gui.setStatus(rls);

				// RouterData
				url = new URL(scheme + "://" + host + "/json/data");
				urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.addRequestProperty("Accept", "application/json");
				if (authenticationHeader != null) {
					urlConnection.addRequestProperty("Authorization", authenticationHeader);
				}

				if (eTag != null) {
					urlConnection.addRequestProperty("If-None-Match", eTag);
				}

				if (urlConnection.getResponseCode() != 200) {
					continue;
				}
				for (final String header : urlConnection.getHeaderFields().keySet()) {
					if (header != null) {
						if (header.equalsIgnoreCase("Etag")) {
							eTag = urlConnection.getHeaderField(header);
						}
						else if (header.equalsIgnoreCase("Refresh")) {
							refresh = Integer.parseInt(urlConnection.getHeaderField(header));
						}
					}
				}
				if (refresh == 0) {
					refresh = 5; // TODO default
				}

				is = urlConnection.getInputStream();
				httpReader = new InputStreamReader(is);
				final RouterDataDto routerDataDto = new Gson().fromJson(httpReader, RouterDataDto.class);
				httpReader.close();
				final RouterData data = new RouterData(routerDataDto.getTimestamp(), routerDataDto.getResponseTime(), routerDataDto.getData());

				// ThresholdsReached
				url = new URL(scheme + "://" + host + "/json/thresholds");
				urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.addRequestProperty("Accept", "application/json");
				if (authenticationHeader != null) {
					urlConnection.addRequestProperty("Authorization", authenticationHeader);
				}
				is = urlConnection.getInputStream();
				httpReader = new InputStreamReader(is);
				final ThresholdsDto thresholdsDto = new Gson().fromJson(httpReader, ThresholdsDto.class);
				httpReader.close();
				final Map<Threshold, String> reached = new LinkedHashMap<>();
				for (final ThresholdDto td : thresholdsDto.getReached()) {
					reached.put(new Threshold(td.getName(), td.getKey(), Type.valueOf(td.getType()), td.getValue(), td.isExcluded()), td.getDetected());
				}
				final ThresholdsReached thresholdsReached = new ThresholdsReached(reached, routerDataDto.getTimestamp());

				// Update GUI
				gui.getDataTable().addRow(++iteration, data, thresholdsReached.getReached());
				gui.getTrayIcon().updateTrayItem(rls.getStatus(), data);
			}
			catch (final IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}
}
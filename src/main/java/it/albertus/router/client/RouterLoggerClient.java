package it.albertus.router.client;

import it.albertus.router.client.engine.RouterData;
import it.albertus.router.client.gui.RouterLoggerGui;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.bind.DatatypeConverter;

import org.eclipse.swt.widgets.Display;

import com.google.gson.Gson;

public class RouterLoggerClient {

	public static void main(final String[] args) {

//		new Thread() {
//			@Override
//			public void run() {
//
//				final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
//					@Override
//					public java.security.cert.X509Certificate[] getAcceptedIssuers() {
//						return null;
//					}
//
//					@Override
//					public void checkClientTrusted(X509Certificate[] certs, String authType) {}
//
//					@Override
//					public void checkServerTrusted(X509Certificate[] certs, String authType) {}
//				} };
//
//				try {
//					SSLContext sc = SSLContext.getInstance("SSL");
//					sc.init(null, trustAllCerts, new SecureRandom());
//					HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
//				}
//				catch (final Exception e) {
//					e.printStackTrace();
//				}
//
//				// Create all-trusting host name verifier
//				final HostnameVerifier allHostsValid = new HostnameVerifier() {
//					@Override
//					public boolean verify(String hostname, SSLSession session) {
//						if (hostname.equals("localhost"))
//						return true;
//						return false;
//					}
//				};
//				
//				// Install the all-trusting host verifier
//				HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
//
//				Reader r = null;
//				try {
//					URL url = new URL("https://norad.sytes.net/json/data");
//					final URLConnection urlConnection = url.openConnection();
//					final String header = "Basic " + DatatypeConverter.printBase64Binary("admin:admin".getBytes());
//					urlConnection.addRequestProperty("Authorization", header);
//
//					InputStream is = urlConnection.getInputStream();
//					r = new InputStreamReader(is);
//				}
//				catch (IOException ioe) {
//					ioe.printStackTrace();
//				}
//				RouterData rd = new Gson().fromJson(r, RouterData.class);
//				System.out.println(rd);
//			}
//		}.start();

		final Display display = Display.getDefault();
		new RouterLoggerGui(display);
		display.dispose();
	}

}

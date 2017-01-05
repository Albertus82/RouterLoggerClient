package it.albertus.router.client.http;

public class HttpConnectionParams {

	private final String baseUrl;
	private final int connectionTimeout;
	private final int readTimeout;
	private final String username;
	private final char[] password;

	public HttpConnectionParams(final String baseUrl, final int connectionTimeout, final int readTimeout, final String username, final char[] password) {
		this.baseUrl = baseUrl;
		this.connectionTimeout = connectionTimeout;
		this.readTimeout = readTimeout;
		this.username = username;
		this.password = password;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public int getReadTimeout() {
		return readTimeout;
	}

	public String getUsername() {
		return username;
	}

	public char[] getPassword() {
		return password;
	}

	@Override
	public String toString() {
		return "HttpConnectionParams [baseUrl=" + baseUrl + ", connectionTimeout=" + connectionTimeout + ", readTimeout=" + readTimeout + ", username=" + username + "]";
	}

}

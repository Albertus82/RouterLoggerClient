package it.albertus.router.client.engine;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

public class RouterData implements Serializable {

	private static final long serialVersionUID = -9084896320968670667L;

	private final Date timestamp;
	private final int responseTime;
	private final Map<String, String> data;

	public RouterData(final Date timestamp, final int responseTime, final Map<String, String> data) {
		this.timestamp = timestamp;
		this.responseTime = responseTime;
		this.data = data;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public int getResponseTime() {
		return responseTime;
	}

	public Map<String, String> getData() {
		return data;
	}

	@Override
	public String toString() {
		return "RouterData [timestamp=" + timestamp + ", responseTime=" + responseTime + ", data=" + data + "]";
	}

}

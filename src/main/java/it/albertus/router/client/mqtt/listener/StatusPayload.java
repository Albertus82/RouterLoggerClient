package it.albertus.router.client.mqtt.listener;

import java.io.Serializable;
import java.util.Date;

public class StatusPayload implements Serializable {

	private static final long serialVersionUID = 5246171948450669402L;

	private Date timestamp;
	private String status;
	private String description;

	public StatusPayload(Date timestamp, String status, String description) {
		this.timestamp = timestamp;
		this.status = status;
		this.description = description;
	}

	@Override
	public String toString() {
		return "StatusPayload [timestamp=" + timestamp + ", status=" + status + ", description=" + description + "]";
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}

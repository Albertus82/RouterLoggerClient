package it.albertus.routerlogger.client.dto;

import java.io.Serializable;
import java.util.Date;

public class StatusDto implements Serializable {

	private static final long serialVersionUID = -5771403443518144259L;

	private Date timestamp;
	private String status;
	private String description;

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

	@Override
	public String toString() {
		return "StatusDto [timestamp=" + timestamp + ", status=" + status + ", description=" + description + "]";
	}

}

package it.albertus.router.client.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

public class RouterDataDto implements Serializable {

	private static final long serialVersionUID = -5450464991895238316L;

	private Date timestamp;
	private Integer responseTime;
	private Map<String, String> data;

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public Integer getResponseTime() {
		return responseTime;
	}

	public void setResponseTime(Integer responseTime) {
		this.responseTime = responseTime;
	}

	public Map<String, String> getData() {
		return data;
	}

	public void setData(Map<String, String> data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "RouterDataDto [timestamp=" + timestamp + ", responseTime=" + responseTime + ", data=" + data + "]";
	}

}

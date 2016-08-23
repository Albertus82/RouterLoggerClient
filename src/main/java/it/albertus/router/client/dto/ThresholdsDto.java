package it.albertus.router.client.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

public class ThresholdsDto implements Serializable {

	private static final long serialVersionUID = -566811797356824879L;

	private Date timestamp;
	private Set<ThresholdDto> reached;

	public Date getTimestamp() {
		return timestamp;
	}

	public Set<ThresholdDto> getReached() {
		return reached;
	}

	@Override
	public String toString() {
		return "ThresholdsDto [timestamp=" + timestamp + ", reached=" + reached + "]";
	}

}

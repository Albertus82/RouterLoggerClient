package it.albertus.router.client.mqtt.listener;

import it.albertus.router.client.Threshold;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

public class ThresholdsPayload implements Serializable {

	private static final long serialVersionUID = -123559420396423308L;

	private Date timestamp;
	private Set<ThresholdItem> thresholds;

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public Set<ThresholdItem> getThresholds() {
		return thresholds;
	}

	public void setThresholds(Set<ThresholdItem> thresholds) {
		this.thresholds = thresholds;
	}

	@Override
	public String toString() {
		return "ThresholdsPayload [timestamp=" + timestamp + ", thresholds=" + thresholds + "]";
	}

}

class ThresholdItem implements Serializable {

	private static final long serialVersionUID = 1582125718825266945L;

	private Threshold threshold;

	private String name;
	private String key;
	private String type;
	private String value;
	// private final boolean excluded;
	private String detected;

	public ThresholdItem(final Threshold threshold, final String value) {
		this.threshold = threshold;
		this.name = threshold.getName();
		this.key = threshold.getKey();
		this.type = threshold.getType().name();
		this.value = threshold.getValue();
		// this.excluded = threshold.isExcluded();
		this.detected = value;
	}

	public Threshold getThreshold() {
		return threshold;
	}

	public void setThreshold(Threshold threshold) {
		this.threshold = threshold;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getDetected() {
		return detected;
	}

	public void setDetected(String detected) {
		this.detected = detected;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((threshold == null) ? 0 : threshold.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ThresholdItem)) {
			return false;
		}
		ThresholdItem other = (ThresholdItem) obj;
		if (threshold == null) {
			if (other.threshold != null) {
				return false;
			}
		}
		else if (!threshold.equals(other.threshold)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "ThresholdItem [name=" + name + ", key=" + key + ", type=" + type + ", value=" + value + ", detected=" + detected + "]";
	}

}

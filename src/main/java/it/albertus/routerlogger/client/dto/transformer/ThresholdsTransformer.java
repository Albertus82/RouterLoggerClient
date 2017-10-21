package it.albertus.routerlogger.client.dto.transformer;

import java.util.LinkedHashMap;
import java.util.Map;

import it.albertus.routerlogger.client.dto.DeviceStatusDto;
import it.albertus.routerlogger.client.dto.DeviceStatusDto.ThresholdDto;
import it.albertus.routerlogger.client.engine.Threshold;
import it.albertus.routerlogger.client.engine.Threshold.Type;
import it.albertus.routerlogger.client.engine.ThresholdsReached;

public class ThresholdsTransformer {

	private ThresholdsTransformer() {
		throw new IllegalAccessError();
	}

	public static ThresholdsReached fromDto(final DeviceStatusDto dto) {
		if (dto != null && dto.getThresholds() != null) {
			final Map<Threshold, String> reached = new LinkedHashMap<>();
			for (final ThresholdDto td : dto.getThresholds()) {
				reached.put(new Threshold(td.getName(), td.getKey(), Type.valueOf(td.getType()), td.getValue(), td.isExcluded()), td.getDetected());
			}
			return new ThresholdsReached(reached, dto.getTimestamp());
		}
		else {
			return null;
		}
	}

}

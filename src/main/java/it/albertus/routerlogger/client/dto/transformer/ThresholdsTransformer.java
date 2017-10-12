package it.albertus.routerlogger.client.dto.transformer;

import java.util.LinkedHashMap;
import java.util.Map;

import it.albertus.routerlogger.client.dto.ThresholdDto;
import it.albertus.routerlogger.client.dto.ThresholdsDto;
import it.albertus.routerlogger.client.engine.Threshold;
import it.albertus.routerlogger.client.engine.ThresholdsReached;
import it.albertus.routerlogger.client.engine.Threshold.Type;

public class ThresholdsTransformer {

	private ThresholdsTransformer() {
		throw new IllegalAccessError();
	}

	public static ThresholdsReached fromDto(final ThresholdsDto dto) {
		if (dto != null) {
			final Map<Threshold, String> reached = new LinkedHashMap<>();
			for (final ThresholdDto td : dto.getReached()) {
				reached.put(new Threshold(td.getName(), td.getKey(), Type.valueOf(td.getType()), td.getValue(), td.isExcluded()), td.getDetected());
			}
			return new ThresholdsReached(reached, dto.getTimestamp());
		}
		else {
			return null;
		}
	}

}

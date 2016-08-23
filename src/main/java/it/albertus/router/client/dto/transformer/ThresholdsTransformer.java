package it.albertus.router.client.dto.transformer;

import it.albertus.router.client.dto.ThresholdDto;
import it.albertus.router.client.dto.ThresholdsDto;
import it.albertus.router.client.engine.Threshold;
import it.albertus.router.client.engine.Threshold.Type;
import it.albertus.router.client.engine.ThresholdsReached;

import java.util.LinkedHashMap;
import java.util.Map;

public class ThresholdsTransformer {

	public static ThresholdsReached fromDto(final ThresholdsDto dto) {
		final Map<Threshold, String> reached = new LinkedHashMap<>();
		for (final ThresholdDto td : dto.getReached()) {
			reached.put(new Threshold(td.getName(), td.getKey(), Type.valueOf(td.getType()), td.getValue(), td.isExcluded()), td.getDetected());
		}
		return new ThresholdsReached(reached, dto.getTimestamp());
	}

}

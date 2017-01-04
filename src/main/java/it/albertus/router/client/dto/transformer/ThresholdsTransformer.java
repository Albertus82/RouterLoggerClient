package it.albertus.router.client.dto.transformer;

import java.util.LinkedHashMap;
import java.util.Map;

import it.albertus.router.client.dto.ThresholdDto;
import it.albertus.router.client.dto.ThresholdsDto;
import it.albertus.router.client.engine.Threshold;
import it.albertus.router.client.engine.Threshold.Type;
import it.albertus.router.client.engine.ThresholdsReached;

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

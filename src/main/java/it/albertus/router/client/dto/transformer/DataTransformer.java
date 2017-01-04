package it.albertus.router.client.dto.transformer;

import it.albertus.router.client.dto.RouterDataDto;
import it.albertus.router.client.engine.RouterData;

public class DataTransformer {

	private DataTransformer() {
		throw new IllegalAccessError();
	}

	public static RouterData fromDto(final RouterDataDto dto) {
		if (dto != null) {
			return new RouterData(dto.getTimestamp(), dto.getResponseTime(), dto.getData());
		}
		else {
			return null;
		}
	}

}

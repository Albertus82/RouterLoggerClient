package it.albertus.routerlogger.client.dto.transformer;

import it.albertus.routerlogger.client.dto.DeviceStatusDto;
import it.albertus.routerlogger.client.engine.RouterData;

public class DataTransformer {

	private DataTransformer() {
		throw new IllegalAccessError();
	}

	public static RouterData fromDto(final DeviceStatusDto dto) {
		if (dto != null) {
			return new RouterData(dto.getTimestamp(), dto.getResponseTime(), dto.getData());
		}
		else {
			return null;
		}
	}

}

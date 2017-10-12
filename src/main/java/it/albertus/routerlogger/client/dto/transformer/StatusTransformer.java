package it.albertus.routerlogger.client.dto.transformer;

import it.albertus.routerlogger.client.dto.StatusDto;
import it.albertus.routerlogger.client.engine.RouterLoggerStatus;
import it.albertus.routerlogger.client.engine.Status;

public class StatusTransformer {

	private StatusTransformer() {
		throw new IllegalAccessError();
	}

	public static RouterLoggerStatus fromDto(final StatusDto dto) {
		if (dto != null) {
			return new RouterLoggerStatus(Status.valueOf(dto.getStatus()), dto.getTimestamp());
		}
		else {
			return null;
		}
	}

}

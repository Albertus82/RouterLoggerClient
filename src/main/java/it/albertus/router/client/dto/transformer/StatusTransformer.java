package it.albertus.router.client.dto.transformer;

import it.albertus.router.client.dto.StatusDto;
import it.albertus.router.client.engine.RouterLoggerStatus;
import it.albertus.router.client.engine.Status;

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

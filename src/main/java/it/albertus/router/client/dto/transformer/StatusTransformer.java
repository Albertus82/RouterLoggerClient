package it.albertus.router.client.dto.transformer;

import it.albertus.router.client.dto.StatusDto;
import it.albertus.router.client.engine.RouterLoggerStatus;
import it.albertus.router.client.engine.Status;

public class StatusTransformer {

	public static RouterLoggerStatus fromDto(final StatusDto dto) {
		return new RouterLoggerStatus(Status.valueOf(dto.getStatus()), dto.getTimestamp());
	}

}

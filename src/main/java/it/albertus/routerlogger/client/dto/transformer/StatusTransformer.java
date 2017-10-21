package it.albertus.routerlogger.client.dto.transformer;

import it.albertus.routerlogger.client.dto.AppStatusDto;
import it.albertus.routerlogger.client.engine.AppStatus;
import it.albertus.routerlogger.client.engine.Status;

public class StatusTransformer {

	private StatusTransformer() {
		throw new IllegalAccessError();
	}

	public static AppStatus fromDto(final AppStatusDto dto) {
		if (dto != null) {
			return new AppStatus(Status.valueOf(dto.getStatus()), dto.getTimestamp());
		}
		else {
			return null;
		}
	}

}

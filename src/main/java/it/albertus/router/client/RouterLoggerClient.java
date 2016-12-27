package it.albertus.router.client;

import it.albertus.router.client.gui.RouterLoggerClientGui;

public class RouterLoggerClient {

	private RouterLoggerClient() {
		throw new IllegalAccessError();
	}

	public static void main(final String[] args) {
		RouterLoggerClientGui.run();
	}

}

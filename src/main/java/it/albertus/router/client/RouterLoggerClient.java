package it.albertus.router.client;

import it.albertus.router.client.gui.RouterLoggerGui;

import org.eclipse.swt.widgets.Display;

public class RouterLoggerClient {

	public static void main(final String[] args) {
		final Display display = Display.getDefault();
		new RouterLoggerGui(display);
		display.dispose();
	}

}

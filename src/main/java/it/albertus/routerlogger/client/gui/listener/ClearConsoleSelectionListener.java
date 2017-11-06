package it.albertus.routerlogger.client.gui.listener;

import org.eclipse.swt.events.SelectionEvent;

import it.albertus.routerlogger.client.gui.RouterLoggerClientGui;

public class ClearConsoleSelectionListener extends ClearSelectionListener {

	public ClearConsoleSelectionListener(final RouterLoggerClientGui gui) {
		super(gui);
	}

	@Override
	public void widgetSelected(final SelectionEvent se) {
		if (gui.canClearConsole()) {
			gui.getConsole().clear();
		}
	}

}

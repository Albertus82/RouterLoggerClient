package it.albertus.routerlogger.client.gui.listener;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import it.albertus.routerlogger.client.gui.RouterLoggerClientGui;

public class CopyMenuBarSelectionListener extends SelectionAdapter {

	private final RouterLoggerClientGui gui;

	public CopyMenuBarSelectionListener(final RouterLoggerClientGui gui) {
		this.gui = gui;
	}

	@Override
	public void widgetSelected(final SelectionEvent se) {
		if (gui.canCopyConsole()) {
			gui.getConsole().getScrollable().copy();
		}
		else if (gui.getDataTable().canCopy()) {
			gui.getDataTable().copy();
		}
	}

}

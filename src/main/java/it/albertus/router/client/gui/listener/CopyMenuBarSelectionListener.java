package it.albertus.router.client.gui.listener;

import it.albertus.router.client.gui.RouterLoggerGui;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class CopyMenuBarSelectionListener extends SelectionAdapter {

	private final RouterLoggerGui gui;

	public CopyMenuBarSelectionListener(final RouterLoggerGui gui) {
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

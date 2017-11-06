package it.albertus.routerlogger.client.gui.listener;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

import it.albertus.routerlogger.client.gui.RouterLoggerClientGui;

public abstract class ClearSelectionListener implements SelectionListener {

	protected final RouterLoggerClientGui gui;

	public ClearSelectionListener(final RouterLoggerClientGui gui) {
		this.gui = gui;
	}

	@Override
	public final void widgetDefaultSelected(final SelectionEvent e) {/* Ignore */}

}

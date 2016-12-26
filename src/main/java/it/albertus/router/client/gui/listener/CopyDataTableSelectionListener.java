package it.albertus.router.client.gui.listener;

import it.albertus.router.client.gui.DataTable;
import it.albertus.router.client.gui.RouterLoggerClientGui;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class CopyDataTableSelectionListener extends SelectionAdapter {

	private final RouterLoggerClientGui gui;

	public CopyDataTableSelectionListener(final RouterLoggerClientGui gui) {
		this.gui = gui;
	}

	@Override
	public void widgetSelected(final SelectionEvent se) {
		final DataTable dataTable = gui.getDataTable();
		if (dataTable.canCopy()) {
			dataTable.copy();
		}
	}

}

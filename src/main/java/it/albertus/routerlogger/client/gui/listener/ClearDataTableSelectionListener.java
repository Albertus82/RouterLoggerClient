package it.albertus.routerlogger.client.gui.listener;

import org.eclipse.swt.events.SelectionEvent;

import it.albertus.routerlogger.client.gui.DataTable;
import it.albertus.routerlogger.client.gui.RouterLoggerClientGui;

public class ClearDataTableSelectionListener extends ClearSelectionListener {

	public ClearDataTableSelectionListener(final RouterLoggerClientGui gui) {
		super(gui);
	}

	@Override
	public void widgetSelected(final SelectionEvent se) {
		final DataTable dataTable = gui.getDataTable();
		if (dataTable.canClear()) {
			dataTable.clear();
		}
	}

}

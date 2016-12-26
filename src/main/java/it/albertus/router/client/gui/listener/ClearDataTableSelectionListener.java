package it.albertus.router.client.gui.listener;

import it.albertus.router.client.gui.DataTable;
import it.albertus.router.client.gui.RouterLoggerClientGui;
import it.albertus.router.client.resources.Messages;

import org.eclipse.swt.events.SelectionEvent;

public class ClearDataTableSelectionListener extends ClearSelectionListener {

	public ClearDataTableSelectionListener(final RouterLoggerClientGui gui) {
		super(gui);
	}

	@Override
	public void widgetSelected(final SelectionEvent se) {
		final DataTable dataTable = gui.getDataTable();
		if (dataTable.canClear()) {
			if (confirm(Messages.get("msg.confirm.clear.table.text"), Messages.get("msg.confirm.clear.table.message")) && dataTable.canClear()) {
				dataTable.clear();
			}
		}
	}

}

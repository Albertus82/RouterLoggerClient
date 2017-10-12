package it.albertus.routerlogger.client.gui.listener;

import org.eclipse.swt.events.SelectionEvent;

import it.albertus.routerlogger.client.gui.DataTable;
import it.albertus.routerlogger.client.gui.RouterLoggerClientGui;
import it.albertus.routerlogger.client.resources.Messages;

public class ClearDataTableSelectionListener extends ClearSelectionListener {

	public ClearDataTableSelectionListener(final RouterLoggerClientGui gui) {
		super(gui);
	}

	@Override
	public void widgetSelected(final SelectionEvent se) {
		final DataTable dataTable = gui.getDataTable();
		if (dataTable.canClear() && confirm(Messages.get("msg.confirm.clear.table.text"), Messages.get("msg.confirm.clear.table.message"))) {
			dataTable.clear();
		}
	}

}

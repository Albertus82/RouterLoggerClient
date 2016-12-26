package it.albertus.router.client.gui.listener;

import it.albertus.router.client.gui.DataTable;
import it.albertus.router.client.gui.RouterLoggerClientGui;

import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;

public class DataTableContextMenuDetectListener implements MenuDetectListener {

	private final RouterLoggerClientGui gui;

	public DataTableContextMenuDetectListener(final RouterLoggerClientGui gui) {
		this.gui = gui;
	}

	@Override
	public void menuDetected(final MenuDetectEvent mde) {
		final DataTable dataTable = gui.getDataTable();
		dataTable.getCopyMenuItem().setEnabled(dataTable.canCopy());
		dataTable.getDeleteMenuItem().setEnabled(dataTable.canDelete());
		dataTable.getSelectAllMenuItem().setEnabled(dataTable.canSelectAll());
		dataTable.getClearMenuItem().setEnabled(dataTable.canClear());
		dataTable.getContextMenu().setVisible(true);
	}

}

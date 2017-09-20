package it.albertus.router.client.gui.listener;

import java.util.function.Supplier;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import it.albertus.router.client.gui.DataTable;

public class SelectAllDataTableSelectionListener extends SelectionAdapter {

	private final Supplier<DataTable> supplier;

	public SelectAllDataTableSelectionListener(final Supplier<DataTable> supplier) {
		this.supplier = supplier;
	}

	@Override
	public void widgetSelected(final SelectionEvent se) {
		final DataTable dataTable = supplier.get();
		if (dataTable.canSelectAll()) {
			dataTable.getTable().selectAll();
		}
	}

}

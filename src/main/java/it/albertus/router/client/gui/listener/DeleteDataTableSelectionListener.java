package it.albertus.router.client.gui.listener;

import java.util.function.Supplier;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import it.albertus.router.client.gui.DataTable;

public class DeleteDataTableSelectionListener extends SelectionAdapter {

	private final Supplier<DataTable> supplier;

	public DeleteDataTableSelectionListener(final Supplier<DataTable> supplier) {
		this.supplier = supplier;
	}

	@Override
	public void widgetSelected(final SelectionEvent se) {
		final DataTable dataTable = supplier.get();
		if (dataTable.getTable().isFocusControl()) {
			dataTable.delete();
		}
	}

}

package it.albertus.routerlogger.client.gui.listener;

import java.util.function.Supplier;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import it.albertus.routerlogger.client.gui.DataTable;

public class CopyDataTableSelectionListener extends SelectionAdapter {

	private final Supplier<DataTable> supplier;

	public CopyDataTableSelectionListener(final Supplier<DataTable> supplier) {
		this.supplier = supplier;
	}

	@Override
	public void widgetSelected(final SelectionEvent se) {
		supplier.get().copy();
	}

}

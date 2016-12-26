package it.albertus.router.client.gui.listener;

import it.albertus.router.client.gui.RouterLoggerClientGui;
import it.albertus.router.client.resources.Messages;

import org.eclipse.swt.events.SelectionEvent;

public class ClearConsoleSelectionListener extends ClearSelectionListener {

	public ClearConsoleSelectionListener(final RouterLoggerClientGui gui) {
		super(gui);
	}

	@Override
	public void widgetSelected(final SelectionEvent se) {
		if (gui.canClearConsole()) {
			if (confirm(Messages.get("msg.confirm.clear.console.text"), Messages.get("msg.confirm.clear.console.message"))) {
				gui.getConsole().clear();
			}
		}
	}

}

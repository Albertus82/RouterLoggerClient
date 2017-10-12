package it.albertus.routerlogger.client.gui.listener;

import org.eclipse.swt.events.SelectionEvent;

import it.albertus.routerlogger.client.gui.RouterLoggerClientGui;
import it.albertus.routerlogger.client.resources.Messages;

public class ClearConsoleSelectionListener extends ClearSelectionListener {

	public ClearConsoleSelectionListener(final RouterLoggerClientGui gui) {
		super(gui);
	}

	@Override
	public void widgetSelected(final SelectionEvent se) {
		if (gui.canClearConsole() && confirm(Messages.get("msg.confirm.clear.console.text"), Messages.get("msg.confirm.clear.console.message"))) {
			gui.getConsole().clear();
		}
	}

}

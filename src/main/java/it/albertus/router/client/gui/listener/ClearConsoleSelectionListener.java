package it.albertus.router.client.gui.listener;

import it.albertus.router.client.gui.RouterLoggerGui;
import it.albertus.router.client.resources.Resources;

import org.eclipse.swt.events.SelectionEvent;

public class ClearConsoleSelectionListener extends ClearSelectionListener {

	public ClearConsoleSelectionListener(final RouterLoggerGui gui) {
		super(gui);
	}

	@Override
	public void widgetSelected(final SelectionEvent se) {
		if (gui.canClearConsole()) {
			if (confirm(Resources.get("msg.confirm.clear.console.text"), Resources.get("msg.confirm.clear.console.message"))) {
				gui.getTextConsole().clear();
			}
		}
	}

}

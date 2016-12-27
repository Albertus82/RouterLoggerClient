package it.albertus.router.client.gui.listener;

import org.eclipse.swt.events.ArmEvent;
import org.eclipse.swt.events.ArmListener;

import it.albertus.router.client.gui.RouterLoggerClientGui;

/**
 * Attenzione: disabilitando gli elementi dei menu, vengono automaticamente
 * disabilitati anche i relativi acceleratori.
 */
public class EditMenuBarArmListener implements ArmListener {

	private final RouterLoggerClientGui gui;

	public EditMenuBarArmListener(final RouterLoggerClientGui gui) {
		this.gui = gui;
	}

	@Override
	public void widgetArmed(final ArmEvent ae) {
		gui.getMenuBar().getEditClearSubMenuItem().setEnabled(gui.getDataTable().canClear() || gui.canClearConsole());
	}

}

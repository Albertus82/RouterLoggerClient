package it.albertus.router.client.gui.listener;

import it.albertus.router.client.gui.RouterLoggerClientGui;

import org.eclipse.swt.events.ArmEvent;
import org.eclipse.swt.events.ArmListener;

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

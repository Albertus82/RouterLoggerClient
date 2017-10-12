package it.albertus.routerlogger.client.gui.listener;

import org.eclipse.swt.events.ArmEvent;
import org.eclipse.swt.events.ArmListener;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;

import it.albertus.routerlogger.client.gui.RouterLoggerClientGui;

/**
 * Attenzione: disabilitando gli elementi dei menu, vengono automaticamente
 * disabilitati anche i relativi acceleratori.
 */
public class EditMenuListener implements ArmListener, MenuListener {

	private final RouterLoggerClientGui gui;

	public EditMenuListener(final RouterLoggerClientGui gui) {
		this.gui = gui;
	}

	@Override
	public void widgetArmed(final ArmEvent ae) {
		execute();
	}

	@Override
	public void menuShown(final MenuEvent e) {
		execute();
	}

	@Override
	public void menuHidden(final MenuEvent e) {/* Ignore */}

	private void execute() {
		gui.getMenuBar().getEditClearSubMenuItem().setEnabled(gui.getDataTable().canClear() || gui.canClearConsole());
	}

}

package it.albertus.routerlogger.client.gui.listener;

import org.eclipse.swt.events.ArmEvent;
import org.eclipse.swt.events.ArmListener;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;

@FunctionalInterface
public interface ArmMenuListener extends ArmListener, MenuListener {

	/** Sent when a menu is shown, is armed, or 'about to be selected'. */
	void menuArmed();

	@Override
	default void menuShown(final MenuEvent e) {
		menuArmed();
	}

	@Override
	default void widgetArmed(final ArmEvent e) {
		menuArmed();
	}

	@Override
	default void menuHidden(final MenuEvent e) {/* Ignore */}

}

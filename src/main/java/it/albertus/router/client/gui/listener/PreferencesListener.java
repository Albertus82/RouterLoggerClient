package it.albertus.router.client.gui.listener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;

import it.albertus.jface.preference.Preferences;
import it.albertus.router.client.engine.RouterLoggerClientConfiguration;
import it.albertus.router.client.gui.Images;
import it.albertus.router.client.gui.RouterLoggerClientGui;
import it.albertus.router.client.gui.preference.Preference;
import it.albertus.router.client.gui.preference.page.PageDefinition;
import it.albertus.router.client.resources.Messages;
import it.albertus.router.client.resources.Messages.Language;
import it.albertus.router.client.util.Logger;
import it.albertus.router.client.util.LoggerFactory;

public class PreferencesListener extends SelectionAdapter implements Listener {

	private static final Logger logger = LoggerFactory.getLogger(PreferencesListener.class);

	private final RouterLoggerClientGui gui;

	public PreferencesListener(final RouterLoggerClientGui gui) {
		this.gui = gui;
	}

	@Override
	public void widgetSelected(final SelectionEvent se) {
		final Language language = Messages.getLanguage();
		final Preferences preferences = new Preferences(PageDefinition.values(), Preference.values(), RouterLoggerClientConfiguration.getInstance(), Images.getMainIcons());
		try {
			preferences.openDialog(gui.getShell());
		}
		catch (final Exception e) {
			logger.error(e);
		}

		// Check if must update texts...
		if (!language.equals(Messages.getLanguage())) {
			gui.getMenuBar().updateTexts();
			gui.getDataTable().updateTexts();
		}
		if (preferences.isRestartRequired()) {
			final MessageBox messageBox = new MessageBox(gui.getShell(), SWT.ICON_WARNING | SWT.YES | SWT.NO);
			messageBox.setText(Messages.get("lbl.window.title"));
			messageBox.setMessage(Messages.get("lbl.preferences.restart"));
			if (messageBox.open() == SWT.YES) {
				gui.restart();
			}
		}
	}

	@Override
	public void handleEvent(final Event event) {
		widgetSelected(null);
	}

}

package it.albertus.router.client.gui.preference;

import org.eclipse.swt.widgets.Shell;

import it.albertus.jface.preference.Preferences;
import it.albertus.jface.preference.page.IPageDefinition;
import it.albertus.router.client.engine.RouterLoggerClientConfiguration;
import it.albertus.router.client.gui.Images;
import it.albertus.router.client.gui.RouterLoggerClientGui;
import it.albertus.router.client.gui.preference.page.PageDefinition;
import it.albertus.router.client.resources.Messages;
import it.albertus.router.client.resources.Messages.Language;

public class RouterLoggerClientPreferences extends Preferences {

	private final RouterLoggerClientGui gui;

	public RouterLoggerClientPreferences(final RouterLoggerClientGui gui) {
		super(PageDefinition.values(), Preference.values(), RouterLoggerClientConfiguration.getInstance(), Images.getMainIcons());
		this.gui = gui;
	}

	public RouterLoggerClientPreferences() {
		this(null);
	}

	@Override
	public int openDialog(final Shell parentShell, final IPageDefinition selectedPage) {
		final Language language = Messages.getLanguage();

		final int returnCode = super.openDialog(parentShell, selectedPage);

		// Check if must update texts...
		if (gui != null && !language.equals(Messages.getLanguage())) {
			gui.getMenuBar().updateTexts();
			gui.getDataTable().updateTexts();
		}

		return returnCode;
	}

}

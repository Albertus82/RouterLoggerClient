package it.albertus.router.client.gui.preference;

import it.albertus.jface.preference.Preferences;
import it.albertus.jface.preference.page.IPageDefinition;
import it.albertus.router.client.engine.RouterLoggerClientConfiguration;
import it.albertus.router.client.gui.Images;
import it.albertus.router.client.gui.RouterLoggerGui;
import it.albertus.router.client.gui.preference.page.PageDefinition;
import it.albertus.router.client.resources.Resources;
import it.albertus.router.client.resources.Resources.Language;

import org.eclipse.swt.widgets.Shell;

public class RouterLoggerClientPreferences extends Preferences {

	private final RouterLoggerGui gui;

	public RouterLoggerClientPreferences(final RouterLoggerGui gui) {
		super(PageDefinition.values(), Preference.values(), RouterLoggerClientConfiguration.getInstance(), Images.MAIN_ICONS);
		this.gui = gui;
	}

	public RouterLoggerClientPreferences() {
		this(null);
	}

	@Override
	public int openDialog(final Shell parentShell, final IPageDefinition selectedPage) {
		final Language language = Resources.getLanguage();

		final int returnCode = super.openDialog(parentShell, selectedPage);

		// Check if must update texts...
		if (gui != null && !language.equals(Resources.getLanguage())) {
			gui.getMenuBar().updateTexts();
			gui.getDataTable().updateTexts();
		}

		return returnCode;
	}

}

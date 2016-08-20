package it.albertus.router.client.gui.preference;

import it.albertus.jface.preference.Preferences;
import it.albertus.jface.preference.page.Page;
import it.albertus.router.client.engine.RouterLoggerClientConfiguration;
import it.albertus.router.client.gui.Images;
import it.albertus.router.client.gui.RouterLoggerGui;
import it.albertus.router.client.gui.preference.page.RouterLoggerClientPage;
import it.albertus.router.client.resources.Resources;
import it.albertus.router.client.resources.Resources.Language;

import org.eclipse.swt.widgets.Shell;

public class RouterLoggerClientPreferences extends Preferences {

	private final RouterLoggerGui gui;

	public RouterLoggerClientPreferences(final RouterLoggerGui gui) {
		super(RouterLoggerClientConfiguration.getInstance(), RouterLoggerClientPage.values(), RouterLoggerClientPreference.values(), Images.MAIN_ICONS);
		this.gui = gui;
	}

	public RouterLoggerClientPreferences() {
		this(null);
	}

	@Override
	public int open(final Shell parentShell, final Page selectedPage) {
		final Language language = Resources.getLanguage();

		final int returnCode = super.open(parentShell, selectedPage);

		// Check if must update texts...
		if (gui != null && !language.equals(Resources.getLanguage())) {
			gui.getMenuBar().updateTexts();
		}

		return returnCode;
	}

}

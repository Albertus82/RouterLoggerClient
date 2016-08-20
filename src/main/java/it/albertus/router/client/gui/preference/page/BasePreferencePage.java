package it.albertus.router.client.gui.preference.page;

import it.albertus.jface.preference.page.AbstractPreferencePage;
import it.albertus.jface.preference.page.Page;
import it.albertus.router.client.engine.RouterLoggerClientConfiguration;
import it.albertus.router.client.gui.preference.RouterLoggerClientPreference;

public abstract class BasePreferencePage extends AbstractPreferencePage {

	public BasePreferencePage() {
		super(RouterLoggerClientConfiguration.getInstance(), RouterLoggerClientPreference.values());
	}

	protected BasePreferencePage(final int style) {
		super(RouterLoggerClientConfiguration.getInstance(), RouterLoggerClientPreference.values(), style);
	}

	@Override
	public Page getPage() {
		return RouterLoggerClientPage.forClass(getClass());
	}

}

package it.albertus.routerlogger.client.gui.preference.page;

import java.net.Proxy.Type;

import it.albertus.jface.preference.StaticLabelsAndValues;
import it.albertus.jface.preference.page.BasePreferencePage;

public class HttpProxyPreferencePage extends BasePreferencePage {

	public static StaticLabelsAndValues getProxyTypeComboOptions() {
		final Type[] types = Type.values();
		final StaticLabelsAndValues options = new StaticLabelsAndValues(types.length - 1);
		for (final Type type : types) {
			if (!Type.DIRECT.equals(type)) {
				options.put(type.toString(), type.name());
			}
		}
		return options;
	}

}

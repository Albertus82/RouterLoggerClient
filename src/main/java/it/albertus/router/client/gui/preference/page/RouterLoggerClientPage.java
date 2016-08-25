package it.albertus.router.client.gui.preference.page;

import it.albertus.jface.preference.page.AbstractPreferencePage;
import it.albertus.jface.preference.page.Page;
import it.albertus.router.client.resources.Resources;

public enum RouterLoggerClientPage implements Page {
	GENERAL(GeneralPreferencePage.class),
	MQTT(MqttPreferencePage.class),
	MQTT_MESSAGES(MqttMessagesPreferencePage.class, MQTT),
	MQTT_ADVANCED(AdvancedMqttPreferencePage.class, MQTT),
	HTTP(HttpPreferencePage.class),
	APPEARANCE(AppearancePreferencePage.class);

	private static final String LABEL_KEY_PREFIX = "lbl.preferences.";

	private final String nodeId;
	private final String labelKey;
	private final Class<? extends AbstractPreferencePage> pageClass;
	private final Page parent;

	private RouterLoggerClientPage(final Class<? extends AbstractPreferencePage> pageClass) {
		this(null, null, pageClass, null);
	}

	private RouterLoggerClientPage(final Class<? extends AbstractPreferencePage> pageClass, final Page parent) {
		this(null, null, pageClass, parent);
	}

	private RouterLoggerClientPage(final String labelKey, final Class<? extends AbstractPreferencePage> pageClass) {
		this(null, labelKey, pageClass, null);
	}

	private RouterLoggerClientPage(final String labelKey, final Class<? extends AbstractPreferencePage> pageClass, final Page parent) {
		this(null, labelKey, pageClass, parent);
	}

	private RouterLoggerClientPage(final String nodeId, final String labelKey, final Class<? extends AbstractPreferencePage> pageClass) {
		this(nodeId, labelKey, pageClass, null);
	}

	private RouterLoggerClientPage(final String nodeId, final String labelKey, final Class<? extends AbstractPreferencePage> pageClass, final Page parent) {
		if (nodeId != null && !nodeId.isEmpty()) {
			this.nodeId = nodeId;
		}
		else {
			this.nodeId = name().toLowerCase().replace('_', '.');
		}
		if (labelKey != null && !labelKey.isEmpty()) {
			this.labelKey = labelKey;
		}
		else {
			this.labelKey = LABEL_KEY_PREFIX + this.nodeId;
		}
		this.pageClass = pageClass;
		this.parent = parent;
	}

	@Override
	public String getNodeId() {
		return nodeId;
	}

	@Override
	public String getLabel() {
		return Resources.get(labelKey);
	}

	@Override
	public Class<? extends AbstractPreferencePage> getPageClass() {
		return pageClass;
	}

	@Override
	public Page getParent() {
		return parent;
	}

	public static Page forClass(final Class<? extends AbstractPreferencePage> clazz) {
		if (clazz != null) {
			for (final RouterLoggerClientPage page : RouterLoggerClientPage.values()) {
				if (clazz.equals(page.pageClass)) {
					return page;
				}
			}
		}
		return null;
	}

}

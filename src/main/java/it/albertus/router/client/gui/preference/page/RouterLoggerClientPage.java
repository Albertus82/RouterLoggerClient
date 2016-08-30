package it.albertus.router.client.gui.preference.page;

import it.albertus.jface.preference.page.BasePreferencePage;
import it.albertus.jface.preference.page.PageDefinition;
import it.albertus.router.client.resources.Resources;

import org.eclipse.jface.resource.ImageDescriptor;

public enum RouterLoggerClientPage implements PageDefinition {
	GENERAL(GeneralPreferencePage.class),
	MQTT(MqttPreferencePage.class),
	MQTT_MESSAGES(RestartHeaderPreferencePage.class, MQTT),
	MQTT_ADVANCED(AdvancedMqttPreferencePage.class, MQTT),
	HTTP(RestartHeaderPreferencePage.class),
	APPEARANCE(RestartHeaderPreferencePage.class);

	private static final String LABEL_KEY_PREFIX = "lbl.preferences.";

	private final String nodeId;
	private final String labelKey;
	private final Class<? extends BasePreferencePage> pageClass;
	private final PageDefinition parent;

	private RouterLoggerClientPage(final Class<? extends BasePreferencePage> pageClass) {
		this(null, null, pageClass, null);
	}

	private RouterLoggerClientPage(final Class<? extends BasePreferencePage> pageClass, final PageDefinition parent) {
		this(null, null, pageClass, parent);
	}

	private RouterLoggerClientPage(final String labelKey, final Class<? extends BasePreferencePage> pageClass) {
		this(null, labelKey, pageClass, null);
	}

	private RouterLoggerClientPage(final String labelKey, final Class<? extends BasePreferencePage> pageClass, final PageDefinition parent) {
		this(null, labelKey, pageClass, parent);
	}

	private RouterLoggerClientPage(final String nodeId, final String labelKey, final Class<? extends BasePreferencePage> pageClass) {
		this(nodeId, labelKey, pageClass, null);
	}

	private RouterLoggerClientPage(final String nodeId, final String labelKey, final Class<? extends BasePreferencePage> pageClass, final PageDefinition parent) {
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
	public Class<? extends BasePreferencePage> getPageClass() {
		return pageClass;
	}

	@Override
	public PageDefinition getParent() {
		return parent;
	}

	@Override
	public ImageDescriptor getImage() {
		return null;
	}

}

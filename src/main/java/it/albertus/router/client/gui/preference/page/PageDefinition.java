package it.albertus.router.client.gui.preference.page;

import it.albertus.jface.preference.page.BasePreferencePage;
import it.albertus.jface.preference.page.IPreferencePageDefinition;
import it.albertus.jface.preference.page.PreferencePageDefinition;
import it.albertus.jface.preference.page.PreferencePageDefinition.PreferencePageDefinitionBuilder;
import it.albertus.router.client.resources.Resources;
import it.albertus.util.Localized;

import org.eclipse.jface.resource.ImageDescriptor;

public enum PageDefinition implements IPreferencePageDefinition {
	GENERAL(new PreferencePageDefinitionBuilder().pageClass(GeneralPreferencePage.class).build()),
	MQTT(new PreferencePageDefinitionBuilder().pageClass(MqttPreferencePage.class).build()),
	MQTT_MESSAGES(new PreferencePageDefinitionBuilder().pageClass(RestartHeaderPreferencePage.class).parent(MQTT).build()),
	MQTT_ADVANCED(new PreferencePageDefinitionBuilder().pageClass(AdvancedMqttPreferencePage.class).parent(MQTT).build()),
	HTTP(new PreferencePageDefinitionBuilder().pageClass(RestartHeaderPreferencePage.class).build()),
	APPEARANCE(new PreferencePageDefinitionBuilder().pageClass(RestartHeaderPreferencePage.class).build());

	private static final String LABEL_KEY_PREFIX = "lbl.preferences.";

	private final IPreferencePageDefinition pageDefinition;

	PageDefinition() {
		this(new PreferencePageDefinition());
	}

	PageDefinition(final PreferencePageDefinition pageDefinition) {
		this.pageDefinition = pageDefinition;
		if (pageDefinition.getNodeId() == null) {
			pageDefinition.setNodeId(name().toLowerCase().replace('_', '.'));
		}
		if (pageDefinition.getLabel() == null) {
			pageDefinition.setLabel(new Localized() {
				@Override
				public String getString() {
					return Resources.get(LABEL_KEY_PREFIX + pageDefinition.getNodeId());
				}
			});
		}
	}

	@Override
	public String getNodeId() {
		return pageDefinition.getNodeId();
	}

	@Override
	public Localized getLabel() {
		return pageDefinition.getLabel();
	}

	@Override
	public Class<? extends BasePreferencePage> getPageClass() {
		return pageDefinition.getPageClass();
	}

	@Override
	public IPreferencePageDefinition getParent() {
		return pageDefinition.getParent();
	}

	@Override
	public ImageDescriptor getImage() {
		return pageDefinition.getImage();
	}

}

package it.albertus.router.client.gui.preference.page;

import it.albertus.jface.preference.page.BasePreferencePage;
import it.albertus.jface.preference.page.IPageDefinition;
import it.albertus.jface.preference.page.PageDefinition;
import it.albertus.jface.preference.page.PageDefinition.PageDefinitionBuilder;
import it.albertus.router.client.resources.Resources;
import it.albertus.util.Localized;

import org.eclipse.jface.resource.ImageDescriptor;

public enum RouterLoggerClientPage implements IPageDefinition {
	GENERAL(new PageDefinitionBuilder().pageClass(GeneralPreferencePage.class).build()),
	MQTT(new PageDefinitionBuilder().pageClass(MqttPreferencePage.class).build()),
	MQTT_MESSAGES(new PageDefinitionBuilder().pageClass(RestartHeaderPreferencePage.class).parent(MQTT).build()),
	MQTT_ADVANCED(new PageDefinitionBuilder().pageClass(AdvancedMqttPreferencePage.class).parent(MQTT).build()),
	HTTP(new PageDefinitionBuilder().pageClass(RestartHeaderPreferencePage.class).build()),
	APPEARANCE(new PageDefinitionBuilder().pageClass(RestartHeaderPreferencePage.class).build());

	private static final String LABEL_KEY_PREFIX = "lbl.preferences.";

	private final PageDefinition pageDefinition;

	RouterLoggerClientPage() {
		this(new PageDefinition());
	}

	RouterLoggerClientPage(final PageDefinition pageDefinition) {
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
	public IPageDefinition getParent() {
		return pageDefinition.getParent();
	}

	@Override
	public ImageDescriptor getImage() {
		return pageDefinition.getImage();
	}

}

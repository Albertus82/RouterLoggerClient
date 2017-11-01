package it.albertus.routerlogger.client.gui.preference.page;

import org.eclipse.jface.resource.ImageDescriptor;

import it.albertus.jface.preference.page.BasePreferencePage;
import it.albertus.jface.preference.page.IPageDefinition;
import it.albertus.jface.preference.page.LoggingPreferencePage;
import it.albertus.jface.preference.page.PageDefinitionDetails;
import it.albertus.jface.preference.page.PageDefinitionDetails.PageDefinitionDetailsBuilder;
import it.albertus.jface.preference.page.RestartHeaderPreferencePage;
import it.albertus.routerlogger.client.resources.Messages;

public enum PageDefinition implements IPageDefinition {

	GENERAL(new PageDefinitionDetailsBuilder().pageClass(GeneralPreferencePage.class).build()),
	MQTT(new PageDefinitionDetailsBuilder().pageClass(MqttPreferencePage.class).build()),
	MQTT_MESSAGES(new PageDefinitionDetailsBuilder().pageClass(RestartHeaderPreferencePage.class).parent(MQTT).build()),
	MQTT_ADVANCED(new PageDefinitionDetailsBuilder().pageClass(AdvancedMqttPreferencePage.class).parent(MQTT).build()),
	HTTP(new PageDefinitionDetailsBuilder().pageClass(RestartHeaderPreferencePage.class).build()),
	HTTP_PROXY(new PageDefinitionDetailsBuilder().pageClass(HttpProxyPreferencePage.class).parent(HTTP).build()),
	APPEARANCE,
	APPEARANCE_TABLE(new PageDefinitionDetailsBuilder().pageClass(RestartHeaderPreferencePage.class).parent(APPEARANCE).build()),
	LOGGING(new PageDefinitionDetailsBuilder().pageClass(LoggingPreferencePage.class).build());

	private static final String LABEL_KEY_PREFIX = "lbl.preferences.";

	private final PageDefinitionDetails pageDefinitionDetails;

	PageDefinition() {
		this(new PageDefinitionDetailsBuilder().build());
	}

	PageDefinition(final PageDefinitionDetails pageDefinitionDetails) {
		this.pageDefinitionDetails = pageDefinitionDetails;
		if (pageDefinitionDetails.getNodeId() == null) {
			pageDefinitionDetails.setNodeId(name().toLowerCase().replace('_', '.'));
		}
		if (pageDefinitionDetails.getLabel() == null) {
			pageDefinitionDetails.setLabel(() -> Messages.get(LABEL_KEY_PREFIX + pageDefinitionDetails.getNodeId()));
		}
	}

	@Override
	public String getNodeId() {
		return pageDefinitionDetails.getNodeId();
	}

	@Override
	public String getLabel() {
		return pageDefinitionDetails.getLabel().get();
	}

	@Override
	public Class<? extends BasePreferencePage> getPageClass() {
		return pageDefinitionDetails.getPageClass();
	}

	@Override
	public IPageDefinition getParent() {
		return pageDefinitionDetails.getParent();
	}

	@Override
	public ImageDescriptor getImage() {
		return pageDefinitionDetails.getImage();
	}

}

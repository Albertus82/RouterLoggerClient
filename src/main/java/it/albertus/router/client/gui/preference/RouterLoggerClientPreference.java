package it.albertus.router.client.gui.preference;

import it.albertus.jface.TextConsole;
import it.albertus.jface.preference.FieldEditorDetails;
import it.albertus.jface.preference.FieldEditorDetails.FieldEditorDetailsBuilder;
import it.albertus.jface.preference.FieldEditorFactory;
import it.albertus.jface.preference.LocalizedLabelsAndValues;
import it.albertus.jface.preference.IPreference;
import it.albertus.jface.preference.PreferenceDetails;
import it.albertus.jface.preference.PreferenceDetails.PreferenceDetailsBuilder;
import it.albertus.jface.preference.field.DefaultBooleanFieldEditor;
import it.albertus.jface.preference.field.DefaultComboFieldEditor;
import it.albertus.jface.preference.field.DefaultDirectoryFieldEditor;
import it.albertus.jface.preference.field.DefaultIntegerFieldEditor;
import it.albertus.jface.preference.field.DefaultRadioGroupFieldEditor;
import it.albertus.jface.preference.field.DefaultStringFieldEditor;
import it.albertus.jface.preference.field.IntegerComboFieldEditor;
import it.albertus.jface.preference.field.PasswordFieldEditor;
import it.albertus.jface.preference.field.ScaleIntegerFieldEditor;
import it.albertus.jface.preference.field.UriListEditor;
import it.albertus.jface.preference.field.WrapStringFieldEditor;
import it.albertus.jface.preference.page.IPreferencePageDefinition;
import it.albertus.router.client.engine.RouterLoggerClientConfiguration;
import it.albertus.router.client.gui.CloseMessageBox;
import it.albertus.router.client.gui.DataTable;
import it.albertus.router.client.gui.RouterLoggerGui;
import it.albertus.router.client.gui.TrayIcon;
import it.albertus.router.client.gui.preference.page.AdvancedMqttPreferencePage;
import it.albertus.router.client.gui.preference.page.GeneralPreferencePage;
import it.albertus.router.client.gui.preference.page.MqttPreferencePage;
import it.albertus.router.client.gui.preference.page.PageDefinition;
import it.albertus.router.client.http.HttpPollingThread;
import it.albertus.router.client.mqtt.RouterLoggerClientMqttClient;
import it.albertus.router.client.resources.Resources;
import it.albertus.router.client.util.Logger;
import it.albertus.util.Localized;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.widgets.Composite;

public enum RouterLoggerClientPreference implements IPreference {

	LANGUAGE(PageDefinition.GENERAL, ComboFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(Locale.getDefault().getLanguage()).build(), new FieldEditorDetailsBuilder().labelsAndValues(GeneralPreferencePage.getLanguageComboOptions()).build()),
	CLIENT_PROTOCOL(PageDefinition.GENERAL, DefaultRadioGroupFieldEditor.class, new PreferenceDetailsBuilder().separate().defaultValue(RouterLoggerGui.Defaults.CLIENT_PROTOCOL).restartRequired().build(), new FieldEditorDetailsBuilder().labelsAndValues(GeneralPreferencePage.getProtocolComboOptions()).radioNumColumns(1).radioUseGroup(true).build()),
	DEBUG(PageDefinition.GENERAL, DefaultBooleanFieldEditor.class, new PreferenceDetailsBuilder().separate().defaultValue(Logger.Defaults.DEBUG).build()),

	MQTT_SERVER_URI(PageDefinition.MQTT, UriListEditor.class, new PreferenceDetailsBuilder().restartRequired().build(), new FieldEditorDetailsBuilder().build()),
	MQTT_USERNAME(PageDefinition.MQTT, DefaultStringFieldEditor.class, new PreferenceDetailsBuilder().restartRequired().build()),
	MQTT_PASSWORD(PageDefinition.MQTT, PasswordFieldEditor.class, new PreferenceDetailsBuilder().restartRequired().build()),
	MQTT_CLIENT_ID(PageDefinition.MQTT, DefaultStringFieldEditor.class, new PreferenceDetailsBuilder().restartRequired().defaultValue(RouterLoggerClientMqttClient.Defaults.CLIENT_ID).build(), new FieldEditorDetailsBuilder().emptyStringAllowed(false).build()),
	MQTT_CONNECT_RETRY(PageDefinition.MQTT, DefaultBooleanFieldEditor.class, new PreferenceDetailsBuilder().restartRequired().separate().defaultValue(RouterLoggerClientMqttClient.Defaults.CONNECT_RETRY).build()),
	MQTT_CONNECT_RETRY_INTERVAL_SECS(PageDefinition.MQTT, ScaleIntegerFieldEditor.class, new PreferenceDetailsBuilder().parent(MQTT_CONNECT_RETRY).defaultValue(RouterLoggerGui.Defaults.MQTT_CONNECT_RETRY_INTERVAL_SECS).build(), new FieldEditorDetailsBuilder().scaleMinimum(1).scaleMaximum(Byte.MAX_VALUE).scalePageIncrement(7).build()),

	MQTT_DATA_TOPIC(PageDefinition.MQTT_MESSAGES, DefaultStringFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(RouterLoggerClientMqttClient.Defaults.DATA_TOPIC).build(), new FieldEditorDetailsBuilder().emptyStringAllowed(false).build()),
	MQTT_DATA_QOS(PageDefinition.MQTT_MESSAGES, DefaultComboFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(RouterLoggerClientMqttClient.Defaults.DATA_QOS).build(), new FieldEditorDetailsBuilder().labelsAndValues(MqttPreferencePage.getMqttQosComboOptions()).build()),
	MQTT_STATUS_TOPIC(PageDefinition.MQTT_MESSAGES, DefaultStringFieldEditor.class, new PreferenceDetailsBuilder().separate().restartRequired().defaultValue(RouterLoggerClientMqttClient.Defaults.STATUS_TOPIC).build(), new FieldEditorDetailsBuilder().emptyStringAllowed(false).build()),
	MQTT_STATUS_QOS(PageDefinition.MQTT_MESSAGES, DefaultComboFieldEditor.class, new PreferenceDetailsBuilder().restartRequired().defaultValue(RouterLoggerClientMqttClient.Defaults.STATUS_QOS).build(), new FieldEditorDetailsBuilder().labelsAndValues(MqttPreferencePage.getMqttQosComboOptions()).build()),
	MQTT_THRESHOLDS_TOPIC(PageDefinition.MQTT_MESSAGES, DefaultStringFieldEditor.class, new PreferenceDetailsBuilder().separate().defaultValue(RouterLoggerClientMqttClient.Defaults.THRESHOLDS_TOPIC).build(), new FieldEditorDetailsBuilder().emptyStringAllowed(false).build()),
	MQTT_THRESHOLDS_QOS(PageDefinition.MQTT_MESSAGES, DefaultComboFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(RouterLoggerClientMqttClient.Defaults.THRESHOLDS_QOS).build(), new FieldEditorDetailsBuilder().labelsAndValues(MqttPreferencePage.getMqttQosComboOptions()).build()),

	MQTT_CLEAN_SESSION(PageDefinition.MQTT_ADVANCED, DefaultBooleanFieldEditor.class, new PreferenceDetailsBuilder().restartRequired().defaultValue(RouterLoggerClientMqttClient.Defaults.CLEAN_SESSION).build()),
	MQTT_AUTOMATIC_RECONNECT(PageDefinition.MQTT_ADVANCED, DefaultBooleanFieldEditor.class, new PreferenceDetailsBuilder().restartRequired().defaultValue(RouterLoggerClientMqttClient.Defaults.AUTOMATIC_RECONNECT).build()),
	MQTT_CONNECTION_TIMEOUT(PageDefinition.MQTT_ADVANCED, DefaultIntegerFieldEditor.class, new PreferenceDetailsBuilder().restartRequired().defaultValue(RouterLoggerClientMqttClient.Defaults.CONNECTION_TIMEOUT).build()),
	MQTT_KEEP_ALIVE_INTERVAL(PageDefinition.MQTT_ADVANCED, DefaultIntegerFieldEditor.class, new PreferenceDetailsBuilder().restartRequired().defaultValue(RouterLoggerClientMqttClient.Defaults.KEEP_ALIVE_INTERVAL).build()),
	MQTT_MAX_INFLIGHT(PageDefinition.MQTT_ADVANCED, DefaultIntegerFieldEditor.class, new PreferenceDetailsBuilder().restartRequired().defaultValue(RouterLoggerClientMqttClient.Defaults.MAX_INFLIGHT).build()),
	MQTT_VERSION(PageDefinition.MQTT_ADVANCED, DefaultComboFieldEditor.class, new PreferenceDetailsBuilder().restartRequired().defaultValue(RouterLoggerClientMqttClient.Defaults.MQTT_VERSION).build(), new FieldEditorDetailsBuilder().labelsAndValues(AdvancedMqttPreferencePage.getMqttVersionComboOptions()).build()),
	MQTT_PERSISTENCE_FILE_ENABLED(PageDefinition.MQTT_ADVANCED, DefaultBooleanFieldEditor.class, new PreferenceDetailsBuilder().restartRequired().defaultValue(RouterLoggerClientMqttClient.Defaults.PERSISTENCE_FILE_ENABLED).build()),
	MQTT_PERSISTENCE_FILE_CUSTOM(PageDefinition.MQTT_ADVANCED, DefaultBooleanFieldEditor.class, new PreferenceDetailsBuilder().restartRequired().defaultValue(RouterLoggerClientMqttClient.Defaults.PERSISTENCE_FILE_CUSTOM).parent(MQTT_PERSISTENCE_FILE_ENABLED).build()),
	MQTT_PERSISTENCE_FILE_PATH(PageDefinition.MQTT_ADVANCED, DefaultDirectoryFieldEditor.class, new PreferenceDetailsBuilder().restartRequired().defaultValue(System.getProperty("user.dir")).parent(MQTT_PERSISTENCE_FILE_CUSTOM).build(), new FieldEditorDetailsBuilder().emptyStringAllowed(false).directoryDialogMessage(new Localized() {
		@Override
		public String getString() {
			return Resources.get("msg.preferences.directory.dialog.message.mqtt");
		}
	}).build()),

	HTTP_HOST(PageDefinition.HTTP, DefaultStringFieldEditor.class),
	HTTP_PORT(PageDefinition.HTTP, DefaultIntegerFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(HttpPollingThread.Defaults.PORT).build(), new FieldEditorDetailsBuilder().integerValidRange(1, 65535).emptyStringAllowed(false).build()),
	HTTP_AUTHENTICATION(PageDefinition.HTTP, DefaultBooleanFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(HttpPollingThread.Defaults.AUTHENTICATION).build()),
	HTTP_USERNAME(PageDefinition.HTTP, DefaultStringFieldEditor.class, new PreferenceDetailsBuilder().parent(HTTP_AUTHENTICATION).build()),
	HTTP_PASSWORD(PageDefinition.HTTP, PasswordFieldEditor.class, new PreferenceDetailsBuilder().parent(HTTP_AUTHENTICATION).build()),
	HTTP_IGNORE_CERTIFICATE(PageDefinition.HTTP, DefaultBooleanFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(HttpPollingThread.Defaults.IGNORE_CERTIFICATE).restartRequired().build()),
	HTTP_CONNECTION_TIMEOUT(PageDefinition.HTTP, IntegerComboFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(HttpPollingThread.Defaults.CONNECTION_TIMEOUT).build(), new FieldEditorDetailsBuilder().labelsAndValues(new LocalizedLabelsAndValues(new Localized() {
		@Override
		public String getString() {
			return Resources.get("lbl.preferences.http.timeout.infinite");
		}
	}, 0)).build()),
	HTTP_READ_TIMEOUT(PageDefinition.HTTP, IntegerComboFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(HttpPollingThread.Defaults.READ_TIMEOUT).build(), new FieldEditorDetailsBuilder().labelsAndValues(new LocalizedLabelsAndValues(new Localized() {
		@Override
		public String getString() {
			return Resources.get("lbl.preferences.http.timeout.infinite");
		}
	}, 0)).build()),
	HTTP_REFRESH_SECS(PageDefinition.HTTP, IntegerComboFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(HttpPollingThread.Defaults.REFRESH_SECS).build(), new FieldEditorDetailsBuilder().labelsAndValues(new LocalizedLabelsAndValues(new Localized() {
		@Override
		public String getString() {
			return Resources.get("lbl.preferences.http.refresh.auto");
		}
	}, 0)).build()),

	GUI_TABLE_ITEMS_MAX(PageDefinition.APPEARANCE, DefaultIntegerFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(DataTable.Defaults.MAX_ITEMS).build(), new FieldEditorDetailsBuilder().textLimit(4).build()),
	GUI_IMPORTANT_KEYS(PageDefinition.APPEARANCE, WrapStringFieldEditor.class, new FieldEditorDetailsBuilder().textHeight(3).build()),
	GUI_IMPORTANT_KEYS_SEPARATOR(PageDefinition.APPEARANCE, DefaultStringFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(RouterLoggerClientConfiguration.Defaults.GUI_IMPORTANT_KEYS_SEPARATOR).build(), new FieldEditorDetailsBuilder().emptyStringAllowed(false).build()),
	GUI_IMPORTANT_KEYS_COLOR(PageDefinition.APPEARANCE, ColorFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(DataTable.Defaults.IMPORTANT_KEY_BACKGROUND_COLOR).build()),
	GUI_THRESHOLDS_REACHED_COLOR(PageDefinition.APPEARANCE, ColorFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(DataTable.Defaults.THRESHOLDS_REACHED_FOREGROUD_COLOR).build()),
	GUI_TABLE_COLUMNS_PACK(PageDefinition.APPEARANCE, DefaultBooleanFieldEditor.class, new PreferenceDetailsBuilder().restartRequired().defaultValue(DataTable.Defaults.COLUMNS_PACK).build()),
	GUI_TABLE_COLUMNS_PADDING_RIGHT(PageDefinition.APPEARANCE, ScaleIntegerFieldEditor.class, new PreferenceDetailsBuilder().restartRequired().defaultValue(DataTable.Defaults.COLUMNS_PADDING_RIGHT).build(), new FieldEditorDetailsBuilder().scaleMaximum(Byte.MAX_VALUE).scalePageIncrement(10).build()),
	GUI_CONSOLE_MAX_CHARS(PageDefinition.APPEARANCE, DefaultIntegerFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(TextConsole.Defaults.GUI_CONSOLE_MAX_CHARS).build(), new FieldEditorDetailsBuilder().textLimit(6).build()),
	GUI_CLIPBOARD_MAX_CHARS(PageDefinition.APPEARANCE, DefaultIntegerFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(RouterLoggerGui.Defaults.GUI_CLIPBOARD_MAX_CHARS).build(), new FieldEditorDetailsBuilder().integerValidRange(0, 128 * 1024).build()),
	GUI_MINIMIZE_TRAY(PageDefinition.APPEARANCE, DefaultBooleanFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(TrayIcon.Defaults.GUI_MINIMIZE_TRAY).build()),
	GUI_TRAY_TOOLTIP(PageDefinition.APPEARANCE, DefaultBooleanFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(TrayIcon.Defaults.GUI_TRAY_TOOLTIP).parent(GUI_MINIMIZE_TRAY).build()),
	GUI_START_MINIMIZED(PageDefinition.APPEARANCE, DefaultBooleanFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(RouterLoggerGui.Defaults.GUI_START_MINIMIZED).build()),
	GUI_CONFIRM_CLOSE(PageDefinition.APPEARANCE, DefaultBooleanFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(CloseMessageBox.Defaults.GUI_CONFIRM_CLOSE).build());

	private static final String LABEL_KEY_PREFIX = "lbl.preferences.";

	private static final FieldEditorFactory fieldEditorFactory = new FieldEditorFactory();

	private final PreferenceDetails preferenceDetails;
	private final FieldEditorDetails fieldEditorDetails;
	private final Class<? extends FieldEditor> fieldEditorType;
	private final IPreferencePageDefinition pageDefinition;

	RouterLoggerClientPreference(final IPreferencePageDefinition page, final Class<? extends FieldEditor> fieldEditorType) {
		this(page, fieldEditorType, new PreferenceDetails(), null);
	}

	RouterLoggerClientPreference(final IPreferencePageDefinition page, final Class<? extends FieldEditor> fieldEditorType, final PreferenceDetails preferenceData) {
		this(page, fieldEditorType, preferenceData, null);
	}

	RouterLoggerClientPreference(final IPreferencePageDefinition page, final Class<? extends FieldEditor> fieldEditorType, final FieldEditorDetails fieldEditorData) {
		this(page, fieldEditorType, new PreferenceDetails(), fieldEditorData);
	}

	RouterLoggerClientPreference(final IPreferencePageDefinition page, final Class<? extends FieldEditor> fieldEditorType, final PreferenceDetails preferenceDetails, final FieldEditorDetails fieldEditorDetails) {
		this.preferenceDetails = preferenceDetails;
		if (preferenceDetails.getName() == null) {
			preferenceDetails.setName(name().toLowerCase().replace('_', '.'));
		}
		if (preferenceDetails.getLabel() == null) {
			preferenceDetails.setLabel(new Localized() {
				@Override
				public String getString() {
					return Resources.get(LABEL_KEY_PREFIX + preferenceDetails.getName());
				}
			});
		}
		this.fieldEditorDetails = fieldEditorDetails;
		this.pageDefinition = page;
		this.fieldEditorType = fieldEditorType;
	}

	@Override
	public String getName() {
		return preferenceDetails.getName();
	}

	@Override
	public String getLabel() {
		return preferenceDetails.getLabel().getString();
	}

	@Override
	public IPreferencePageDefinition getPageDefinition() {
		return pageDefinition;
	}

	@Override
	public String getDefaultValue() {
		return preferenceDetails.getDefaultValue();
	}

	@Override
	public IPreference getParent() {
		return preferenceDetails.getParent();
	}

	@Override
	public boolean isRestartRequired() {
		return preferenceDetails.isRestartRequired();
	}

	@Override
	public boolean isSeparate() {
		return preferenceDetails.isSeparate();
	}

	@Override
	public Set<? extends IPreference> getChildren() {
		final Set<RouterLoggerClientPreference> preferences = EnumSet.noneOf(RouterLoggerClientPreference.class);
		for (final RouterLoggerClientPreference item : RouterLoggerClientPreference.values()) {
			if (this.equals(item.getParent())) {
				preferences.add(item);
			}
		}
		return preferences;
	}

	@Override
	public FieldEditor createFieldEditor(final Composite parent) {
		return fieldEditorFactory.createFieldEditor(fieldEditorType, getName(), getLabel(), parent, fieldEditorDetails);
	}

	public static IPreference forConfigurationKey(final String configurationKey) {
		if (configurationKey != null) {
			for (final RouterLoggerClientPreference preference : RouterLoggerClientPreference.values()) {
				if (configurationKey.equals(preference.getName())) {
					return preference;
				}
			}
		}
		return null;
	}

}

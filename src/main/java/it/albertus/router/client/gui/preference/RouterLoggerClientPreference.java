package it.albertus.router.client.gui.preference;

import it.albertus.jface.TextConsole;
import it.albertus.jface.preference.FieldEditorData;
import it.albertus.jface.preference.FieldEditorData.FieldEditorDataBuilder;
import it.albertus.jface.preference.FieldEditorFactory;
import it.albertus.jface.preference.LocalizedNamesAndValues;
import it.albertus.jface.preference.Preference;
import it.albertus.jface.preference.PreferenceData;
import it.albertus.jface.preference.PreferenceData.PreferenceDataBuilder;
import it.albertus.jface.preference.field.ComboFieldEditor;
import it.albertus.jface.preference.field.DefaultBooleanFieldEditor;
import it.albertus.jface.preference.field.DefaultRadioGroupFieldEditor;
import it.albertus.jface.preference.field.FormattedComboFieldEditor;
import it.albertus.jface.preference.field.FormattedDirectoryFieldEditor;
import it.albertus.jface.preference.field.FormattedIntegerFieldEditor;
import it.albertus.jface.preference.field.FormattedStringFieldEditor;
import it.albertus.jface.preference.field.IntegerComboFieldEditor;
import it.albertus.jface.preference.field.PasswordFieldEditor;
import it.albertus.jface.preference.field.ScaleIntegerFieldEditor;
import it.albertus.jface.preference.field.UriListEditor;
import it.albertus.jface.preference.field.WrapStringFieldEditor;
import it.albertus.jface.preference.page.Page;
import it.albertus.router.client.engine.RouterLoggerClientConfiguration;
import it.albertus.router.client.gui.CloseMessageBox;
import it.albertus.router.client.gui.DataTable;
import it.albertus.router.client.gui.RouterLoggerGui;
import it.albertus.router.client.gui.TrayIcon;
import it.albertus.router.client.gui.preference.page.AdvancedMqttPreferencePage;
import it.albertus.router.client.gui.preference.page.GeneralPreferencePage;
import it.albertus.router.client.gui.preference.page.MqttPreferencePage;
import it.albertus.router.client.gui.preference.page.RouterLoggerClientPage;
import it.albertus.router.client.http.HttpPollingThread;
import it.albertus.router.client.mqtt.RouterLoggerClientMqttClient;
import it.albertus.router.client.resources.Resources;
import it.albertus.util.Localized;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.widgets.Composite;

public enum RouterLoggerClientPreference implements Preference {

	LANGUAGE(RouterLoggerClientPage.GENERAL, ComboFieldEditor.class, new PreferenceDataBuilder().defaultValue(Locale.getDefault().getLanguage()).build(), new FieldEditorDataBuilder().namesAndValues(GeneralPreferencePage.getLanguageComboOptions()).build()),
	CLIENT_PROTOCOL(RouterLoggerClientPage.GENERAL, DefaultRadioGroupFieldEditor.class, new PreferenceDataBuilder().separator().defaultValue(RouterLoggerGui.Defaults.CLIENT_PROTOCOL).restartRequired().build(), new FieldEditorDataBuilder().namesAndValues(GeneralPreferencePage.getProtocolComboOptions()).radioNumColumns(1).radioUseGroup(true).build()),

	MQTT_SERVER_URI(RouterLoggerClientPage.MQTT, UriListEditor.class, new PreferenceDataBuilder().restartRequired().build(), new FieldEditorDataBuilder().build()),
	MQTT_USERNAME(RouterLoggerClientPage.MQTT, FormattedStringFieldEditor.class, new PreferenceDataBuilder().restartRequired().build()),
	MQTT_PASSWORD(RouterLoggerClientPage.MQTT, PasswordFieldEditor.class, new PreferenceDataBuilder().restartRequired().build()),
	MQTT_CLIENT_ID(RouterLoggerClientPage.MQTT, FormattedStringFieldEditor.class, new PreferenceDataBuilder().restartRequired().defaultValue(RouterLoggerClientMqttClient.Defaults.CLIENT_ID).build(), new FieldEditorDataBuilder().emptyStringAllowed(false).build()),
	MQTT_CONNECT_RETRY(RouterLoggerClientPage.MQTT, DefaultBooleanFieldEditor.class, new PreferenceDataBuilder().restartRequired().separator().defaultValue(RouterLoggerClientMqttClient.Defaults.CONNECT_RETRY).build()),
	MQTT_CONNECT_RETRY_INTERVAL_SECS(RouterLoggerClientPage.MQTT, ScaleIntegerFieldEditor.class, new PreferenceDataBuilder().parent(MQTT_CONNECT_RETRY).defaultValue(RouterLoggerGui.Defaults.MQTT_CONNECT_RETRY_INTERVAL_SECS).build(), new FieldEditorDataBuilder().scaleMinimum(1).scaleMaximum(Byte.MAX_VALUE).scalePageIncrement(7).build()),

	MQTT_DATA_TOPIC(RouterLoggerClientPage.MQTT_MESSAGES, FormattedStringFieldEditor.class, new PreferenceDataBuilder().defaultValue(RouterLoggerClientMqttClient.Defaults.DATA_TOPIC).build(), new FieldEditorDataBuilder().emptyStringAllowed(false).build()),
	MQTT_DATA_QOS(RouterLoggerClientPage.MQTT_MESSAGES, FormattedComboFieldEditor.class, new PreferenceDataBuilder().defaultValue(RouterLoggerClientMqttClient.Defaults.DATA_QOS).build(), new FieldEditorDataBuilder().namesAndValues(MqttPreferencePage.getMqttQosComboOptions()).build()),
	MQTT_STATUS_TOPIC(RouterLoggerClientPage.MQTT_MESSAGES, FormattedStringFieldEditor.class, new PreferenceDataBuilder().separator().restartRequired().defaultValue(RouterLoggerClientMqttClient.Defaults.STATUS_TOPIC).build(), new FieldEditorDataBuilder().emptyStringAllowed(false).build()),
	MQTT_STATUS_QOS(RouterLoggerClientPage.MQTT_MESSAGES, FormattedComboFieldEditor.class, new PreferenceDataBuilder().restartRequired().defaultValue(RouterLoggerClientMqttClient.Defaults.STATUS_QOS).build(), new FieldEditorDataBuilder().namesAndValues(MqttPreferencePage.getMqttQosComboOptions()).build()),
	MQTT_THRESHOLDS_TOPIC(RouterLoggerClientPage.MQTT_MESSAGES, FormattedStringFieldEditor.class, new PreferenceDataBuilder().separator().defaultValue(RouterLoggerClientMqttClient.Defaults.THRESHOLDS_TOPIC).build(), new FieldEditorDataBuilder().emptyStringAllowed(false).build()),
	MQTT_THRESHOLDS_QOS(RouterLoggerClientPage.MQTT_MESSAGES, FormattedComboFieldEditor.class, new PreferenceDataBuilder().defaultValue(RouterLoggerClientMqttClient.Defaults.THRESHOLDS_QOS).build(), new FieldEditorDataBuilder().namesAndValues(MqttPreferencePage.getMqttQosComboOptions()).build()),

	MQTT_CLEAN_SESSION(RouterLoggerClientPage.MQTT_ADVANCED, DefaultBooleanFieldEditor.class, new PreferenceDataBuilder().restartRequired().defaultValue(RouterLoggerClientMqttClient.Defaults.CLEAN_SESSION).build()),
	MQTT_AUTOMATIC_RECONNECT(RouterLoggerClientPage.MQTT_ADVANCED, DefaultBooleanFieldEditor.class, new PreferenceDataBuilder().restartRequired().defaultValue(RouterLoggerClientMqttClient.Defaults.AUTOMATIC_RECONNECT).build()),
	MQTT_CONNECTION_TIMEOUT(RouterLoggerClientPage.MQTT_ADVANCED, FormattedIntegerFieldEditor.class, new PreferenceDataBuilder().restartRequired().defaultValue(RouterLoggerClientMqttClient.Defaults.CONNECTION_TIMEOUT).build()),
	MQTT_KEEP_ALIVE_INTERVAL(RouterLoggerClientPage.MQTT_ADVANCED, FormattedIntegerFieldEditor.class, new PreferenceDataBuilder().restartRequired().defaultValue(RouterLoggerClientMqttClient.Defaults.KEEP_ALIVE_INTERVAL).build()),
	MQTT_MAX_INFLIGHT(RouterLoggerClientPage.MQTT_ADVANCED, FormattedIntegerFieldEditor.class, new PreferenceDataBuilder().restartRequired().defaultValue(RouterLoggerClientMqttClient.Defaults.MAX_INFLIGHT).build()),
	MQTT_VERSION(RouterLoggerClientPage.MQTT_ADVANCED, FormattedComboFieldEditor.class, new PreferenceDataBuilder().restartRequired().defaultValue(RouterLoggerClientMqttClient.Defaults.MQTT_VERSION).build(), new FieldEditorDataBuilder().namesAndValues(AdvancedMqttPreferencePage.getMqttVersionComboOptions()).build()),
	MQTT_PERSISTENCE_FILE_ENABLED(RouterLoggerClientPage.MQTT_ADVANCED, DefaultBooleanFieldEditor.class, new PreferenceDataBuilder().restartRequired().defaultValue(RouterLoggerClientMqttClient.Defaults.PERSISTENCE_FILE_ENABLED).build()),
	MQTT_PERSISTENCE_FILE_CUSTOM(RouterLoggerClientPage.MQTT_ADVANCED, DefaultBooleanFieldEditor.class, new PreferenceDataBuilder().restartRequired().defaultValue(RouterLoggerClientMqttClient.Defaults.PERSISTENCE_FILE_CUSTOM).parent(MQTT_PERSISTENCE_FILE_ENABLED).build()),
	MQTT_PERSISTENCE_FILE_PATH(RouterLoggerClientPage.MQTT_ADVANCED, FormattedDirectoryFieldEditor.class, new PreferenceDataBuilder().restartRequired().defaultValue(System.getProperty("user.dir")).parent(MQTT_PERSISTENCE_FILE_CUSTOM).build(), new FieldEditorDataBuilder().emptyStringAllowed(false).directoryDialogMessage(new Localized() {
		@Override
		public String getString() {
			return Resources.get("msg.preferences.directory.dialog.message.mqtt");
		}
	}).build()),

	HTTP_HOST(RouterLoggerClientPage.HTTP, FormattedStringFieldEditor.class),
	HTTP_PORT(RouterLoggerClientPage.HTTP, FormattedIntegerFieldEditor.class, new PreferenceDataBuilder().defaultValue(HttpPollingThread.Defaults.PORT).build(), new FieldEditorDataBuilder().integerValidRange(1, 65535).emptyStringAllowed(false).build()),
	HTTP_AUTHENTICATION(RouterLoggerClientPage.HTTP, DefaultBooleanFieldEditor.class, new PreferenceDataBuilder().defaultValue(HttpPollingThread.Defaults.AUTHENTICATION).build()),
	HTTP_USERNAME(RouterLoggerClientPage.HTTP, FormattedStringFieldEditor.class, new PreferenceDataBuilder().parent(HTTP_AUTHENTICATION).build()),
	HTTP_PASSWORD(RouterLoggerClientPage.HTTP, PasswordFieldEditor.class, new PreferenceDataBuilder().parent(HTTP_AUTHENTICATION).build()),
	HTTP_IGNORE_CERTIFICATE(RouterLoggerClientPage.HTTP, DefaultBooleanFieldEditor.class, new PreferenceDataBuilder().defaultValue(HttpPollingThread.Defaults.IGNORE_CERTIFICATE).restartRequired().build()),
	HTTP_REFRESH_SECS(RouterLoggerClientPage.HTTP,  IntegerComboFieldEditor.class, new PreferenceDataBuilder().defaultValue(HttpPollingThread.Defaults.REFRESH_SECS).build(), new FieldEditorDataBuilder().namesAndValues(new LocalizedNamesAndValues(new Localized() {
		@Override
		public String getString() {
			return Resources.get("lbl.preferences.http.refresh.auto");
		}
	}, 0)).build()),
	
	GUI_TABLE_ITEMS_MAX(RouterLoggerClientPage.APPEARANCE, FormattedIntegerFieldEditor.class, new PreferenceDataBuilder().defaultValue(DataTable.Defaults.MAX_ITEMS).build(), new FieldEditorDataBuilder().textLimit(4).build()),
	GUI_IMPORTANT_KEYS(RouterLoggerClientPage.APPEARANCE, WrapStringFieldEditor.class),
	GUI_IMPORTANT_KEYS_SEPARATOR(RouterLoggerClientPage.APPEARANCE, FormattedStringFieldEditor.class, new PreferenceDataBuilder().defaultValue(RouterLoggerClientConfiguration.Defaults.GUI_IMPORTANT_KEYS_SEPARATOR).build(), new FieldEditorDataBuilder().emptyStringAllowed(false).build()),
	GUI_TABLE_COLUMNS_PACK(RouterLoggerClientPage.APPEARANCE, DefaultBooleanFieldEditor.class, new PreferenceDataBuilder().restartRequired().defaultValue(DataTable.Defaults.COLUMNS_PACK).build()),
	GUI_TABLE_COLUMNS_PADDING_RIGHT(RouterLoggerClientPage.APPEARANCE, ScaleIntegerFieldEditor.class, new PreferenceDataBuilder().restartRequired().defaultValue(DataTable.Defaults.COLUMNS_PADDING_RIGHT).build(), new FieldEditorDataBuilder().scaleMaximum(Byte.MAX_VALUE).scalePageIncrement(10).build()),
	GUI_CONSOLE_MAX_CHARS(RouterLoggerClientPage.APPEARANCE, FormattedIntegerFieldEditor.class, new PreferenceDataBuilder().defaultValue(TextConsole.Defaults.GUI_CONSOLE_MAX_CHARS).build(), new FieldEditorDataBuilder().textLimit(6).build()),
	GUI_CLIPBOARD_MAX_CHARS(RouterLoggerClientPage.APPEARANCE, FormattedIntegerFieldEditor.class, new PreferenceDataBuilder().defaultValue(RouterLoggerGui.Defaults.GUI_CLIPBOARD_MAX_CHARS).build(), new FieldEditorDataBuilder().integerValidRange(0, 128 * 1024).build()),
	GUI_MINIMIZE_TRAY(RouterLoggerClientPage.APPEARANCE, DefaultBooleanFieldEditor.class, new PreferenceDataBuilder().defaultValue(TrayIcon.Defaults.GUI_MINIMIZE_TRAY).build()),
	GUI_TRAY_TOOLTIP(RouterLoggerClientPage.APPEARANCE, DefaultBooleanFieldEditor.class, new PreferenceDataBuilder().defaultValue(TrayIcon.Defaults.GUI_TRAY_TOOLTIP).parent(GUI_MINIMIZE_TRAY).build()),
	GUI_START_MINIMIZED(RouterLoggerClientPage.APPEARANCE, DefaultBooleanFieldEditor.class, new PreferenceDataBuilder().defaultValue(RouterLoggerGui.Defaults.GUI_START_MINIMIZED).build()),
	GUI_CONFIRM_CLOSE(RouterLoggerClientPage.APPEARANCE, DefaultBooleanFieldEditor.class, new PreferenceDataBuilder().defaultValue(CloseMessageBox.Defaults.GUI_CONFIRM_CLOSE).build());

	private static final String LABEL_KEY_PREFIX = "lbl.preferences.";

	private static final FieldEditorFactory fieldEditorFactory = new FieldEditorFactory();

	private final Page page;
	private final Class<? extends FieldEditor> fieldEditorType;
	private final String defaultValue;
	private final FieldEditorData fieldEditorData;
	private final Preference parent;
	private final String configurationKey;
	private final String labelKey;
	private final boolean restartRequired;
	private final boolean separator;

	private RouterLoggerClientPreference(final Page page, final Class<? extends FieldEditor> fieldEditorType) {
		this(page, fieldEditorType, null, null);
	}

	private RouterLoggerClientPreference(final Page page, final Class<? extends FieldEditor> fieldEditorType, final PreferenceData preferenceData) {
		this(page, fieldEditorType, preferenceData, null);
	}

	private RouterLoggerClientPreference(final Page page, final Class<? extends FieldEditor> fieldEditorType, final FieldEditorData fieldEditorData) {
		this(page, fieldEditorType, null, fieldEditorData);
	}

	private RouterLoggerClientPreference(final Page page, final Class<? extends FieldEditor> fieldEditorType, final PreferenceData preferenceData, final FieldEditorData fieldEditorData) {
		if (preferenceData != null) {
			final String configurationKey = preferenceData.getConfigurationKey();
			if (configurationKey != null && !configurationKey.isEmpty()) {
				this.configurationKey = configurationKey;
			}
			else {
				this.configurationKey = name().toLowerCase().replace('_', '.');
			}
			final String labelKey = preferenceData.getLabelResourceKey();
			if (labelKey != null && !labelKey.isEmpty()) {
				this.labelKey = labelKey;
			}
			else {
				this.labelKey = LABEL_KEY_PREFIX + this.configurationKey;
			}
			this.defaultValue = preferenceData.getDefaultValue();
			this.parent = preferenceData.getParent();
			this.restartRequired = preferenceData.isRestartRequired();
			this.separator = preferenceData.hasSeparator();
		}
		else {
			this.configurationKey = name().toLowerCase().replace('_', '.');
			this.labelKey = LABEL_KEY_PREFIX + this.configurationKey;
			this.restartRequired = false;
			this.defaultValue = null;
			this.parent = null;
			this.separator = false;
		}
		this.fieldEditorData = fieldEditorData;
		this.fieldEditorType = fieldEditorType;
		this.page = page;
	}

	@Override
	public String getConfigurationKey() {
		return configurationKey;
	}

	@Override
	public String getLabel() {
		return Resources.get(labelKey);
	}

	@Override
	public Page getPage() {
		return page;
	}

	@Override
	public String getDefaultValue() {
		return defaultValue;
	}

	@Override
	public Preference getParent() {
		return parent;
	}

	@Override
	public boolean isRestartRequired() {
		return restartRequired;
	}

	@Override
	public boolean hasSeparator() {
		return separator;
	}

	@Override
	public Set<? extends Preference> getChildren() {
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
		return fieldEditorFactory.createFieldEditor(fieldEditorType, configurationKey, getLabel(), parent, fieldEditorData);
	}

	public static Preference forConfigurationKey(final String configurationKey) {
		if (configurationKey != null) {
			for (final RouterLoggerClientPreference preference : RouterLoggerClientPreference.values()) {
				if (configurationKey.equals(preference.configurationKey)) {
					return preference;
				}
			}
		}
		return null;
	}

}

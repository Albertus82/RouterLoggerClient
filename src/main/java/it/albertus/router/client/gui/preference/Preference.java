package it.albertus.router.client.gui.preference;

import static it.albertus.router.client.gui.preference.page.PageDefinition.APPEARANCE;
import static it.albertus.router.client.gui.preference.page.PageDefinition.APPEARANCE_TABLE;
import static it.albertus.router.client.gui.preference.page.PageDefinition.GENERAL;
import static it.albertus.router.client.gui.preference.page.PageDefinition.HTTP;
import static it.albertus.router.client.gui.preference.page.PageDefinition.MQTT;
import static it.albertus.router.client.gui.preference.page.PageDefinition.MQTT_ADVANCED;
import static it.albertus.router.client.gui.preference.page.PageDefinition.MQTT_MESSAGES;
import it.albertus.jface.TextConsole;
import it.albertus.jface.preference.FieldEditorDetails;
import it.albertus.jface.preference.FieldEditorDetails.FieldEditorDetailsBuilder;
import it.albertus.jface.preference.IPreference;
import it.albertus.jface.preference.LocalizedLabelsAndValues;
import it.albertus.jface.preference.PreferenceDetails;
import it.albertus.jface.preference.PreferenceDetails.PreferenceDetailsBuilder;
import it.albertus.jface.preference.field.DefaultBooleanFieldEditor;
import it.albertus.jface.preference.field.DefaultComboFieldEditor;
import it.albertus.jface.preference.field.DefaultRadioGroupFieldEditor;
import it.albertus.jface.preference.field.EnhancedDirectoryFieldEditor;
import it.albertus.jface.preference.field.EnhancedIntegerFieldEditor;
import it.albertus.jface.preference.field.EnhancedStringFieldEditor;
import it.albertus.jface.preference.field.IntegerComboFieldEditor;
import it.albertus.jface.preference.field.PasswordFieldEditor;
import it.albertus.jface.preference.field.ScaleIntegerFieldEditor;
import it.albertus.jface.preference.field.UriListEditor;
import it.albertus.jface.preference.field.WrapStringFieldEditor;
import it.albertus.jface.preference.page.IPageDefinition;
import it.albertus.router.client.engine.RouterLoggerClientConfiguration;
import it.albertus.router.client.gui.CloseMessageBox;
import it.albertus.router.client.gui.DataTable;
import it.albertus.router.client.gui.RouterLoggerGui;
import it.albertus.router.client.gui.TrayIcon;
import it.albertus.router.client.gui.preference.page.AdvancedMqttPreferencePage;
import it.albertus.router.client.gui.preference.page.GeneralPreferencePage;
import it.albertus.router.client.gui.preference.page.MqttPreferencePage;
import it.albertus.router.client.http.HttpPollingThread;
import it.albertus.router.client.mqtt.RouterLoggerClientMqttClient;
import it.albertus.router.client.resources.Messages;
import it.albertus.router.client.util.Logger;
import it.albertus.util.Localized;

import java.util.EnumSet;
import java.util.Set;

import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.widgets.Composite;

public enum Preference implements IPreference {

	LANGUAGE(new PreferenceDetailsBuilder(GENERAL).defaultValue(RouterLoggerClientConfiguration.Defaults.LANGUAGE).build(), new FieldEditorDetailsBuilder(DefaultComboFieldEditor.class).labelsAndValues(GeneralPreferencePage.getLanguageComboOptions()).build()),
	CLIENT_PROTOCOL(new PreferenceDetailsBuilder(GENERAL).separate().defaultValue(RouterLoggerGui.Defaults.CLIENT_PROTOCOL).restartRequired().build(), new FieldEditorDetailsBuilder(DefaultRadioGroupFieldEditor.class).labelsAndValues(GeneralPreferencePage.getProtocolComboOptions()).radioNumColumns(1).radioUseGroup(true).build()),
	DEBUG(new PreferenceDetailsBuilder(GENERAL).separate().defaultValue(Logger.Defaults.DEBUG).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),

	MQTT_SERVER_URI(new PreferenceDetailsBuilder(MQTT).restartRequired().build(), new FieldEditorDetailsBuilder(UriListEditor.class).build()),
	MQTT_USERNAME(new PreferenceDetailsBuilder(MQTT).restartRequired().build(), new FieldEditorDetailsBuilder(EnhancedStringFieldEditor.class).build()),
	MQTT_PASSWORD(new PreferenceDetailsBuilder(MQTT).restartRequired().build(), new FieldEditorDetailsBuilder(PasswordFieldEditor.class).build()),
	MQTT_CLIENT_ID(new PreferenceDetailsBuilder(MQTT).restartRequired().defaultValue(RouterLoggerClientMqttClient.Defaults.CLIENT_ID).build(), new FieldEditorDetailsBuilder(EnhancedStringFieldEditor.class).emptyStringAllowed(false).build()),
	MQTT_CONNECT_RETRY(new PreferenceDetailsBuilder(MQTT).restartRequired().separate().defaultValue(RouterLoggerClientMqttClient.Defaults.CONNECT_RETRY).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	MQTT_CONNECT_RETRY_INTERVAL_SECS(new PreferenceDetailsBuilder(MQTT).parent(MQTT_CONNECT_RETRY).defaultValue(RouterLoggerGui.Defaults.MQTT_CONNECT_RETRY_INTERVAL_SECS).build(), new FieldEditorDetailsBuilder(ScaleIntegerFieldEditor.class).scaleMinimum(1).scaleMaximum(Byte.MAX_VALUE).scalePageIncrement(7).build()),

	MQTT_DATA_TOPIC(new PreferenceDetailsBuilder(MQTT_MESSAGES).defaultValue(RouterLoggerClientMqttClient.Defaults.DATA_TOPIC).restartRequired().build(), new FieldEditorDetailsBuilder(EnhancedStringFieldEditor.class).emptyStringAllowed(false).build()),
	MQTT_DATA_QOS(new PreferenceDetailsBuilder(MQTT_MESSAGES).defaultValue(RouterLoggerClientMqttClient.Defaults.DATA_QOS).restartRequired().build(), new FieldEditorDetailsBuilder(DefaultComboFieldEditor.class).labelsAndValues(MqttPreferencePage.getMqttQosComboOptions()).build()),
	MQTT_STATUS_TOPIC(new PreferenceDetailsBuilder(MQTT_MESSAGES).separate().restartRequired().defaultValue(RouterLoggerClientMqttClient.Defaults.STATUS_TOPIC).build(), new FieldEditorDetailsBuilder(EnhancedStringFieldEditor.class).emptyStringAllowed(false).build()),
	MQTT_STATUS_QOS(new PreferenceDetailsBuilder(MQTT_MESSAGES).restartRequired().defaultValue(RouterLoggerClientMqttClient.Defaults.STATUS_QOS).build(), new FieldEditorDetailsBuilder(DefaultComboFieldEditor.class).labelsAndValues(MqttPreferencePage.getMqttQosComboOptions()).build()),
	MQTT_THRESHOLDS_TOPIC(new PreferenceDetailsBuilder(MQTT_MESSAGES).separate().restartRequired().defaultValue(RouterLoggerClientMqttClient.Defaults.THRESHOLDS_TOPIC).build(), new FieldEditorDetailsBuilder(EnhancedStringFieldEditor.class).emptyStringAllowed(false).build()),
	MQTT_THRESHOLDS_QOS(new PreferenceDetailsBuilder(MQTT_MESSAGES).defaultValue(RouterLoggerClientMqttClient.Defaults.THRESHOLDS_QOS).restartRequired().build(), new FieldEditorDetailsBuilder(DefaultComboFieldEditor.class).labelsAndValues(MqttPreferencePage.getMqttQosComboOptions()).build()),

	MQTT_CLEAN_SESSION(new PreferenceDetailsBuilder(MQTT_ADVANCED).restartRequired().defaultValue(RouterLoggerClientMqttClient.Defaults.CLEAN_SESSION).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	MQTT_AUTOMATIC_RECONNECT(new PreferenceDetailsBuilder(MQTT_ADVANCED).restartRequired().defaultValue(RouterLoggerClientMqttClient.Defaults.AUTOMATIC_RECONNECT).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	MQTT_CONNECTION_TIMEOUT(new PreferenceDetailsBuilder(MQTT_ADVANCED).restartRequired().defaultValue(RouterLoggerClientMqttClient.Defaults.CONNECTION_TIMEOUT).build(), new FieldEditorDetailsBuilder(EnhancedIntegerFieldEditor.class).build()),
	MQTT_KEEP_ALIVE_INTERVAL(new PreferenceDetailsBuilder(MQTT_ADVANCED).restartRequired().defaultValue(RouterLoggerClientMqttClient.Defaults.KEEP_ALIVE_INTERVAL).build(), new FieldEditorDetailsBuilder(EnhancedIntegerFieldEditor.class).build()),
	MQTT_MAX_INFLIGHT(new PreferenceDetailsBuilder(MQTT_ADVANCED).restartRequired().defaultValue(RouterLoggerClientMqttClient.Defaults.MAX_INFLIGHT).build(), new FieldEditorDetailsBuilder(EnhancedIntegerFieldEditor.class).build()),
	MQTT_VERSION(new PreferenceDetailsBuilder(MQTT_ADVANCED).restartRequired().defaultValue(RouterLoggerClientMqttClient.Defaults.MQTT_VERSION).build(), new FieldEditorDetailsBuilder(DefaultComboFieldEditor.class).labelsAndValues(AdvancedMqttPreferencePage.getMqttVersionComboOptions()).build()),
	MQTT_PERSISTENCE_FILE_ENABLED(new PreferenceDetailsBuilder(MQTT_ADVANCED).restartRequired().defaultValue(RouterLoggerClientMqttClient.Defaults.PERSISTENCE_FILE_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	MQTT_PERSISTENCE_FILE_CUSTOM(new PreferenceDetailsBuilder(MQTT_ADVANCED).restartRequired().defaultValue(RouterLoggerClientMqttClient.Defaults.PERSISTENCE_FILE_CUSTOM).parent(MQTT_PERSISTENCE_FILE_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	MQTT_PERSISTENCE_FILE_PATH(new PreferenceDetailsBuilder(MQTT_ADVANCED).restartRequired().defaultValue(System.getProperty("user.dir")).parent(MQTT_PERSISTENCE_FILE_CUSTOM).build(), new FieldEditorDetailsBuilder(EnhancedDirectoryFieldEditor.class).emptyStringAllowed(false).directoryDialogMessage(new Localized() {
		@Override
		public String getString() {
			return Messages.get("msg.preferences.directory.dialog.message.mqtt");
		}
	}).build()),

	HTTP_HOST(new PreferenceDetailsBuilder(HTTP).build(), new FieldEditorDetailsBuilder(EnhancedStringFieldEditor.class).build()),
	HTTP_PORT(new PreferenceDetailsBuilder(HTTP).defaultValue(HttpPollingThread.Defaults.PORT).build(), new FieldEditorDetailsBuilder(EnhancedIntegerFieldEditor.class).numberValidRange(1, 65535).emptyStringAllowed(false).build()),
	HTTP_AUTHENTICATION(new PreferenceDetailsBuilder(HTTP).defaultValue(HttpPollingThread.Defaults.AUTHENTICATION).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	HTTP_USERNAME(new PreferenceDetailsBuilder(HTTP).parent(HTTP_AUTHENTICATION).build(), new FieldEditorDetailsBuilder(EnhancedStringFieldEditor.class).build()),
	HTTP_PASSWORD(new PreferenceDetailsBuilder(HTTP).parent(HTTP_AUTHENTICATION).build(), new FieldEditorDetailsBuilder(PasswordFieldEditor.class).build()),
	HTTP_IGNORE_CERTIFICATE(new PreferenceDetailsBuilder(HTTP).defaultValue(HttpPollingThread.Defaults.IGNORE_CERTIFICATE).restartRequired().build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	HTTP_CONNECTION_TIMEOUT(new PreferenceDetailsBuilder(HTTP).defaultValue(HttpPollingThread.Defaults.CONNECTION_TIMEOUT).build(), new FieldEditorDetailsBuilder(IntegerComboFieldEditor.class).labelsAndValues(new LocalizedLabelsAndValues(new Localized() {
		@Override
		public String getString() {
			return Messages.get("lbl.preferences.http.timeout.infinite");
		}
	}, 0)).build()),
	HTTP_READ_TIMEOUT(new PreferenceDetailsBuilder(HTTP).defaultValue(HttpPollingThread.Defaults.READ_TIMEOUT).build(), new FieldEditorDetailsBuilder(IntegerComboFieldEditor.class).labelsAndValues(new LocalizedLabelsAndValues(new Localized() {
		@Override
		public String getString() {
			return Messages.get("lbl.preferences.http.timeout.infinite");
		}
	}, 0)).build()),
	HTTP_REFRESH_SECS(new PreferenceDetailsBuilder(HTTP).defaultValue(HttpPollingThread.Defaults.REFRESH_SECS).build(), new FieldEditorDetailsBuilder(IntegerComboFieldEditor.class).labelsAndValues(new LocalizedLabelsAndValues(new Localized() {
		@Override
		public String getString() {
			return Messages.get("lbl.preferences.http.refresh.auto");
		}
	}, 0)).build()),

	GUI_CONSOLE_MAX_CHARS(new PreferenceDetailsBuilder(APPEARANCE).defaultValue(TextConsole.Defaults.GUI_CONSOLE_MAX_CHARS).build(), new FieldEditorDetailsBuilder(EnhancedIntegerFieldEditor.class).textLimit(6).build()),
	GUI_CLIPBOARD_MAX_CHARS(new PreferenceDetailsBuilder(APPEARANCE).defaultValue(RouterLoggerGui.Defaults.GUI_CLIPBOARD_MAX_CHARS).build(), new FieldEditorDetailsBuilder(EnhancedIntegerFieldEditor.class).numberValidRange(0, 128 * 1024).build()),
	GUI_MINIMIZE_TRAY(new PreferenceDetailsBuilder(APPEARANCE).separate().defaultValue(TrayIcon.Defaults.GUI_MINIMIZE_TRAY).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	GUI_TRAY_TOOLTIP(new PreferenceDetailsBuilder(APPEARANCE).defaultValue(TrayIcon.Defaults.GUI_TRAY_TOOLTIP).parent(GUI_MINIMIZE_TRAY).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	GUI_START_MINIMIZED(new PreferenceDetailsBuilder(APPEARANCE).defaultValue(RouterLoggerGui.Defaults.GUI_START_MINIMIZED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	GUI_CONFIRM_CLOSE(new PreferenceDetailsBuilder(APPEARANCE).defaultValue(CloseMessageBox.Defaults.GUI_CONFIRM_CLOSE).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),

	GUI_TABLE_ITEMS_MAX(new PreferenceDetailsBuilder(APPEARANCE_TABLE).defaultValue(DataTable.Defaults.MAX_ITEMS).build(), new FieldEditorDetailsBuilder(EnhancedIntegerFieldEditor.class).textLimit(4).build()),
	GUI_IMPORTANT_KEYS(new PreferenceDetailsBuilder(APPEARANCE_TABLE).build(), new FieldEditorDetailsBuilder(WrapStringFieldEditor.class).textHeight(4).build()),
	GUI_IMPORTANT_KEYS_SEPARATOR(new PreferenceDetailsBuilder(APPEARANCE_TABLE).defaultValue(RouterLoggerClientConfiguration.Defaults.GUI_IMPORTANT_KEYS_SEPARATOR).build(), new FieldEditorDetailsBuilder(EnhancedStringFieldEditor.class).emptyStringAllowed(false).build()),
	GUI_TABLE_COLUMNS_PACK(new PreferenceDetailsBuilder(APPEARANCE_TABLE).separate().restartRequired().defaultValue(DataTable.Defaults.COLUMNS_PACK).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	GUI_TABLE_COLUMNS_PADDING_RIGHT(new PreferenceDetailsBuilder(APPEARANCE_TABLE).restartRequired().defaultValue(DataTable.Defaults.COLUMNS_PADDING_RIGHT).build(), new FieldEditorDetailsBuilder(ScaleIntegerFieldEditor.class).scaleMaximum(Byte.MAX_VALUE).scalePageIncrement(10).build()),
	GUI_IMPORTANT_KEYS_COLOR_BACKGROUND(new PreferenceDetailsBuilder(APPEARANCE_TABLE).separate().defaultValue(DataTable.Defaults.IMPORTANT_KEYS_COLOR_BACKGROUND).build(), new FieldEditorDetailsBuilder(ColorFieldEditor.class).build()),
	GUI_THRESHOLDS_REACHED_COLOR_FOREGROUND(new PreferenceDetailsBuilder(APPEARANCE_TABLE).defaultValue(DataTable.Defaults.THRESHOLDS_REACHED_COLOR_FOREGROUND).build(), new FieldEditorDetailsBuilder(ColorFieldEditor.class).build());

	private static final String LABEL_KEY_PREFIX = "lbl.preferences.";

	private final PreferenceDetails preferenceDetails;
	private final FieldEditorDetails fieldEditorDetails;

	Preference(final PreferenceDetails preferenceDetails, final FieldEditorDetails fieldEditorDetails) {
		this.preferenceDetails = preferenceDetails;
		this.fieldEditorDetails = fieldEditorDetails;
		if (preferenceDetails.getName() == null) {
			preferenceDetails.setName(name().toLowerCase().replace('_', '.'));
		}
		if (preferenceDetails.getLabel() == null) {
			preferenceDetails.setLabel(new Localized() {
				@Override
				public String getString() {
					return Messages.get(LABEL_KEY_PREFIX + preferenceDetails.getName());
				}
			});
		}
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
	public IPageDefinition getPageDefinition() {
		return preferenceDetails.getPageDefinition();
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
		final Set<Preference> preferences = EnumSet.noneOf(Preference.class);
		for (final Preference item : Preference.values()) {
			if (this.equals(item.getParent())) {
				preferences.add(item);
			}
		}
		return preferences;
	}

	@Override
	public FieldEditor createFieldEditor(final Composite parent) {
		return fieldEditorFactory.createFieldEditor(getName(), getLabel(), parent, fieldEditorDetails);
	}

	public static IPreference forName(final String name) {
		if (name != null) {
			for (final IPreference preference : Preference.values()) {
				if (name.equals(preference.getName())) {
					return preference;
				}
			}
		}
		return null;
	}

}

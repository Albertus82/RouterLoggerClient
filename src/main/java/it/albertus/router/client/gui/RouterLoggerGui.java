package it.albertus.router.client.gui;

import it.albertus.jface.TextConsole;
import it.albertus.router.client.engine.RouterLoggerClientConfiguration;
import it.albertus.router.client.engine.RouterLoggerStatus;
import it.albertus.router.client.gui.listener.CloseListener;
import it.albertus.router.client.http.DummyTrustManager;
import it.albertus.router.client.http.HttpPollingThread;
import it.albertus.router.client.mqtt.RouterLoggerClientMqttClient;
import it.albertus.router.client.resources.Resources;
import it.albertus.util.Configuration;
import it.albertus.util.Configured;
import it.albertus.util.ThreadUtils;
import it.albertus.util.Version;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class RouterLoggerGui extends ApplicationWindow {

	public static final String CFG_KEY_GUI_CLIPBOARD_MAX_CHARS = "gui.clipboard.max.chars";
	public static final int GUI_CLIPBOARD_MAX_CHARS = 100000;

	public static final SSLSocketFactory defaultSSLSocketFactory = HttpsURLConnection.getDefaultSSLSocketFactory();

	private static final float SASH_MAGNIFICATION_FACTOR = 1.5f;

	private final Configuration configuration = RouterLoggerClientConfiguration.getInstance();
	private final RouterLoggerClientMqttClient mqttClient = RouterLoggerClientMqttClient.getInstance();

	private TrayIcon trayIcon;
	private MenuBar menuBar;
	private SashForm sashForm;
	private DataTable dataTable;
	private TextConsole textConsole;

	private RouterLoggerStatus currentStatus;
	private RouterLoggerStatus previousStatus;

	private volatile Thread mqttConnectionThread;
	private volatile Thread httpPollingThread;

	public interface Defaults {
		boolean GUI_START_MINIMIZED = false;
		int GUI_CLIPBOARD_MAX_CHARS = 100000;
		boolean CONSOLE_SHOW_CONFIGURATION = false;
		int MQTT_CONNECT_RETRY_INTERVAL_SECS = 5;
	}

	private class MqttConnectionThread extends Thread {
		private MqttConnectionThread() {
			super("mqttConnectionThread");
			this.setDaemon(true);
		}

		@Override
		public void run() {
			while (true) {
				mqttClient.subscribeStatus();
				if (mqttClient.getClient() == null) {
					ThreadUtils.sleep(1000 * configuration.getInt("mqtt.connect.retry.interval.secs", Defaults.MQTT_CONNECT_RETRY_INTERVAL_SECS)); // Wait between retries
					continue; // Retry
				}
				mqttClient.subscribeData();
				mqttClient.subscribeThresholds();
				break;
			}
		}
	}

	public RouterLoggerGui(final Display display) {
		super(null);
		open();

		printWelcome();
		if (true) { // MQTT
			mqttClient.init(this);
			mqttConnectionThread = new MqttConnectionThread();
			mqttConnectionThread.start();
		}
		else { // HTTP
			if (true) { // SSL insecure
				try {
					final SSLContext sslContext = SSLContext.getInstance("SSL");
					sslContext.init(null, new TrustManager[] { new DummyTrustManager() }, new SecureRandom());
					HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
				}
				catch (final Exception e) {
					e.printStackTrace();
				}
			}
			httpPollingThread = new HttpPollingThread(this);
			httpPollingThread.start();
		}

		final Shell shell = getShell();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				Display.getCurrent().sleep();
			}
		}
		//		mqttClient.disconnect();
		printGoodbye();
	}

	@Override
	protected void configureShell(final Shell shell) {
		super.configureShell(shell);
		shell.setMinimized(configuration.getBoolean("gui.start.minimized", Defaults.GUI_START_MINIMIZED));
		shell.setText(Resources.get("lbl.window.title"));
		shell.setImages(Images.MAIN_ICONS);
	}

	@Override
	protected void handleShellCloseEvent() {
		final Event event = new Event();
		new CloseListener(this).handleEvent(event);
		if (event.doit) {
			super.handleShellCloseEvent();
		}
	}

	@Override
	protected Control createContents(final Composite parent) {
		trayIcon = new TrayIcon(this);

		menuBar = new MenuBar(this);

		sashForm = new SashForm(parent, SWT.VERTICAL);
		sashForm.setSashWidth((int) (sashForm.getSashWidth() * SASH_MAGNIFICATION_FACTOR));
		sashForm.setLayout(new GridLayout());
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		dataTable = new DataTable(sashForm, new GridData(SWT.FILL, SWT.FILL, true, true), this);
		textConsole = new TextConsole(sashForm, new GridData(SWT.FILL, SWT.FILL, true, true), new Configured<Integer>() {
			@Override
			public Integer getValue() {
				return configuration.getInt("gui.console.max.chars");
			}
		});
		return parent;
	}

	protected void printWelcome() {
		final Version version = Version.getInstance();
		System.out.println(Resources.get("msg.welcome", Resources.get("msg.application.name"), Resources.get("msg.version", version.getNumber(), version.getDate()), Resources.get("msg.website")));
		System.out.println();
		System.out.println(Resources.get("msg.startup.date", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date())));
		if (configuration.getBoolean("console.show.configuration", Defaults.CONSOLE_SHOW_CONFIGURATION)) {
			System.out.println(Resources.get("msg.settings", configuration));
		}
		System.out.println();
	}

	protected void printGoodbye() {
		System.out.println(Resources.get("msg.bye"));
	}

	@Override
	protected Layout getLayout() {
		return new GridLayout();
	}

	@Override
	protected void createTrimWidgets(final Shell shell) {/* Not needed */}

	public TrayIcon getTrayIcon() {
		return trayIcon;
	}

	public MenuBar getMenuBar() {
		return menuBar;
	}

	public SashForm getSashForm() {
		return sashForm;
	}

	public DataTable getDataTable() {
		return dataTable;
	}

	public TextConsole getTextConsole() {
		return textConsole;
	}

	public boolean canCopyConsole() {
		final Text text = textConsole.getText();
		return text != null && text.getSelectionCount() > 0 && (text.isFocusControl() || !dataTable.canCopy());
	}

	public boolean canSelectAllConsole() {
		final Text text = textConsole.getText();
		return text != null && !text.getText().isEmpty() && (text.isFocusControl() || !dataTable.canSelectAll());
	}

	public boolean canClearConsole() {
		final Text text = textConsole.getText();
		return text != null && !text.getText().isEmpty();
	}

	public RouterLoggerStatus getCurrentStatus() {
		return currentStatus;
	}

	public RouterLoggerStatus getPreviousStatus() {
		return previousStatus;
	}

	public void setStatus(final RouterLoggerStatus newStatus) {
		if (currentStatus == null || !currentStatus.getStatus().equals(newStatus.getStatus())) {
			previousStatus = currentStatus;
			currentStatus = newStatus;
			if (trayIcon != null) {
				trayIcon.updateTrayItem(currentStatus.getStatus());
			}
		}
	}

	public void restart() {
		mqttClient.disconnect();
		printGoodbye();

		configuration.reload();
		dataTable.reset();
		textConsole.clear();

		printWelcome();
		mqttClient.init(this);
		mqttConnectionThread = new MqttConnectionThread();
		mqttConnectionThread.start();
	}

}

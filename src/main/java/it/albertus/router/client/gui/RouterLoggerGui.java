package it.albertus.router.client.gui;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jface.util.Util;
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

import it.albertus.jface.SwtThreadExecutor;
import it.albertus.jface.TextConsole;
import it.albertus.router.client.engine.Protocol;
import it.albertus.router.client.engine.RouterLoggerClientConfiguration;
import it.albertus.router.client.engine.RouterLoggerStatus;
import it.albertus.router.client.engine.Status;
import it.albertus.router.client.gui.listener.CloseListener;
import it.albertus.router.client.gui.listener.PreferencesListener;
import it.albertus.router.client.http.HttpPollingThread;
import it.albertus.router.client.mqtt.RouterLoggerClientMqttClient;
import it.albertus.router.client.resources.Messages;
import it.albertus.router.client.util.Logger;
import it.albertus.util.Configuration;
import it.albertus.util.Configured;
import it.albertus.util.Version;

public class RouterLoggerGui extends ApplicationWindow {

	public static final String CFG_KEY_GUI_CLIPBOARD_MAX_CHARS = "gui.clipboard.max.chars";
	public static final int GUI_CLIPBOARD_MAX_CHARS = 100000;

	//	public static final SSLSocketFactory defaultSSLSocketFactory = HttpsURLConnection.getDefaultSSLSocketFactory();

	private static final float SASH_MAGNIFICATION_FACTOR = 1.5f;

	private final Configuration configuration = RouterLoggerClientConfiguration.getInstance();
	private final RouterLoggerClientMqttClient mqttClient = RouterLoggerClientMqttClient.getInstance();

	private final ThresholdsManager thresholdsManager;

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

	public static void run() {
		Display.setAppName(Messages.get("msg.application.name"));
		Display.setAppVersion(Version.getInstance().getNumber());
		final Display display = Display.getDefault();
		final RouterLoggerGui gui = new RouterLoggerGui(display);
		gui.open();
		final Shell shell = gui.getShell();
		try {
			Protocol.valueOf(gui.configuration.getString("client.protocol"));
			gui.connect();
		}
		catch (final RuntimeException re) {
			new PreferencesListener(gui).widgetSelected(null);
		}
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				Display.getCurrent().sleep();
			}
		}
		gui.release();
		display.dispose();
	}

	private class MqttConnectionThread extends Thread {

		private volatile boolean exit = false;

		private MqttConnectionThread() {
			super("mqttConnectionThread");
			this.setDaemon(true);
		}

		@Override
		public void interrupt() {
			exit = true;
			super.interrupt();
		}

		@Override
		public void run() {
			while (!exit) {
				mqttClient.subscribeStatus();
				if (mqttClient.getClient() == null) {
					try {
						Thread.sleep(1000 * configuration.getInt("mqtt.connect.retry.interval.secs", Defaults.MQTT_CONNECT_RETRY_INTERVAL_SECS)); // Wait between retries
					}
					catch (final InterruptedException ie) {
						break;
					}
					continue; // Retry
				}
				mqttClient.subscribeData();
				mqttClient.subscribeThresholds();
				break;
			}
		}
	}

	private class ConnectThread extends Thread {
		private ConnectThread() {
			super("connectThread");
		}

		@Override
		public void run() {
			final String protocol = configuration.getString("client.protocol").trim();
			if (protocol.equalsIgnoreCase(Protocol.MQTT.toString())) { // MQTT
				mqttClient.init(RouterLoggerGui.this);
				mqttConnectionThread = new MqttConnectionThread();
				mqttConnectionThread.start();
			}
			else if (protocol.toUpperCase().startsWith(Protocol.HTTP.toString().toUpperCase())) { // HTTP
				httpPollingThread = new HttpPollingThread(RouterLoggerGui.this);
				httpPollingThread.start();
			}
			else {
				Logger.getInstance().log(Messages.get("err.invalid.protocol", protocol));
			}
		}
	}

	private class ReleaseThread extends Thread {
		private ReleaseThread() {
			super("releaseThread");
		}

		@Override
		public void run() {
			mqttClient.disconnect();
			if (mqttConnectionThread != null) {
				mqttConnectionThread.interrupt();
				try {
					mqttConnectionThread.join();
				}
				catch (final InterruptedException ie) {/* Ignore */}
			}
			if (httpPollingThread != null) {
				httpPollingThread.interrupt();
				try {
					httpPollingThread.join();
				}
				catch (final InterruptedException ie) {/* Ignore */}
			}
			printGoodbye();
		}
	}

	public RouterLoggerGui(final Display display) {
		super(null);
		thresholdsManager = new ThresholdsManager(this);
	}

	private void connect() {
		printWelcome();
		new ConnectThread().start();
	}

	private void release() {
		new ReleaseThread().start();
	}

	@Override
	protected void configureShell(final Shell shell) {
		super.configureShell(shell);

		// Fix invisible (transparent) shell bug with some Linux distibutions
		if (!Util.isGtk() && configuration.getBoolean("gui.start.minimized", Defaults.GUI_START_MINIMIZED)) {
			shell.setMinimized(true);
		}

		shell.setText(Messages.get("lbl.window.title"));
		shell.setImages(Images.MAIN_ICONS);
	}

	@Override
	public int open() {
		final int code = super.open();

		// Fix invisible (transparent) shell bug with some Linux distibutions
		if (Util.isGtk() && configuration.getBoolean("gui.start.minimized", Defaults.GUI_START_MINIMIZED)) {
			getShell().setMinimized(true);
		}

		return code;
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

	@Override
	protected void initializeBounds() {/* Do not pack the shell */}

	protected void printWelcome() {
		System.out.println("  ____             _            _                                   ____ _ _            _");
		System.out.println(" |  _ \\ ___  _   _| |_ ___ _ __| |    ___   __ _  __ _  ___ _ __   / ___| (_) ___ _ __ | |_");
		System.out.println(" | |_) / _ \\| | | | __/ _ \\ '__| |   / _ \\ / _` |/ _` |/ _ \\ '__| | |   | | |/ _ \\ '_ \\| __|");
		System.out.println(" |  _ < (_) | |_| | ||  __/ |  | |__| (_) | (_| | (_| |  __/ |    | |___| | |  __/ | | | |_");
		System.out.println(" |_| \\_\\___/ \\__,_|\\__\\___|_|  |_____\\___/ \\__, |\\__, |\\___|_|     \\____|_|_|\\___|_| |_|\\__|");
		System.out.println("                                           |___/ |___/");
		final Version version = Version.getInstance();
		System.out.println(Messages.get("msg.welcome", Messages.get("msg.application.name"), Messages.get("msg.version", version.getNumber(), version.getDate()), Messages.get("msg.website")));
		System.out.println();
		System.out.println(Messages.get("msg.startup.date", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date())));
		if (configuration.getBoolean("console.show.configuration", Defaults.CONSOLE_SHOW_CONFIGURATION)) {
			System.out.println(Messages.get("msg.settings", configuration));
		}
		System.out.println();
	}

	@Override
	protected Layout getLayout() {
		return new GridLayout();
	}

	@Override
	protected void createTrimWidgets(final Shell shell) {/* Not needed */}

	protected void printGoodbye() {
		System.out.println(Messages.get("msg.bye"));
	}

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
				if (Status.WARNING.equals(currentStatus.getStatus())) {
					trayIcon.setShowToolTip(true);
				}
			}
		}
	}

	public void restart() {
		// Disable "Restart..." menu item...
		menuBar.getFileRestartItem().setEnabled(false);

		new Thread("resetThread") {
			@Override
			public void run() {
				final Thread releaseThread = new ReleaseThread();
				releaseThread.start();
				try {
					releaseThread.join();
				}
				catch (final InterruptedException ie) {/* Ignore */}

				configuration.reload();
				new SwtThreadExecutor(getShell()) {
					@Override
					protected void run() {
						dataTable.reset();
						textConsole.clear();
					}
				}.start();

				connect();

				// Enable "Restart..." menu item...
				new SwtThreadExecutor(getShell()) {
					@Override
					protected void run() {
						menuBar.getFileRestartItem().setEnabled(true);
					}
				}.start();
			}
		}.start();
	}

	public ThresholdsManager getThresholdsManager() {
		return thresholdsManager;
	}

}

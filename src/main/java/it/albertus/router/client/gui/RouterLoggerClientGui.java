package it.albertus.router.client.gui;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IStatus;
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

import it.albertus.jface.EnhancedErrorDialog;
import it.albertus.jface.SwtThreadExecutor;
import it.albertus.jface.console.StyledTextConsole;
import it.albertus.router.client.RouterLoggerClient;
import it.albertus.router.client.RouterLoggerClient.InitializationException;
import it.albertus.router.client.engine.Protocol;
import it.albertus.router.client.engine.RouterLoggerStatus;
import it.albertus.router.client.engine.Status;
import it.albertus.router.client.gui.listener.CloseListener;
import it.albertus.router.client.gui.listener.PreferencesListener;
import it.albertus.router.client.http.HttpPollingThread;
import it.albertus.router.client.mqtt.RouterLoggerClientMqttClient;
import it.albertus.router.client.resources.Messages;
import it.albertus.util.Configuration;
import it.albertus.util.Configured;
import it.albertus.util.Version;
import it.albertus.util.logging.LoggerFactory;

public class RouterLoggerClientGui extends ApplicationWindow {

	private static final Logger logger = LoggerFactory.getLogger(RouterLoggerClientGui.class);

	public static final String CFG_KEY_GUI_CLIPBOARD_MAX_CHARS = "gui.clipboard.max.chars";
	public static final int GUI_CLIPBOARD_MAX_CHARS = 100000;

	private static final float SASH_MAGNIFICATION_FACTOR = 1.5f;

	private static final Configuration configuration = RouterLoggerClient.getConfiguration();

	private final RouterLoggerClientMqttClient mqttClient = RouterLoggerClientMqttClient.getInstance();

	private final ThresholdsManager thresholdsManager;

	private TrayIcon trayIcon;
	private MenuBar menuBar;
	private SashForm sashForm;
	private DataTable dataTable;
	private StyledTextConsole console;

	private RouterLoggerStatus currentStatus;
	private RouterLoggerStatus previousStatus;

	private volatile Thread mqttConnectionThread;
	private volatile Thread httpPollingThread;

	public RouterLoggerClientGui() {
		super(null);
		thresholdsManager = new ThresholdsManager(this);
	}

	public static class Defaults {
		public static final boolean GUI_START_MINIMIZED = false;
		public static final int GUI_CLIPBOARD_MAX_CHARS = 100000;
		public static final boolean CONSOLE_SHOW_CONFIGURATION = false;
		public static final int MQTT_CONNECT_RETRY_INTERVAL_SECS = 5;

		private Defaults() {
			throw new IllegalAccessError("Constants class");
		}
	}

	public static void run(final InitializationException ie) {
		Display.setAppName(Messages.get("msg.application.name"));
		Display.setAppVersion(Version.getInstance().getNumber());
		final Display display = Display.getDefault();

		if (ie != null) { // Display error dialog and exit.
			EnhancedErrorDialog.openError(null, Messages.get("lbl.window.title"), ie.getLocalizedMessage() != null ? ie.getLocalizedMessage() : ie.getMessage(), IStatus.ERROR, ie.getCause() != null ? ie.getCause() : ie, Images.getMainIcons());
		}
		else {
			final RouterLoggerClientGui gui = new RouterLoggerClientGui();
			gui.open();
			final Shell shell = gui.getShell();
			try {
				Protocol.valueOf(configuration.getString("client.protocol"));
				gui.connect();
			}
			catch (final RuntimeException re) {
				logger.log(Level.FINE, re.toString(), re);
				new PreferencesListener(gui).widgetSelected(null);
			}
			while (!shell.isDisposed()) {
				if (!display.isDisposed() && !display.readAndDispatch()) {
					display.sleep();
				}
			}
			gui.release();
		}
		display.dispose();
	}

	private class MqttConnectionThread extends Thread {

		private MqttConnectionThread() {
			super("mqttConnectionThread");
			this.setDaemon(true);
		}

		@Override
		public void run() {
			while (!Thread.interrupted()) {
				mqttClient.subscribeStatus();
				if (mqttClient.getClient() == null) {
					try {
						TimeUnit.SECONDS.sleep(configuration.getInt("mqtt.connect.retry.interval.secs", Defaults.MQTT_CONNECT_RETRY_INTERVAL_SECS)); // Wait between retries
					}
					catch (final InterruptedException ie) {
						logger.log(Level.FINER, ie.toString(), ie);
						interrupt();
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
				mqttClient.init(RouterLoggerClientGui.this);
				mqttConnectionThread = new MqttConnectionThread();
				mqttConnectionThread.start();
			}
			else if (protocol.toUpperCase().startsWith(Protocol.HTTP.toString().toUpperCase())) { // HTTP
				httpPollingThread = new HttpPollingThread(RouterLoggerClientGui.this);
				httpPollingThread.start();
			}
			else {
				logger.info(Messages.get("err.invalid.protocol", protocol));
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
				catch (final InterruptedException ie) {
					logger.log(Level.FINER, ie.toString(), ie);
					interrupt();
				}
			}
			if (httpPollingThread != null) {
				httpPollingThread.interrupt();
				try {
					httpPollingThread.join();
				}
				catch (final InterruptedException ie) {
					logger.log(Level.FINER, ie.toString(), ie);
					interrupt();
				}
			}
			printGoodbye();
		}
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
		shell.setImages(Images.getMainIcons());
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

		console = new StyledTextConsole(sashForm, new GridData(SWT.FILL, SWT.FILL, true, true), true);
		console.setLimit(new Configured<Integer>() {
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
		System.out.println(" ____             _            _                                   ____ _ _            _");
		System.out.println("|  _ \\ ___  _   _| |_ ___ _ __| |    ___   __ _  __ _  ___ _ __   / ___| (_) ___ _ __ | |_");
		System.out.println("| |_) / _ \\| | | | __/ _ \\ '__| |   / _ \\ / _` |/ _` |/ _ \\ '__| | |   | | |/ _ \\ '_ \\| __|");
		System.out.println("|  _ < (_) | |_| | ||  __/ |  | |__| (_) | (_| | (_| |  __/ |    | |___| | |  __/ | | | |_");
		System.out.println("|_| \\_\\___/ \\__,_|\\__\\___|_|  |_____\\___/ \\__, |\\__, |\\___|_|     \\____|_|_|\\___|_| |_|\\__|");
		System.out.println("                                          |___/ |___/");
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

	public StyledTextConsole getConsole() {
		return console;
	}

	public boolean canCopyConsole() {
		return console.hasSelection() && (console.getScrollable().isFocusControl() || !dataTable.canCopy());
	}

	public boolean canSelectAllConsole() {
		return !console.isEmpty() && (console.getScrollable().isFocusControl() || !dataTable.canSelectAll());
	}

	public boolean canClearConsole() {
		return !console.isEmpty();
	}

	public RouterLoggerStatus getCurrentStatus() {
		return currentStatus;
	}

	public RouterLoggerStatus getPreviousStatus() {
		return previousStatus;
	}

	public void setStatus(final RouterLoggerStatus newStatus) {
		if (currentStatus == null || currentStatus.getStatus() == null || !currentStatus.getStatus().equals(newStatus.getStatus())) {
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

	public void reconnectAfterConnectionLoss() {
		new Thread("resetThread") {
			@Override
			public void run() {
				// Disconnect
				mqttClient.disconnect();
				if (mqttConnectionThread != null) {
					mqttConnectionThread.interrupt();
					try {
						mqttConnectionThread.join();
					}
					catch (final InterruptedException ie) {
						logger.log(Level.FINER, ie.toString(), ie);
						interrupt();
					}
				}

				// Reconnect
				mqttClient.init(RouterLoggerClientGui.this);
				mqttConnectionThread = new MqttConnectionThread();
				mqttConnectionThread.start();
			}
		}.start();
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
				catch (final InterruptedException ie) {
					logger.log(Level.FINE, ie.toString(), ie);
					interrupt();
				}

				try {
					configuration.reload();
				}
				catch (final IOException ioe) {
					logger.log(Level.SEVERE, ioe.toString(), ioe);
				}
				new SwtThreadExecutor(getShell()) {
					@Override
					protected void run() {
						dataTable.reset();
						if (!logger.isLoggable(Level.FINE)) {
							console.clear();
						}
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

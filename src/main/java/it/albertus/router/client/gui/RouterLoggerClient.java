package it.albertus.router.client.gui;

import it.albertus.jface.TextConsole;
import it.albertus.router.client.RouterLoggerConfiguration;
import it.albertus.router.client.RouterLoggerStatus;
import it.albertus.router.client.gui.listener.CloseListener;
import it.albertus.router.client.mqtt.RouterLoggerMqttClient;
import it.albertus.router.client.resources.Resources;
import it.albertus.util.Configuration;
import it.albertus.util.Configured;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

public class RouterLoggerClient extends ApplicationWindow implements RouterLoggerGui {

	private static final float SASH_MAGNIFICATION_FACTOR = 1.5f;

	public static void main(String[] args) {
		final Display display = Display.getDefault();
		new RouterLoggerClient();
		display.dispose();
	}

	private final Configuration configuration = RouterLoggerConfiguration.getInstance();
	private final RouterLoggerMqttClient mqttClient = RouterLoggerMqttClient.getInstance();

	private TrayIcon trayIcon;
	private MenuBar menuBar;
	private SashForm sashForm;
	private DataTable dataTable;
	private TextConsole textConsole;

	private RouterLoggerStatus currentStatus = RouterLoggerStatus.STARTING;
	private RouterLoggerStatus previousStatus;

	public interface Defaults {
		boolean GUI_START_MINIMIZED = false;
		int GUI_CLIPBOARD_MAX_CHARS = 100000;
	}

	public RouterLoggerClient() {
		super(null);
		open();
		mqttClient.init(this);
		new Thread("MQTT Client Start") {
			@Override
			public void run() {
				mqttClient.subscribeStatus();
				mqttClient.subscribeData();
				mqttClient.subscribeThresholds();
			};
		}.start();

		final Display display = Display.getCurrent();
		final Shell shell = getShell();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				Display.getCurrent().sleep();
			}
		}
		mqttClient.disconnect();
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setMinimized(configuration.getBoolean("gui.start.minimized", Defaults.GUI_START_MINIMIZED));
		shell.setText(Resources.get("lbl.window.title"));
		shell.setImages(Images.MAIN_ICONS);
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

		parent.addListener(SWT.Close, new CloseListener(this));
		return parent;
	}

	@Override
	protected Layout getLayout() {
		return new GridLayout();
	}

	@Override
	protected void createTrimWidgets(final Shell shell) {/* Not needed */}

	@Override
	public TrayIcon getTrayIcon() {
		return trayIcon;
	}

	@Override
	public MenuBar getMenuBar() {
		return menuBar;
	}

	public SashForm getSashForm() {
		return sashForm;
	}

	@Override
	public DataTable getDataTable() {
		return dataTable;
	}

	@Override
	public TextConsole getTextConsole() {
		return textConsole;
	}

	@Override
	public boolean canCopyConsole() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean canSelectAllConsole() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean canClearConsole() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public RouterLoggerStatus getCurrentStatus() {
		return currentStatus;
	}

	@Override
	public RouterLoggerStatus getPreviousStatus() {
		return previousStatus;
	}

	@Override
	public void setStatus(final RouterLoggerStatus newStatus) {
		this.previousStatus = this.currentStatus;
		this.currentStatus = newStatus;
		if (trayIcon != null) {
			trayIcon.updateTrayItem(newStatus);
		}
	}

}

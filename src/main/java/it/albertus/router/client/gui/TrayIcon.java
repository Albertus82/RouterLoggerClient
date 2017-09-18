package it.albertus.router.client.gui;

import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;

import it.albertus.jface.listener.TrayRestoreListener;
import it.albertus.router.client.engine.RouterData;
import it.albertus.router.client.engine.RouterLoggerClientConfig;
import it.albertus.router.client.engine.Status;
import it.albertus.router.client.gui.listener.CloseListener;
import it.albertus.router.client.resources.Messages;
import it.albertus.util.NewLine;
import it.albertus.util.logging.LoggerFactory;

public class TrayIcon {

	private static final Logger logger = LoggerFactory.getLogger(TrayIcon.class);

	private static final RouterLoggerClientConfig configuration = RouterLoggerClientConfig.getInstance();

	public static class Defaults {
		public static final boolean GUI_MINIMIZE_TRAY = true;
		public static final boolean GUI_TRAY_TOOLTIP = true;

		private Defaults() {
			throw new IllegalAccessError("Constants class");
		}
	}

	private final RouterLoggerClientGui gui;

	private Tray tray;
	private TrayItem trayItem;
	private ToolTip toolTip;

	private Menu trayMenu;
	private MenuItem showMenuItem;
	private MenuItem exitMenuItem;

	/* To be accessed only from this class */
	private String toolTipText;
	private Image trayIcon;

	private boolean showToolTip;

	protected TrayIcon(final RouterLoggerClientGui gui) {
		this.gui = gui;
		gui.getShell().addShellListener(new ShellAdapter() {
			@Override
			public void shellIconified(final ShellEvent se) {
				if (configuration.getBoolean("gui.minimize.tray", Defaults.GUI_MINIMIZE_TRAY)) {
					iconify();
				}
			}
		});
	}

	public void setShowToolTip(boolean showToolTip) {
		this.showToolTip = showToolTip;
	}

	private Image getTrayIcon(final Status status) {
		if (status != null) {
			switch (status) {
			case STARTING:
			case CONNECTING:
			case DISCONNECTED:
			case CLOSED:
				return Images.TRAY_ICON_INACTIVE;
			case RECONNECTING:
				return Images.TRAY_ICON_INACTIVE_CLOCK;
			case INFO:
			case WARNING:
				return Images.TRAY_ICON_ACTIVE_WARNING;
			case AUTHENTICATING:
				return Images.TRAY_ICON_ACTIVE_LOCK;
			case ERROR:
				return Images.TRAY_ICON_INACTIVE_ERROR;
			default:
				return Images.TRAY_ICON_ACTIVE;
			}
		}
		else {
			return Images.TRAY_ICON_ACTIVE;
		}
	}

	private void iconify() {
		if (tray == null || trayItem == null || trayItem.isDisposed()) {
			/* Inizializzazione */
			try {
				tray = gui.getShell().getDisplay().getSystemTray();

				if (tray != null) {
					trayItem = new TrayItem(tray, SWT.NONE);
					trayIcon = getTrayIcon(gui.getCurrentStatus() != null ? gui.getCurrentStatus().getStatus() : null);
					trayItem.setImage(trayIcon);
					toolTipText = getBaseToolTipText(gui.getCurrentStatus() != null ? gui.getCurrentStatus().getStatus() : null);
					trayItem.setToolTipText(toolTipText);
					final TrayRestoreListener trayRestoreListener = new TrayRestoreListener(gui.getShell(), trayItem);

					toolTip = new ToolTip(gui.getShell(), SWT.BALLOON | SWT.ICON_WARNING);
					toolTip.setText(Messages.get("lbl.tray.tooltip.thresholds.reached"));
					toolTip.setVisible(false);
					toolTip.setAutoHide(true);
					toolTip.addSelectionListener(trayRestoreListener);
					trayItem.setToolTip(toolTip);

					trayMenu = new Menu(gui.getShell(), SWT.POP_UP);
					showMenuItem = new MenuItem(trayMenu, SWT.PUSH);
					showMenuItem.setText(Messages.get("lbl.tray.show"));
					showMenuItem.addSelectionListener(trayRestoreListener);
					trayMenu.setDefaultItem(showMenuItem);

					new MenuItem(trayMenu, SWT.SEPARATOR);

					exitMenuItem = new MenuItem(trayMenu, SWT.PUSH);
					exitMenuItem.setText(Messages.get("lbl.tray.close"));
					exitMenuItem.addSelectionListener(new CloseListener(gui));
					trayItem.addMenuDetectListener(e -> trayMenu.setVisible(true));

					trayItem.addSelectionListener(trayRestoreListener);
					if (!Util.isLinux()) {
						gui.getShell().addShellListener(trayRestoreListener);
					}
				}
			}
			catch (final Exception e) {
				logger.log(Level.SEVERE, e.toString(), e);
			}
		}

		if (tray != null && !tray.isDisposed() && trayItem != null && !trayItem.isDisposed()) {
			gui.getShell().setVisible(false);
			trayItem.setVisible(true);
			trayItem.setImage(trayIcon); // Update icon
			gui.getShell().setMinimized(false);
		}
		else {
			logger.info("Tray not available.");
		}
	}

	public void updateTrayItem(final Status status) {
		updateTrayItem(status, null);
	}

	public void updateTrayItem(final Status status, final RouterData info) {
		if (trayItem != null && !trayItem.isDisposed()) {
			final StringBuilder sb = new StringBuilder(getBaseToolTipText(status));
			if (!configuration.getGuiImportantKeys().isEmpty() && info != null && info.getData() != null && !info.getData().isEmpty()) {
				for (final String key : configuration.getGuiImportantKeys()) {
					if (info.getData().containsKey(key)) {
						sb.append(NewLine.SYSTEM_LINE_SEPARATOR).append(key).append(": ").append(info.getData().get(key));
					}
				}
			}
			final String updatedToolTipText = sb.toString();
			if (!updatedToolTipText.equals(toolTipText) || (status != null && !getTrayIcon(status).equals(trayIcon))) {
				try {
					trayItem.getDisplay().syncExec(() -> {
						if (!trayItem.isDisposed()) {
							if (!updatedToolTipText.equals(toolTipText)) {
								toolTipText = updatedToolTipText;
								trayItem.setToolTipText(toolTipText);
							}
							if (status != null && !getTrayIcon(status).equals(trayIcon)) {
								trayIcon = getTrayIcon(status);
								if (trayItem.getVisible() && gui != null && gui.getShell() != null && !gui.getShell().isDisposed() && !gui.getShell().getVisible()) {
									trayItem.setImage(trayIcon); // Only if visible!
								}
							}
						}
					});
				}
				catch (final SWTException se) {
					logger.log(Level.FINE, se.toString(), se);
				}
			}
		}
	}

	public void showBalloonToolTip(final Map<String, String> thresholdsReached) {
		if (configuration.getBoolean("gui.tray.tooltip", Defaults.GUI_TRAY_TOOLTIP) && showToolTip && thresholdsReached != null && !thresholdsReached.isEmpty() && toolTip != null && trayItem != null && gui != null && gui.getShell() != null && !gui.getShell().isDisposed() && !trayItem.isDisposed() && !toolTip.isDisposed()) {
			final StringBuilder message = new StringBuilder();
			for (final Entry<String, String> entry : thresholdsReached.entrySet()) {
				message.append(entry.getKey()).append('=').append(entry.getValue()).append(NewLine.SYSTEM_LINE_SEPARATOR);
			}

			try {
				trayItem.getDisplay().syncExec(() -> {
					if (configuration.getBoolean("gui.tray.tooltip", Defaults.GUI_TRAY_TOOLTIP) && showToolTip && toolTip != null && trayItem != null && gui != null && gui.getShell() != null && !gui.getShell().isDisposed() && !trayItem.isDisposed() && !toolTip.isDisposed() && trayItem.getVisible() && !gui.getShell().getVisible()) {
						toolTip.setMessage(message.toString().trim());
						toolTip.setVisible(true);
						showToolTip = false;
					}
				});
			}
			catch (final SWTException se) {
				logger.log(Level.FINE, se.toString(), se);
			}
		}
	}

	private String getBaseToolTipText(final Status status) {
		final StringBuilder sb = new StringBuilder(Messages.get("lbl.tray.tooltip"));
		if (status != null) {
			sb.append(" (").append(status.getDescription()).append(')');
		}
		return sb.toString();
	}

	public Tray getTray() {
		return tray;
	}

	public TrayItem getTrayItem() {
		return trayItem;
	}

	public ToolTip getToolTip() {
		return toolTip;
	}

	public Menu getTrayMenu() {
		return trayMenu;
	}

	public MenuItem getShowMenuItem() {
		return showMenuItem;
	}

	public MenuItem getExitMenuItem() {
		return exitMenuItem;
	}

}

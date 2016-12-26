package it.albertus.router.client.gui;

import java.util.Map;

import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
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
import it.albertus.router.client.engine.RouterLoggerClientConfiguration;
import it.albertus.router.client.engine.Status;
import it.albertus.router.client.engine.Threshold;
import it.albertus.router.client.gui.listener.CloseListener;
import it.albertus.router.client.resources.Messages;
import it.albertus.router.client.util.Logger;
import it.albertus.util.NewLine;

public class TrayIcon {

	public interface Defaults {
		boolean GUI_MINIMIZE_TRAY = true;
		boolean GUI_TRAY_TOOLTIP = true;
	}

	private final RouterLoggerClientConfiguration configuration = RouterLoggerClientConfiguration.getInstance();
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

	public void setShowToolTip(boolean showToolTip) {
		this.showToolTip = showToolTip;
	}

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
					toolTip.addListener(SWT.Selection, trayRestoreListener);
					trayItem.setToolTip(toolTip);

					trayMenu = new Menu(gui.getShell(), SWT.POP_UP);
					showMenuItem = new MenuItem(trayMenu, SWT.PUSH);
					showMenuItem.setText(Messages.get("lbl.tray.show"));
					showMenuItem.addListener(SWT.Selection, trayRestoreListener);
					trayMenu.setDefaultItem(showMenuItem);

					new MenuItem(trayMenu, SWT.SEPARATOR);

					exitMenuItem = new MenuItem(trayMenu, SWT.PUSH);
					exitMenuItem.setText(Messages.get("lbl.tray.close"));
					exitMenuItem.addSelectionListener(new CloseListener(gui));
					trayItem.addMenuDetectListener(new MenuDetectListener() {
						@Override
						public void menuDetected(MenuDetectEvent e) {
							trayMenu.setVisible(true);
						}
					});

					trayItem.addListener(SWT.Selection, trayRestoreListener);
					if (!Util.isLinux()) {
						gui.getShell().addShellListener(trayRestoreListener);
					}
				}
			}
			catch (final Exception e) {
				Logger.getInstance().log(e);
			}
		}

		if (tray != null && !tray.isDisposed() && trayItem != null && !trayItem.isDisposed()) {
			gui.getShell().setVisible(false);
			trayItem.setVisible(true);
			trayItem.setImage(trayIcon); // Update icon
			gui.getShell().setMinimized(false);
		}
		else {
			Logger.getInstance().log("Tray not available.");
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
					trayItem.getDisplay().syncExec(new Runnable() {
						@Override
						public void run() {
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
						}
					});
				}
				catch (SWTException se) {}
			}
		}
	}

	public void showBalloonToolTip(final Map<Threshold, String> thresholdsReached) {
		if (configuration.getBoolean("gui.tray.tooltip", Defaults.GUI_TRAY_TOOLTIP) && showToolTip && thresholdsReached != null && !thresholdsReached.isEmpty() && toolTip != null && trayItem != null && gui != null && gui.getShell() != null && !gui.getShell().isDisposed() && !trayItem.isDisposed() && !toolTip.isDisposed()) {
			final StringBuilder message = new StringBuilder();
			for (final Threshold threshold : thresholdsReached.keySet()) {
				message.append(threshold.getKey()).append('=').append(thresholdsReached.get(threshold)).append(NewLine.SYSTEM_LINE_SEPARATOR);
			}

			try {
				trayItem.getDisplay().syncExec(new Runnable() {
					@Override
					public void run() {
						if (configuration.getBoolean("gui.tray.tooltip", Defaults.GUI_TRAY_TOOLTIP) && showToolTip && toolTip != null && trayItem != null && gui != null && gui.getShell() != null && !gui.getShell().isDisposed() && !trayItem.isDisposed() && !toolTip.isDisposed() && trayItem.getVisible() && !gui.getShell().getVisible()) {
							toolTip.setMessage(message.toString().trim());
							toolTip.setVisible(true);
							showToolTip = false;
						}
					}
				});
			}
			catch (SWTException se) {}
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

package it.albertus.router.client.gui;

import it.albertus.jface.TextConsole;
import it.albertus.router.client.RouterLoggerStatus;

import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.widgets.Shell;

public interface RouterLoggerGui extends IShellProvider {

	String CFG_KEY_GUI_CLIPBOARD_MAX_CHARS = null;
	int GUI_CLIPBOARD_MAX_CHARS = 100000;

	@Override
	public Shell getShell();

	public MenuBar getMenuBar();

	public DataTable getDataTable();

	public TextConsole getTextConsole();

	public TrayIcon getTrayIcon();

	public boolean canCopyConsole();

	public boolean canSelectAllConsole();

	public boolean canClearConsole();

	public RouterLoggerStatus getCurrentStatus();

	public RouterLoggerStatus getPreviousStatus();

	public void setStatus(RouterLoggerStatus newStatus);

}

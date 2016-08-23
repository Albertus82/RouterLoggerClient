package it.albertus.router.client.gui;

import it.albertus.jface.SwtUtils;
import it.albertus.router.client.gui.listener.AboutSelectionListener;
import it.albertus.router.client.gui.listener.ClearConsoleSelectionListener;
import it.albertus.router.client.gui.listener.ClearDataTableSelectionListener;
import it.albertus.router.client.gui.listener.CloseListener;
import it.albertus.router.client.gui.listener.CopyMenuBarSelectionListener;
import it.albertus.router.client.gui.listener.DeleteDataTableSelectionListener;
import it.albertus.router.client.gui.listener.EditClearSubMenuArmListener;
import it.albertus.router.client.gui.listener.EditMenuBarArmListener;
import it.albertus.router.client.gui.listener.PreferencesSelectionListener;
import it.albertus.router.client.gui.listener.RestartSelectionListener;
import it.albertus.router.client.gui.listener.SelectAllMenuBarSelectionListener;
import it.albertus.router.client.resources.Resources;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 * Solo i <tt>MenuItem</tt> che fanno parte di una barra dei men&ugrave; con
 * stile <tt>SWT.BAR</tt> hanno gli acceleratori funzionanti; negli altri casi
 * (ad es. <tt>SWT.POP_UP</tt>), bench&eacute; vengano visualizzate le
 * combinazioni di tasti, gli acceleratori non funzioneranno e le relative
 * combinazioni di tasti saranno ignorate.
 */
public class MenuBar {

	private final Menu bar;

	private final Menu fileMenu;
	private final MenuItem fileMenuHeader;
	private final MenuItem fileRestartItem;
	private final MenuItem fileExitItem;

	private final Menu editMenu;
	private final MenuItem editMenuHeader;
	private final MenuItem editCopyMenuItem;
	private final MenuItem editDeleteMenuItem;
	private final MenuItem editSelectAllMenuItem;

	private final Menu editClearSubMenu;
	private final MenuItem editClearSubMenuItem;
	private final MenuItem editClearDataTableMenuItem;
	private final MenuItem editClearConsoleMenuItem;

	//	private final Menu connectionMenu;
	//	private final MenuItem connectionMenuHeader;
	//	private final MenuItem connectionConnectItem;
	//	private final MenuItem connectionDisconnectItem;

	private final Menu toolsMenu;
	private final MenuItem toolsMenuHeader;
	private final MenuItem toolsPreferencesMenuItem;

	private final Menu helpMenu;
	private final MenuItem helpMenuHeader;
	private final MenuItem helpAboutItem;

	protected MenuBar(final RouterLoggerGui gui) {
		bar = new Menu(gui.getShell(), SWT.BAR); // Barra

		/* File */
		fileMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		fileMenuHeader = new MenuItem(bar, SWT.CASCADE);
		fileMenuHeader.setText(Resources.get("lbl.menu.header.file"));
		fileMenuHeader.setMenu(fileMenu);

		fileRestartItem = new MenuItem(fileMenu, SWT.PUSH);
		fileRestartItem.setText(Resources.get("lbl.menu.item.restart"));
		fileRestartItem.addSelectionListener(new RestartSelectionListener(gui));

		new MenuItem(fileMenu, SWT.SEPARATOR);

		fileExitItem = new MenuItem(fileMenu, SWT.PUSH);
		fileExitItem.setText(Resources.get("lbl.menu.item.exit"));
		fileExitItem.addSelectionListener(new CloseListener(gui));

		/* Edit */
		editMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		editMenuHeader = new MenuItem(bar, SWT.CASCADE);
		editMenuHeader.setText(Resources.get("lbl.menu.header.edit"));
		editMenuHeader.setMenu(editMenu);
		editMenuHeader.addArmListener(new EditMenuBarArmListener(gui));

		editCopyMenuItem = new MenuItem(editMenu, SWT.PUSH);
		editCopyMenuItem.setText(Resources.get("lbl.menu.item.copy") + SwtUtils.getMod1ShortcutLabel(SwtUtils.KEY_COPY));
		editCopyMenuItem.addSelectionListener(new CopyMenuBarSelectionListener(gui));
		editCopyMenuItem.setAccelerator(SWT.MOD1 | SwtUtils.KEY_COPY); // Vero!

		editDeleteMenuItem = new MenuItem(editMenu, SWT.PUSH);
		editDeleteMenuItem.setText(Resources.get("lbl.menu.item.delete") + SwtUtils.getShortcutLabel(Resources.get("lbl.menu.item.delete.key")));
		editDeleteMenuItem.addSelectionListener(new DeleteDataTableSelectionListener(gui));
		editDeleteMenuItem.setAccelerator(SwtUtils.KEY_DELETE); // Vero!

		new MenuItem(editMenu, SWT.SEPARATOR);

		editSelectAllMenuItem = new MenuItem(editMenu, SWT.PUSH);
		editSelectAllMenuItem.setText(Resources.get("lbl.menu.item.select.all") + SwtUtils.getMod1ShortcutLabel(SwtUtils.KEY_SELECT_ALL));
		editSelectAllMenuItem.addSelectionListener(new SelectAllMenuBarSelectionListener(gui));
		editSelectAllMenuItem.setAccelerator(SWT.MOD1 | SwtUtils.KEY_SELECT_ALL); // Vero!

		new MenuItem(editMenu, SWT.SEPARATOR);

		editClearSubMenuItem = new MenuItem(editMenu, SWT.CASCADE);
		editClearSubMenuItem.setText(Resources.get("lbl.menu.item.clear"));

		editClearSubMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		editClearSubMenuItem.setMenu(editClearSubMenu);
		editClearSubMenuItem.addArmListener(new EditClearSubMenuArmListener(gui));

		editClearDataTableMenuItem = new MenuItem(editClearSubMenu, SWT.PUSH);
		editClearDataTableMenuItem.setText(Resources.get("lbl.menu.item.clear.table"));
		editClearDataTableMenuItem.addSelectionListener(new ClearDataTableSelectionListener(gui));

		editClearConsoleMenuItem = new MenuItem(editClearSubMenu, SWT.PUSH);
		editClearConsoleMenuItem.setText(Resources.get("lbl.menu.item.clear.console"));
		editClearConsoleMenuItem.addSelectionListener(new ClearConsoleSelectionListener(gui));

		/* Connection */
		//		connectionMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		//		connectionMenuHeader = new MenuItem(bar, SWT.CASCADE);
		//		connectionMenuHeader.setText(Resources.get("lbl.menu.header.connection"));
		//		connectionMenuHeader.setMenu(connectionMenu);
		//		connectionMenuHeader.addArmListener(new ConnectionMenuBarArmListener(gui));

		//		connectionConnectItem = new MenuItem(connectionMenu, SWT.PUSH);
		//		connectionConnectItem.setText(Resources.get("lbl.menu.item.connect"));
		//		connectionConnectItem.addSelectionListener(new ConnectSelectionListener(gui));

		//		new MenuItem(connectionMenu, SWT.SEPARATOR);

		//		connectionDisconnectItem = new MenuItem(connectionMenu, SWT.PUSH);
		//		connectionDisconnectItem.setText(Resources.get("lbl.menu.item.disconnect"));
		//		connectionDisconnectItem.addSelectionListener(new DisconnectSelectionListener(gui));

		/* Tools */
		toolsMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		toolsMenuHeader = new MenuItem(bar, SWT.CASCADE);
		toolsMenuHeader.setText(Resources.get("lbl.menu.header.tools"));
		toolsMenuHeader.setMenu(toolsMenu);

		toolsPreferencesMenuItem = new MenuItem(toolsMenu, SWT.PUSH);
		toolsPreferencesMenuItem.setText(Resources.get("lbl.menu.item.preferences"));
		toolsPreferencesMenuItem.addSelectionListener(new PreferencesSelectionListener(gui));

		/* Help */
		helpMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		helpMenuHeader = new MenuItem(bar, SWT.CASCADE);
		helpMenuHeader.setText(Resources.get("lbl.menu.header.help"));
		helpMenuHeader.setMenu(helpMenu);

		helpAboutItem = new MenuItem(helpMenu, SWT.PUSH);
		helpAboutItem.setText(Resources.get("lbl.menu.item.about"));
		helpAboutItem.addSelectionListener(new AboutSelectionListener(gui));

		gui.getShell().setMenuBar(bar);
	}

	public void updateTexts() {
		fileMenuHeader.setText(Resources.get("lbl.menu.header.file"));
		fileRestartItem.setText(Resources.get("lbl.menu.item.restart"));
		fileExitItem.setText(Resources.get("lbl.menu.item.exit"));
		editMenuHeader.setText(Resources.get("lbl.menu.header.edit"));
		editCopyMenuItem.setText(Resources.get("lbl.menu.item.copy") + SwtUtils.getMod1ShortcutLabel(SwtUtils.KEY_COPY));
		editDeleteMenuItem.setText(Resources.get("lbl.menu.item.delete") + SwtUtils.getShortcutLabel(Resources.get("lbl.menu.item.delete.key")));
		editSelectAllMenuItem.setText(Resources.get("lbl.menu.item.select.all") + SwtUtils.getMod1ShortcutLabel(SwtUtils.KEY_SELECT_ALL));
		editClearSubMenuItem.setText(Resources.get("lbl.menu.item.clear"));
		editClearDataTableMenuItem.setText(Resources.get("lbl.menu.item.clear.table"));
		editClearConsoleMenuItem.setText(Resources.get("lbl.menu.item.clear.console"));
		//		connectionMenuHeader.setText(Resources.get("lbl.menu.header.connection"));
		//		connectionConnectItem.setText(Resources.get("lbl.menu.item.connect"));
		//		connectionDisconnectItem.setText(Resources.get("lbl.menu.item.disconnect"));
		toolsMenuHeader.setText(Resources.get("lbl.menu.header.tools"));
		toolsPreferencesMenuItem.setText(Resources.get("lbl.menu.item.preferences"));
		helpMenuHeader.setText(Resources.get("lbl.menu.header.help"));
		helpAboutItem.setText(Resources.get("lbl.menu.item.about"));
	}

	public Menu getBar() {
		return bar;
	}

	public Menu getFileMenu() {
		return fileMenu;
	}

	public MenuItem getFileMenuHeader() {
		return fileMenuHeader;
	}

	public MenuItem getFileRestartItem() {
		return fileRestartItem;
	}

	public MenuItem getFileExitItem() {
		return fileExitItem;
	}

	public Menu getEditMenu() {
		return editMenu;
	}

	public MenuItem getEditMenuHeader() {
		return editMenuHeader;
	}

	public MenuItem getEditCopyMenuItem() {
		return editCopyMenuItem;
	}

	public MenuItem getEditDeleteMenuItem() {
		return editDeleteMenuItem;
	}

	public MenuItem getEditSelectAllMenuItem() {
		return editSelectAllMenuItem;
	}

	public Menu getEditClearSubMenu() {
		return editClearSubMenu;
	}

	public MenuItem getEditClearSubMenuItem() {
		return editClearSubMenuItem;
	}

	public MenuItem getEditClearDataTableMenuItem() {
		return editClearDataTableMenuItem;
	}

	public MenuItem getEditClearConsoleMenuItem() {
		return editClearConsoleMenuItem;
	}

	//	public Menu getConnectionMenu() {
	//		return connectionMenu;
	//	}
	//
	//	public MenuItem getConnectionMenuHeader() {
	//		return connectionMenuHeader;
	//	}
	//
	//	public MenuItem getConnectionConnectItem() {
	//		return connectionConnectItem;
	//	}
	//
	//	public MenuItem getConnectionDisconnectItem() {
	//		return connectionDisconnectItem;
	//	}

	public Menu getToolsMenu() {
		return toolsMenu;
	}

	public MenuItem getToolsMenuHeader() {
		return toolsMenuHeader;
	}

	public MenuItem getToolsPreferencesMenuItem() {
		return toolsPreferencesMenuItem;
	}

	public Menu getHelpMenu() {
		return helpMenu;
	}

	public MenuItem getHelpMenuHeader() {
		return helpMenuHeader;
	}

	public MenuItem getHelpAboutItem() {
		return helpAboutItem;
	}

}
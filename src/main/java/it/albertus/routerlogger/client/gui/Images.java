package it.albertus.routerlogger.client.gui;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;

import it.albertus.util.logging.LoggerFactory;

public class Images {

	private static final Logger logger = LoggerFactory.getLogger(Images.class);

	// Main application icon (in various formats)
	private static final List<Image> mainIcons = load("main.ico");

	// Icone base per l'area di notifica (16x16)
	public static final Image TRAY_ICON_ACTIVE = mainIcons.get(2);
	public static final Image TRAY_ICON_INACTIVE = load("inactive.ico").get(0);

	// Simboli in sovraimpressione per l'area di notifica (16x16, non utilizzabili da soli)
	private static final Image TRAY_ICON_OVERLAY_CLOCK = load("clock.ico").get(0);
	private static final Image TRAY_ICON_OVERLAY_ERROR = load("error.ico").get(0);
	private static final Image TRAY_ICON_OVERLAY_LOCK = load("lock.ico").get(0);
	private static final Image TRAY_ICON_OVERLAY_WARNING = load("warning.ico").get(0);

	// Icone composte per l'area di notifica (16x16)
	public static final Image TRAY_ICON_ACTIVE_WARNING = new DecorationOverlayIcon(TRAY_ICON_ACTIVE, ImageDescriptor.createFromImage(Images.TRAY_ICON_OVERLAY_WARNING), IDecoration.BOTTOM_RIGHT).createImage();
	public static final Image TRAY_ICON_ACTIVE_LOCK = new DecorationOverlayIcon(TRAY_ICON_ACTIVE, ImageDescriptor.createFromImage(Images.TRAY_ICON_OVERLAY_LOCK), IDecoration.BOTTOM_RIGHT).createImage();
	public static final Image TRAY_ICON_INACTIVE_CLOCK = new DecorationOverlayIcon(TRAY_ICON_INACTIVE, ImageDescriptor.createFromImage(Images.TRAY_ICON_OVERLAY_CLOCK), IDecoration.BOTTOM_RIGHT).createImage();
	public static final Image TRAY_ICON_INACTIVE_ERROR = new DecorationOverlayIcon(TRAY_ICON_INACTIVE, ImageDescriptor.createFromImage(Images.TRAY_ICON_OVERLAY_ERROR), IDecoration.BOTTOM_RIGHT).createImage();

	private Images() {
		throw new IllegalAccessError();
	}

	private static List<Image> load(final String fileName) {
		final List<Image> images = new ArrayList<>();
		try (final InputStream stream = Images.class.getResourceAsStream(fileName)) {
			for (final ImageData data : new ImageLoader().load(stream)) {
				images.add(new Image(Display.getCurrent(), data));
			}
		}
		catch (final IOException e) {
			logger.log(Level.WARNING, e.toString(), e);
		}
		return images;
	}

	public static Image[] getMainIcons() {
		return mainIcons.toArray(new Image[mainIcons.size()]);
	}

}

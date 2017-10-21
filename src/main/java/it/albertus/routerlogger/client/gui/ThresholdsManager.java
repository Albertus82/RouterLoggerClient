package it.albertus.routerlogger.client.gui;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import it.albertus.routerlogger.client.engine.Threshold;
import it.albertus.routerlogger.client.engine.ThresholdsReached;
import it.albertus.routerlogger.client.resources.Messages;
import it.albertus.util.logging.LoggerFactory;

public class ThresholdsManager {

	private static final Logger logger = LoggerFactory.getLogger(ThresholdsManager.class);

	private final RouterLoggerClientGui gui;

	public ThresholdsManager(final RouterLoggerClientGui gui) {
		this.gui = gui;
	}

	public void printThresholdsReached(final ThresholdsReached thresholdsReached) {
		if (thresholdsReached != null && thresholdsReached.getReached() != null && !thresholdsReached.getReached().isEmpty()) {
			final Map<String, String> message = new TreeMap<>();
			boolean print = false;
			for (final Entry<Threshold, String> entry : thresholdsReached.getReached().entrySet()) {
				message.put(entry.getKey().getKey(), entry.getValue());
				if (!entry.getKey().isExcluded()) {
					print = true;
				}
			}
			if (print) {
				final LogRecord record = new LogRecord(Level.INFO, Messages.get("msg.thresholds.reached", message));
				record.setMillis(thresholdsReached.getTimestamp().getTime());
				logger.log(record);
				final TrayIcon trayIcon = gui.getTrayIcon();
				if (trayIcon != null) {
					trayIcon.showBalloonToolTip(message);
				}
			}
		}
	}

}

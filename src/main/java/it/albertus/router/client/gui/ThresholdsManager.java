package it.albertus.router.client.gui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import it.albertus.jface.DisplayThreadExecutor;
import it.albertus.router.client.engine.Threshold;
import it.albertus.router.client.engine.ThresholdsReached;
import it.albertus.router.client.resources.Messages;
import it.albertus.util.logging.LoggerFactory;

public class ThresholdsManager {

	private static final Logger logger = LoggerFactory.getLogger(ThresholdsManager.class);

	private static final ThreadLocal<DateFormat> timestampFormat = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat(DataTable.TIMESTAMP_PATTERN);
		}
	};

	private final Map<Date, ThresholdsReached> thresholdsBuffer = new HashMap<>(2);
	private final RouterLoggerClientGui gui;

	public ThresholdsManager(final RouterLoggerClientGui gui) {
		this.gui = gui;
	}

	public Map<Date, ThresholdsReached> getThresholdsBuffer() {
		return thresholdsBuffer;
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

	public synchronized void updateDataTable(final ThresholdsReached thresholdsReached) {
		if (thresholdsReached != null) {
			thresholdsBuffer.put(thresholdsReached.getTimestamp(), thresholdsReached);
		}
		final Set<Date> updatedElements = new HashSet<>();
		for (final ThresholdsReached tr : thresholdsBuffer.values()) {
			if (updateThreshold(tr)) {
				updatedElements.add(tr.getTimestamp());
			}
		}
		for (final Date timestamp : updatedElements) {
			thresholdsBuffer.remove(timestamp);
		}
	}

	protected boolean updateThreshold(final ThresholdsReached thresholdsReached) {
		final boolean[] updated = { false };
		final DataTable dataTable = gui.getDataTable();
		if (dataTable != null && dataTable.getTable() != null) {
			final Set<Integer> indexes = new HashSet<>(thresholdsReached.getReached().size());
			new DisplayThreadExecutor(dataTable.getTable()).execute(new Runnable() {
				@Override
				public void run() {
					for (int index = 0; index < dataTable.getTable().getColumnCount(); index++) {
						final TableColumn tc = dataTable.getTable().getColumn(index);
						for (final Threshold t : thresholdsReached.getReached().keySet()) {
							if (tc.getText().equals(t.getKey())) {
								indexes.add(index);
							}
						}
					}

					for (final TableItem ti : dataTable.getTable().getItems()) {
						if (ti.getText(1).equals(timestampFormat.get().format(thresholdsReached.getTimestamp()))) {
							for (final int index : indexes) {
								ti.setForeground(index, dataTable.getThresholdsReachedForegroundColor());
							}
							updated[0] = true;
							break;
						}
					}
				}
			});
		}
		return updated[0];
	}

}

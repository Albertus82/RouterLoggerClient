package it.albertus.router.client.gui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import it.albertus.jface.SwtThreadExecutor;
import it.albertus.router.client.engine.Threshold;
import it.albertus.router.client.engine.ThresholdsReached;
import it.albertus.router.client.resources.Messages;
import it.albertus.router.client.util.Logger;

public class ThresholdsManager {

	private static final ThreadLocal<DateFormat> timestampFormat = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat(DataTable.TIMESTAMP_PATTERN);
		};
	};

	private final Map<Date, ThresholdsReached> thresholdsBuffer = new HashMap<>(2);
	private final RouterLoggerGui gui;

	public ThresholdsManager(final RouterLoggerGui gui) {
		this.gui = gui;
	}

	public Map<Date, ThresholdsReached> getThresholdsBuffer() {
		return thresholdsBuffer;
	}

	public void printThresholdsReached(final ThresholdsReached thresholdsReached) {
		if (thresholdsReached != null && thresholdsReached.getReached() != null && !thresholdsReached.getReached().isEmpty()) {
			final Map<String, String> message = new TreeMap<String, String>();
			boolean print = false;
			for (final Threshold threshold : thresholdsReached.getReached().keySet()) {
				message.put(threshold.getKey(), thresholdsReached.getReached().get(threshold));
				if (!threshold.isExcluded()) {
					print = true;
				}
			}
			if (print) {
				Logger.getInstance().log(Messages.get("msg.thresholds.reached", message), thresholdsReached.getTimestamp());
				final TrayIcon trayIcon = gui.getTrayIcon();
				if (trayIcon != null) {
					trayIcon.showBalloonToolTip(thresholdsReached.getReached());
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
			final Set<Integer> indexes = new HashSet<Integer>(thresholdsReached.getReached().size());
			new SwtThreadExecutor(dataTable.getTable()) {
				@Override
				protected void run() {
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
			}.start();
		}
		return updated[0];
	}

}

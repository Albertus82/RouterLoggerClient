package it.albertus.router.client.mqtt.listener;

import it.albertus.jface.SwtThreadExecutor;
import it.albertus.router.client.Logger;
import it.albertus.router.client.Threshold;
import it.albertus.router.client.Threshold.Type;
import it.albertus.router.client.gui.DataTable;
import it.albertus.router.client.gui.RouterLoggerGui;
import it.albertus.router.client.gui.TrayIcon;
import it.albertus.router.client.mqtt.BaseMqttClient;
import it.albertus.router.client.resources.Resources;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class ThresholdsMqttMessageListener implements IMqttMessageListener {

	private final RouterLoggerGui gui;

	private static final DateFormat timestampFormat = new SimpleDateFormat(DataTable.TIMESTAMP_PATTERN);

	public ThresholdsMqttMessageListener(RouterLoggerGui gui) {
		this.gui = gui;
	}

	@Override
	public void messageArrived(final String topic, final MqttMessage message) throws JsonSyntaxException, UnsupportedEncodingException {
		final ThresholdsPayload tp = new Gson().fromJson(new String(message.getPayload(), BaseMqttClient.PREFERRED_CHARSET), ThresholdsPayload.class);
		if (gui.getDataTable() != null && gui.getDataTable().getTable() != null) {
			final Set<Integer> indexes = new HashSet<Integer>(tp.getThresholds().size());
			new SwtThreadExecutor(gui.getDataTable().getTable()) {
				@Override
				protected void run() {
					for (int i = 0; i < gui.getDataTable().getTable().getColumnCount(); i++) {
						final TableColumn tc = gui.getDataTable().getTable().getColumn(i);
						for (final ThresholdItem ti : tp.getThresholds()) {
							if (tc.getText().equals(ti.getKey())) {
								indexes.add(i);
							}
						}
					}

					for (final TableItem ti : gui.getDataTable().getTable().getItems()) {
						if (ti.getText(1).equals(formatTimestamp(tp.getTimestamp()))) {
							for (final int i : indexes) {
								ti.setForeground(i, gui.getDataTable().thresholdReachedForegroudColor);
							}
							break;
						}
					}
				}
			}.start();
		}

		if (!message.isRetained()) {
			Map<Threshold, String> m = new LinkedHashMap<Threshold, String>();
			for (Threshold t : getThresholds(tp)) {
				for (ThresholdItem ti : tp.getThresholds()) {
					if (t.getName().equals(ti.getName())) {
						m.put(t, ti.getDetected());
					}
				}
			}
			printThresholdsReached(m);
		}
	}

	private void printThresholdsReached(final Map<Threshold, String> thresholdsReached) {
		if (thresholdsReached != null && !thresholdsReached.isEmpty()) {
			final Map<String, String> message = new TreeMap<String, String>();
			boolean print = false;
			for (final Threshold threshold : thresholdsReached.keySet()) {
				message.put(threshold.getKey(), thresholdsReached.get(threshold));
				if (!threshold.isExcluded()) {
					print = true;
				}
			}
			if (print) {
				Logger.getInstance().log(Resources.get("msg.thresholds.reached", message));
				final TrayIcon trayIcon = gui.getTrayIcon();
				if (trayIcon != null) {
					trayIcon.showBalloonToolTip(thresholdsReached);
				}
			}
		}
	}

	protected Iterable<Threshold> getThresholds(final ThresholdsPayload tp) {
		final Set<Threshold> thresholds = new LinkedHashSet<Threshold>();
		for (final ThresholdItem ti : tp.getThresholds()) {
			thresholds.add(new Threshold(ti.getName(), ti.getKey(), Type.valueOf(ti.getType()), ti.getValue(), false));
		}
		return thresholds;
	}

	private synchronized String formatTimestamp(final Date timestamp) {
		return timestampFormat.format(timestamp);
	}

}

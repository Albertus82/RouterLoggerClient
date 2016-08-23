package it.albertus.router.client.mqtt.listener;

import it.albertus.jface.SwtThreadExecutor;
import it.albertus.router.client.dto.ThresholdDto;
import it.albertus.router.client.dto.ThresholdsDto;
import it.albertus.router.client.engine.Threshold;
import it.albertus.router.client.engine.Threshold.Type;
import it.albertus.router.client.gui.DataTable;
import it.albertus.router.client.gui.RouterLoggerGui;
import it.albertus.router.client.gui.TrayIcon;
import it.albertus.router.client.mqtt.BaseMqttClient;
import it.albertus.router.client.resources.Resources;
import it.albertus.router.client.util.Logger;

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

	private static final DateFormat timestampFormat = new SimpleDateFormat(DataTable.TIMESTAMP_PATTERN);

	private static synchronized String formatTimestamp(final Date timestamp) {
		return timestampFormat.format(timestamp);
	}

	private final RouterLoggerGui gui;

	public ThresholdsMqttMessageListener(final RouterLoggerGui gui) {
		this.gui = gui;
	}

	@Override
	public void messageArrived(final String topic, final MqttMessage message) throws JsonSyntaxException, UnsupportedEncodingException {
		final ThresholdsDto dto = new Gson().fromJson(new String(message.getPayload(), BaseMqttClient.PREFERRED_CHARSET), ThresholdsDto.class);
		if (gui.getDataTable() != null && gui.getDataTable().getTable() != null) {
			final Set<Integer> indexes = new HashSet<Integer>(dto.getThresholds().size());
			new SwtThreadExecutor(gui.getDataTable().getTable()) {
				@Override
				protected void run() {
					for (int index = 0; index < gui.getDataTable().getTable().getColumnCount(); index++) {
						final TableColumn tc = gui.getDataTable().getTable().getColumn(index);
						for (final ThresholdDto ti : dto.getThresholds()) {
							if (tc.getText().equals(ti.getKey())) {
								indexes.add(index);
							}
						}
					}

					for (final TableItem ti : gui.getDataTable().getTable().getItems()) {
						if (ti.getText(1).equals(formatTimestamp(dto.getTimestamp()))) {
							for (final int index : indexes) {
								ti.setForeground(index, gui.getDataTable().thresholdReachedForegroudColor);
							}
							break;
						}
					}
				}
			}.start();
		}

		if (!message.isRetained()) {
			final Map<Threshold, String> m = new LinkedHashMap<Threshold, String>();
			for (final Threshold t : getThresholds(dto)) {
				for (final ThresholdDto ti : dto.getThresholds()) {
					if (t.getName().equals(ti.getName())) {
						m.put(t, ti.getDetected());
					}
				}
			}
			printThresholdsReached(m, dto.getTimestamp());
		}
	}

	private void printThresholdsReached(final Map<Threshold, String> thresholdsReached, final Date timestamp) {
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
				Logger.getInstance().log(Resources.get("msg.thresholds.reached", message), timestamp);
				final TrayIcon trayIcon = gui.getTrayIcon();
				if (trayIcon != null) {
					trayIcon.showBalloonToolTip(thresholdsReached);
				}
			}
		}
	}

	protected Iterable<Threshold> getThresholds(final ThresholdsDto tp) {
		final Set<Threshold> thresholds = new LinkedHashSet<Threshold>();
		for (final ThresholdDto ti : tp.getThresholds()) {
			thresholds.add(new Threshold(ti.getName(), ti.getKey(), Type.valueOf(ti.getType()), ti.getValue(), false));
		}
		return thresholds;
	}

}

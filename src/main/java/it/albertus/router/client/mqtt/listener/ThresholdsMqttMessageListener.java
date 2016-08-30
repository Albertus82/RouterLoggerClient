package it.albertus.router.client.mqtt.listener;

import it.albertus.jface.SwtThreadExecutor;
import it.albertus.router.client.dto.ThresholdsDto;
import it.albertus.router.client.dto.transformer.ThresholdsTransformer;
import it.albertus.router.client.engine.Threshold;
import it.albertus.router.client.engine.ThresholdsReached;
import it.albertus.router.client.gui.DataTable;
import it.albertus.router.client.gui.RouterLoggerGui;
import it.albertus.router.client.mqtt.BaseMqttClient;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

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

		if (dto != null && dto.getReached() != null && !dto.getReached().isEmpty()) {
			final ThresholdsReached thresholdsReached = ThresholdsTransformer.fromDto(dto);

			if (gui.getDataTable() != null && gui.getDataTable().getTable() != null) {
				final Set<Integer> indexes = new HashSet<Integer>(thresholdsReached.getReached().size());
				new SwtThreadExecutor(gui.getDataTable().getTable()) {
					@Override
					protected void run() {
						for (int index = 0; index < gui.getDataTable().getTable().getColumnCount(); index++) {
							final TableColumn tc = gui.getDataTable().getTable().getColumn(index);
							for (final Threshold t : thresholdsReached.getReached().keySet()) {
								if (tc.getText().equals(t.getKey())) {
									indexes.add(index);
								}
							}
						}

						for (final TableItem ti : gui.getDataTable().getTable().getItems()) {
							if (ti.getText(1).equals(formatTimestamp(thresholdsReached.getTimestamp()))) {
								for (final int index : indexes) {
									ti.setForeground(index, gui.getDataTable().getThresholdsReachedForegroundColor());
								}
								break;
							}
						}
					}
				}.start();
			}

			if (!message.isRetained()) {
				gui.printThresholdsReached(thresholdsReached);
			}
		}
	}

}

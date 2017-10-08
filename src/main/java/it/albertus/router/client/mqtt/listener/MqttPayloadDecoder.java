package it.albertus.router.client.mqtt.listener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import it.albertus.router.client.mqtt.BaseMqttClient;
import it.albertus.util.IOUtils;
import it.albertus.util.NewLine;

public class MqttPayloadDecoder {

	public byte[] decode(final byte[] messagePayload) throws IOException {
		final List<byte[]> tokens = split(messagePayload, NewLine.CRLF.toString().getBytes(BaseMqttClient.PREFERRED_CHARSET));
		final Map<String, String> headers = new Headers();
		for (int i = 0; i < tokens.size() - 2; i++) {
			final String headerLine = new String(tokens.get(i), BaseMqttClient.PREFERRED_CHARSET);
			final String key = headerLine.substring(0, headerLine.indexOf(':'));
			final String value = headerLine.substring(headerLine.indexOf(':') + 1);
			headers.put(key.trim(), value.trim());
		}
		if ("gzip".equalsIgnoreCase(headers.get("Content-Encoding"))) {
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try (final GZIPInputStream gzis = new GZIPInputStream(new ByteArrayInputStream(tokens.get(tokens.size() - 1)))) {
				IOUtils.copy(gzis, baos, 8192);
			}
			return baos.toByteArray();
		}
		else {
			return tokens.get(tokens.size() - 1);
		}
	}

	private static List<byte[]> split(byte[] array, byte[] delimiter) {
		List<byte[]> byteArrays = new LinkedList<>();
		if (delimiter.length == 0) {
			return byteArrays;
		}
		int begin = 0;

		outer: for (int i = 0; i < array.length - delimiter.length + 1; i++) {
			for (int j = 0; j < delimiter.length; j++) {
				if (array[i + j] != delimiter[j]) {
					continue outer;
				}
			}
			byteArrays.add(Arrays.copyOfRange(array, begin, i));
			begin = i + delimiter.length;
		}
		byteArrays.add(Arrays.copyOfRange(array, begin, array.length));
		return byteArrays;
	}

	private static class Headers extends HashMap<String, String> {

		private static final long serialVersionUID = -9160311802757600284L;

		@Override
		public boolean containsKey(final Object key) {
			final String keyStr = key.toString();
			for (final String k : keySet()) {
				if (k.equalsIgnoreCase(keyStr)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public String get(final Object key) {
			final String keyStr = key.toString();
			for (final Entry<String, String> e : entrySet()) {
				if (e.getKey().equalsIgnoreCase(keyStr)) {
					return e.getValue();
				}
			}
			return null;
		}
	}

}

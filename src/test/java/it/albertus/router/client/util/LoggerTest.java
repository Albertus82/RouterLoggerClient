package it.albertus.router.client.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Calendar;
import java.util.Date;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import it.albertus.util.ExceptionUtils;
import it.albertus.util.IOUtils;
import it.albertus.util.NewLine;

public class LoggerTest {

	private static final String CHARSET = "UTF-8";

	private static final String TEXT = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";

	private static Logger logger;

	private static PrintStream defaultSysOut;
	private static PrintStream defaultSysErr;

	private static PrintStream ps;
	private static ByteArrayOutputStream baos;

	@BeforeClass
	public static void init() {
		defaultSysOut = System.out;
		defaultSysErr = System.err;

		baos = new ByteArrayOutputStream();
		ps = new PrintStream(baos, true);
		System.setOut(ps);
		System.setErr(ps);

		logger = Logger.getInstance();
	}

	@Before
	public void resetByteArrayOutputStream() {
		baos.reset();
	}

	@Test
	public void logMessage() throws IOException {
		logger.log(TEXT);
		final int expectedLength = Logger.timestampFormat.get().format(new Date()).length() + " ".length() + TEXT.length() + NewLine.SYSTEM_LINE_SEPARATOR.length();
		Assert.assertEquals(expectedLength, baos.toString(CHARSET).length());
		defaultSysOut.write(baos.toByteArray());
	}

	@Test
	public void logMessageWithTimestamp() throws IOException {
		final Calendar calendar = Calendar.getInstance();
		calendar.setLenient(false);
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		calendar.add(Calendar.MONTH, 2);
		calendar.add(Calendar.YEAR, 3);
		calendar.add(Calendar.HOUR_OF_DAY, 4);
		calendar.add(Calendar.MINUTE, 5);
		calendar.add(Calendar.SECOND, 6);
		logger.log(TEXT, calendar.getTime());
		final String expectedString = Logger.timestampFormat.get().format(calendar.getTime()) + ' ' + TEXT + NewLine.SYSTEM_LINE_SEPARATOR;
		Assert.assertEquals(expectedString, baos.toString(CHARSET));
		defaultSysOut.write(baos.toByteArray());
	}

	@Test
	public void logThrowable() throws IOException {
		final Throwable t = new IllegalAccessError("Error message!");
		logger.log(t);
		final int expectedLength = Logger.timestampFormat.get().format(new Date()).length() + " ".length() + ExceptionUtils.getStackTrace(t).trim().length() + NewLine.SYSTEM_LINE_SEPARATOR.length();
		Assert.assertEquals(expectedLength, baos.toString(CHARSET).length());
		defaultSysErr.write(baos.toByteArray());
	}

	@AfterClass
	public static void destroy() {
		System.setErr(defaultSysErr);
		System.setOut(defaultSysOut);

		IOUtils.closeQuietly(ps);
	}

}

package app.xml;

import static org.junit.Assert.assertTrue;

import java.nio.charset.Charset;
import java.util.Map;

import models.DailyRate;

import org.junit.Test;

import util.FileUtil;
import xml.XmlProcessor;

public class XmlProcessorTest {

	private static final String FILEPATH_STRING = "/test/resources/eurofxref-hist-90d.xml";

	@Test
	public void testProcessXmlStringFileSax() throws Exception {

		final String projectPath = System.getProperty("user.dir");
		final String xmlString = FileUtil.readFile(projectPath + FILEPATH_STRING, Charset.forName("UTF-8"));

		final Map<String, DailyRate> dailyRateList = XmlProcessor.extractDailyRates(xmlString);

		System.out.println("# dailyRate processed: " + dailyRateList.size());

		assertTrue(dailyRateList.size() == 65);

		for (final DailyRate dailyRate : dailyRateList.values()) {
			System.out.println("Date: " + dailyRate.getDate() + "------" + dailyRate.getCurrencyRates().size());
			assertTrue(dailyRate.getCurrencyRates().size() > 0);
		}
	}

}

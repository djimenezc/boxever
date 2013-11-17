package app.xml;

import static org.junit.Assert.assertTrue;

import java.nio.charset.Charset;
import java.util.List;

import org.junit.Test;

import util.FileUtil;
import xml.XmlProcessor;

public class XmlProcessorTest {

	private static final String FILEPATH_STRING = "/Users/david/Projects/sources/boxever/currency/currency/test/resources/eurofxref-hist-90d.xml";

	@Test
	public void testProcessXmlStringFileSax() throws Exception {

		final String xmlString = FileUtil.readFile(FILEPATH_STRING, Charset.forName("UTF-8"));

		final List dailyRateList = XmlProcessor.extractDailyRates(xmlString);

		assertTrue(dailyRateList.size() > 0);
	}

}

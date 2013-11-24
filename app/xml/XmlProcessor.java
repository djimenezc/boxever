package xml;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.CurrencyRate;
import models.CurrencyType;
import models.DailyRate;

import org.apache.commons.io.IOUtils;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

/**
 * Class that process a xml file represented as a string using XPATH to extract information about the exchange price of
 * different currencies against EUR
 * 
 * @author david
 * 
 */
public class XmlProcessor {

	private static final String ROOT_NAMESPACE_URL = "http://www.ecb.int/vocabulary/2002-08-01/eurofxref";
	private static final String GESMES_NAMESPACE_URL = "http://www.gesmes.org/xml/2002-08-01";
	private static final String ROOT_NAMESPACE = "root";
	private static final String GESMES_NAMESPACE = "gesmes";
	private static final String ELEMENTS_XPATH_PATH = "/gesmes:Envelope/root:Cube/root:Cube/root:Cube";
	private static final String API_DATE_FORMAT = "yy-MM-dd";

	/**
	 * Methods to process a XML file represented as a string and get a map with beans that represent the exchange of
	 * difference currencies against the EUR
	 * 
	 * @param xmlString
	 * @return
	 * @throws JDOMException
	 * @throws IOException
	 * @throws ParseException
	 */
	public static Map<String, DailyRate> extractDailyRates(final String xmlString) throws JDOMException, IOException,
			ParseException {

		final SAXBuilder builder = new SAXBuilder();
		final InputStream in = IOUtils.toInputStream(xmlString);
		final org.jdom.Document doc = builder.build(in);
		final XPath xpath = XPath.newInstance(ELEMENTS_XPATH_PATH);
		xpath.addNamespace(GESMES_NAMESPACE, GESMES_NAMESPACE_URL);
		xpath.addNamespace(ROOT_NAMESPACE, ROOT_NAMESPACE_URL);

		final Map<String, DailyRate> result = new HashMap<String, DailyRate>();
		final List<?> all2 = xpath.selectNodes(doc);

		// Process the node extracted with XPATH
		for (final Object o : all2) {
			final Element element = (Element) o;
			final Element parentElement = (Element) element.getParent();
			final String timeString = parentElement.getAttributeValue(ApiResponseFieldsConstants.TIME);
			final DateFormat formatter = new SimpleDateFormat(API_DATE_FORMAT);
			final Date date = formatter.parse(parentElement.getAttributeValue(ApiResponseFieldsConstants.TIME));

			final String currency = element.getAttributeValue(ApiResponseFieldsConstants.CURRENCY);
			final String rate = element.getAttributeValue(ApiResponseFieldsConstants.RATE);

			final CurrencyRate currencyRate = new CurrencyRate(CurrencyType.valueOf(currency), Double.valueOf(rate));

			processNode(result, timeString, date, currencyRate);
		}
		return result;
	}

	/**
	 * Method that process the content of a xml node and build a DailyRate bean
	 * 
	 * @param result
	 * @param timeString
	 * @param date
	 * @param currencyRate
	 */
	private static void processNode(final Map<String, DailyRate> result, final String timeString, final Date date,
			final CurrencyRate currencyRate) {

		if (result.containsKey(timeString)) {
			result.get(timeString).getCurrencyRates().add(currencyRate);
		}
		else {
			final List<CurrencyRate> currencyRates = new ArrayList<CurrencyRate>();
			currencyRates.add(currencyRate);
			final DailyRate dailyRate = new DailyRate(date, currencyRates);
			result.put(timeString, dailyRate);
		}
	}
}

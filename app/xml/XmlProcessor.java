package xml;

import java.io.InputStream;
import java.text.DateFormat;
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
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

public class XmlProcessor {

	private static final String API_DATE_FORMAT = "yy-MM-dd";

	public static Map<String, DailyRate> extractDailyRates(final String xmlString) throws Exception {

		final SAXBuilder builder = new SAXBuilder();
		final InputStream in = IOUtils.toInputStream(xmlString);
		final org.jdom.Document doc = builder.build(in);
		final XPath xpath = XPath.newInstance("/gesmes:Envelope/root:Cube/root:Cube/root:Cube");
		xpath.addNamespace("gesmes", "http://www.gesmes.org/xml/2002-08-01");
		xpath.addNamespace("root", "http://www.ecb.int/vocabulary/2002-08-01/eurofxref");

		final Map<String, DailyRate> result = new HashMap<String, DailyRate>();

		final List<?> all2 = xpath.selectNodes(doc);
		for (final Object o : all2) {
			final Element element = (Element) o;
			final DateFormat formatter = new SimpleDateFormat(API_DATE_FORMAT);
			final Element parentElement = (Element) element.getParent();
			final String timeString = parentElement.getAttributeValue("time");
			final Date date = formatter.parse(parentElement.getAttributeValue("time"));

			final String currency = element.getAttributeValue("currency");
			final String rate = element.getAttributeValue("rate");

			final CurrencyRate currencyRate = new CurrencyRate(CurrencyType.valueOf(currency), Double.valueOf(rate));

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
		return result;
	}
}

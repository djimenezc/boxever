package xml;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import models.CurrencyRate;
import models.CurrencyType;
import models.DailyRate;

import org.apache.commons.io.IOUtils;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

public class XmlProcessor {

	public static List<DailyRate> extractDailyRates(final String xmlString) throws Exception {

		final SAXBuilder builder = new SAXBuilder();
		final InputStream in = IOUtils.toInputStream(xmlString);
		final org.jdom.Document doc = builder.build(in);
		final XPath xpath = XPath.newInstance("/gesmes:Envelope/root:Cube/root:Cube");
		xpath.addNamespace("gesmes", "http://www.gesmes.org/xml/2002-08-01");
		xpath.addNamespace("root", "http://www.ecb.int/vocabulary/2002-08-01/eurofxref");

		final List<DailyRate> result = new ArrayList<DailyRate>();

		final List<?> all2 = xpath.selectNodes(doc);
		for (final Object o : all2) {
			final Element element = (Element) o;
			final DateFormat formatter = new SimpleDateFormat("yy-MM-dd");
			final Date date = formatter.parse(element.getAttributeValue("time"));
			final List<CurrencyRate> currencyRates = new ArrayList<CurrencyRate>();

			for (final Object child : element.getChildren()) {
				final String currency = ((Element) child).getAttributeValue("currency");
				final String rate = ((Element) child).getAttributeValue("rate");

				final CurrencyRate currencyRate = new CurrencyRate(CurrencyType.valueOf(currency), Double.valueOf(rate));
				currencyRates.add(currencyRate);
			}
			final DailyRate dailyRate = new DailyRate(date, currencyRates);
			result.add(dailyRate);
		}
		return result;
	}

}

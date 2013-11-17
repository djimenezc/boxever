package actors;

import java.util.List;

import models.DailyRate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.libs.F.Function;
import play.libs.WS;
import play.mvc.Controller;
import play.mvc.Result;
import xml.XmlProcessor;
import dal.CassandraAstyanaxConnection;

public class CurrencyActor extends Controller {

	private static final Logger LOGGER = LoggerFactory.getLogger(CurrencyActor.class);

	public static Result readXmlDataFromApi(final String feedUrl) {
		return async(WS.url(feedUrl).get().map(new Function<WS.Response, Result>() {
			@Override
			public Result apply(final WS.Response response) {
				Result result;
				try {
					final List<DailyRate> dailyRateList = XmlProcessor.extractDailyRates(response.getBody());

					CassandraAstyanaxConnection.getInstance().writeDailyCurrencies(dailyRateList);

					result = ok("Data retrieved successfully from the remote API");

				} catch (final Exception e) {
					LOGGER.error("Error processing data from the BCE API", e);
					result = internalServerError("error processing rates from the remote API");
				}

				return result;

			}
		}));
	}
}
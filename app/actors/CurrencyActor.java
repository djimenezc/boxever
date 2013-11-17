package actors;

import java.util.List;

import models.DailyRate;
import play.libs.F.Function;
import play.libs.WS;
import play.mvc.Controller;
import play.mvc.Result;
import xml.XmlProcessor;

public class CurrencyActor extends Controller {

	public static Result readXmlDataFromApi(final String feedUrl) {
		return async(WS.url(feedUrl).get().map(new Function<WS.Response, Result>() {
			@Override
			public Result apply(final WS.Response response) {
				try {
					final List<DailyRate> dailyRateList = XmlProcessor.extractDailyRates(response.getBody());

				} catch (final Exception e) {
					e.printStackTrace();
					return internalServerError("error processing rates from the remote API");
				}
				return ok("data processed successfully");

			}
		}));
	}
}
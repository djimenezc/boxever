package controllers;

import java.util.List;
import java.util.concurrent.Callable;

import models.CurrencyType;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import play.libs.Akka;
import play.libs.F.Function;
import play.libs.F.Promise;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;
import actors.CurrencyActor;
import base.ValuePair;

import com.netflix.astyanax.connectionpool.exceptions.ConnectionException;

import dal.CassandraAstyanaxConnection;

public class Application extends Controller {

	private static final String API_PATH = "http://www.ecb.europa.eu/stats/eurofxref/eurofxref-hist-90d.xml";
	private static final Logger LOGGER = Logger.getLogger(Application.class);

	public static Result currencyList() {
		final List<ValuePair> currencyList = CurrencyType.buildCurrencyList();
		final JsonNode jsonCurrencyRate = play.libs.Json.toJson(currencyList);

		return ok(jsonCurrencyRate);
	}

	public static Result getCurrencyRateData(final String currencyId) {

		Result result = null;
		// TODO extract into the service class
		try {
			final CurrencyType currencyType = CurrencyType.valueOf(currencyId);
			final List<ValuePair> ratesByCurrency = CassandraAstyanaxConnection.getInstance().readByCurrency(
					currencyType);

			final ObjectMapper mapper = new ObjectMapper();
			final JsonNode node = mapper.convertValue(ratesByCurrency, JsonNode.class);

			result = ok(node);

		} catch (final ConnectionException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error reading rates by currency " + currencyId);
			result = internalServerError("Error processing request");
		}

		return result;
	}

	public static Result index() {
		return ok(index.render("Boxever"));
	}

	public static Result refresh(final String value) {

		return ok("value: {Boxever -- asdasdasd}");
	}

	public static Result refreshAll() {

		LOGGER.info("Refresh all exchange rates");

		final Promise<String> promiseAsync = Akka.future(new Callable<String>() {
			@Override
			public String call() {
				return CurrencyActor.readXmlDataFromApi(API_PATH).toString();
			}
		});

		final Promise<Result> promiseXmlProcessed = promiseAsync.map(new Function<String, Result>() {

			@Override
			public Result apply(final String xmlFile) throws Exception {

				final ValuePair valuePair = new ValuePair("status",
						"Retrieved data from the 3rd party API successfully");
				final ObjectMapper mapper = new ObjectMapper();
				final JsonNode node = mapper.convertValue(valuePair, JsonNode.class);

				return ok(node);
			}

		});

		return (Result) promiseXmlProcessed.getWrappedPromise();
	}
}

package controllers;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import models.CurrencyType;
import models.DailyRate;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.jdom.JDOMException;

import play.libs.Akka;
import play.libs.F.Function;
import play.libs.F.Promise;
import play.libs.WS;
import play.libs.WS.Response;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;
import xml.XmlProcessor;
import base.ValuePair;

import com.netflix.astyanax.connectionpool.exceptions.ConnectionException;

import dal.CassandraAstyanaxConnection;

public class Application extends Controller {

	private static final String API_PATH = "http://www.ecb.europa.eu/stats/eurofxref/eurofxref-hist-90d.xml";
	private static final Logger LOGGER = Logger.getLogger(Application.class);

	private static Promise<JsonNode> computeCurrencyListAsynchronously() {

		final Promise<JsonNode> result = Akka.future(new Callable<JsonNode>() {
			@Override
			public JsonNode call() {
				final List<ValuePair> currencyList = CurrencyType.buildCurrencyList();
				final JsonNode jsonCurrencyRate = play.libs.Json.toJson(currencyList);

				return jsonCurrencyRate;
			}
		});

		return result;
	}

	public static Result currencyList() {

		final Promise<JsonNode> promiseOfPIValue = computeCurrencyListAsynchronously();

		return async(promiseOfPIValue.map(new Function<JsonNode, Result>() {
			@Override
			public Result apply(final JsonNode JsonNode) {
				return ok(JsonNode);
			}
		}));
	}

	public static Result getCurrencyRateData(final String currencyId) {

		Result result = null;
		try {
			final CurrencyType currencyType = CurrencyType.valueOf(currencyId);
			final Map<Date, ValuePair> ratesByCurrency = CassandraAstyanaxConnection.getInstance().readByCurrency(
					currencyType);

			final ObjectMapper mapper = new ObjectMapper();
			final JsonNode node = mapper.convertValue(ratesByCurrency.values(), JsonNode.class);

			result = ok(node);

		} catch (final ConnectionException e) {
			LOGGER.error("Error reading rates by currency " + currencyId);
			result = internalServerError("Error processing request");
		}

		return result;
	}

	public static Result index() {
		return ok(index.render("Boxever"));
	}

	private static JsonNode processXmlResponse(final String xmlString) throws JDOMException, IOException,
			ParseException, ConnectionException {

		final Map<String, DailyRate> dailyRateMap = XmlProcessor.extractDailyRates(xmlString);

		CassandraAstyanaxConnection.getInstance().writeDailyCurrencies(dailyRateMap);

		final ValuePair valuePair = new ValuePair("status", "Retrieved data from the 3rd party API successfully");
		final ObjectMapper mapper = new ObjectMapper();
		final JsonNode node = mapper.convertValue(valuePair, JsonNode.class);

		return node;
	}

	public static Result refreshAll() {

		LOGGER.info("Refresh all exchange rates");

		final Promise<String> promiseXmlProcessed = WS.url(API_PATH).get().map(new Function<WS.Response, String>() {

			@Override
			public String apply(final Response response) throws Exception {

				return response.getBody();
			}
		});

		return async(promiseXmlProcessed.map(new Function<String, Result>() {
			@Override
			public Result apply(final String xmlString) throws Throwable {

				final JsonNode node = processXmlResponse(xmlString);

				return ok(node);
			}
		}));
	}
}

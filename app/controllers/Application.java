package controllers;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collection;
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

/**
 * Class where are described the API that the fronted can utilize. The publics methods represent entry points to the
 * back-end functionality
 * 
 * @author david
 * 
 */
public class Application extends Controller {

	private static final CurrencyType DEFAULT_CURRENCY_TYPE = CurrencyType.USD;
	private static final String API_PATH = "http://www.ecb.europa.eu/stats/eurofxref/eurofxref-hist-90d.xml";
	private static final Logger LOGGER = Logger.getLogger(Application.class);

	/**
	 * Method that allow to clean up the database from the front-end, truncating the tables
	 * 
	 * @return Message indicating if the action was performed properly
	 * 
	 */
	public static Result cleanDatabase() {

		final Promise<Boolean> promiseOfLoadTable = cleanDatabaseAsynchronously();

		return async(promiseOfLoadTable.map(new Function<Boolean, Result>() {
			@Override
			public Result apply(final Boolean truncateStatus) throws Throwable {

				Result result;

				if (truncateStatus) {
					LOGGER.info("Table truncated successfully");
					result = ok("Table truncated successfully");
				}
				else {
					result = ok("Problem truncating table");
				}
				return result;
			}
		}));
	}

	/**
	 * Asynchronously call to clean up the database and remove the data
	 * 
	 * @return
	 */
	private static Promise<Boolean> cleanDatabaseAsynchronously() {

		return Akka.future(new Callable<Boolean>() {
			@Override
			public Boolean call() {

				return CassandraAstyanaxConnection.getInstance().truncateCurrencyTable();
			}
		});
	}

	/**
	 * Method to build the list of currencies availables asynchronously
	 * 
	 * @return
	 */
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

	/**
	 * Method that allow to get the list of currencies available to display its rate
	 * 
	 * @return
	 */
	public static Result currencyList() {

		LOGGER.info("Building currency List ");

		final Promise<JsonNode> promiseOfPIValue = computeCurrencyListAsynchronously();

		return async(promiseOfPIValue.map(new Function<JsonNode, Result>() {
			@Override
			public Result apply(final JsonNode JsonNode) {
				return ok(JsonNode);
			}
		}));
	}

	/**
	 * Method that retrieve a list of rates of an specific currency
	 * 
	 * @param currencyId
	 * @return
	 */
	public static Result getCurrencyRateData(final String currencyId) {

		LOGGER.info("getCurrencyRateData " + currencyId);

		final Promise<Map<Date, ValuePair>> promiseOfLoadTable = getCurrencyRateMapAsynchronously(currencyId);

		return async(promiseOfLoadTable.map(new Function<Map<Date, ValuePair>, Result>() {
			@Override
			public Result apply(final Map<Date, ValuePair> ratesByCurrency) {

				Result result;

				if (ratesByCurrency != null) {
					final ObjectMapper mapper = new ObjectMapper();
					final JsonNode jsonNode = mapper.convertValue(ratesByCurrency.values(), JsonNode.class);

					// The table is empty
					if (ratesByCurrency.size() < 1) {
						LOGGER.info("Currency table empty, loading data from the remote API");
						result = refreshFromRemoteAPI();
					}
					else {
						result = ok(jsonNode);
					}
				}
				else {
					result = internalServerError("Error processing get currency rate request");
				}

				return result;
			}
		}));
	}

	/**
	 * Method that read the exchange rate of a concrete currency from asynchronously
	 * 
	 * @param currencyId
	 * @return
	 */
	private static Promise<Map<Date, ValuePair>> getCurrencyRateMapAsynchronously(final String currencyId) {

		return Akka.future(new Callable<Map<Date, ValuePair>>() {
			@Override
			public Map<Date, ValuePair> call() {
				Map<Date, ValuePair> ratesByCurrency = null;

				try {
					final CurrencyType currencyType = CurrencyType.valueOf(currencyId);
					ratesByCurrency = CassandraAstyanaxConnection.getInstance().readByCurrency(currencyType);

				} catch (final ConnectionException e) {
					LOGGER.error("Error reading rates by currency " + currencyId);
				}
				return ratesByCurrency;
			}
		});
	}

	/**
	 * Return the index page checking if the currency table is not empty. If the currency table is empty the data is
	 * populate from the remote API
	 * 
	 * @return
	 */
	public static Result index() {

		final Promise<Map<Date, ValuePair>> promiseOfLoadTable = getCurrencyRateMapAsynchronously(DEFAULT_CURRENCY_TYPE
				.name());

		return async(promiseOfLoadTable.map(new Function<Map<Date, ValuePair>, Result>() {
			@Override
			public Result apply(final Map<Date, ValuePair> currencyRates) throws Throwable {

				if (currencyRates.size() < 1) {
					LOGGER.info("Get index(): Currency table empty, loading data from the remote API");
					refreshFromRemoteAPI();
				}

				return ok(index.render(DEFAULT_CURRENCY_TYPE.name()));
			}
		}));
	}

	/**
	 * Process the response from the remote API and storing the exchange information in the database
	 * 
	 * @param xmlString
	 * @return
	 * @throws JDOMException
	 * @throws IOException
	 * @throws ParseException
	 * @throws ConnectionException
	 */
	private static JsonNode processXmlResponse(final String xmlString) throws JDOMException, IOException,
			ParseException, ConnectionException {

		final Map<String, DailyRate> dailyRateMap = XmlProcessor.extractDailyRates(xmlString);

		CassandraAstyanaxConnection.getInstance().writeDailyCurrencies(dailyRateMap);

		final Collection<ValuePair> response = CassandraAstyanaxConnection.getInstance()
				.readByCurrency(DEFAULT_CURRENCY_TYPE).values();
		final ObjectMapper mapper = new ObjectMapper();
		final JsonNode node = mapper.convertValue(response, JsonNode.class);

		return node;
	}

	/**
	 * Method to clean the database and get fresh info from the remote API
	 * 
	 * @return
	 */
	public static Result refreshFromRemoteAPI() {

		LOGGER.info("Refresh exchange rates from the remote API");

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

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("refreshFromRemoteAPI result: " + node.toString());
				}

				return ok(node);
			}
		}));
	}
}

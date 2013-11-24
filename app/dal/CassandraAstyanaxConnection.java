package dal;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import models.CurrencyRate;
import models.CurrencyType;
import models.DailyRate;

import org.apache.log4j.Logger;

import util.UUIDUtil;
import base.ValuePair;

import com.google.common.collect.ImmutableMap;
import com.netflix.astyanax.AstyanaxContext;
import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.MutationBatch;
import com.netflix.astyanax.connectionpool.NodeDiscoveryType;
import com.netflix.astyanax.connectionpool.OperationResult;
import com.netflix.astyanax.connectionpool.exceptions.ConnectionException;
import com.netflix.astyanax.connectionpool.impl.ConnectionPoolConfigurationImpl;
import com.netflix.astyanax.connectionpool.impl.CountingConnectionPoolMonitor;
import com.netflix.astyanax.impl.AstyanaxConfigurationImpl;
import com.netflix.astyanax.model.ColumnFamily;
import com.netflix.astyanax.model.ColumnList;
import com.netflix.astyanax.model.CqlResult;
import com.netflix.astyanax.model.Row;
import com.netflix.astyanax.model.Rows;
import com.netflix.astyanax.serializers.StringSerializer;
import com.netflix.astyanax.thrift.ThriftFamilyFactory;

/**
 * Class to handle the connection and different operation with Cassandra using Astyanax driver. The class implement the
 * singleton pattern
 * 
 * @author david
 * 
 */
public class CassandraAstyanaxConnection {

	private static class ConnectionHolder {
		static final CassandraAstyanaxConnection connection = new CassandraAstyanaxConnection();
	}

	private static final String CURRENCIES_KEYSPACE = "currencies";

	private static final String DAILY_CURRENCIES_FC = "dailyCurrencies";

	private static final Logger LOGGER = Logger.getLogger(CassandraAstyanaxConnection.class);

	private static final String DATE_FORMAT = "dd-MMM-yy";

	/**
	 * Method to connect with Cassandra using ConnectKeyspaceConfig parameters
	 * 
	 * @param parameterObject
	 * @return AstyanaxContext that represents the connection with Cassandra
	 */
	public static AstyanaxContext<Keyspace> connectClusterContext(final ConnectKeyspaceConfig parameterObject) {

		final AstyanaxContext<Keyspace> context = new AstyanaxContext.Builder()
				.forKeyspace(parameterObject.getKeyspace())
				.withAstyanaxConfiguration(
						new AstyanaxConfigurationImpl().setDiscoveryType(NodeDiscoveryType.RING_DESCRIBE))
				.withConnectionPoolConfiguration(
						new ConnectionPoolConfigurationImpl(parameterObject.getConnectionPoolName())
								.setPort(parameterObject.getPort())
								.setMaxConnsPerHost(parameterObject.getMaxConnectionPerHost())
								.setSeeds(parameterObject.getSeed()))
				.withConnectionPoolMonitor(new CountingConnectionPoolMonitor())
				.buildKeyspace(ThriftFamilyFactory.getInstance());

		return context;
	}

	/**
	 * Method that connect a Cassandra Keyspace base on the ConnectKeyspaceConfig parameter and start the Astyanax
	 * context
	 * 
	 * @param parameterObject
	 * @return
	 */
	public static Keyspace connectKeyspace(final ConnectKeyspaceConfig parameterObject) {

		final AstyanaxContext<Keyspace> context = connectClusterContext(parameterObject);

		context.start();
		final Keyspace keyspace = context.getEntity();

		return keyspace;
	}

	/**
	 * Method to create a new keyspace base on ConnectKeyspaceConfig parameters
	 * 
	 * @param parameterObject
	 * @return
	 * @throws ConnectionException
	 */
	public static Keyspace createKeyspace(final ConnectKeyspaceConfig parameterObject) throws ConnectionException {

		final AstyanaxContext<Keyspace> ctx = connectClusterContext(parameterObject);
		ctx.start();
		final Keyspace keyspace = ctx.getEntity();

		// Using simple strategy
		keyspace.createKeyspace(ImmutableMap
				.<String, Object> builder()
				.put("strategy_options", ImmutableMap.<String, Object> builder().put("replication_factor", "1").build())
				.put("strategy_class", "SimpleStrategy").build());

		return keyspace;
	}

	/**
	 * Method to create a column family in a specific keyspace
	 * 
	 * @param columnName
	 * @param keyspace
	 * @return
	 * @throws ConnectionException
	 */
	public static ColumnFamily<String, String> createSimpleColumnFamily(final String columnName, final Keyspace keyspace)
			throws ConnectionException {

		final ColumnFamily<String, String> cfStandard = getColumnFamily(columnName, keyspace);

		keyspace.createColumnFamily(cfStandard, null);

		return cfStandard;
	}

	/**
	 * Method to get a column family from a keyspace
	 * 
	 * @param columnName
	 * @param keyspace
	 * @return
	 * @throws ConnectionException
	 */
	public static ColumnFamily<String, String> getColumnFamily(final String columnName, final Keyspace keyspace)
			throws ConnectionException {

		final ColumnFamily<String, String> cfStandard = ColumnFamily.newColumnFamily(columnName,
				StringSerializer.get(), StringSerializer.get());

		return cfStandard;
	}

	/**
	 * Method to get a instance of the CassandraAstyanaxConnection
	 * 
	 * @return
	 */
	public static CassandraAstyanaxConnection getInstance() {
		return ConnectionHolder.connection;
	}

	/**
	 * Default constructor hidden to force to use the getInstance method to use CassandraAstyanaxConnection object
	 */
	private CassandraAstyanaxConnection() {
		super();
	}

	/**
	 * Method to convert a list of rows in a Map of value pair with the currency and the rate identify by date
	 * 
	 * @param rows
	 * @return
	 */
	public Map<Date, ValuePair> processCurrencyRateFromDatabase(final Rows<String, String> rows) {
		final Map<Date, ValuePair> valuePairSortedMap = new TreeMap<Date, ValuePair>();

		for (final Row<String, String> row : rows) {

			final ColumnList<String> cols = row.getColumns();

			final String rate = Double.toString(cols.getDoubleValue(ModelConstants.COL_NAME_RATE, null));
			final Date date = cols.getDateValue(ModelConstants.COL_NAME_DATE, null);
			final DateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
			final String dateString = formatter.format(date);

			valuePairSortedMap.put(date, new ValuePair(rate, dateString));
		}

		return valuePairSortedMap;
	}

	/**
	 * Method to get a row from a column family identify for the id parameter
	 * 
	 * @param columnFamily
	 * @param keyspace
	 * @param id
	 * @return
	 */
	public OperationResult<ColumnList<String>> read(final ColumnFamily<String, String> columnFamily,
			final Keyspace keyspace, final String id) {

		OperationResult<ColumnList<String>> result;
		try {
			result = keyspace.prepareQuery(columnFamily).getKey(id).execute();

		} catch (final ConnectionException e) {
			LOGGER.error("failed to read from C*", e);
			throw new RuntimeException("failed to read from C*", e);
		}

		return result;
	}

	/**
	 * Method to get all the elements store in a column family
	 * 
	 * @param columnFamily
	 * @param keyspace
	 * @return
	 */
	public Rows<String, String> readAll(final ColumnFamily<String, String> columnFamily, final Keyspace keyspace) {
		LOGGER.debug("read()");
		try {
			final OperationResult<CqlResult<String, String>> result = keyspace.prepareQuery(columnFamily)
					.withCql(String.format("SELECT * FROM %s;", columnFamily.getName())).execute();
			return result.getResult().getRows();
		} catch (final ConnectionException e) {
			LOGGER.error("failed to read from C*", e);
			throw new RuntimeException("failed to read from C*", e);
		}
	}

	/**
	 * Method to get all the elements store in a column family with a specific currency
	 * 
	 * @param columnFamily
	 * @param keyspace
	 * @param currencyType
	 * @return
	 */
	public Rows<String, String> readByCurrency(final ColumnFamily<String, String> columnFamily,
			final Keyspace keyspace, final CurrencyType currencyType) {

		LOGGER.debug("read by currency()");
		try {
			final OperationResult<CqlResult<String, String>> result = keyspace
					.prepareQuery(columnFamily)
					.withCql(
							String.format("SELECT * FROM %s WHERE %s=%s", columnFamily.getName(),
									ModelConstants.COL_NAME_CURRENCY, currencyType.getName())).execute();
			return result.getResult().getRows();
		} catch (final ConnectionException e) {
			LOGGER.error("failed to read from C*", e);
			throw new RuntimeException("failed to read from C*", e);
		}

	}

	/**
	 * Method to get all the elements in the currency column family with a specific currency
	 * 
	 * @param currencyType
	 * @return
	 * @throws ConnectionException
	 */
	public Map<Date, ValuePair> readByCurrency(final CurrencyType currencyType) throws ConnectionException {

		final ConnectKeyspaceConfig parameterObject = new ConnectKeyspaceConfig(CURRENCIES_KEYSPACE);
		final Keyspace keyspace = CassandraAstyanaxConnection.connectKeyspace(parameterObject);

		final ColumnFamily<String, String> columnFamily = CassandraAstyanaxConnection.getColumnFamily(
				DAILY_CURRENCIES_FC, keyspace);

		final Rows<String, String> rows = this.readByCurrency(columnFamily, keyspace, currencyType);

		return processCurrencyRateFromDatabase(rows);
	}

	/**
	 * Method to remove all the rows of the currency column family
	 * 
	 * @return
	 */
	public Boolean truncateCurrencyTable() {

		final ConnectKeyspaceConfig parameterObject = new ConnectKeyspaceConfig(CURRENCIES_KEYSPACE);
		final Keyspace keyspace = CassandraAstyanaxConnection.connectKeyspace(parameterObject);

		boolean result = false;
		try {
			keyspace.truncateColumnFamily(DAILY_CURRENCIES_FC);
			result = true;
		} catch (final ConnectionException e) {
			LOGGER.error("Error truncating " + DAILY_CURRENCIES_FC + " table", e);
		}

		return result;
	}

	/**
	 * Method to write a Map of DailyRate in a column family
	 * 
	 * @param columnFamily
	 * @param keyspace
	 * @param dailyRateMap
	 * @return
	 * @throws ConnectionException
	 */
	public Boolean writeDailyCurrencies(final ColumnFamily<String, String> columnFamily, final Keyspace keyspace,
			final Map<String, DailyRate> dailyRateMap) throws ConnectionException {

		Boolean result = true;
		final MutationBatch m = keyspace.prepareMutationBatch();

		for (final java.util.Map.Entry<String, DailyRate> dailyRateEntry : dailyRateMap.entrySet()) {

			for (final CurrencyRate currencyRate : dailyRateEntry.getValue().getCurrencyRates()) {

				final String uuidString = UUIDUtil.cut(UUIDUtil.getTimeUUID().toString(), 16);
				LOGGER.debug("currencyRate" + currencyRate.getCurrencyType() + currencyRate.getRate() + "id: "
						+ uuidString);

				m.withRow(columnFamily, uuidString)
						.putColumn(ModelConstants.COL_NAME_DATE, dailyRateEntry.getValue().getDate(), null)
						.putColumn(ModelConstants.COL_NAME_CURRENCY, currencyRate.getCurrencyType().name(), null)
						.putColumn(ModelConstants.COL_NAME_RATE, currencyRate.getRate(), null);
			}
		}

		try {
			m.execute();
		} catch (final ConnectionException e) {
			LOGGER.error("Error inserting daily rates data", e);
			result = false;
		}
		return result;
	}

	/**
	 * Method to write a Map of DailyRate in the currency column family
	 * 
	 * @param dailyRateMap
	 * @return
	 * @throws ConnectionException
	 */
	public Boolean writeDailyCurrencies(final Map<String, DailyRate> dailyRateMap) throws ConnectionException {

		final ConnectKeyspaceConfig parameterObject = new ConnectKeyspaceConfig(CURRENCIES_KEYSPACE);
		final Keyspace keyspace = CassandraAstyanaxConnection.connectKeyspace(parameterObject);

		final ColumnFamily<String, String> columnFamily = CassandraAstyanaxConnection.getColumnFamily(
				DAILY_CURRENCIES_FC, keyspace);

		keyspace.truncateColumnFamily(columnFamily);

		final Boolean result = writeDailyCurrencies(columnFamily, keyspace, dailyRateMap);

		return result;
	}
}

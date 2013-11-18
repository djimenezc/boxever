package dal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

public class CassandraAstyanaxConnection {

	private static class ConnectionHolder {
		static final CassandraAstyanaxConnection connection = new CassandraAstyanaxConnection();
	}

	private static final String CURRENCIES_KEYSPACE = "currencies";

	private static final String DAILY_CURRENCIES_FC = "dailyCurrencies";

	private static final Logger LOGGER = Logger.getLogger(CassandraAstyanaxConnection.class);

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

	public static Keyspace connectKeyspace(final ConnectKeyspaceConfig parameterObject) {

		final AstyanaxContext<Keyspace> context = connectClusterContext(parameterObject);

		context.start();
		final Keyspace keyspace = context.getEntity();

		return keyspace;
	}

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

	public static ColumnFamily<String, String> createSimpleColumnFamily(final String columnName, final Keyspace keyspace)
			throws ConnectionException {

		final ColumnFamily<String, String> cfStandard = getColumnFamily(columnName, keyspace);

		keyspace.createColumnFamily(cfStandard, null);

		return cfStandard;
	}

	public static ColumnFamily<String, String> getColumnFamily(final String columnName, final Keyspace keyspace)
			throws ConnectionException {

		final ColumnFamily<String, String> cfStandard = ColumnFamily.newColumnFamily(columnName,
				StringSerializer.get(), StringSerializer.get());

		return cfStandard;
	}

	public static CassandraAstyanaxConnection getInstance() {
		return ConnectionHolder.connection;
	}

	private CassandraAstyanaxConnection() {
		super();
	}

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

	public List<ValuePair> readByCurrency(final CurrencyType currencyType) throws ConnectionException {

		final ConnectKeyspaceConfig parameterObject = new ConnectKeyspaceConfig(CURRENCIES_KEYSPACE);
		final Keyspace keyspace = CassandraAstyanaxConnection.connectKeyspace(parameterObject);

		final ColumnFamily<String, String> columnFamily = CassandraAstyanaxConnection.getColumnFamily(
				DAILY_CURRENCIES_FC, keyspace);

		final Rows<String, String> rows = this.readByCurrency(columnFamily, keyspace, currencyType);

		final List<ValuePair> valuePairList = new ArrayList<ValuePair>();

		for (final Row<String, String> row : rows) {

			final ColumnList<String> cols = row.getColumns();

			final String rate = cols.getStringValue(ModelConstants.COL_NAME_RATE, null);
			final String dateString = Double.toString(cols.getDoubleValue(ModelConstants.COL_NAME_DATE, null));

			valuePairList.add(new ValuePair(dateString, rate));
		}

		return valuePairList;
	}

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
						.putColumn(ModelConstants.COL_NAME_DATE, dailyRateEntry.getValue().getDate().getTime(), null)
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

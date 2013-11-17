package dal;

import java.util.Map;

import models.CurrencyRate;
import models.CurrencyType;
import models.DailyRate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.UUIDUtil;

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
import com.netflix.astyanax.model.Rows;
import com.netflix.astyanax.serializers.StringSerializer;
import com.netflix.astyanax.thrift.ThriftFamilyFactory;

public class CassandraAstyanaxConnection {

	private static class ConnectionHolder {
		static final CassandraAstyanaxConnection connection = new CassandraAstyanaxConnection();
	}

	private static final String DAILY_CURRENCIES_FC = "dailyCurrencies";

	private static final Logger LOGGER = LoggerFactory.getLogger(CassandraAstyanaxConnection.class);

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
							String.format("SELECT * FROM %s WHERE %s=%d", columnFamily.getName(),
									ModelConstants.COL_NAME_CURRENCY, currencyType.getName())).execute();
			return result.getResult().getRows();
		} catch (final ConnectionException e) {
			LOGGER.error("failed to read from C*", e);
			throw new RuntimeException("failed to read from C*", e);
		}

	}

	public OperationResult<Void> writeDailyCurrencies(final ColumnFamily<String, String> columnFamily,
			final Keyspace keyspace, final Map<String, DailyRate> dailyRateMap) throws ConnectionException {

		final MutationBatch m = keyspace.prepareMutationBatch();

		for (final java.util.Map.Entry<String, DailyRate> dailyRateEntry : dailyRateMap.entrySet()) {

			final java.util.UUID uuid = UUIDUtil.getTimeUUID();

			for (final CurrencyRate currencyRate : dailyRateEntry.getValue().getCurrencyRates()) {
				m.withRow(columnFamily, UUIDUtil.cut(uuid.toString(), 16))
						.putColumn(ModelConstants.COL_NAME_DATE, dailyRateEntry.getValue().getDate().getTime(), null)
						.putColumn(ModelConstants.COL_NAME_CURRENCY, currencyRate.getCurrencyType().name(), null)
						.putColumn(ModelConstants.COL_NAME_RATE, currencyRate.getRate(), null);
			}
		}

		OperationResult<Void> result = null;

		try {
			result = m.execute();
		} catch (final ConnectionException e) {
			LOGGER.error("Error inserting daily rates data", e);
		}
		return result;
	}

	public OperationResult<Void> writeDailyCurrencies(final Map<String, DailyRate> dailyRateMap)
			throws ConnectionException {

		final ConnectKeyspaceConfig parameterObject = new ConnectKeyspaceConfig();
		final String keyspaceName = "currencies";
		parameterObject.setKeyspace(keyspaceName);
		final Keyspace keyspace = CassandraAstyanaxConnection.connectKeyspace(parameterObject);

		final ColumnFamily<String, String> columnFamily = CassandraAstyanaxConnection.getColumnFamily(
				DAILY_CURRENCIES_FC, keyspace);

		keyspace.truncateColumnFamily(columnFamily);

		return writeDailyCurrencies(columnFamily, keyspace, dailyRateMap);
	}

}

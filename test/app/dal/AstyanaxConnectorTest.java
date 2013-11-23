package app.dal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.CurrencyRate;
import models.CurrencyType;
import models.DailyRate;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.connectionpool.OperationResult;
import com.netflix.astyanax.connectionpool.exceptions.ConnectionException;
import com.netflix.astyanax.ddl.SchemaChangeResult;
import com.netflix.astyanax.model.ColumnFamily;
import com.netflix.astyanax.model.Rows;

import dal.CassandraAstyanaxConnection;
import dal.ConnectKeyspaceConfig;

public class AstyanaxConnectorTest {

	private static final String DAILY_CURRENCIES2 = "dailyCurrencies2";
	public static final String DEFAULT_KEYSPACE = "currencies";
	public static final String TEST_KEYSPACE = "test";
	public static final String TEST_KEYSPACE_2 = "test_drop";

	public static Map<String, DailyRate> generateDailyRatesMap() {

		final Map<String, DailyRate> dailyRatesList = new HashMap<String, DailyRate>();
		List<CurrencyRate> currencyRates = new ArrayList<CurrencyRate>();
		currencyRates.add(new CurrencyRate(CurrencyType.USD, 1.3333D));
		currencyRates.add(new CurrencyRate(CurrencyType.AUD, 2.4333D));
		currencyRates.add(new CurrencyRate(CurrencyType.BGN, 3.3333D));

		DailyRate dailyRate = new DailyRate(new Date(), currencyRates);
		dailyRatesList.put("2013-09-11", dailyRate);

		currencyRates = new ArrayList<CurrencyRate>();
		currencyRates.add(new CurrencyRate(CurrencyType.USD, 1.3333D));
		currencyRates.add(new CurrencyRate(CurrencyType.AUD, 2.4333D));
		currencyRates.add(new CurrencyRate(CurrencyType.BGN, 3.3333D));

		dailyRate = new DailyRate(new Date(), currencyRates);
		dailyRatesList.put("2013-09-12", dailyRate);

		return dailyRatesList;
	}

	private CassandraAstyanaxConnection dataSourceConnector;

	@Test
	public void connectCurrenciesKeyspaceTest() {

		final ConnectKeyspaceConfig parameterObject = new ConnectKeyspaceConfig();
		parameterObject.setKeyspace(DEFAULT_KEYSPACE);
		final Keyspace keyspace = CassandraAstyanaxConnection.connectKeyspace(parameterObject);

		assertEquals(DEFAULT_KEYSPACE, keyspace.getKeyspaceName());
	}

	private Keyspace createTestKeySpace(final String keyspaceId) throws ConnectionException {

		final ConnectKeyspaceConfig parameterObject = new ConnectKeyspaceConfig();
		parameterObject.setKeyspace(keyspaceId);
		final Keyspace keyspace = CassandraAstyanaxConnection.createKeyspace(parameterObject);

		return keyspace;
	}

	private void dropKeyspace(final Keyspace keyspace) throws ConnectionException {

		final OperationResult<SchemaChangeResult> operationResult = keyspace.dropKeyspace();
		assertNotNull(operationResult.getResult().getSchemaId());
	}

	@Before
	public void setUp() {
		dataSourceConnector = CassandraAstyanaxConnection.getInstance();
	}

	@Test
	public void testConnection() {

		final ConnectKeyspaceConfig parameterObject = new ConnectKeyspaceConfig();
		final Keyspace keyspace = CassandraAstyanaxConnection.connectKeyspace(parameterObject);

		assertEquals(ConnectKeyspaceConfig.DEFAULT_KEYSPACE, keyspace.getKeyspaceName());
	}

	@Test
	public void testCreateAndDropKeyspace() throws ConnectionException {

		final Keyspace keyspace = createTestKeySpace(TEST_KEYSPACE);

		assertEquals(TEST_KEYSPACE, keyspace.getKeyspaceName());

		dropKeyspace(keyspace);
	}

	@Test
	public void testCreateColumnFamilyWithIndexes() throws ConnectionException {

		final Keyspace keyspace = createTestKeySpace(TEST_KEYSPACE_2);

		assertEquals(TEST_KEYSPACE_2, keyspace.getKeyspaceName());

		ColumnFamily<String, String> columnFamily;
		try {
			columnFamily = CassandraAstyanaxConnection.createSimpleColumnFamily("testColumnFamily", keyspace);
			assertNotNull(columnFamily);

			keyspace.createColumnFamily(
					columnFamily,
					ImmutableMap
							.<String, Object> builder()
							.put("column_metadata",
									ImmutableMap
											.<String, Object> builder()
											.put("Index1",
													ImmutableMap.<String, Object> builder()
															.put("validation_class", "UTF8Type")
															.put("index_name", "Index1").put("index_type", "KEYS")
															.build())
											.put("Index2",
													ImmutableMap.<String, Object> builder()
															.put("validation_class", "UTF8Type")
															.put("index_name", "Index2").put("index_type", "KEYS")
															.build()).build()).build());
		} catch (final ConnectionException e) {
			dropKeyspace(keyspace);
		}
	}

	@Test
	public void testCreateColumnsFamily() throws ConnectionException {

		final Keyspace keyspace = createTestKeySpace(TEST_KEYSPACE);

		assertEquals(TEST_KEYSPACE, keyspace.getKeyspaceName());

		final ColumnFamily<String, String> columnFamily = CassandraAstyanaxConnection.createSimpleColumnFamily(
				"testColumnFamily", keyspace);
		assertNotNull(columnFamily);

		keyspace.dropColumnFamily("testColumnFamily");

		dropKeyspace(keyspace);
	}

	@Test
	public void testInsertDailyRates() throws ConnectionException {

		final ConnectKeyspaceConfig parameterObject = new ConnectKeyspaceConfig();
		parameterObject.setKeyspace(DEFAULT_KEYSPACE);
		final Keyspace keyspace = CassandraAstyanaxConnection.connectKeyspace(parameterObject);

		assertEquals(DEFAULT_KEYSPACE, keyspace.getKeyspaceName());

		final Map<String, DailyRate> dailyRatesMap = generateDailyRatesMap();

		final ColumnFamily<String, String> columnFamily = CassandraAstyanaxConnection.getColumnFamily(
				DAILY_CURRENCIES2, keyspace);

		final Boolean result = dataSourceConnector.writeDailyCurrencies(columnFamily, keyspace, dailyRatesMap);

		dataSourceConnector.readAll(columnFamily, keyspace);

		if (columnFamily == null) {
			keyspace.dropColumnFamily(columnFamily);
		}
		assertTrue(result);
	}

	@Test
	public void testReadDailyRate() throws ConnectionException {

		final ConnectKeyspaceConfig parameterObject = new ConnectKeyspaceConfig();
		final String keyspaceName = DEFAULT_KEYSPACE;
		parameterObject.setKeyspace(keyspaceName);
		final Keyspace keyspace = CassandraAstyanaxConnection.connectKeyspace(parameterObject);

		final ColumnFamily<String, String> columnFamily = CassandraAstyanaxConnection.getColumnFamily(
				DAILY_CURRENCIES2, keyspace);
		assertNotNull(columnFamily);

		dataSourceConnector.read(columnFamily, keyspace, "bfc9e920-4f9c-11");
	}

	@Test
	public void testReadDailyRateByCurrency() throws ConnectionException {

		final ConnectKeyspaceConfig parameterObject = new ConnectKeyspaceConfig();
		final String keyspaceName = DEFAULT_KEYSPACE;
		parameterObject.setKeyspace(keyspaceName);
		final Keyspace keyspace = CassandraAstyanaxConnection.connectKeyspace(parameterObject);

		final ColumnFamily<String, String> columnFamily = CassandraAstyanaxConnection.getColumnFamily(
				DAILY_CURRENCIES2, keyspace);
		assertNotNull(columnFamily);

		final Rows<String, String> rows = dataSourceConnector.readByCurrency(columnFamily, keyspace, CurrencyType.USD);

		assertTrue(rows.size() > 0);
	}

	@Test
	public void testReadDailyRateByDifferentCurrencies() throws ConnectionException {

		final ConnectKeyspaceConfig parameterObject = new ConnectKeyspaceConfig();
		final String keyspaceName = DEFAULT_KEYSPACE;
		parameterObject.setKeyspace(keyspaceName);
		final Keyspace keyspace = CassandraAstyanaxConnection.connectKeyspace(parameterObject);

		final ColumnFamily<String, String> columnFamily = CassandraAstyanaxConnection.getColumnFamily(
				DAILY_CURRENCIES2, keyspace);
		assertNotNull(columnFamily);

		for (final CurrencyType currencyType : CurrencyType.values()) {
			final Rows<String, String> rows = dataSourceConnector.readByCurrency(columnFamily, keyspace, currencyType);
			assertTrue(rows.size() > 0);
		}
	}
}

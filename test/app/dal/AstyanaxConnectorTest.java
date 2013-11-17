package app.dal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

import dal.CassandraAstyanaxConnection;
import dal.ConnectKeyspaceConfig;

public class AstyanaxConnectorTest {

	private static final String TEST_KEYSPACE = "test";
	private static final String TEST_KEYSPACE_2 = "test_drop";
	private CassandraAstyanaxConnection dataSourceConnector;

	@Test
	public void connectCurrenciesKeyspaceTest() {

		final ConnectKeyspaceConfig parameterObject = new ConnectKeyspaceConfig();
		final String keyspaceName = "currencies";
		parameterObject.setKeyspace(keyspaceName);
		final Keyspace keyspace = CassandraAstyanaxConnection.connectKeyspace(parameterObject);

		assertEquals(keyspaceName, keyspace.getKeyspaceName());
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

	private List<DailyRate> generateDailyRatesList() {

		final List<DailyRate> dailyRatesList = new ArrayList<DailyRate>();
		List<CurrencyRate> currencyRates = new ArrayList<CurrencyRate>();
		currencyRates.add(new CurrencyRate(CurrencyType.USD, 1.3333D));
		currencyRates.add(new CurrencyRate(CurrencyType.AUD, 2.4333D));
		currencyRates.add(new CurrencyRate(CurrencyType.BGN, 3.3333D));
		DailyRate dailyRate = new DailyRate(new Date(), currencyRates);
		dailyRatesList.add(dailyRate);
		currencyRates = new ArrayList<CurrencyRate>();
		currencyRates.add(new CurrencyRate(CurrencyType.USD, 1.3333D));
		currencyRates.add(new CurrencyRate(CurrencyType.AUD, 2.4333D));
		currencyRates.add(new CurrencyRate(CurrencyType.BGN, 3.3333D));
		dailyRate = new DailyRate(new Date(), currencyRates);
		dailyRatesList.add(dailyRate);

		return dailyRatesList;
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
		final String keyspaceName = "currencies";
		parameterObject.setKeyspace(keyspaceName);
		final Keyspace keyspace = CassandraAstyanaxConnection.connectKeyspace(parameterObject);

		assertEquals(keyspaceName, keyspace.getKeyspaceName());

		final List<DailyRate> dailyRatesList = generateDailyRatesList();
		final String columnFamilyName = "dailyCurrencies2";

		final ColumnFamily<String, String> columnFamily = CassandraAstyanaxConnection.getColumnFamily(columnFamilyName,
				keyspace);

		final OperationResult<Void> result = dataSourceConnector.writeDailyCurrencies(columnFamily, keyspace,
				dailyRatesList);

		dataSourceConnector.readAll(columnFamily, keyspace);

		if (columnFamily == null) {
			keyspace.dropColumnFamily(columnFamily);
		}
		assertNotNull(result);
	}

	@Test
	public void testReadDailyRate() throws ConnectionException {

		final ConnectKeyspaceConfig parameterObject = new ConnectKeyspaceConfig();
		final String keyspaceName = "currencies";
		parameterObject.setKeyspace(keyspaceName);
		final Keyspace keyspace = CassandraAstyanaxConnection.connectKeyspace(parameterObject);

		final String columnFamilyName = "dailyCurrencies2";

		final ColumnFamily<String, String> columnFamily = CassandraAstyanaxConnection.getColumnFamily(columnFamilyName,
				keyspace);
		assertNotNull(columnFamily);

		dataSourceConnector.read(columnFamily, keyspace, "bfc9e920-4f9c-11");
	}
}

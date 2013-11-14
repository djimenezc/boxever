package app.controllers.dal;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.netflix.astyanax.Keyspace;

import dal.AstyanaxConnector;
import dal.ConnectKeyspaceConfig;
import dal.DataSourceConnector;

public class AstyanaxConnectorTest {

	private DataSourceConnector dataSourceConnector;

	@Test
	public void connectCurrenciesKeyspaceTest() {

		final ConnectKeyspaceConfig parameterObject = new ConnectKeyspaceConfig();
		final String keyspaceName = "currencies";
		parameterObject.setKeyspace(keyspaceName);
		final Keyspace keyspace = dataSourceConnector.connectKeyspace(parameterObject);

		assertEquals(keyspaceName, keyspace.getKeyspaceName());
	}

	@Test
	public void connectTest() {

		final ConnectKeyspaceConfig parameterObject = new ConnectKeyspaceConfig();
		final Keyspace keyspace = dataSourceConnector.connectKeyspace(parameterObject);

		assertEquals(ConnectKeyspaceConfig.DEFAULT_KEYSPACE, keyspace.getKeyspaceName());
	}

	@Before
	public void setUp() {
		dataSourceConnector = new AstyanaxConnector();
	}
}

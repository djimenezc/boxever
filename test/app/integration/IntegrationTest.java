package app.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static play.test.Helpers.HTMLUNIT;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.inMemoryDatabase;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;

import java.nio.charset.Charset;
import java.util.Map;

import models.CurrencyType;
import models.DailyRate;

import org.junit.Test;

import play.libs.F.Callback;
import play.test.TestBrowser;
import util.FileUtil;
import xml.XmlProcessor;
import app.dal.AstyanaxConnectorTest;

import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.model.ColumnFamily;
import com.netflix.astyanax.model.Rows;

import dal.CassandraAstyanaxConnection;
import dal.ConnectKeyspaceConfig;

public class IntegrationTest {

	private static final String FILEPATH_STRING = "/test/resources/eurofxref-hist-90d.xml";

	/**
	 * add your integration test here in this example we just check if the welcome page is being shown
	 */
	@Test
	public void testPageLoaded() {
		running(testServer(3333, fakeApplication(inMemoryDatabase())), HTMLUNIT, new Callback<TestBrowser>() {
			@Override
			public void invoke(final TestBrowser browser) {
				// browser.goTo("http://localhost:3333");
				// assertThat(browser.pageSource()).contains("Your new application is ready.");
			}
		});
	}

	@Test
	public void testProcessXmlFileAndStoreInDatabase() throws Exception {

		final String projectPath = System.getProperty("user.dir");
		final String xmlString = FileUtil.readFile(projectPath + FILEPATH_STRING, Charset.forName("UTF-8"));

		final Map<String, DailyRate> dailyRateList = XmlProcessor.extractDailyRates(xmlString);

		System.out.println("# dailyRate processed: " + dailyRateList.size());

		assertTrue(dailyRateList.size() == 65);

		final ConnectKeyspaceConfig parameterObject = new ConnectKeyspaceConfig();
		parameterObject.setKeyspace(AstyanaxConnectorTest.DEFAULT_KEYSPACE);
		final Keyspace keyspace = CassandraAstyanaxConnection.connectKeyspace(parameterObject);

		assertEquals(AstyanaxConnectorTest.DEFAULT_KEYSPACE, keyspace.getKeyspaceName());

		final String columnFamilyName = "dailyCurrencies2";

		final ColumnFamily<String, String> columnFamily = CassandraAstyanaxConnection.getColumnFamily(columnFamilyName,
				keyspace);

		keyspace.truncateColumnFamily(columnFamily);

		final Boolean result = CassandraAstyanaxConnection.getInstance().writeDailyCurrencies(columnFamily, keyspace,
				dailyRateList);

		if (columnFamily == null) {
			keyspace.dropColumnFamily(columnFamily);
		}
		assertTrue(result);

		final Rows<String, String> rows = CassandraAstyanaxConnection.getInstance().readAll(columnFamily, keyspace);

		assertTrue(rows.size() == dailyRateList.values().size()
				* dailyRateList.values().iterator().next().getCurrencyRates().size());

		final Rows<String, String> rowByCurrency = CassandraAstyanaxConnection.getInstance().readByCurrency(
				columnFamily, keyspace, CurrencyType.USD);

		assertTrue(rowByCurrency.size() == dailyRateList.values().size());
	}

}

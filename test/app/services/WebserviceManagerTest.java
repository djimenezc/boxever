package app.services;

import static org.junit.Assert.assertEquals;
import static play.test.Helpers.HTMLUNIT;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.inMemoryDatabase;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import play.libs.F.Callback;
import play.mvc.Result;
import play.test.TestBrowser;
import actors.CurrencyActor;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/components.xml" })
public class WebserviceManagerTest {

	@Autowired
	private CurrencyActor currencyActor;

	@Test
	public void connectCurrKeyspaceTest() {

		running(testServer(3333, fakeApplication(inMemoryDatabase())), HTMLUNIT, new Callback<TestBrowser>() {
			@Override
			public void invoke(final TestBrowser browser) {
				final Result apiResult = currencyActor
						.feedTitle("http://www.ecb.europa.eu/stats/eurofxref/eurofxref-hist-90d.xml");

				assertEquals("", apiResult);
			}
		});

	}

	// @BeforeClass
	// public static void globalSetup() {
	// webserviceManager = new WebserviceManager();
	// }

}

package app.services;

import static play.test.Helpers.contentAsString;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.route;
import static play.test.Helpers.running;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import play.mvc.Result;
import actors.CurrencyActor;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/components.xml" })
public class CurrencyActorTest {

	@Autowired
	private CurrencyActor currencyActor;

	@Test
	public void connectCurrKeyspaceTest() {

		running(fakeApplication(), new Runnable() {
			@Override
			public void run() {

				final String username = "Aerus";
				final Result res = route(fakeRequest("GET", "/").withSession("username", username).withSession("key",
						"value"));
				assert contentAsString(res).contains(username);

				final Result apiResult = currencyActor
						.feedTitle("http://www.ecb.europa.eu/stats/eurofxref/eurofxref-hist-90d.xml");

				// assertEquals("", apiResult);
			}
		});

	}

	// @BeforeClass
	// public static void globalSetup() {
	// webserviceManager = new WebserviceManager();
	// }

}

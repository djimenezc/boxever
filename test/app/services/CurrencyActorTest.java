package app.services;

import static org.junit.Assert.assertEquals;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.fakeGlobal;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.inMemoryDatabase;
import static play.test.Helpers.route;
import static play.test.Helpers.running;
import static play.test.Helpers.session;
import static play.test.Helpers.start;

import org.junit.Before;
import org.junit.Test;

import play.api.test.FakeApplication;
import play.api.test.WithApplication;
import play.mvc.Http.Session;
import play.mvc.Result;
import actors.CurrencyActor;

// @RunWith(SpringJUnit4ClassRunner.class)
// @ContextConfiguration(locations = { "classpath:/components.xml" })
public class CurrencyActorTest extends WithApplication {

	// @Autowired
	// private CurrencyActor currencyActor;

	public CurrencyActorTest(final FakeApplication app) {
		super(app);
	}

	@Test
	public void connectCurrKeyspaceTest() {

		running(fakeApplication(), new Runnable() {
			@Override
			public void run() {
				// TODO remove
				final String username = "Aerus";
				final Result res = route(fakeRequest("GET", "/").withSession("username", username).withSession("key",
						"value"));
				// assert contentAsString(res).contains(username);

				final Session sessionFake = session(res);

				// CurrencyActor.
				final Result apiResult = CurrencyActor
						.readXmlDataFromApi("http://www.ecb.europa.eu/stats/eurofxref/eurofxref-hist-90d.xml");

				assertEquals("", apiResult.toString());
			}
		});

	}

	@Before
	public void setUp() {
		start(fakeApplication(inMemoryDatabase(), fakeGlobal()));
		// Ebean.save((List) Yaml.load("test-data.yml"));
	}

}

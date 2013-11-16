package actors;

import org.springframework.stereotype.Service;

import play.libs.F.Function;
import play.libs.WS;
import play.mvc.Controller;
import play.mvc.Result;

@Service("currencyActor")
public class CurrencyActor extends Controller {

	// @Override
	// public Result call(final Context ctx) throws Throwable {
	// Context.current.set(ctx);
	//
	// return null;
	// }

	public Result feedTitle(final String feedUrl) {
		return async(WS.url(feedUrl).get().map(new Function<WS.Response, Result>() {
			@Override
			public Result apply(final WS.Response response) {
				return ok("Feed title:" + response.asJson().findPath("title"));
			}
		}));
	}

}
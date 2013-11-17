package app.models;

import static org.junit.Assert.assertEquals;
import models.CurrencyType;

import org.junit.Test;

/**
 * 
 * tests to verify that the CurrencyType return the correct elements
 * 
 * @author david
 * 
 */
public class CurrencyTypeTest {

	@Test
	public void testBuildCurrencyList() {

		assertEquals(CurrencyType.values().length, CurrencyType.buildCurrencyList().size());
	}

}

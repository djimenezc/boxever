package models;

import java.util.ArrayList;
import java.util.List;

import base.ValuePair;

/**
 * Enumeration that represents the different types of currencies
 * 
 * @author david
 * 
 */
public enum CurrencyType {

	USD("USD"),
	JPY("JPY"),
	BGN("BGN"),
	CZK(""),
	DKK(""),
	GBP(""),
	HUF(""),
	LTL(""),
	LVL(""),
	PLN(""),
	RON(""),
	SEK(""),
	CHF(""),
	NOK(""),
	HRK(""),
	RUB(""),
	TRY(""),
	AUD(""),
	BRL(""),
	CAD(""),
	CNY(""),
	HKD(""),
	IDR(""),
	ILS(""),
	INR(""),
	KRW(""),
	MXN(""),
	MYR(""),
	NZD(""),
	PHP(""),
	SGD(""),
	THB(""),
	ZAR("");

	public static List<ValuePair> buildCurrencyList() {

		final List<ValuePair> result = new ArrayList<ValuePair>();

		for (final CurrencyType b : CurrencyType.values()) {

			result.add(new ValuePair(b.name(), b.getName()));
		}

		return result;
	}

	private String name;

	private CurrencyType(final String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	};

}

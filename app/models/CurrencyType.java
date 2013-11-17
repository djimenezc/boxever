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
	CZK("CZK"),
	DKK("DKK"),
	GBP("GBP"),
	HUF("HUF"),
	LTL("LTL"),
	LVL("LVL"),
	PLN("PLN"),
	RON("RON"),
	SEK("SEK"),
	CHF("CHF"),
	NOK("NOK"),
	HRK("HRK"),
	RUB("RUB"),
	TRY("TRY"),
	AUD("AUD"),
	BRL("BRL"),
	CAD("CAD"),
	CNY("CNY"),
	HKD("HKD"),
	IDR("IDR"),
	ILS("ILS"),
	INR("INR"),
	KRW("KRW"),
	MXN("MXN"),
	MYR("MYR"),
	NZD("NZD"),
	PHP("PHP"),
	SGD("SGD"),
	THB("THB"),
	ZAR("ZAR");

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

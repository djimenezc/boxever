package models;

/**
 * Bean that represent the exchange rate for a concrete currency
 * 
 * @author david
 * 
 */
public class CurrencyRate {

	private CurrencyType currencyType;
	private Double rate;

	/**
	 * Constructor
	 * 
	 * @param currencyType
	 * @param rate
	 */
	public CurrencyRate(final CurrencyType currencyType, final Double rate) {
		this.currencyType = currencyType;
		this.rate = rate;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final CurrencyRate other = (CurrencyRate) obj;
		if (currencyType != other.currencyType) {
			return false;
		}
		if (rate == null) {
			if (other.rate != null) {
				return false;
			}
		}
		else if (!rate.equals(other.rate)) {
			return false;
		}
		return true;
	}

	/**
	 * Getter for the attribute currencyType
	 * 
	 * @return the currencyType
	 */
	public CurrencyType getCurrencyType() {
		return currencyType;
	}

	/**
	 * Getter for the attribute rate
	 * 
	 * @return the rate
	 */
	public Double getRate() {
		return rate;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (currencyType == null ? 0 : currencyType.hashCode());
		result = prime * result + (rate == null ? 0 : rate.hashCode());
		return result;
	}

	/**
	 * @param currencyType
	 *            the currencyType to set
	 */
	public void setCurrencyType(final CurrencyType currencyType) {
		this.currencyType = currencyType;
	}

	/**
	 * @param rate
	 *            the rate to set
	 */
	public void setRate(final Double rate) {
		this.rate = rate;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "CurrencyRate [currencyType=" + currencyType + ", rate=" + rate + "]";
	}

}

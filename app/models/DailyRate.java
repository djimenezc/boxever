package models;

import java.sql.Date;

import javax.persistence.Id;

import play.db.ebean.Model;

/**
 * Bean that represent the exchange rate for a concrete currency in a concrete day
 * 
 * @author david
 * 
 */
public class DailyRate extends Model {

	private static final long serialVersionUID = 8047134644652840084L;

	@Id
	private Long id;
	private final Date date;
	private final CurrencyRate currencyRate;

	/**
	 * @param date
	 * @param currencyRate
	 */
	public DailyRate(final Date date, final CurrencyRate currencyRate) {
		super();
		this.date = date;
		this.currencyRate = currencyRate;
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
		final DailyRate other = (DailyRate) obj;
		if (currencyRate == null) {
			if (other.currencyRate != null) {
				return false;
			}
		}
		else if (!currencyRate.equals(other.currencyRate)) {
			return false;
		}
		if (date == null) {
			if (other.date != null) {
				return false;
			}
		}
		else if (!date.equals(other.date)) {
			return false;
		}
		return true;
	}

	public Long getId() {
		return id;
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
		result = prime * result + (currencyRate == null ? 0 : currencyRate.hashCode());
		result = prime * result + (date == null ? 0 : date.hashCode());
		return result;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "DailyRate [date=" + date + ", currencyRate=" + currencyRate + "]";
	}

}

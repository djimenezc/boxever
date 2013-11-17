package models;

import java.util.Date;
import java.util.List;

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
	private final List<CurrencyRate> currencyRates;

	/**
	 * @param date
	 * @param currencyRate
	 */
	public DailyRate(final Date date, final List<CurrencyRate> currencyRates) {
		super();
		this.date = date;
		this.currencyRates = currencyRates;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final DailyRate other = (DailyRate) obj;
		if (currencyRates == null) {
			if (other.currencyRates != null) {
				return false;
			}
		}
		else if (!currencyRates.equals(other.currencyRates)) {
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
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		}
		else if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}

	public List<CurrencyRate> getCurrencyRates() {
		return currencyRates;
	}

	public Date getDate() {
		return date;
	}

	public Long getId() {
		return id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (currencyRates == null ? 0 : currencyRates.hashCode());
		result = prime * result + (date == null ? 0 : date.hashCode());
		result = prime * result + (id == null ? 0 : id.hashCode());
		return result;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "DailyRate [id=" + id + ", date=" + date + ", currencyRates=" + currencyRates + "]";
	}

}

package dal;

import com.netflix.astyanax.annotations.Component;

public class DailyCurrencyType {

	@Component(ordinal = 0)
	String currencyId;
	@Component(ordinal = 1)
	Double currencyRate;
	public String getCurrencyId() {
		return currencyId;
	}
	public void setCurrencyId(String currencyId) {
		this.currencyId = currencyId;
	}
	public Double getCurrencyRate() {
		return currencyRate;
	}
	public void setCurrencyRate(Double currencyRate) {
		this.currencyRate = currencyRate;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	@Component(ordinal = 2)
	long timestamp;

}
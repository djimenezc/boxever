package base;

/**
 * Simple bean representing a name/value entity.
 * 
 * @author David Jimenez
 */
public class ValuePair extends NameObject {

	private static final long serialVersionUID = 1L;

	private String value;

	public ValuePair() {

	}

	public ValuePair(final String value, final String name) {
		this.value = value;
		setName(name);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ValuePair)) {
			return false;
		}
		final ValuePair other = (ValuePair) obj;
		if (value == null) {
			if (other.value != null) {
				return false;
			}
		}
		else if (!value.equals(other.value)) {
			return false;
		}
		return true;
	}

	public String getValue() {
		return value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (value == null ? 0 : value.hashCode());
		return result;
	}

	public void setValue(final String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "ValuePair [value=" + value + " name:" + getName() + "]";
	}

}

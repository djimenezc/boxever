package base;

/**
 * Bean that is necessary to extend to build a bean that contains a name field and has to implement the methods:
 * toString, hasCode and equals
 * 
 * @author david
 * 
 */
public abstract class NameObject extends BaseObject {

	private static final long serialVersionUID = -927474631214793470L;
	private String name;

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

}

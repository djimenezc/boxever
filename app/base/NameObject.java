package base;

public abstract class NameObject extends BaseObject {

  private static final long serialVersionUID = -927474631214793470L;
  private String name;

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

}

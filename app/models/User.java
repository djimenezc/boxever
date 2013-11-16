package models;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.db.ebean.Model;

@Entity
public class User extends Model {

    /**
	 * 
	 */
	private static final long serialVersionUID = -4436969634220710538L;
	
	@Id
    public String email;
    public String name;
    public String password;
    
    public User(String email, String name, String password) {
      this.email = email;
      this.name = name;
      this.password = password;
    }

    public static Finder<String,User> find = new Finder<String,User>(
        String.class, User.class
    ); 
}
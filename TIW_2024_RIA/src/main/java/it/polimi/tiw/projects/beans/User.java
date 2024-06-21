package it.polimi.tiw.projects.beans;

public class User {
	private int ID;
	private String username;
	private String email;
	private String password;
	
	/**
	 * getter and setter for "ID"
	 */
	public void setId(int ID) {
		this.ID = ID;
	}
	
	public int getId() {
		return ID;
	}
	
	/**
	 * getter and setter for "username"
	 */
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getUsername() {
		return username;
	}
	
	/**
	 * getter and setter for "password"
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getPassword() {
		return password;
	}

	/**
	 * getter and setter for "email"
	 */
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

}

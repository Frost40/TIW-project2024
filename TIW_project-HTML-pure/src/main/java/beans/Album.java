package beans;

import java.sql.Date;

public class Album {
	private int ID;
	private String title;
	private Date creationDate;
	private int userId;
	
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
	 * getter and setter for "title"
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getTitle() {
		return title;
	}
	
	/**
	 * getter and setter for "creationDate"
	 */
	public void setCreationDate(Date dateOfCreation) {
		this.creationDate = dateOfCreation;
	}
	
	public Date getCreationDate() {
		return creationDate;
	}
	
	/**
	 * getter and setter for "userId"
	 */
	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getUserId() {
		return userId;
	}
}

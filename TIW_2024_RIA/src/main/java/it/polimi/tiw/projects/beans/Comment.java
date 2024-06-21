package it.polimi.tiw.projects.beans;

public class Comment {
	private int ID;
	private String text;
	private int userId;
	private int imageId;
	
	/**
	 * getter and setter for "ID"
	 */
	public int getID() {
		return ID;
	}
	
	public void setID(int iD) {
		this.ID = iD;
	}

	/**
	 * getter and setter for "text"
	 */
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	/**
	 * getter and setter for "userId"
	 */
	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	/**
	 * getter and setter for "imageId"
	 */
	public int getImageId() {
		return imageId;
	}

	public void setImageId(int imageId) {
		this.imageId = imageId;
	}
	
}

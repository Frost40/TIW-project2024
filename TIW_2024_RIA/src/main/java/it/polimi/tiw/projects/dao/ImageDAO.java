package it.polimi.tiw.projects.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import it.polimi.tiw.projects.beans.Image;

public class ImageDAO {
	private Connection connection;
	
	public ImageDAO(Connection connection) {
		this.connection = connection;
	}
	
	public Image getImageById(int imageId) throws SQLException {
		Image image = null;
		String performedAction = " getting all image's info by using the 'imageId' ";
		String query = "SELECT * FROM Image WHERE id = ?";
		PreparedStatement preparedStatement = null;
	    ResultSet resultSet = null;
	    
	    try {
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setInt(1, imageId);
			
			resultSet = preparedStatement.executeQuery();

			while(resultSet.next()) {
				image = new Image();
				image.setId(resultSet.getInt("id"));
				image.setTitle(resultSet.getString("title"));
				image.setPath(resultSet.getString("path"));
				image.setDescription(resultSet.getString("description"));
				image.setCreationDate(resultSet.getDate("creationDate"));
				image.setUserId(resultSet.getInt("userId"));
			}
			
	    } catch (SQLException e) {
			throw new SQLException("Error accessing the DB when" + performedAction + "[ " + e.getMessage() + " ]");

		} finally {

			try {
				resultSet.close();

			} catch (Exception e) {
				throw new SQLException("Error closing the result set when" + performedAction + "[ " + e.getMessage() + " ]");
			}

			try {
				preparedStatement.close();

			} catch (Exception e) {
				throw new SQLException("Error closing the statement when" + performedAction + "[ " + e.getMessage() + " ]");
			}
		}
	    
	    return image;
	}
	
	public List<Image> getImagesByAlbumId(int albumId) throws SQLException {
		List<Image> images = new ArrayList<Image>();
		String performedAction = " getting all images in 'albumId' by joining 'ImageAlbumLink' with 'Image' ";
		String query = "SELECT Image.* " +
	               "FROM Image " +
	               "JOIN ImageAlbumLink ON Image.id = ImageAlbumLink.imageId " +
	               "WHERE ImageAlbumLink.albumId = ?";
		PreparedStatement preparedStatement = null;
	    ResultSet resultSet = null;
	    
	    try {
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setInt(1, albumId);
			
			resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {
				Image image = new Image();
				image.setId(resultSet.getInt("id"));
				image.setTitle(resultSet.getString("title"));
				image.setPath(resultSet.getString("path"));
				image.setDescription(resultSet.getString("description"));
				image.setCreationDate(resultSet.getDate("creationDate"));
				image.setUserId(resultSet.getInt("userId"));
				
				images.add(image);
			}
			
	    } catch (SQLException e) {
			throw new SQLException("Error accessing the DB when" + performedAction + "[ " + e.getMessage() + " ]");

		} finally {

			try {
				resultSet.close();

			} catch (Exception e) {
				throw new SQLException("Error closing the result set when" + performedAction + "[ " + e.getMessage() + " ]");
			}

			try {
				preparedStatement.close();

			} catch (Exception e) {
				throw new SQLException("Error closing the statement when" + performedAction + "[ " + e.getMessage() + " ]");
			}
		}
	    
	    return images;
	}
	
	public int uploadImage(String title, String description, String path, int creatorid) throws SQLException  {
		int generatedId = 0;
		String performedAction = " uploading infos relative to a new image in the database ";
		String query = "INSERT into Image (title, description, path, creationDate, userId) VALUES (?, ?, ?, NOW(), ?)";
		PreparedStatement preparedStatement = null;
	    ResultSet resultSet = null;
		
		try {
	        preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
	        preparedStatement.setString(1, title);
	        preparedStatement.setString(2, description);
	        preparedStatement.setString(3, path);
	        preparedStatement.setInt(4, creatorid);
	        preparedStatement.executeUpdate();
	        
	        resultSet = preparedStatement.getGeneratedKeys();
	        
	        while (resultSet.next()) {
	        	generatedId =  resultSet.getInt(1);
	        }

	    } catch (SQLException e) {
			throw new SQLException("Error accessing the DB when" + performedAction + "[ " + e.getMessage() + " ]");

		} finally {

			try {
				resultSet.close();

			} catch (Exception e) {
				throw new SQLException("Error closing the result set when" + performedAction + "[ " + e.getMessage() + " ]");
			}

			try {
				preparedStatement.close();

			} catch (Exception e) {
				throw new SQLException("Error closing the statement when" + performedAction + "[ " + e.getMessage() + " ]");
			}
		}
		return generatedId;
	}
	
	public void deleteImage(int id) throws SQLException {
		String performedAction = " removing image info from the database ";
		String query = "DELETE FROM Image WHERE id = ?";
		PreparedStatement preparedStatement = null;
		
		try {
	        preparedStatement = connection.prepareStatement(query);
	        preparedStatement.setInt(1, id);
	        preparedStatement.executeUpdate();

	    } catch (SQLException e) {
			throw new SQLException("Error accessing the DB when" + performedAction + "[ " + e.getMessage() + " ]");

		} finally {

			try {
				preparedStatement.close();

			} catch (Exception e) {
				throw new SQLException("Error closing the statement when" + performedAction + "[ " + e.getMessage() + " ]");
			}
		}
	}
}

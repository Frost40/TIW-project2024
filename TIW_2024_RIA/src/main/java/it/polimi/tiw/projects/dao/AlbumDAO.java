package it.polimi.tiw.projects.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import it.polimi.tiw.projects.beans.Album;
import it.polimi.tiw.projects.utils.Message;

public class AlbumDAO {
	private Connection connection;
	
	public AlbumDAO(Connection connection) {
		this.connection = connection;
	}
	
	public int getAlbumAllPhotosId(int userId) throws SQLException {
	    int albumId = 0;
		String performedAction = " getting album id via pair (title, creatorId) ";
		String query = "SELECT id FROM Album WHERE userId = ? AND title = ?";
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		try {
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setInt(1, userId);
			preparedStatement.setString(2, "allPhotos");
			
			resultSet = preparedStatement.executeQuery();

			while(resultSet.next()) {
				albumId = resultSet.getInt("id");
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
		
		return albumId;
	}
	
	public List<Album> getUserAlbums(int id) throws SQLException {
		List<Album> albumsList = new ArrayList<>();
		String performedAction = " getting all user's albums ";
		String query = "SELECT * FROM Album WHERE userId = ? ORDER BY CASE WHEN title = ? THEN 0 ELSE 1 END, creationDate DESC";	//All albums will by ordered by decreasing creationDate except for the one that contains all images that will always show as first 
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		try {
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setInt(1, id);
			preparedStatement.setString(2, "allPhotos");
			
			resultSet = preparedStatement.executeQuery();

			while(resultSet.next()) {
				Album album = new Album();
				album.setId(resultSet.getInt("id"));
				album.setTitle(resultSet.getString("title"));
				album.setCreationDate(resultSet.getDate("creationDate"));
				album.setUserId(resultSet.getInt("userId"));
				
				albumsList.add(album);
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
		
		return albumsList;
	}
	
	public List<Album> getAlbumsNotFromUser(int id) throws SQLException {
		List<Album> albumsList = new ArrayList<>();
		String performedAction = " getting all albums not form user ";
		String query = "SELECT * FROM Album WHERE userId <> ? AND title <> ? ORDER BY creationDate DESC";
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		try {
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setInt(1, id);
			preparedStatement.setString(2, "allPhotos");

			resultSet = preparedStatement.executeQuery();

			while(resultSet.next()) {
				Album album = new Album();
				album.setId(resultSet.getInt("id"));
				album.setTitle(resultSet.getString("title"));
				album.setCreationDate(resultSet.getDate("creationDate"));
				album.setUserId(resultSet.getInt("userId"));
				
				albumsList.add(album);
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
						
		return albumsList;
	}
	
	public Message getUsernameCreatorAndTitle(int albumId) throws SQLException  {
		Message message = new Message();
		String performedAction = " getting username of the album creator ";
		String query = "SELECT User.username, Album.title FROM Album JOIN User ON Album.userId = User.id WHERE Album.id = ?";
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		try {
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setInt(1, albumId);

			resultSet = preparedStatement.executeQuery();

			while(resultSet.next()) {
				message.setInfo(resultSet.getString("username"));
				message.setInfo(resultSet.getString("title"));
				
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
		
		return message;
	}
	
	public Album getAlbumById(int albumId) throws SQLException  {
		Album album = null;
		String performedAction = " getting username of the album creator ";
		String query = "SELECT * FROM Album WHERE id = ?";
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		try {
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setInt(1, albumId);

			resultSet = preparedStatement.executeQuery();

			while(resultSet.next()) {
				album = new Album();
				album.setId(resultSet.getInt("id"));
				album.setTitle(resultSet.getString("title"));
				album.setCreationDate(resultSet.getDate("creationDate"));
				album.setUserId(resultSet.getInt("userId"));
				
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
		
		return album;
	}

	
	public void createAlbum(String title, int creatorId) throws SQLException  {
		String performedAction = " creating a new empty album ";
		String query = "INSERT into Album (title, creationDate, userId) VALUES (?, NOW(), ?)";
		PreparedStatement preparedStatement = null;
		
		try {
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, title);
			preparedStatement.setInt(2, creatorId);
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
	
	public void createAlbumWithImages(String title, int creatorId, List<Integer> imagesIds) throws SQLException  {
		int albumId;
		String performedAction = " creating a new album with images already in it ";
        String createAlbumQuery = "INSERT INTO Album (title, creationDate, userId) VALUES (?, NOW(), ?)";
        String linkImageToAlbumQuery = "INSERT INTO ImageAlbumLink (albumId, imageId) VALUES (?, ?)";
        PreparedStatement createAlbumStmt = null;
        PreparedStatement linkImageStmt = null;
		ResultSet resultSet = null;
        
		try {
            connection.setAutoCommit(false);		//Disabling auto commit

			createAlbumStmt = connection.prepareStatement(createAlbumQuery, Statement.RETURN_GENERATED_KEYS);
			createAlbumStmt.setString(1, title);
			createAlbumStmt.setInt(2, creatorId);
			createAlbumStmt.executeUpdate();
			
			resultSet = createAlbumStmt.getGeneratedKeys();
			
			if (resultSet.next()) {
				albumId =  resultSet.getInt(1);
	        } else {
	            throw new SQLException("No generated key was returned after inserting a new user into the database.");
	        }
			//Linking each image with the album just created
			linkImageStmt = connection.prepareStatement(linkImageToAlbumQuery);
            for (int imageId : imagesIds) {
                linkImageStmt.setInt(1, albumId);
                linkImageStmt.setInt(2, imageId);
                linkImageStmt.executeUpdate();
            }

            connection.commit();		//Manual commit

		} catch (SQLException e) {
			connection.rollback();		// If an SQLException is raised a roll-back to the last commit is needed
			throw new SQLException("Error accessing the DB when" + performedAction + "[ "+ e.getMessage() + " ]");

		} finally {
			connection.setAutoCommit(true);		//Re-activating auto commit

			try {
				createAlbumStmt.close();
				linkImageStmt.close();

			} catch (Exception e) {
				throw new SQLException("Error closing the statement when" + performedAction + "[ " + e.getMessage() + " ]");
			}
		}
	}
}

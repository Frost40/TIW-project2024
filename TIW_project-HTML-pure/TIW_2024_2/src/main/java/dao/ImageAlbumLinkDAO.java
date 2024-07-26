package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


public class ImageAlbumLinkDAO {
	private Connection connection;
	
	public ImageAlbumLinkDAO(Connection connection) {
		this.connection = connection;
	}
	
	public void addImageToAlbum(int albumId, int imageId) throws SQLException {
		String performedAction = " adding the image to the album ";
		String query = "INSERT into ImageAlbumLink (albumId, imageId, chosenOrder) VALUES(?, ?, ?)";
		PreparedStatement preparedStatement = null;

		try {
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setInt(1, albumId);
			preparedStatement.setInt(2, imageId);
			preparedStatement.setInt(3, 0);
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
	
	public void deleteImageFromAllAlbums(int imageId) throws SQLException {
		String performedAction = " removing all istances of 'imageId' from all albums where present ";
		String query = "DELETE FROM ImageALbumLink WHERE imageId = ?";
		PreparedStatement preparedStatement = null;

		try {
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setInt(1, imageId);
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

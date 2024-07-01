package it.polimi.tiw.projects.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.polimi.tiw.projects.beans.Image;
import it.polimi.tiw.projects.utils.TupleOfInteger;


public class ImageAlbumLinkDAO {
	private Connection connection;
	
	public ImageAlbumLinkDAO(Connection connection) {
		this.connection = connection;
	}
	
	public void addImageToAlbum(int albumId, int imageId) throws SQLException {
		String performedAction = " adding the image to the album ";
		String query = "INSERT into ImageAlbumLink (albumId, imageId) VALUES(?, ?)";
		PreparedStatement preparedStatement = null;

		try {
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setInt(1, albumId);
			preparedStatement.setInt(2, imageId);
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
	
	public void updateImageOrder(int albumId, List<TupleOfInteger> newOrderList) throws SQLException {
        String performedAction = "updating image order in album";
        String updateOrderQuery = "UPDATE ImageAlbumLink SET chosenOrder = ? WHERE albumId = ? AND imageId = ?";
        PreparedStatement updateOrderStmt = null;

        try {
            connection.setAutoCommit(false); // Disabling auto commit

            updateOrderStmt = connection.prepareStatement(updateOrderQuery);

            // Iterate over the list of tuples and update the chosenOrder
            for (TupleOfInteger tuple : newOrderList) {
                updateOrderStmt.setInt(1, tuple.getValue());
                updateOrderStmt.setInt(2, albumId);
                updateOrderStmt.setInt(3, tuple.getKey());
                updateOrderStmt.executeUpdate();
            }

            connection.commit(); // Manual commit

        } catch (SQLException e) {
            connection.rollback(); // If an SQLException is raised a roll-back to the last commit is needed
            throw new SQLException("Error accessing the DB when " + performedAction + " [ " + e.getMessage() + " ]");

        } finally {
            connection.setAutoCommit(true); // Re-activating auto commit

            try {
                if (updateOrderStmt != null) {
                    updateOrderStmt.close();
                }

            } catch (Exception e) {
                throw new SQLException("Error closing the statement when " + performedAction + " [ " + e.getMessage() + " ]");
            }
        }
    }
	
	public List<Image> getImagesInOrder(int albumId) throws SQLException {
		List<Image> images = new ArrayList<Image>();
		String performedAction = " getting all images ordered in the album ";
		String query = "SELECT Image.* " +
	               "FROM Image " +
	               "JOIN ImageAlbumLink ON Image.id = ImageAlbumLink.imageId " +
	               "WHERE ImageAlbumLink.albumId = ? " + 
	               "ORDER BY chosenOrder ASC";
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
}

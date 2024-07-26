package it.polimi.tiw.projects.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import it.polimi.tiw.projects.utils.Tuple;

public class CommentDAO {
	private Connection connection;
	
	public CommentDAO(Connection connection) {
		this.connection = connection;
	}
	
	public List<Tuple> getCommentsByImageId(int imageId) throws SQLException {
		List<Tuple> usernameAndComment = new ArrayList<>();
		Tuple comment = null;
		String performedAction = " getting all albums not form user ";
        String query = "SELECT Comment.text, User.username " +
                       "FROM Comment " +
                       "JOIN User ON Comment.userId = User.id " +
                       "WHERE Comment.imageId = ?";
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		try {
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setInt(1, imageId);

			resultSet = preparedStatement.executeQuery();

			while(resultSet.next()) {
				comment = new Tuple(resultSet.getString("username"), resultSet.getString("text"));
				usernameAndComment.add(comment);
				
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
		
		return usernameAndComment;
	}
	
	public HashMap<Integer, List<Tuple>> getAllComments(List<Integer> imageIds) throws SQLException {
        HashMap<Integer, List<Tuple>> commentsMap = new HashMap<>();
        if (imageIds == null || imageIds.isEmpty()) {
            return commentsMap; // Ritorna una mappa vuota se la lista è nulla o vuota
        }
        
        String performedAction = "getting all comments";
        
        // Creiamo una stringa con punti interrogativi per ogni imageId nella lista
        StringBuilder queryBuilder = new StringBuilder("SELECT Comment.text, User.username, Comment.imageId ")
            .append("FROM Comment ")
            .append("JOIN User ON Comment.userId = User.id ")
            .append("WHERE Comment.imageId IN (");

        for (int i = 0; i < imageIds.size(); i++) {
            queryBuilder.append("?");
            if (i < imageIds.size() - 1) {
                queryBuilder.append(",");
            }
        }
        queryBuilder.append(")");

        String query = queryBuilder.toString();
        
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            preparedStatement = connection.prepareStatement(query);

            // Imposta i valori dei punti interrogativi nella query
            for (int i = 0; i < imageIds.size(); i++) {
                preparedStatement.setInt(i + 1, imageIds.get(i));
            }

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int imageId = resultSet.getInt("imageId");
                String username = resultSet.getString("username");
                String text = resultSet.getString("text");

                Tuple comment = new Tuple(username, text);

                if (!commentsMap.containsKey(imageId)) {
                    List<Tuple> commentList = new ArrayList<>();
                    commentList.add(comment);
                    commentsMap.put(imageId, commentList);
                } else {
                    commentsMap.get(imageId).add(comment);
                }
            }

        } catch (SQLException e) {
            throw new SQLException("Error accessing the DB when " + performedAction + " [ " + e.getMessage() + " ]");

        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (Exception e) {
                throw new SQLException("Error closing the result set when " + performedAction + " [ " + e.getMessage() + " ]");
            }

            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (Exception e) {
                throw new SQLException("Error closing the statement when " + performedAction + " [ " + e.getMessage() + " ]");
            }
        }

        return commentsMap;
    }

	
	
	public void addComment(String comment, int creatorId, int imageId) throws SQLException  {
		String performedAction = " adding the new comment to database ";
		String query = "INSERT into Comment (text, userId, imageId) VALUES (?, ?, ?)";
		PreparedStatement preparedStatement = null;
		
		try {
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, comment);
			preparedStatement.setInt(2, creatorId);
			preparedStatement.setInt(3, imageId);
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

	
	public void deleteAllComments(int imageId) throws SQLException {
		String performedAction = " deleting all comments relative to 'imageId' ";
		String query = "DELETE FROM Comment WHERE imageId = ?";
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

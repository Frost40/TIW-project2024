package it.polimi.tiw.projects.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import it.polimi.tiw.projects.beans.User;

public class UserDAO {
	private Connection connection;
	
	public UserDAO(Connection connection) {
		this.connection = connection;
	}

	public User authenticationViaUsername(String username, String password) throws SQLException {
		User user = null;
		String performedAction = " finding a user by username and password ";
		String query = "SELECT * FROM User WHERE username = ? AND password = ?";
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		try {
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, username);
			preparedStatement.setString(2, password);
			
			resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {
				user = new User();
				user.setId(resultSet.getInt("id"));
				user.setUsername(resultSet.getString("username"));
				user.setPassword(resultSet.getString("password"));
				user.setEmail(resultSet.getString("email"));
				
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

		return user;
	}

	public User authenticationViaEmail(String email, String password) throws SQLException {
		User user = null;
		String performedAction = " finding a user by email and password ";
		String query = "SELECT * FROM User WHERE email = ? AND password = ?";
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		try {
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, email);
			preparedStatement.setString(2, password);
			
			resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {
				user = new User();
				user.setId(resultSet.getInt("id"));
				user.setUsername(resultSet.getString("username"));
				user.setPassword(resultSet.getString("password"));
				user.setEmail(resultSet.getString("email"));
				
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

		return user;
	}
	
	public User findUserById(int id) throws SQLException {
		User user = null;
		String performedAction = " finding a ser by id ";
		String query = "SELECT * FROM User WHERE id = ?";
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		try {

			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setInt(1, id);
			
			resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {

				user = new User();
				user.setId(resultSet.getInt("id"));
				user.setUsername(resultSet.getString("username"));
				user.setPassword(resultSet.getString("password"));
				user.setEmail(resultSet.getString("email"));
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

		return user;
	}
	
	public User findUserByEmail(String email) throws SQLException {
		User user = null;
		String performedAction = " finding a user by email ";
		String query = "SELECT * FROM User WHERE email = ?";
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		try {
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, email);
			
			resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {

				user = new User();
				user.setId(resultSet.getInt("id"));
				user.setUsername(resultSet.getString("username"));
				user.setPassword(resultSet.getString("password"));
				user.setEmail(resultSet.getString("email"));
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

		return user;
	}
	
	public User findUserByUsername(String username) throws SQLException {
		User user = null;
		String performedAction = " finding a user by username ";
		String query = "SELECT * FROM User WHERE username = ?";
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		try {
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, username);
			
			resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {
				user = new User();
				user.setId(resultSet.getInt("id"));
				user.setUsername(resultSet.getString("username"));
				user.setPassword(resultSet.getString("password"));
				user.setEmail(resultSet.getString("email"));
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

		return user;
	}
	
	public int registerUser(String email, String username, String password) throws SQLException {
	    int generatedId = 0;
	    String performedAction = " creating a new user in the database ";
	    String query = "INSERT INTO User (username,email,password) VALUES(?,?,?)";
	    PreparedStatement preparedStatement = null;
	    ResultSet resultSet = null;

	    try {
	        // Deactivate auto-commit to ensure an atomic transaction
	        connection.setAutoCommit(false);

	        preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
	        preparedStatement.setString(1, username);
	        preparedStatement.setString(2, email);
	        preparedStatement.setString(3, password);
	        preparedStatement.executeUpdate();

	        resultSet = preparedStatement.getGeneratedKeys();

	        if (resultSet.next()) {
	            generatedId =  resultSet.getInt(1);
	        } else {
	            throw new SQLException("No generated key was returned after inserting a new user into the database.");
	        }

	        connection.commit();

	    } catch (SQLException e) {
	        // If an SQLException is raised a roll-back to the last commit is needed
	        connection.rollback();
	        throw new SQLException("Error accessing the DB when" + performedAction + "[ " + e.getMessage() + " ]");

	    } finally {
	        // When the transaction is completed (successfully or not) we need to resume the
	        // auto-commit
	        connection.setAutoCommit(true);

	        try {
	            if (resultSet != null) {
	                resultSet.close();
	            }
	        } catch (Exception e) {
	            throw new SQLException("Error closing the result set when" + performedAction + "[ " + e.getMessage() + " ]");
	        }

	        try {
	            if (preparedStatement != null) {
	                preparedStatement.close();
	            }
	        } catch (Exception e) {
	            throw new SQLException("Error closing the statement when" + performedAction + "[ " + e.getMessage() + " ]");
	        }
	    }

	    return generatedId;
	}

	
}

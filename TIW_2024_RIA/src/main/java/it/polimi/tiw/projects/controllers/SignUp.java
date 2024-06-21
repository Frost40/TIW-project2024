package it.polimi.tiw.projects.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.polimi.tiw.projects.beans.User;
import it.polimi.tiw.projects.dao.AlbumDAO;
import it.polimi.tiw.projects.dao.UserDAO;
import it.polimi.tiw.projects.utils.ConnectionHandler;
import it.polimi.tiw.projects.utils.Message;
import it.polimi.tiw.projects.utils.PathHelper;

/**
 * Servlet implementation class SignUp
 */
@WebServlet("/SignUp")
public class SignUp extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SignUp() {
        super();
        // TODO Auto-generated constructor stub
    }

    @Override
    public void init() throws ServletException {
    	
    	ServletContext servletContext = getServletContext();
		this.connection = ConnectionHandler.getConnection(servletContext);
    }
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//Gets the parameters of the request and verifies if they are in the correct format (length and syntax)
		String email      = request.getParameter("email");
		String username   = request.getParameter("username");
		String password   = request.getParameter("password");
		String repeatedPassword = request.getParameter("repeat_pwd");
		
		UserDAO userDAO = new UserDAO(connection);

		Message returnedMessage = readyForRegistration(email, username, password, repeatedPassword, userDAO);
		switch(returnedMessage.getResult()) {
			case "error" -> {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.getWriter().println(returnedMessage.getText());
				return;
			}
			case "warning" -> {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);		
				response.getWriter().println(returnedMessage.getText());
				return;

			}
			case "unauthorized" -> {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.getWriter().println(returnedMessage.getText());
				return;
			}
			case "success" -> {
				//Adding user's data to database and returning user's id automatically generated
				int userId = 0;			
				try {
					//At this point the user is ready to be registered
					userId = userDAO.registerUser(email, username, password);
					
				//If an error occurred during the process the user is redirected to errorPage
				} catch (SQLException e) {
					response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
					response.getWriter().println(e.getMessage());
					return;	
				}
				
				//In case the system filed to retrieve user's id it redirects to errorPage
				if (userId == 0) {
					response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
					response.getWriter().println("An error occured while retreving user's id");
					return;	
				}
				
				//Creating the first album for the user that will contain all of his images
				AlbumDAO albumDAO = new AlbumDAO(connection);
				try {
					albumDAO.createAlbum("allPhotos", userId);
					
				//If an error occurred during the process the user is redirected to errorPage
				} catch (SQLException e) {
					response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
					response.getWriter().println(e.getMessage());
					return;	
				}
				
				response.setStatus(HttpServletResponse.SC_OK);
			}
		}
	}
	
	private Message readyForRegistration(String email, String username, String password, String repeatedPassword, UserDAO userDAO) throws ServletException, IOException {
		Message messageToReturn = new Message();
				
		//Verifying that there are no null parameters
		if(email == null || username == null || password == null || repeatedPassword == null) {
			messageToReturn.setResult("warning");
			messageToReturn.setText("Some data is missing from the sign up module!");
			
			return messageToReturn;
		}
		
		//Checking if the inserted email is actually a valid email address and assuring that its length is <= 255
		if (!isAnEmail(email) || email.length()>255) {
			messageToReturn.setResult("warning");
			messageToReturn.setText("The email address is NOT valid!");

			return messageToReturn;
		}
				
		//Checking if the inserted username string has correct length (1-45)
		if (username.length() <= 0 || username.length() > 45) {
			messageToReturn.setResult("warning");
			messageToReturn.setText("Invalid username (a valid username has more than one character and less than 45)!");
			
			return messageToReturn;
		}
				
		//Checking if the inserted strings (PASSWORD and REPEAT_PWD) have the correct length (1-10) and equal
		if (password.length() <= 0 || password.length() > 10) {
			messageToReturn.setResult("warning");
			messageToReturn.setText("Invalid password (a valid password has more than one character and less than 10)!");
			
			return messageToReturn;
		}
				
		if (!password.equals(repeatedPassword)) {
			messageToReturn.setResult("warning");
			messageToReturn.setText("Password and repeat password field not equal!");
			
			return messageToReturn;
		}
				
		//Checking uniqueness of email
		User user = null;
		try {
			user = userDAO.findUserByEmail(email);
			
		//If an error occurred during the process the user is redirected to errorPage
		} catch (SQLException e) {
			messageToReturn.setResult("error");
			messageToReturn.setText(e.getMessage());
			
			return messageToReturn;
		}
				
		if(user != null) {
			messageToReturn.setResult("unauthorized");
			messageToReturn.setText("Email already in use!");
			
			return messageToReturn;
		}
			
		
		//Checking uniqueness of username
		try {
			user = userDAO.findUserByUsername(username);
		
		//If an error occurred during the process the user is redirected to errorPage
		} catch (SQLException e) {
			messageToReturn.setResult("error");
			messageToReturn.setText(e.getMessage());
			
			return messageToReturn;
		}
				
		if(user != null) {
			messageToReturn.setResult("unauthorized");
			messageToReturn.setText("Username already taken");
		
			return messageToReturn;
		}
		
		messageToReturn.setResult("success");
		return messageToReturn;

	}
	
private boolean isAnEmail(String emailAddress) {
		String EMAIL_REGEX = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
		
	    Pattern pattern = Pattern.compile(EMAIL_REGEX);
	    Matcher matcher = pattern.matcher(emailAddress);
	    return matcher.matches();
	}

}

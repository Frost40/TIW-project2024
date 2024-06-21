package it.polimi.tiw.projects.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.polimi.tiw.projects.beans.Image;
import it.polimi.tiw.projects.beans.User;
import it.polimi.tiw.projects.dao.CommentDAO;
import it.polimi.tiw.projects.dao.ImageDAO;
import it.polimi.tiw.projects.utils.ConnectionHandler;
import it.polimi.tiw.projects.utils.PathHelper;
import it.polimi.tiw.projects.utils.Tuple;

/**
 * Servlet implementation class GoToImagePage
 */
@WebServlet("/GoToImagePage")
public class GoToImagePage extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;
              
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GoToImagePage() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    @Override
    public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		
		} catch (SQLException e) {
			e.printStackTrace();
		}
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
		HttpSession session = request.getSession();
		User currentUser    = (User)session.getAttribute("currentUser");
		
		String albumIdString = request.getParameter("albumId");
		String imageIdString = request.getParameter("imageId");
		int albumId;
		int imageId;
		
		if(albumIdString == null) {
			request.setAttribute("error", "Null albumId!");
			return;
		}
		
		if(imageIdString == null) {
			request.setAttribute("error", "Null imageId!");
			return;
		}
		
		//Parsing process for albumIdString
		albumId = parsingChecker(request, response, albumIdString);
		imageId = parsingChecker(request, response, imageIdString);

		//Checking if the parsing process has been successful
		if (albumId == -1) return;
		if(imageId == -1)	return;
		
		//Getting image's info from database
		Image image;
		ImageDAO imageDAO = new ImageDAO(connection);
		
		try {
			image = imageDAO.getImageById(imageId);
		
		//If an error occurred during the process the user is redirected to errorPage
		} catch (SQLException e) {
			request.setAttribute("error", e.getMessage());
			return;
		}
		
		//Getting all image's comments with relative username info from database
		CommentDAO commentDAO = new CommentDAO(connection);
		List<Tuple> usernameAndComment;
		
		try {
			usernameAndComment = commentDAO.getCommentsByImageId(imageId);
			
		//If an error occurred during the process the user is redirected to errorPage
		} catch (SQLException e) {
			request.setAttribute("error", e.getMessage());
			return;
		}
		
		//Checking if the user has to right to delete the image
		if(currentUser.getId() == image.getUserId())	request.setAttribute("canBeDeleted", true);
		else	request.setAttribute("canBeDeleted", false);
		
		request.setAttribute("albumId", albumId);
		request.setAttribute("image", image);
		request.setAttribute("comments", usernameAndComment);
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}
	
	private int parsingChecker(HttpServletRequest request, HttpServletResponse response, String stringToParse) throws ServletException, IOException {
		int idToReturn;
		if(stringToParse == null) {
			request.setAttribute("error", "");
			return -1;
		}
		
		try {
			idToReturn = Integer.parseInt(stringToParse);
			
		} catch (NumberFormatException e) {
			request.setAttribute("error", "Id provided for the album is not a number");
			return -1;
		}
		
		return idToReturn;
	}
}

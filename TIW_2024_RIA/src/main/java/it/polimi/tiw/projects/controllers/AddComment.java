package it.polimi.tiw.projects.controllers;

import java.io.IOException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.polimi.tiw.projects.beans.User;
import it.polimi.tiw.projects.dao.CommentDAO;
import it.polimi.tiw.projects.utils.ConnectionHandler;
import it.polimi.tiw.projects.utils.PathHelper;

/**
 * Servlet implementation class AddComment
 */
@WebServlet("/AddComment")
public class AddComment extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;
                     
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AddComment() {
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
		// TODO Auto-generated method stub
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		User currentUser    = (User)session.getAttribute("currentUser");
		
		String comment = request.getParameter("comment");
		String imageIdString = request.getParameter("imageId");
		String albumIdString = request.getParameter("albumId");
		int imageId;
		int albumId;
		
		//Checking if the inserted email is actually a valid email address and assuring that its length is <= 255
		if (comment==null || comment.length()>255 ||  comment.length()<=0) {
			request.setAttribute("error", "Invalid comment (a valid comment has more than one character and less than 255)!");
			return;
		}
				
		if(albumIdString == null) {
			request.setAttribute("error", "Null albumId!");
			return;
		}
		
		//Checking if the imageIdString is valid
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
		

		CommentDAO commentDAO = new CommentDAO(connection);
		try {
			commentDAO.addComment(comment, currentUser.getId(), imageId);
		
		//If an error occurred during the process the user is redirected to errorPage
		} catch (SQLException e) {
			request.setAttribute("error", e.getMessage());
			return;
		}
		
		response.sendRedirect(getServletContext().getContextPath() + PathHelper.goToServlet("image") + "?imageId=" + URLEncoder.encode(Integer.toString(imageId), "UTF-8") + "&albumId=" + URLEncoder.encode(Integer.toString(albumId), "UTF-8"));
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

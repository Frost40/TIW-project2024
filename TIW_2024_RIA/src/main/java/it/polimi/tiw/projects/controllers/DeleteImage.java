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

import it.polimi.tiw.projects.beans.Image;
import it.polimi.tiw.projects.beans.User;
import it.polimi.tiw.projects.dao.CommentDAO;
import it.polimi.tiw.projects.dao.ImageAlbumLinkDAO;
import it.polimi.tiw.projects.dao.ImageDAO;
import it.polimi.tiw.projects.utils.ConnectionHandler;
import it.polimi.tiw.projects.utils.PathHelper;

/**
 * Servlet implementation class DeleteImage
 */
@WebServlet("/DeleteImage")
public class DeleteImage extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DeleteImage() {
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
		String imageIdString = request.getParameter("imageId");
		int imageId;
		
		if(imageIdString == null) {
			request.setAttribute("error", "Null account code, when accessing account details");
			return;
		}
		
		//Parsing process for albumIdString
		imageId = parsingChecker(request, response, imageIdString);
		if (imageId == -1) return;
		
		Image currentImage;
		ImageDAO imageDAO = new ImageDAO(connection);
		try {
			currentImage = imageDAO.getImageById(imageId);
		
		//If an error occurred during the process the user is redirected to errorPage
		} catch (SQLException e) {
			request.setAttribute("error", e.getMessage());
			return;
		}
		
		if(currentUser.getId() != currentImage.getUserId()) {
			request.setAttribute("error", "You can only delate images that you created!");
		}
		
		//Deleting the image from the database
		try {
			imageDAO.deleteImage(imageId);
		
		//If an error occurred during the process the user is redirected to errorPage
		} catch (SQLException e) {
			request.setAttribute("error", e.getMessage());
			return;
		}
		
		//Deleting the image from albums in which is saved
		ImageAlbumLinkDAO imageALbumLinkDAO = new ImageAlbumLinkDAO(connection);
		try {
			imageALbumLinkDAO.deleteImageFromAllAlbums(imageId);
		
		//If an error occurred during the process the user is redirected to errorPage
		} catch (SQLException e) {
			request.setAttribute("error", e.getMessage());
			return;
		}
		
		//Deleting all comments relative to the image
		CommentDAO commentDAO = new CommentDAO(connection);
		try {
			commentDAO.deleteAllComments(imageId);
		
		//If an error occurred during the process the user is redirected to errorPage
		} catch (SQLException e) {
			request.setAttribute("error", e.getMessage());
			return;
		}
		
		String warningMessage = "Image deleted successfully!";
        response.sendRedirect(getServletContext().getContextPath() + PathHelper.goToServlet("home") + "?deleteImage=" + URLEncoder.encode(warningMessage, "UTF-8"));
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

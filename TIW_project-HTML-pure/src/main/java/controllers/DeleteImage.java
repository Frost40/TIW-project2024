package controllers;

import java.io.IOException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import beans.Image;
import beans.User;
import dao.CommentDAO;
import dao.ImageAlbumLinkDAO;
import dao.ImageDAO;
import utils.ConnectionHandler;
import utils.PathHelper;
import utils.TemplateHandler;

/**
 * Servlet implementation class DeleteImage
 */
@WebServlet("/DeleteImage")
public class DeleteImage extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;
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
		this.templateEngine = TemplateHandler.getEngine(servletContext, ".html");
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
		
		if(imageIdString == null  || imageIdString.isEmpty()) {
			request.setAttribute("error", "Please provide a valid id for the image to delete!");
			forward(request, response, PathHelper.goToErrorPage());
			return;
		}
		
		//Parsing process for albumIdString
		Optional<Integer> parsedId = parsingChecker(request, response, imageIdString);
        if (parsedId.isPresent())		imageId = parsedId.get();
        else		return;
		
		Image currentImage;
		ImageDAO imageDAO = new ImageDAO(connection);
		try {
			currentImage = imageDAO.getImageById(imageId);
		
		//If an error occurred during the process the user is redirected to errorPage
		} catch (SQLException e) {
			request.setAttribute("error", e.getMessage());
			forward(request, response, PathHelper.goToErrorPage());
			return;
		}
		
		if(currentImage == null) {
			request.setAttribute("error", "Image does not exist. (imageId_given: " + imageId + ")");
			forward(request, response, PathHelper.goToErrorPage());
			return;
		}
		
		if(currentUser.getId() != currentImage.getUserId()) {
			request.setAttribute("error", "You can only delate images that you created!");
			forward(request, response, PathHelper.goToErrorPage());
		}
		
		//Deleting the image from the database
		try {
			imageDAO.deleteImage(imageId);
		
		//If an error occurred during the process the user is redirected to errorPage
		} catch (SQLException e) {
			request.setAttribute("error", e.getMessage());
			forward(request, response, PathHelper.goToErrorPage());
			return;
		}
		
		//Deleting the image from albums in which is saved
		ImageAlbumLinkDAO imageALbumLinkDAO = new ImageAlbumLinkDAO(connection);
		try {
			imageALbumLinkDAO.deleteImageFromAllAlbums(imageId);
		
		//If an error occurred during the process the user is redirected to errorPage
		} catch (SQLException e) {
			request.setAttribute("error", e.getMessage());
			forward(request, response, PathHelper.goToErrorPage());
			return;
		}
		
		//Deleting all comments relative to the image
		CommentDAO commentDAO = new CommentDAO(connection);
		try {
			commentDAO.deleteAllComments(imageId);
		
		//If an error occurred during the process the user is redirected to errorPage
		} catch (SQLException e) {
			request.setAttribute("error", e.getMessage());
			forward(request, response, PathHelper.goToErrorPage());
			return;
		}
		
        response.sendRedirect(getServletContext().getContextPath() + PathHelper.goToServlet("home"));
	}

	private Optional<Integer> parsingChecker(HttpServletRequest request, HttpServletResponse response,
            String stringToParse) throws ServletException, IOException {
        int idToReturn;

        try {
            idToReturn = Integer.parseInt(stringToParse);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Id provided for the image is not a number: " + stringToParse);
            return Optional.empty();
        }

        return Optional.of(idToReturn);
    }
	
	private void forward(HttpServletRequest request, HttpServletResponse response, String path) throws ServletException, IOException{
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		templateEngine.process(path, ctx, response.getWriter());
	}

}

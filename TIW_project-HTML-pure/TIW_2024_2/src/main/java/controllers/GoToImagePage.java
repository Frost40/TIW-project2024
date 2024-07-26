package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import dao.ImageDAO;
import utils.ConnectionHandler;
import utils.PathHelper;
import utils.TemplateHandler;
import utils.Tuple;

/**
 * Servlet implementation class GoToImagePage
 */
@WebServlet("/GoToImagePage")
public class GoToImagePage extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;
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
		this.templateEngine = TemplateHandler.getEngine(servletContext, ".html");
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
		
		if(albumIdString == null || albumIdString.isEmpty()) {
			request.setAttribute("error", "Null albumId!");
			forward(request, response, PathHelper.goToErrorPage());
			return;
		}
		
		if(imageIdString == null || imageIdString.isEmpty()) {
			request.setAttribute("error", "Null imageId!");
			forward(request, response, PathHelper.goToErrorPage());
			return;
		}
		
		Optional<Integer> parsedString = parsingChecker(request, response, albumIdString);
		if (parsedString.isPresent())		albumId = parsedString.get();
		else		return;
		
		parsedString = parsingChecker(request, response, imageIdString);
		if (parsedString.isPresent())		imageId = parsedString.get();
		else		return;
		
		//Getting image's info from database
		Image image;
		ImageDAO imageDAO = new ImageDAO(connection);
		try {
			image = imageDAO.getImageById(imageId);
		
		//If an error occurred during the process the user is redirected to errorPage
		} catch (SQLException e) {
			request.setAttribute("error", e.getMessage());
			forward(request, response, PathHelper.goToErrorPage());
			return;
		}
		
		if (image == null) {
			request.setAttribute("error", "Image does not exist. (imageId_given: " + imageId + ")");
			forward(request, response, PathHelper.goToErrorPage());
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
			forward(request, response, PathHelper.goToErrorPage());
			return;
		}
		
		//Checking if the user has to right to delete the image
		if(currentUser.getId() == image.getUserId())	request.setAttribute("canBeDeleted", true);
		else	request.setAttribute("canBeDeleted", false);
		
		request.setAttribute("albumId", albumId);
		request.setAttribute("image", image);
		request.setAttribute("comments", usernameAndComment);
		
		forward(request, response, PathHelper.goToImagePage());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}
	
	private Optional<Integer> parsingChecker(HttpServletRequest request, HttpServletResponse response, String stringToParse) throws ServletException, IOException {
        int idToReturn;

        try {
            idToReturn = Integer.parseInt(stringToParse);
            
        } catch (NumberFormatException e) {
        	request.setAttribute("error", "Id provided for the album is not a number");
			forward(request, response, PathHelper.goToErrorPage());
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

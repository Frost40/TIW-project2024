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
import dao.ImageDAO;
import utils.ConnectionHandler;
import utils.PathHelper;
import utils.TemplateHandler;

/**
 * Servlet implementation class AddComment
 */
@WebServlet("/AddComment")
public class AddComment extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;
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
		
		String comment = request.getParameter("comment");
		String imageIdString = request.getParameter("imageId");
		String albumIdString = request.getParameter("albumId");
		int imageId;
		int albumId;
		
		//Checking if the inserted email is actually a valid email address and assuring that its length is <= 150
		if (comment == null || comment.length() > 150 ||  comment.length() <= 0) {
			request.setAttribute("error", "Invalid comment (a valid comment has more than one character and less than 150)!");
			forward(request, response, PathHelper.goToErrorPage());
			return;
		}
				
		if(albumIdString == null || albumIdString.isEmpty()) {
			request.setAttribute("error", "Null albumId!");
			forward(request, response, PathHelper.goToErrorPage());
			return;
		}
		
		//Checking if the imageIdString is valid
		if(imageIdString == null || imageIdString.isEmpty()) {
			request.setAttribute("error", "Null imageId!");
			forward(request, response, PathHelper.goToErrorPage());
			return;
		}
		
		//Parsing process for albumIdString
		Optional<Integer> parsedId = parsingChecker(request, response, albumIdString);
		if (parsedId.isPresent())		albumId = parsedId.get();
        else		return;
		
		parsedId = parsingChecker(request, response, imageIdString);
		if (parsedId.isPresent())		imageId = parsedId.get();
        else		return;
		
		 //Accessing the database in order to check if the image exists
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

		CommentDAO commentDAO = new CommentDAO(connection);
		try {
			commentDAO.addComment(comment, currentUser.getId(), imageId);
		
		//If an error occurred during the process the user is redirected to errorPage
		} catch (SQLException e) {
			request.setAttribute("error", e.getMessage());
			forward(request, response, PathHelper.goToErrorPage());
			return;
		}
		
		response.sendRedirect(getServletContext().getContextPath() + PathHelper.goToServlet("image") + "?imageId=" + URLEncoder.encode(Integer.toString(imageId), "UTF-8") + "&albumId=" + URLEncoder.encode(Integer.toString(albumId), "UTF-8"));
	}
	
	private Optional<Integer> parsingChecker(HttpServletRequest request, HttpServletResponse response, String stringToParse) throws ServletException, IOException {
        int idToReturn;

        try {
            idToReturn = Integer.parseInt(stringToParse);
        } catch (NumberFormatException e) {
        	request.setAttribute("error", "Id provided is not a number");
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

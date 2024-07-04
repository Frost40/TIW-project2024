package it.polimi.tiw.projects.controllers;

import java.io.IOException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import it.polimi.tiw.projects.beans.Image;
import it.polimi.tiw.projects.beans.User;
import it.polimi.tiw.projects.dao.CommentDAO;
import it.polimi.tiw.projects.dao.ImageDAO;
import it.polimi.tiw.projects.utils.ConnectionHandler;
import it.polimi.tiw.projects.utils.PathHelper;
import it.polimi.tiw.projects.utils.Tuple;

/**
 * Servlet implementation class AddComment
 */
@WebServlet("/AddComment")
@MultipartConfig
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
		int imageId;
		
		//Checking if the comment is actually valid by assuring that its length is <= 150
		if (comment == null || comment.length() > 150 ||  comment.length() <= 0) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Invalid comment (a valid comment has more than one character and less than 150)!");
			return;
		}
		
		//Checking if the imageIdString is valid
		if(imageIdString == null || imageIdString.isEmpty()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Null imageId!");
			return;
		}
		
		//Parsing process for albumIdString
		Optional<Integer> parsedId = parsingChecker(request, response, imageIdString);
        if (parsedId.isPresent())		imageId = parsedId.get();
        else		return;
		
        //Accessing the database in order to check if the image exists
        Image image;
		ImageDAO imageDAO = new ImageDAO(connection);
		try {
			image = imageDAO.getImageById(imageId);
		
		//If an error occurred during the process the user is redirected to errorPage
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println(e.getMessage());
			return;	
		}
		
		if (image == null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().println("Image does not exist. (imageId_given: " + imageId + ")");
			return;
		}
        
		//Adding the comment to database
		CommentDAO commentDAO = new CommentDAO(connection);
		try {
			commentDAO.addComment(comment, currentUser.getId(), imageId);
		
		//If an error occurred during the process the user is redirected to errorPage
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println(e.getMessage());
			return;	
		}
		
		//Getting all comments related to the image from database
		List<Tuple> usernameAndComment;
		try {
			usernameAndComment = commentDAO.getCommentsByImageId(imageId);
			
		//If an error occurred during the process the user is redirected to errorPage
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println(e.getMessage());
			return;	
		}
		
		// JSON serialization
		Map<String, Object> jsonObject = new HashMap<>();
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		Gson gson = new GsonBuilder().create();

		jsonObject.put("comments", usernameAndComment);

		String json = gson.toJson(jsonObject);
		response.getWriter().write(json);
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

}

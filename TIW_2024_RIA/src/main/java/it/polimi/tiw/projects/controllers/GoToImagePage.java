package it.polimi.tiw.projects.controllers;

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
		
		String imageIdString = request.getParameter("imageId");
		int imageId;
		
		if(imageIdString == null) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Null imageId!");
			return;
		}
        
        Optional<Integer> parsedId = parsingChecker(request, response, imageIdString);
        if (parsedId.isPresent())		imageId = parsedId.get();
        else		return;
        
		//Getting image's info from database
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
		
		//Getting all image's comments with relative username info from database
		CommentDAO commentDAO = new CommentDAO(connection);
		List<Tuple> usernameAndComment;
		
		try {
			usernameAndComment = commentDAO.getCommentsByImageId(imageId);
			
		//If an error occurred during the process the user is redirected to errorPage
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println(e.getMessage());
			return;	
		}
		
		Map<String, Object> jsonObject = new HashMap<>();

		//Checking if the user has to right to delete the image
		if(currentUser.getId() == image.getUserId())			jsonObject.put("canBeDeleted", true);
		else	jsonObject.put("canBeDeleted", false);
		
		request.setAttribute("image", image);
		request.setAttribute("comments", usernameAndComment);
		
		// JSON serialization
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		Gson gson = new GsonBuilder().create();

		jsonObject.put("image", image);
		jsonObject.put("comments", usernameAndComment);

		String json = gson.toJson(jsonObject);
		response.getWriter().write(json);
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}
	
	private Optional<Integer> parsingChecker(HttpServletRequest request, HttpServletResponse response,
            String stringToParse) throws ServletException, IOException {
        int idToReturn;

        try {
            idToReturn = Integer.parseInt(stringToParse);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Invalid format: " + stringToParse);
            return Optional.empty();
        }

        return Optional.of(idToReturn);
    }
}

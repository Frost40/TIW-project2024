package it.polimi.tiw.projects.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

import it.polimi.tiw.projects.beans.Album;
import it.polimi.tiw.projects.beans.Image;
import it.polimi.tiw.projects.beans.User;
import it.polimi.tiw.projects.dao.AlbumDAO;
import it.polimi.tiw.projects.dao.CommentDAO;
import it.polimi.tiw.projects.dao.ImageAlbumLinkDAO;
import it.polimi.tiw.projects.dao.ImageDAO;
import it.polimi.tiw.projects.utils.ConnectionHandler;
import it.polimi.tiw.projects.utils.Message;
import it.polimi.tiw.projects.utils.Tuple;

/**
 * Servlet implementation class goToAlbumPage
 */
@WebServlet("/GoToAlbumPage")
@MultipartConfig
public class GoToAlbumPage extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GoToAlbumPage() {
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
		int albumId;

		if(albumIdString == null || albumIdString.isEmpty()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);		
			response.getWriter().println("Null albumId!");
			return;		
		}
		
		Optional<Integer> parsedId = parsingChecker(request, response, albumIdString);
		if (parsedId.isPresent())			albumId = parsedId.get();
		else	return;
		
		if(albumId < 0) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);		
			response.getWriter().println("Id provided for the album is not a valid number!");
			return;
		}
		
		//Assuring the album exists
		AlbumDAO albumDAO = new AlbumDAO(connection);
		Album album;
		try {
			album = albumDAO.getAlbumById(albumId);
			
		//If an error occurred during the process the user is redirected to errorPage
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println(e.getMessage());
			return;
		}
		
		if(album == null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().println("The album you are trying to access does not exists");
			return;
		}
		
		if(album.getTitle().equals("allPhotos") && album.getUserId() != currentUser.getId()) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().println("You are not authorized to open thi album!");
			return;
		}
		
		//Getting album's username creator and title using "albumID"
		Message returnedMessage;
		try {
			returnedMessage = albumDAO.getUsernameCreatorAndTitle(albumId);
					
		//If an error occurred during the process the user is redirected to errorPage
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println(e.getMessage());
			return;
		}
		
		if (returnedMessage == null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().println("An error occured while getting info related to 'albumId' equals to " + albumId);
			return;
		}

		//Getting images in 'albumId'
		ImageAlbumLinkDAO imageAlbumLinkDAO = new ImageAlbumLinkDAO(connection);
        List<Image> images = null;
        try {
        	images = imageAlbumLinkDAO.getImagesInOrder(albumId);
        	
		//If an error occurred during the process the user is redirected to errorPage
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Error while creating album: " + e.getMessage());
            return;
        }
        
        //Getting all image's comments with relative username info from database
		CommentDAO commentDAO = new CommentDAO(connection);
		HashMap<Integer, List<Tuple>> allComments;
		List<Integer> imageIds = images.stream()
                .map(Image::getId)
                .collect(Collectors.toList());

		try {
			allComments = commentDAO.getAllComments(imageIds);
			
		//If an error occurred during the process the user is redirected to errorPage
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println(e.getMessage());
			return;	
		}
		
		HashMap<Image, List<Tuple>> imagesWithComments = new HashMap<>();
		for (Image x : images) {
			if (allComments.containsKey(x.getId()))		imagesWithComments.put(x, allComments.get(x.getId()));
			else	imagesWithComments.put(x, null);
		}
		
		for (Map.Entry<Image, List<Tuple>> entry : imagesWithComments.entrySet()) {
			String title = entry.getKey().getTitle();
	        Integer imageId = entry.getKey().getId();
	        List<Tuple> commentsList = entry.getValue();

	        System.out.println("Image ID: " + imageId + ", Title: " + title);
	        if (commentsList != null) {
	        	for (Tuple comment : commentsList) {
		            System.out.println("Username: " + comment.getKey() + ", Comment: " + comment.getValue());
		        }
		        System.out.println(); // Aggiungi una linea vuota tra i diversi imageId
	        }
	        
	    }
		
		// JSON serialization
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		Gson gson = new GsonBuilder().create();
		Map<String, Object> jsonObject = new HashMap<>();

		jsonObject.put("creator", returnedMessage.getInfo().get(0));
		jsonObject.put("albumTitle", returnedMessage.getInfo().get(1));
		jsonObject.put("images", images);
		jsonObject.put("comments", allComments);


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
	
	private Optional<Integer> parsingChecker(HttpServletRequest request, HttpServletResponse response, String stringToParse) throws ServletException, IOException {
		int idToReturn;
		
		try {
			idToReturn = Integer.parseInt(stringToParse);
			
		} catch (NumberFormatException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println(e.getMessage());
			return Optional.empty();
		}
		
		return Optional.of(idToReturn);
		
	}
}

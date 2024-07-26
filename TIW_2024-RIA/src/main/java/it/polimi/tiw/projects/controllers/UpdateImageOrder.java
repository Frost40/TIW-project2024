package it.polimi.tiw.projects.controllers;

import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.polimi.tiw.projects.beans.Album;
import it.polimi.tiw.projects.beans.Image;
import it.polimi.tiw.projects.dao.AlbumDAO;
import it.polimi.tiw.projects.dao.ImageAlbumLinkDAO;
import it.polimi.tiw.projects.dao.ImageDAO;
import it.polimi.tiw.projects.utils.ConnectionHandler;
import it.polimi.tiw.projects.utils.TupleOfInteger;

/**
 * Servlet implementation class UpdateImageOrder
 */
@WebServlet("/UpdateImageOrder")
@MultipartConfig
public class UpdateImageOrder extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private Connection connection;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UpdateImageOrder() {
        super();
        // TODO Auto-generated constructor stub
    }

    @Override
    public void init() throws ServletException {
        ServletContext servletContext = getServletContext();
        this.connection = ConnectionHandler.getConnection(servletContext);
    }

    @Override
    public void destroy() {
        try {
            ConnectionHandler.closeConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
		String albumIdString = request.getParameter("albumId");
		String newImageOrderJson = request.getParameter("orderedIds");
        List<TupleOfInteger> listOfInfoImage = new ArrayList<>();
        int albumId;

        if (albumIdString == null || albumIdString.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Unable to process album's id!");
            return;
        }
        
        if (newImageOrderJson == null || newImageOrderJson.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Unable to retrive the new ordered for the images in the album!");
            return;
        }
        
        Optional<Integer> parsedId = parsingChecker(request, response, albumIdString);
        if (parsedId.isPresent())			albumId = parsedId.get();
		else	return;
        
        if (albumId < 0) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("The insert id for the album is not valid!");
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
  		
        //Reading and extracting data from the JSON array
        try (StringReader stringReader = new StringReader(newImageOrderJson);
             JsonReader jsonReader = Json.createReader(stringReader)) {

            //Reading the array
            JsonArray jsonArray = jsonReader.readArray();

            int i = 1;
            for (JsonValue jsonValue : jsonArray) {
                String imageIdString = ((JsonString) jsonValue).getString();

                //Parsing the string found
                Optional<Integer> parsedImageId = parsingChecker(request, response, imageIdString);
                if (!parsedImageId.isPresent()) {
                    return;
                }
            	
                TupleOfInteger tupleOfInteger = new TupleOfInteger(parsedImageId.get(), i);
                listOfInfoImage.add(tupleOfInteger);
                i++;
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Error while extracting the creation dates of the images: " + e.getMessage());
            return;
        }
        
        ImageDAO imageDAO = new ImageDAO(connection);
        List<Image> imagesInAlbum;
        try {
        	imagesInAlbum = imageDAO.getImagesByAlbumId(albumId);
        	
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Error while updating the images' order in the album: " + e.getMessage());
            return;
        }
        
        
        List<Integer> imageIdsInAlbum = imagesInAlbum.stream()
									        		 .map(Image::getId)
									                 .collect(Collectors.toList());
        List<Integer> imageIdsFromClient = listOfInfoImage.stream()
										        		  .map(x -> x.getKey())
										                  .collect(Collectors.toList());
        
        if (imageIdsInAlbum.size() != imageIdsFromClient.size()) {
        	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Some image went missing, unable to update album's order!");
            return;
        }
        
        if(!haveSameElements(imageIdsInAlbum, imageIdsFromClient)) {
        	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("One or more images do not exist!");
            return;
        }
        
        ImageAlbumLinkDAO imageAlbumLinkDAO = new ImageAlbumLinkDAO(connection);
        try {
        	imageAlbumLinkDAO.updateImageOrder(albumId, listOfInfoImage);
        	
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Error while updating the images' order in the album: " + e.getMessage());
            return;
        }
        
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println("Order update successfully!");

	}
	
	public static boolean haveSameElements(List<?> list1, List<?> list2) {
        Set<?> set1 = new HashSet<>(list1);
        Set<?> set2 = new HashSet<>(list2);
        return set1.equals(set2);
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

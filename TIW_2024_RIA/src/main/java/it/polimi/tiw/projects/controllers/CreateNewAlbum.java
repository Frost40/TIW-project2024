package it.polimi.tiw.projects.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

import java.io.StringReader;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;
import java.util.ArrayList;

import it.polimi.tiw.projects.beans.Album;
import it.polimi.tiw.projects.beans.User;
import it.polimi.tiw.projects.dao.AlbumDAO;
import it.polimi.tiw.projects.dao.ImageDAO;
import it.polimi.tiw.projects.utils.ConnectionHandler;
import it.polimi.tiw.projects.utils.TupleOfInteger;

@WebServlet("/CreateNewAlbum")
@MultipartConfig
public class CreateNewAlbum extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection;

    public CreateNewAlbum() {
        super();
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

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("currentUser");

        String albumTitle = request.getParameter("albumTitle");
        String selectedImagesJson = request.getParameter("selectedImages");
        List<TupleOfInteger> listOfInfoImage = new ArrayList<>();

        if (albumTitle == null || albumTitle.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Null or empty album title!");
            return;
        }

        if (selectedImagesJson == null || selectedImagesJson.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("No images have been selected to be part of the album");
            return;
        }

        //Extracting info from Json via stringReader
        try (StringReader stringReader = new StringReader(selectedImagesJson);
             JsonReader jsonReader = Json.createReader(stringReader)) {

            JsonArray jsonArray = jsonReader.readArray();

            // Iterare attraverso gli elementi dell'array JSON
            for (JsonValue jsonValue : jsonArray) {
                String imageIdString = ((JsonString) jsonValue).getString();

                //Parsing the imageId
                Optional<Integer> parsedImageId = parsingChecker(request, response, imageIdString);
                if (!parsedImageId.isPresent()) {
                    return;
                }

                TupleOfInteger tupleOfInteger = new TupleOfInteger();
                tupleOfInteger.setKey(parsedImageId.get());
                listOfInfoImage.add(tupleOfInteger);
            }
    		
        //If an error occurred during the process the user is redirected to errorPage   
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Error while extracting the creation dates of the images: " + e.getMessage());
            return;
        }
        
        ImageDAO imageDAO = new ImageDAO(connection);
        try {
        	listOfInfoImage = imageDAO.getCreationDates(listOfInfoImage);
    		
        //If an error occurred during the process the user is redirected to errorPage
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Error while extracting the creation dates of the images: " + e.getMessage());
            return;
        }
        
        listOfInfoImage = assignOrderBasedOnTimestamp(listOfInfoImage);
        
        AlbumDAO albumDAO = new AlbumDAO(connection);
        try {
            albumDAO.createAlbumWithImages(albumTitle, currentUser.getId(), listOfInfoImage);
    	
        //If an error occurred during the process the user is redirected to errorPage
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Error while creating album: " + e.getMessage());
            return;
        }
        
        List<Album> myAlbums;
        try {
        	myAlbums = albumDAO.getUserAlbums(currentUser.getId());
        	
		//If an error occurred during the process the user is redirected to errorPage
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Error while creating album: " + e.getMessage());
            return;
        }
        
        for(Album x : myAlbums) {
        	System.out.println(x.getId() + ", " + x.getTitle() + "," + x.getCreationDate() + "," + x.getUserId());
        }
        
        // JSON serialization
 		response.setStatus(HttpServletResponse.SC_OK);
 		response.setContentType("application/json");
 		response.setCharacterEncoding("UTF-8");
 		Gson gson = new GsonBuilder().create();

 		Map<String, Object> jsonObject = new HashMap<>();
 		jsonObject.put("albums", myAlbums);

 		String json = gson.toJson(jsonObject);
 		response.getWriter().write(json);
    }
    
 // Metodo per parsare la stringa JSON e ottenere la lista di TupleOfInteger
    public void processSelectedImages(HttpServletRequest request, HttpServletResponse response, String selectedImagesJson) throws IOException {
        List<TupleOfInteger> listOfInfoImage = new ArrayList<>();

        // Creare un StringReader per leggere la stringa JSON
        try (StringReader stringReader = new StringReader(selectedImagesJson);
             JsonReader jsonReader = Json.createReader(stringReader)) {

            // Leggere l'array JSON
            JsonArray jsonArray = jsonReader.readArray();

            // Iterare attraverso gli elementi dell'array JSON
            for (JsonValue jsonValue : jsonArray) {
                String imageIdString = ((JsonString) jsonValue).getString();

                // Assumendo che parsingChecker restituisca un Optional<Integer>
                Optional<Integer> parsedImageId = parsingChecker(request, response, imageIdString);
                if (!parsedImageId.isPresent()) {
                    //return;
                }

                TupleOfInteger tupleOfInteger = new TupleOfInteger();
                tupleOfInteger.setKey(parsedImageId.get());
                listOfInfoImage.add(tupleOfInteger);
            }
        } catch (Exception e) {
        	response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println(e.getMessage());
            return;
        }
    }


    private Optional<Integer> parsingChecker(HttpServletRequest request, HttpServletResponse response,
            String stringToParse) throws ServletException, IOException {
        int idToReturn;

        try {
            idToReturn = Integer.parseInt(stringToParse);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Invalid image ID format: " + stringToParse);
            return Optional.empty();
        }

        return Optional.of(idToReturn);
    }
    
    public List<TupleOfInteger> assignOrderBasedOnTimestamp(List<TupleOfInteger> listOfTuples) {
        Collections.sort(listOfTuples, new Comparator<TupleOfInteger>() {
            @Override
            public int compare(TupleOfInteger t1, TupleOfInteger t2) {
                return Long.compare(t2.getValueLong(), t1.getValueLong());
            }
        });

        int orderValue = 1;
        for (TupleOfInteger tuple : listOfTuples) {
            tuple.setValue(orderValue++);
        }
        
        return listOfTuples;
    }
}
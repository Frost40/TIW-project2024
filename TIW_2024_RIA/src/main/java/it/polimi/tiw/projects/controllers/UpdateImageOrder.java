package it.polimi.tiw.projects.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.polimi.tiw.projects.beans.Image;
import it.polimi.tiw.projects.dao.ImageAlbumLinkDAO;
import it.polimi.tiw.projects.utils.ConnectionHandler;

/**
 * Servlet implementation class UpdateImageOrder
 */
@WebServlet("/UpdateImageOrder")
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
		String newImageOrderJson = request.getParameter("orderedList");
        List<Integer> newImageOrder = new ArrayList<>();
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
        
        if (parsingChecker(request, response, albumIdString).isPresent())			albumId = parsingChecker(request, response, albumIdString).get();
		else	return;
        
		String[] selectedImagesArray = newImageOrderJson.split(",");
        for (String imageIdString : selectedImagesArray) {
            imageIdString = imageIdString.trim();
            Optional<Integer> parsedImageId = parsingChecker(request, response, imageIdString);
            
            if (!parsedImageId.isPresent())		return;
            
            newImageOrder.add(parsedImageId.get());
        }
        
		ImageAlbumLinkDAO imageAlbumLinkDAO = new ImageAlbumLinkDAO(connection);
		
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

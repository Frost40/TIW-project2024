package it.polimi.tiw.projects.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.polimi.tiw.projects.beans.User;
import it.polimi.tiw.projects.dao.AlbumDAO;
import it.polimi.tiw.projects.utils.ConnectionHandler;

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

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("currentUser");

        String albumTitle = request.getParameter("albumTitle");
        String selectedImagesJson = request.getParameter("selectedImages");
        List<Integer> selectedImagesIds = new ArrayList<>();

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

        String[] selectedImagesArray = selectedImagesJson.split(",");
        for (String imageIdString : selectedImagesArray) {
            imageIdString = imageIdString.trim();
            Optional<Integer> parsedImageId = parsingChecker(request, response, imageIdString);
            if (!parsedImageId.isPresent()) {
                return;
            }
            selectedImagesIds.add(parsedImageId.get());
        }

        AlbumDAO albumDAO = new AlbumDAO(connection);
        try {
            albumDAO.createAlbumWithImages(albumTitle, currentUser.getId(), selectedImagesIds);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println("Album created successfully!");
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Error while creating album: " + e.getMessage());
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
}

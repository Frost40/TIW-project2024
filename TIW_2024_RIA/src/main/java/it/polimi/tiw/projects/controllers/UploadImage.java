package it.polimi.tiw.projects.controllers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import it.polimi.tiw.projects.beans.User;
import it.polimi.tiw.projects.dao.AlbumDAO;
import it.polimi.tiw.projects.dao.ImageAlbumLinkDAO;
import it.polimi.tiw.projects.dao.ImageDAO;
import it.polimi.tiw.projects.utils.ConnectionHandler;
import it.polimi.tiw.projects.utils.PathHelper;

@WebServlet("/UploadImage")
@MultipartConfig
public class UploadImage extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection;      
    private static final Logger LOGGER = Logger.getLogger(UploadImage.class.getName());
    private String imageStorage;

    public UploadImage() {
        super();
    }
    
    @Override
    public void init() throws ServletException {
        ServletContext servletContext = getServletContext();
        this.connection = ConnectionHandler.getConnection(servletContext);
        imageStorage = getServletContext().getInitParameter("database");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Part filePart = request.getPart("image");
        String imageTitle = request.getParameter("title");
        String imageDescription = request.getParameter("description");
        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("currentUser");
        
        if (imageTitle.length() <= 0 || imageTitle.length() > 45) {            
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);		
			response.getWriter().println("Invalid title (a valid title has more than one character and less than 45)!");
			return;
		}

        if (imageDescription.length() <= 0 || imageDescription.length() > 255) {            
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);		
			response.getWriter().println("Invalid description (a valid description has more than one character and less than 255)!");
			return;        
		}
        
        if (filePart == null || filePart.getSize() <= 0) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);		
			response.getWriter().println("No file uploaded or file is empty!");
			return;        
		}        

        // Starting the process to save the image in server storage
        if(!filePart.getContentType().startsWith("image")) {
        	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);		
			response.getWriter().println("File format not permitted!");
			return;        
		}
        
        // Ensure savePath ends with a file separator
        if (!imageStorage.endsWith(File.separator)) {
        	imageStorage += File.separator;
        }
        String fileName = imageTitle + ".jpg";
        fileName = fieNameGenerator(imageStorage, fileName);

        //InputStream fileContent = filePart.getInputStream();
        String outputFilePath = imageStorage + fileName;
        File file = new File(outputFilePath);

        try (InputStream fileContent = filePart.getInputStream()) {
			
			Files.copy(fileContent, file.toPath());
			System.out.println("File saved correctly!");
            
        } catch (IOException e) {
        	response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println(e.getMessage());
			return;
        }

        //Saving image's info in database first in order to retrieve the id
        ImageDAO imageDAO = new ImageDAO(connection);
        int imageId = 0;
        String path = "http://localhost:8080/imageStorage/" + fileName;

        try {
            imageId = imageDAO.uploadImage(imageTitle, imageDescription, path, currentUser.getId());
        } catch (SQLException e) {
        	response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println(e.getMessage());
			return;
        }

        if (imageId == 0) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().println("An error occurred while retrieving image's id");
            return;
        }
        
        //Starting pairing album with image
        ImageAlbumLinkDAO imageAlbumLinkDAO = new ImageAlbumLinkDAO(connection);
        AlbumDAO albumDAO = new AlbumDAO(connection);
        int albumId = 0;

        try {
            albumId = albumDAO.getAlbumAllPhotosId(currentUser.getId());
        } catch (SQLException e) {
        	response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println(e.getMessage());
			return;
        }

        if (albumId == 0) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().println("An error occurred while retrieving album's id");
            return;
        }

        //Adding image to album
        try {
            imageAlbumLinkDAO.addImageToAlbum(albumId, imageId);
        } catch (SQLException e) {
        	response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println(e.getMessage());
			return;
        }
        
        response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().println("Image saved successfully!");
    }
    
    private String fieNameGenerator(String directory, String fileName) {
        File file = new File(directory, fileName);
        if (!file.exists()) {
            return fileName;
        }

        int counter = 1;
        String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
        String extension = fileName.substring(fileName.lastIndexOf('.'));
        String newFileName;

        do {
            newFileName = baseName + "_" + counter + extension;
            file = new File(directory, newFileName);
            counter++;
        } while (file.exists());

        return newFileName;
    }
}

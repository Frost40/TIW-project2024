package controllers;

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

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import beans.User;
import dao.AlbumDAO;
import dao.ImageAlbumLinkDAO;
import dao.ImageDAO;
import utils.ConnectionHandler;
import utils.PathHelper;
import utils.TemplateHandler;

@WebServlet("/UploadImage")
@MultipartConfig
public class UploadImage extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private TemplateEngine templateEngine;
    private Connection connection;      
    private String imageStorage;

    public UploadImage() {
        super();
    }
    
    @Override
    public void init() throws ServletException {
        ServletContext servletContext = getServletContext();
        this.templateEngine = TemplateHandler.getEngine(servletContext, ".html");
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
        
        if (imageTitle == null || imageTitle.length() <= 0 || imageTitle.length() > 45) {
            String warningMessage = "Invalid title (a valid title has more than one character and less than 45)!";
            request.setAttribute("error", warningMessage);
            forward(request, response, PathHelper.goToErrorPage());
            return;
        }

        if (imageDescription == null || imageDescription.length() <= 0 || imageDescription.length() > 255) {
            String warningMessage = "Invalid description (a valid description has more than one character and less than 255)!";
            request.setAttribute("error", warningMessage);
            forward(request, response, PathHelper.goToErrorPage());
            return;
        }
        
        if (filePart == null || filePart.getSize() <= 0) {
        	String warningMessage = "No file uploaded or file is empty!";
        	request.setAttribute("error", warningMessage);
            forward(request, response, PathHelper.goToErrorPage());
            return;
        }        

        // Starting the process to save the image in server storage
        if(!filePart.getContentType().startsWith("image")) {
        	request.setAttribute("error", "File format not permitted!");
            forward(request, response, PathHelper.goToErrorPage());
            return;
        }
        
        // Ensure savePath ends with a file separator
        if (!imageStorage.endsWith(File.separator)) {
        	imageStorage += File.separator;
        }
        String fileName = removeSpecialCharacters(imageTitle) + ".jpg";
        fileName = fieNameGenerator(imageStorage, fileName);

        //InputStream fileContent = filePart.getInputStream();
        String outputFilePath = imageStorage + fileName;
        File file = new File(outputFilePath);

        try (InputStream fileContent = filePart.getInputStream()) {
			Files.copy(fileContent, file.toPath());
			System.out.println("File saved correctly!");
            
        } catch (IOException e) {
            request.setAttribute("error", e.getMessage());
            forward(request, response, PathHelper.goToErrorPage());
            return;
            
        }
        
        //Saving image's info in database first in order to retrieve the id
        ImageDAO imageDAO = new ImageDAO(connection);
        int imageId = 0;
        String path = "http://localhost:8080/imageStorage/" + fileName;

        try {
            imageId = imageDAO.uploadImage(imageTitle, imageDescription, path, currentUser.getId());
        } catch (SQLException e) {
            request.setAttribute("error", e.getMessage());
            forward(request, response, PathHelper.goToErrorPage());
            return;
        }

        if (imageId == 0) {
            request.setAttribute("error", "An error occurred while retrieving image's id");
            forward(request, response, PathHelper.goToErrorPage());
            return;
        }
        
        //Starting pairing album with image
        ImageAlbumLinkDAO imageAlbumLinkDAO = new ImageAlbumLinkDAO(connection);
        AlbumDAO albumDAO = new AlbumDAO(connection);
        int albumId = 0;

        try {
            albumId = albumDAO.getAlbumAllPhotosId(currentUser.getId());
        } catch (SQLException e) {
            request.setAttribute("error", e.getMessage());
            forward(request, response, PathHelper.goToErrorPage());
            return;
        }

        if (albumId == 0) {
            request.setAttribute("error", "An error occurred while retrieving album's id");
            forward(request, response, PathHelper.goToErrorPage());
            return;
        }

        //Adding image to album
        try {
            imageAlbumLinkDAO.addImageToAlbum(albumId, imageId);
        } catch (SQLException e) {
            request.setAttribute("error", e.getMessage());
            forward(request, response, PathHelper.goToErrorPage());
            return;
        }
        
        response.sendRedirect(getServletContext().getContextPath() + PathHelper.goToServlet("home"));
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
    
    public static String removeSpecialCharacters(String input) {
        String cleanedString = input.replaceAll("[^a-zA-Z0-9]", "");
        
        if (cleanedString.isEmpty()) {
            return "placeHolder";
        }
        
        return cleanedString;
    }

    private void forward(HttpServletRequest request, HttpServletResponse response, String path) throws ServletException, IOException {
        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
        templateEngine.process(path, ctx, response.getWriter());
    }
}

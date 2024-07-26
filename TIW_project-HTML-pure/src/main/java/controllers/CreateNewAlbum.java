package controllers;

import java.io.IOException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
import dao.AlbumDAO;
import dao.ImageDAO;
import utils.ConnectionHandler;
import utils.PathHelper;
import utils.TemplateHandler;

/**
 * Servlet implementation class CreatNewAlbum
 */
@WebServlet("/CreateNewAlbum")
public class CreateNewAlbum extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;
	private Connection connection;       
    
	/**
     * @see HttpServlet#HttpServlet()
     */
    public CreateNewAlbum() {
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
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		User currentUser    = (User)session.getAttribute("currentUser");
		
        String albumTitle = request.getParameter("albumTitle");
        String[] selectedImagesIdsStrings = request.getParameterValues("selectedImages");
        List<Integer> selectedImagesIds = new ArrayList<>();
        int imageId;
        
        if(albumTitle == null) {
        	request.setAttribute("error", "Null or empty album title!");
			forward(request, response, PathHelper.goToErrorPage());
			return;
        }
        
        if (albumTitle.length() <= 0 || albumTitle.length() > 45 || albumTitle.equals("allPhotos")) {
            request.setAttribute("error", "Invalid album title (a valid title has more than one character and less than 45 and can not be called 'allPhotos')!");
			forward(request, response, PathHelper.goToErrorPage());
			return;
        }
        
        if(selectedImagesIdsStrings == null || selectedImagesIdsStrings.length == 0) {
        	request.setAttribute("error", "No images have been selected to be part of the album");
			forward(request, response, PathHelper.goToErrorPage());
			return;
        }
        
        for(String x : selectedImagesIdsStrings) {
        	if(x == null) {
            	request.setAttribute("error", "An error occred while getting one of the image's id");
    			forward(request, response, PathHelper.goToErrorPage());
    			return;
    			
            } else {
            	//Parsing the imageId
                Optional<Integer> parsedImageId = parsingChecker(request, response, x);
                if (parsedImageId.isPresent()) {
                	imageId = parsedImageId.get();
                	selectedImagesIds.add(imageId);
                }
                else	return;
            }
    	}
        
        ImageDAO imageDAO = new ImageDAO(connection);
        List<Image> images;
        try {
        	images = imageDAO.getImagesByIds(selectedImagesIds);
		
		//If an error occurred during the process the user is redirected to errorPage
		} catch (SQLException e) {
			request.setAttribute("error", e.getMessage());
			forward(request, response, PathHelper.goToErrorPage());
			return;
		}
        
        List<Integer> idsSelectedDb = images.stream()
							        		 .sorted((img1, img2) -> img2.getCreationDate().compareTo(img1.getCreationDate())) 
							                 .map(Image::getId) 
							                 .collect(Collectors.toList());
        
        if (idsSelectedDb.size() != selectedImagesIds.size()) {
        	request.setAttribute("error", "One or more images selected do not exist!");
			forward(request, response, PathHelper.goToErrorPage());
			return;
        }
        	
        AlbumDAO albumDAO = new AlbumDAO(connection);
        Collections.reverse(idsSelectedDb);
        try {
        	albumDAO.createAlbumWithImages(albumTitle, currentUser.getId(), idsSelectedDb);
        
    	//If an error occurred during the process the user is redirected to errorPage
		} catch (SQLException e) {
			request.setAttribute("error", e.getMessage());
			forward(request, response, PathHelper.goToErrorPage());
			return;
		}
        
        response.sendRedirect(getServletContext().getContextPath() + PathHelper.goToServlet("home"));
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
	
	private void forward(HttpServletRequest request, HttpServletResponse response, String path) throws ServletException, IOException{
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		templateEngine.process(path, ctx, response.getWriter());
	}

}

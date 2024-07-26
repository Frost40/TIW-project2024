package controllers;

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
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import beans.Album;
import beans.Image;
import beans.User;
import dao.AlbumDAO;
import dao.ImageDAO;
import utils.ConnectionHandler;
import utils.Message;
import utils.PathHelper;
import utils.TemplateHandler;

/**
 * Servlet implementation class goToAlbumPage
 */
@WebServlet("/GoToAlbumPage")
public class GoToAlbumPage extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;
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
		this.templateEngine = TemplateHandler.getEngine(servletContext, ".html");
		this.connection = ConnectionHandler.getConnection(servletContext);
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		User currentUser    = (User)session.getAttribute("currentUser");
		
		String albumIdString = request.getParameter("albumId");
		String pageNumberString = request.getParameter("pageNumber");
		int albumId;
		int pageNumber;
		
		if(albumIdString == null || albumIdString.isEmpty()) {
			request.setAttribute("error", "Null albumId!");
			forward(request, response, PathHelper.goToErrorPage());
			return;
		}
		
		//If the pageNumber is present as a parameter in the request it gets parsed correctly, else it gets set to 0 by default
		if(pageNumberString == null || pageNumberString.isEmpty())	pageNumber = 0;
		else	{
			Optional<Integer> parsedPageNumber = parsingChecker(request, response, pageNumberString);
			if(parsedPageNumber.isPresent()) 		pageNumber = parsedPageNumber.get();		
			else	return;
		}
		
		Optional<Integer> parsedId = parsingChecker(request, response, albumIdString);
		if (parsedId.isPresent())			albumId = parsedId.get();
		else	return;
		
		if(albumId < 0) {
			request.setAttribute("error", "Id provided for the album is not a valid number");
			forward(request, response, PathHelper.goToErrorPage());
			return;
		}
		
		if(pageNumber < 0) {
			request.setAttribute("error", "Page number is not valid");
			forward(request, response, PathHelper.goToErrorPage());
			return;
		}
		
		//Assuring the album exists
		AlbumDAO albumDAO = new AlbumDAO(connection);
		Album album;
		try {
			album = albumDAO.getAlbumById(albumId);
			
		//If an error occurred during the process the user is redirected to errorPage
		} catch (SQLException e) {
			request.setAttribute("error", e.getMessage());
			forward(request, response, PathHelper.goToErrorPage());
			return;
		}
		
		if(album == null) {
			request.setAttribute("error", "The album you are trying to access does not exists");
			forward(request, response, PathHelper.goToErrorPage());
			return;
		}

		if(album.getTitle().equals("allPhotos") && album.getUserId() != currentUser.getId()) {
			request.setAttribute("error", "You are not authorized to open the album!");
			forward(request, response, PathHelper.goToErrorPage());
			return;
		}
		
		//Getting all album's username creator and title using "albumID"
		Message returnedMessage;
		try {
			returnedMessage = albumDAO.getUsernameCreatorAndTitle(albumId);
					
		//If an error occurred during the process the user is redirected to errorPage
		} catch (SQLException e) {
			request.setAttribute("error", e.getMessage());
			forward(request, response, PathHelper.goToErrorPage());
			return;
		}
		
		if(returnedMessage == null) {
			request.setAttribute("error", "Unable to get info related to the album");
			forward(request, response, PathHelper.goToErrorPage());
			return;
		}

		//Getting images in 'albumId'
		ImageDAO imageDAO = new ImageDAO(connection);
		List<Image> images = null;
		try {
			images = imageDAO.getImagesByAlbumId(albumId);
			
		//If an error occurred during the process the user is redirected to errorPage
		} catch (SQLException e) {
			request.setAttribute("error", e.getMessage());
			forward(request, response, PathHelper.goToErrorPage());
			return;
		}
		
		//Making the pageNumber does not exceed the actual number of pages
		int lastPage = getLastPageIndex(images);
		if(pageNumber > lastPage)	pageNumber = 0;
				
		//If the album is empty is created an empty list in order to redirect to errorPage, else "imagesToShow" is filled with the images in page number "pageNumber"
		List<Image> imagesToShow;
		if(images == null || images.isEmpty()) {
			imagesToShow = new ArrayList<>();
			request.setAttribute("hasNext", Boolean.FALSE);
		}
		else	{
			imagesToShow = getPage(images, pageNumber);
			
			//Setting "hasNext" boolean in html file in order to show or hide buttons
			if(pageNumber < lastPage) {
				request.setAttribute("hasNext", Boolean.TRUE);

			}
			else	request.setAttribute("hasNext", Boolean.FALSE);
			
		}
		
		if(imagesToShow.isEmpty()) {
			request.setAttribute("warning", "An error occured you have been redirected to the first page");
			forward(request, response, PathHelper.goToAlbumPage());
			return;
		}
		
		request.setAttribute("creator", returnedMessage.getInfo().get(0));
		request.setAttribute("albumTitle", returnedMessage.getInfo().get(1));
		request.setAttribute("albumId", albumId);
		request.setAttribute("pageNumber", pageNumber);
		request.setAttribute("images", imagesToShow);
		
		forward(request, response, PathHelper.goToAlbumPage());
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
		if(stringToParse == null) {
			request.setAttribute("error", "");
			forward(request, response, PathHelper.goToErrorPage());
			return Optional.empty();
		}
		
		try {
			idToReturn = Integer.parseInt(stringToParse);
			
		} catch (NumberFormatException e) {
			request.setAttribute("error", "Error while parsing: " + stringToParse + " is not a valid number");
			forward(request, response, PathHelper.goToErrorPage());
			return Optional.empty();
		}
		
		return Optional.of(idToReturn);
	}
	
	private List<Image> getPage(List<Image> images, int groupNumber) {
        int groupSize = 5;		// Defining the size of each group

        // Calculate the start index of the desired group
        int startIndex = groupNumber * groupSize;

        // If the startIndex is greater than or equal to the size of the list, return an empty list
        if (startIndex >= images.size())	return new ArrayList<>();

        // Calculate the end index of the desired group
        int endIndex = Math.min(startIndex + groupSize, images.size());

        // Return the sublist representing the desired group
        return images.subList(startIndex, endIndex);
    }
	
	private int getLastPageIndex(List<Image> images) {
        int groupSize = 5;		// Defining the size of each group

        // Calculate the total number of groups (pages)
        int totalGroups = (int) Math.ceil((double) images.size() / groupSize);

        // The last page index is the total number of groups minus one (because pages start from 0)
        return totalGroups - 1;
    }

	
	private void forward(HttpServletRequest request, HttpServletResponse response, String path) throws ServletException, IOException{
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		templateEngine.process(path, ctx, response.getWriter());
	}
}

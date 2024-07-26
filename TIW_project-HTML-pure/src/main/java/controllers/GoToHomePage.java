package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

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
import utils.PathHelper;
import utils.TemplateHandler;

/**
 * Servlet implementation class goToHomePage
 */
@WebServlet("/GoToHomePage")
public class GoToHomePage extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;
	private Connection connection;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GoToHomePage() {
        super();
        // TODO Auto-generated constructor stub
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

		AlbumDAO albumDAO = new AlbumDAO(connection);
		List<Album> myAlbums = null;
		List<Album> othersAlbums = null;
		
		try {
			myAlbums = albumDAO.getUserAlbums(currentUser.getId());
			
		} catch(SQLException e) {
			
			request.setAttribute("error", e.getMessage());
			forward(request, response, PathHelper.goToErrorPage());
			return;
		}
		
		if (myAlbums == null || myAlbums.isEmpty()) {
			request.setAttribute("error", "An error occured while getting your albums from database");
			forward(request, response, PathHelper.goToErrorPage());
			return;
		}
		
		try {
			othersAlbums = albumDAO.getAlbumsNotFromUser(currentUser.getId());

		} catch(SQLException e) {
			
			request.setAttribute("error", e.getMessage());
			forward(request, response, PathHelper.goToErrorPage());
			return;
		}
		
		//Getting all images from "allPhotos" to be ready in case of new album creation
		ImageDAO imageDAO = new ImageDAO(connection);
		List<Image> allImages;
		try {
			allImages = imageDAO.getImagesByAlbumId(myAlbums.get(0).getId());
			
		//If an error occurred during the process the user is redirected to errorPage
		} catch (SQLException e) {
			request.setAttribute("error", e.getMessage());
			forward(request, response, PathHelper.goToErrorPage());
			return;
		}
		
		request.setAttribute("myAlbums", myAlbums);
		request.setAttribute("othersAlbums", othersAlbums);
		request.setAttribute("images", allImages);

		forward(request, response, PathHelper.goToHomePage());

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}
	
private void forward(HttpServletRequest request, HttpServletResponse response, String path) throws ServletException, IOException{
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		templateEngine.process(path, ctx, response.getWriter());
	}

}

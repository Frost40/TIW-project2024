package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import beans.User;
import dao.UserDAO;
import utils.ConnectionHandler;
import utils.PathHelper;
import utils.TemplateHandler;

/**
 * Servlet implementation class Login
 */
@WebServlet("/Login")
public class Login extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;
	private Connection connection = null;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Login() {
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
		// TODO Auto-generated method stub
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String id    = request.getParameter("id");
		String password = request.getParameter("password");
		
		String email = null;
		String username = null;
		UserDAO userDAO = new UserDAO(connection);
		User user = null;
		
		//Verifying given argument are not null. In case, redirecting to errorPage
		if(id == null || password == null) {
			request.setAttribute("error", "Null parameters");
			forward(request, response, PathHelper.goToErrorPage());
			return;
		}
		
		if(isAnEmail(id)) {
			email = id;
			
			if (email.length() > 255 || email.length() <= 0) {
				request.setAttribute("error", "Invalid email");
				forward(request, response, PathHelper.goToErrorPage());
				return;
			}
			
			if (password.length() <= 0 || password.length() > 10) {
				request.setAttribute("error", "Invalid password");
				forward(request, response, PathHelper.goToErrorPage());
				return;
			}	
			
			try {
				user = userDAO.authenticationViaEmail(email, password);
				
			} catch (SQLException e) {
				request.setAttribute("error", e.getMessage());
				
				forward(request, response, PathHelper.goToErrorPage());
				return;
			}

			if(user == null) {
				request.setAttribute("warning", "Email or password incorrect!");
				forward(request, response, PathHelper.goToLoginPage());
				return;
			}
			
		} else {
			username = id;
			
			if (username.length() > 20 || username.length() <= 0 || containsSpecialCharacters(username)) {
				request.setAttribute("error", "Invalid username!");
				forward(request, response, PathHelper.goToErrorPage());
				return;
			}
			
			if (password.length() <= 0 || password.length() > 10) {
				request.setAttribute("error", "Invalid password");
				forward(request, response, PathHelper.goToErrorPage());
				return;
			}	
			
			try {
				user = userDAO.authenticationViaUsername(username, password);
				
			} catch (SQLException e) {
				request.setAttribute("error", e.getMessage());
				forward(request, response, PathHelper.goToErrorPage());
				return;
			}
			
			if(user == null) {
				request.setAttribute("warning", "Username or password incorrect!");
				forward(request, response, PathHelper.goToLoginPage());
				return;
			}
		}
		
		HttpSession session = request.getSession();
		session.setAttribute("currentUser", user);
		
		response.sendRedirect(getServletContext().getContextPath() + PathHelper.goToServlet("home"));
	}
	
private boolean isAnEmail(String emailAddress) {
	String EMAIL_REGEX = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
	
    Pattern pattern = Pattern.compile(EMAIL_REGEX);
    Matcher matcher = pattern.matcher(emailAddress);
    return matcher.matches();
}

public static boolean containsSpecialCharacters(String input) {
    String specialCharactersPattern = "[^a-zA-Z0-9]";

    return input != null && input.matches(".*" + specialCharactersPattern + ".*");
}

private void forward(HttpServletRequest request, HttpServletResponse response, String path) throws ServletException, IOException{
		
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		templateEngine.process(path, ctx, response.getWriter());
	}

}

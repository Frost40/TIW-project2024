package Filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import utils.PathHelper;

public class LoginCheck implements Filter{
	private TemplateEngine templateEngine;

	/**
	 * Default constructor.
	 */
	public LoginCheck() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
		ServletContext servletContext = fConfig.getServletContext();
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
	}

	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub
	}

	@Override
	public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain arg2) throws IOException, ServletException {
		HttpServletRequest  req = (HttpServletRequest) arg0;
		HttpServletResponse res = (HttpServletResponse) arg1;
		HttpSession s = req.getSession(false);

		if (s != null) {
			Object user = s.getAttribute("currentUser");
			
			if (user != null) {
				
				arg2.doFilter(arg0, arg1);
				return;
			}
		}
		
		req.setAttribute("error", "You are not authorized to access this page");
		forward(req, res, PathHelper.goToErrorPage());
	}
	
	/**
	 * Forwards to the specified path
	 * 
	 * @param request
	 * @param response
	 * @param path
	 * @throws ServletException
	 * @throws IOException
	 */
	public void forward(HttpServletRequest request, HttpServletResponse response, String path)
			throws ServletException, IOException {

		ServletContext servletContext = request.getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		templateEngine.process(path, ctx, response.getWriter());

	}

}

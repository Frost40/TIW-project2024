package it.polimi.tiw.projects.filters;

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

import it.polimi.tiw.projects.utils.PathHelper;

public class LoginCheck implements Filter{

	/**
	 * Default constructor.
	 */
	public LoginCheck() {
		// TODO Auto-generated constructor stub
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
		
		res.sendRedirect(req.getServletContext().getContextPath() + PathHelper.goToLoginPage());
	}

}

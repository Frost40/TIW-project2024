package utils;

public class PathHelper {

	public static String goToHomePage() {
		return "/WEB-INF/homePage.html";
	}

	public static String goToLoginPage() {
		return "/WEB-INF/login.html";
	}
	
	public static String goToErrorPage() {
		return "/WEB-INF/error.html";
	}
	
	public static String goToSignUpPage() {
		return "/WEB-INF/signUp.html";
	}
	
	public static String goToAlbumPage() {
		return "/WEB-INF/albumPage.html";
	}
	
	public static String goToImagePage() {
		return "/WEB-INF/imagePage.html";
	}
	
	public static String goToServlet(String servletName) {
		String path = null;
		
		switch(servletName.toLowerCase()) {
			case "login" -> path = "/GoToLogin";
			case "home" -> path = "/GoToHomePage";
			case "album" -> path = "/GoToAlbumPage";
			case "image" -> path = "/GoToImagePage";
		}
		
		return path;
	}

}

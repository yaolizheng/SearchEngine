import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The account servlet provides the account service
 * 
 * @author Yaoli Zheng
 *
 */
@SuppressWarnings("serial")
public class Account extends BaseServlet{
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		
		PrintWriter out;
		try {
			Map<String, String> cookies = getCookieMap(request);
			
			String login = cookies.get("login");
			String user  = cookies.get("name");
			if(login != null && login.equals("true") && user != null) {

			out = response.getWriter();
			out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">\n");
			out.println("<html>");
			out.println("");
			out.println("<head>");
			out.println("\t<title>Account</title>" );
			out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"css/style.css\">");
			out.println("</head>");
			out.println("<frameset rows=\"50,*\" cols=\"*\" framespacing=\"2\" frameborder=\"no\" border=\"1\" bordercolor=\"White\">");
			out.println("<frame src=\"/title\" name=\"logoframe\" scrolling=\"NO\" noresize>");
			out.println("<frameset rows=\"*\" cols=\"250,*\" framespacing=\"2\" frameborder=\"no\" border=\"2\" bordercolor=\"White\">");
			out.println("<frame src=\"/menu\" name=\"leftFrame\" noresize scrolling=\"no\">");
			out.println("<frame src=\"\" name=\"rightFrame\" scrolling=\"yes\" noresize>");
			out.println("<frameset>");
			out.println("<frameset>");
			out.println("</html>");
			out.flush();
			}
			else {
				response.sendRedirect(response.encodeRedirectURL("/login" ));
			}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

}

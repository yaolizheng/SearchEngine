import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet is the home page of the search engine
 * 
 * @author Yaoli Zheng
 *
 */

@SuppressWarnings("serial")
public class IndexServlet extends BaseServlet
{	
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		
		prepareResponse("Search", response);
		PrintWriter out;
		try {
			out = response.getWriter();
		
			out.println("<div class=\"background\">");
		
			Map<String, String> cookies = getCookieMap(request);
			
			String login = cookies.get("login");
			String user  = cookies.get("name");

			if(login != null && login.equals("true") && user != null) {
				
				out.println("<div class=\"welcome\">welcome," + user + "/last login:" + db.getLastlogin(user) + "/<a href = \"/login?logout\">logout</a>/<a href = \"/account\">Account</a>/<a href = \"/advanced\">Advanced Search</a></div>");
			}
			else {
				out.println("<div class=\"welcome\"><a href = \"/login\">login</a>/<a href = \"/register\">register</a>/<a href = \"/advanced\">Advanced Search</a></div>");
			}
			out.println("<div class=\"searchTag\">");
			out.println("<form action=\"/results\" method=\"post\">");
			out.println("<input type=\"text\" class=\"text\" name=\"key\"><input type=\"submit\" class=\"button\" value=\"\">");
			out.println("</form>");
			out.println("</div>");
			out.println("</div>");
			} catch (IOException e) {
				e.printStackTrace();
			}
			finishResponse(response);
	}
}

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet provides a title for pages
 * 
 * @author Yaoli Zheng
 *
 */
@SuppressWarnings("serial")
public class TitleServlet extends BaseServlet {
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		
		PrintWriter out;
		try {
			out = response.getWriter();
			Map<String, String> cookies = getCookieMap(request);
			
			String login = cookies.get("login");
			String user  = cookies.get("name");
			if(login != null && login.equals("true") && user != null) {
				String theme = db.getTheme(user);
				prepareResponseWithTheme("Title", response, theme);
			}
			else {
				prepareResponse("Title", response);
			}
			
			out.println("<div class=\"Top\">");
			out.println("<div class=\"return\"><a href=\"/index\" target=_parent>HOME</a></div>");
			

			if(login != null && login.equals("true") && user != null) {
				out.println("<div class=\"welcome\">welcome," + user + "/last login:" + db.getLastlogin(user) + "/<a href = \"/login?logout\" target=_parent>logout</a>/<a href = \"/account\" target=_parent>Account</a>/<a href = \"/advanced\" target=_parent>Advanced Search</a></div>");
			}
			else {
				out.println("<div class=\"welcome\"><a href = \"/login\" target=_parent>login</a>/<a href = \"/register target=_parent\">register</a><a href = \"/advanced\" target=_parent>Advanced Search</a></div>");
			}
			out.println("</div>");
		} catch (IOException e) {
			e.printStackTrace();
		}
		finishResponse(response);
	}
}

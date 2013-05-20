import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet provides a menu of account
 * 
 * @author Yaoli Zheng
 *
 */
@SuppressWarnings("serial")
public class MenuServlet extends BaseServlet {
public void doGet(HttpServletRequest request, HttpServletResponse response) {
		
		Map<String, String> cookies = getCookieMap(request);
		
		String login = cookies.get("login");
		String user  = cookies.get("name");
		if(login != null && login.equals("true") && user != null) {
			String theme = db.getTheme(user);
			prepareResponseWithTheme("Menu", response, theme);
		}
		else {
			prepareResponse("Menu", response);
		}
		PrintWriter out;
		try {
			out = response.getWriter();
		
			out.println("<div class=\"menu\">");
			out.println("<div><a href = \"/password\" target=\"rightFrame\">Change password</a></div>");
			out.println("<div><a href = \"/history\" target=\"rightFrame\">View history</a></div>");
			out.println("<div><a href = \"/page\" target=\"rightFrame\">View visited page</a></div>");
			out.println("<div><a href = \"/seed\" target=\"rightFrame\">Add new seed</a></div>");
			out.println("<div><a href = \"/num\" target=\"rightFrame\">Change number per page</a></div>");
			out.println("<div><a href = \"/theme\" target=\"rightFrame\">Change theme</a></div>");
			if(invertedIndex.getToggle() == true)
				out.println("<div>Partial Toggle  <a href = \"/toggle?act=false\">off</a></div>");
			else
				out.println("<div>Partial Toggle  <a href = \"/toggle?act=true\">on</a></div>");
			out.println("<div><a href = \"/shutdown\" target=\"rightFrame\">Shut down</a></div>");
			out.println("</div>");
			} catch (IOException e) {
				e.printStackTrace();
			}
			finishResponse(response);
	}
}

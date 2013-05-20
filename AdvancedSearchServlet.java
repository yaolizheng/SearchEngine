import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The AdvancedSearch servlet provides the advanced search
 * 
 * @author Yaoli Zheng
 *
 */
@SuppressWarnings("serial")
public class AdvancedSearchServlet extends BaseServlet{
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String> cookies = getCookieMap(request);
			
			String login = cookies.get("login");
			String user  = cookies.get("name");
			if(login != null && login.equals("true") && user != null) {
				String theme = db.getTheme(user);
				prepareResponseWithTheme("Advanced search", response, theme);
			}
			else {
				prepareResponse("Advanced search", response);
			}
			
			PrintWriter out = response.getWriter();
			
			out.println("<div class=\"Top\">");
			out.println("<div class=\"return\"><a href=\"/index\">HOME</a></div>");
			if(login != null && login.equals("true") && user != null) {
				out.println("<div class=\"welcome\">welcome," + user + "/last login:" + db.getLastlogin(user) + "/<a href = \"/login?logout\">logout</a>/<a href = \"/account\">Account</a>/<a href = \"/advanced\">Advanced Search</a></div>");
			}
			else {
				out.println("<div class=\"welcome\"><a href = \"/login target=_parent\">login</a>/<a href = \"/register target=_parent\">register</a>/<a href = \"/advanced\">Advanced Search</a></div>");
			}
			out.println("</div>");
			out.println("<div class=\"login\">");
			out.println("<div class=\"loginform\">");
			out.println("<p class=\"title\">Advanced Search</p>");

			printForm(out);
			out.println("<p>(click <a href=\"/index\">here</a> to homepage)</p>");
			out.println("</div>");
			out.println("</div>");
			finishResponse(response);
		}
		catch(IOException ex)
		{
			log.debug("Unable to prepare response properly.", ex);
		}
	}
	
	private void printForm(PrintWriter out)
	{
		assert out != null;
		
		out.println("<form action=\"/results\" method=\"post\">");
		out.println("<table class=\"logintable\">");
		out.println("\t<tr>");
		out.println("\t\t<td>All these words: </td>");
		out.println("\t\t<td><input type=\"text\" name=\"key\" size=\"20\"></td>");
		out.println("\t</tr>");
		out.println("\t<tr>");
		out.println("\t\t<td>Consecutive Words: </td>");
		out.println("\t\t<td><input type=\"text\" name=\"consecutive\" size=\"20\"></td>");
		out.println("</tr>");
		out.println("\t<tr>");
		out.println("\t\t<td>Excluded Words: </td>");
		out.println("\t\t<td><input type=\"text\" name=\"no\" size=\"20\"></td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("<p><input type=\"submit\" value=\"Search\"></p>");
		out.println("</form>");
	}

}

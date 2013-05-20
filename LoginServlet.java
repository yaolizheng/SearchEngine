import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet for handling login requests.
 * 
 * @author Yaoli Zheng
 */

@SuppressWarnings("serial")
public class LoginServlet extends BaseServlet 
{

	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		prepareResponse("Login", response);
		
		try {
			PrintWriter out = response.getWriter();
			String error = request.getParameter("error");
			out.println("<div class=\"Top\">");
			out.println("<div class=\"return\"><a href=\"/index\">HOME</a></div>");
			Map<String, String> cookies = getCookieMap(request);
			
			String login = cookies.get("login");
			String user  = cookies.get("name");
			String time  = cookies.get("lastlogin");

			if((login != null && login.equals("true") && user != null) && request.getParameter("logout") == null) {
				out.println("<div class=\"welcome\">welcome," + user + "/last login:" + db.getLastlogin(user) + "/<a href = \"/login?logout\">logout</a>/<a href = \"/account\">Account</a>/<a href = \"/advanced\">Advanced Search</a></div>");
			}
			else {
				out.println("<div class=\"welcome\"><a href = \"/login\">login</a>/<a href = \"/register\">register</a>/<a href = \"/advanced\">Advanced Search</a></div>");
			}
			out.println("</div>");
			out.println("<div class=\"login\">");
			out.println("<div class=\"loginform\">");
			if(request.getParameter("logout") != null) {
				db.updateLastLogin(user, time);
				eraseCookies(request, response);
				out.println("<p class=\"title\">LOGOUT</p>");
				out.println("<p>Successfully logged out.</p>");
			}
			else {
				out.println("<p class=\"title\">LOGIN</p>");
			}

			
			if(error != null) {

				String errorMessage = getStatusMessage(error);
				out.println("<p style=\"color: red;\">" + errorMessage + "</p>");
			}
			if(request.getParameter("newuser") != null)
			{
				out.println("<p>Registration was successful!</p>");
				out.println("<p>Login with your new username and password below.</p>");
			}

			printForm(out);
			out.println("<p>(<a href=\"/register\">new user? register here.</a>)</p>");
			out.println("<p>(click <a href=\"/index\">here</a> to homepage)</p>");
			out.println("</div>");
			out.println("</div>");
		}
		catch(IOException ex)
		{
			log.debug("Unable to prepare response body.", ex);
		}

		finishResponse(response);
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response)
	{
		String user = request.getParameter("user");
		String pass = request.getParameter("pass");
		
		Status status = db.verifyLogin(user, pass);
		
		try
		{
			if(status == Status.OK)
			{
				// add cookies to indicate user successfully logged in
				response.addCookie(new Cookie("login", "true"));
				response.addCookie(new Cookie("name", user));
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				Date date = new Date();
				response.addCookie(new Cookie("lastlogin", df.format(date)));
				
				
				// redirect to welcome page
				response.sendRedirect(response.encodeRedirectURL("/index"));
			}
			else
			{
				// make sure any old login cookies are cleared
				response.addCookie(new Cookie("login", "false"));
				response.addCookie(new Cookie("name", ""));

				// let user try again
				response.sendRedirect(response.encodeRedirectURL("/login?error=" + status.name()));
			}
		}
		catch(Exception ex)
		{
			log.error("Unable to process login form.", ex);
		}
	}

	private void printForm(PrintWriter out)
	{
		assert out != null;

		
		out.println("<form action=\"/login\" method=\"post\">");
		out.println("<table class=\"logintable\">");
		out.println("\t<tr>");
		out.println("\t\t<td>Usename:</td>");
		out.println("\t\t<td><input type=\"text\" name=\"user\" size=\"20\"></td>");
		out.println("\t</tr>");
		out.println("\t<tr>");
		out.println("\t\t<td>Password:</td>");
		out.println("\t\t<td><input type=\"password\" name=\"pass\" size=\"20\"></td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("<p><input type=\"submit\" value=\"Login\"></p>");
		out.println("</form>");

	}
}

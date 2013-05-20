import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet for handling registration requests
 * 
 * @author Yaoli Zheng
 */
@SuppressWarnings("serial")
public class RegisterServlet extends BaseServlet 
{

	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		try {
			prepareResponse("Register New User", response);
			
			PrintWriter out = response.getWriter();
			String error = request.getParameter("error");
			
			out.println("<div class=\"Top\">");
			out.println("<div class=\"return\"><a href=\"/index\">HOME</a></div>");
			out.println("<div class=\"welcome\"><a href = \"/login\">login</a>/<a href = \"/register\">register</a>/<a href = \"/advanced\">Advanced Search</a></div>");
			out.println("</div>");
			out.println("<div class=\"login\">");
			out.println("<div class=\"loginform\">");
			out.println("<p class=\"title\">REGISTER</p>");

			if(error != null) {
				String errorMessage = getStatusMessage(error);
				out.println("<p style=\"color: red;\">" + errorMessage + "</p>");
			}
			
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


	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		prepareResponse("Register New User", response);

		// get username and password from form
		String newuser = request.getParameter("user");
		String newpass = request.getParameter("pass");
		
		// get status from database handler registration attempt
		Status status = db.registerUser(newuser, newpass);

		try {
			if(status == Status.OK) {
				// if everything went okay, let the new user login
				response.sendRedirect(response.encodeRedirectURL("/login?newuser=true"));
			}
			else {				
				// include status name in url to provide user-friendly
				// error message later
				String url = "/register?error=" + status.name();
				
				// encode url properly (see http://www.w3schools.com/tags/ref_urlencode.asp)
				url = response.encodeRedirectURL(url);
				
				// make user try to register again by redirecting back
				// to registration servlet
				response.sendRedirect(url);
			}
		}
		catch(IOException ex) {
			log.warn("Unable to redirect user. " + status, ex);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
	

	private void printForm(PrintWriter out)
	{
		assert out != null;
		
		out.println("<form action=\"/register\" method=\"post\">");
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
		out.println("<p><input type=\"submit\" value=\"Regist\"></p>");
		out.println("</form>");
	}
}

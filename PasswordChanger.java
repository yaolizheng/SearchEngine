import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet handle for changing the password
 * 
 * @author Yaoli Zheng
 *
 */
@SuppressWarnings("serial")
public class PasswordChanger extends BaseServlet{
	
	private String user;
	private String pass;
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
	{
		
		Map<String, String> cookies = getCookieMap(request);
		
		String login = cookies.get("login");
		String user  = cookies.get("name");
		if(login != null && login.equals("true") && user != null) {
			String theme = db.getTheme(user);
			prepareResponseWithTheme("Change Password", response, theme);
		}
		else {
			prepareResponse("Change Password", response);
		}
		PrintWriter out;
		String success = request.getParameter("success");
		String error = request.getParameter("error");
		try {
			out = response.getWriter();
			out.println("<div class=\"login\">");
			out.println("<div class=\"loginform\">");
			out.println("<p class=\"title\">CHANGE PASSWORD</p>");
			
			if(error != null) {
				String errorMessage = getStatusMessage(error);
				out.println("<p style=\"color: blue;\">" + errorMessage +"</p>");
			}
			if(success != null) {
				out.println("<p style=\"color: blue;\">successful!</p>");
			}
			else {
				printForm(out);
			}	
				
				out.println("</div>");
				out.println("</div>");

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
			finishResponse(response);
	
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response)
	{
		pass = request.getParameter("pass");
		String retype = request.getParameter("retype");
		Status status = null;
		Map<String, String> cookies = getCookieMap(request);
		user  = cookies.get("name");
		if(pass.equals(retype)) {
			status = db.updatePass(user, pass);
		
			try {
				if(status == Status.OK) {
					response.sendRedirect(response.encodeRedirectURL("/password?success"));
				}
				else {
					response.sendRedirect(response.encodeRedirectURL("/password?error=" + status.name()));
				}
			} catch(Exception ex) {
				log.error("Unable to process login form.", ex);
			}
		}
		else {
			status = Status.RETYPE_ERR;
			try {
				response.sendRedirect(response.encodeRedirectURL("/password?error=" + status.name()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}

	private void printForm(PrintWriter out)
	{
		assert out != null;
		
		out.println("<form action=\"/password\" method=\"post\">");
		out.println("<table class=\"logintable\">");
		out.println("\t<tr>");
		out.println("\t\t<td>Password:</td>");
		out.println("\t\t<td><input type=\"text\" name=\"pass\" size=\"20\"></td>");
		out.println("\t</tr>");
		out.println("\t<tr>");
		out.println("\t\t<td>Retype:</td>");
		out.println("\t\t<td><input type=\"password\" name=\"retype\" size=\"20\"></td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("<p><input type=\"submit\" value=\"Change\"></p>");
		out.println("</form>");
	}

}

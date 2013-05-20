import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet handles for setting theme
 * 
 * @author Yaoli Zheng
 *
 */
@SuppressWarnings("serial")
public class ThemeChangerServlet extends BaseServlet {
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
	{
		
		Map<String, String> cookies = getCookieMap(request);
		
		String login = cookies.get("login");
		String user  = cookies.get("name");
		if(login != null && login.equals("true") && user != null) {
			String theme = db.getTheme(user);
			prepareResponseWithTheme("Change theme", response, theme);
		}
		else {
			prepareResponse("Change theme", response);
		}
		PrintWriter out;
		String success = request.getParameter("success");
		String error = request.getParameter("error");
		try {
			out = response.getWriter();
			out.println("<div class=\"login\">");
			out.println("<div class=\"loginform\">");
			out.println("<p class=\"title\">Change theme</p>");
			
			if(error != null) {
				out.println("<p style=\"color: blue;\">" + error +"</p>");
			}
			if(success != null) {
				out.println("<p style=\"color: blue;\">successfully!please refresh</p>");
			}
			else {
				printForm(out);
			}	
				
				out.println("</div>");
				out.println("</div>");

			} catch (IOException e) {
				e.printStackTrace();
			}
		
			finishResponse(response);
	
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response)
	{
		String theme = request.getParameter("theme");
		Status status = null;
		if(theme != null) {
		String choice = "1";
			if(theme.equals("gray") )
				choice = "1";
			if(theme.equals("red") )
				choice = "2";
			if(theme.equals("blue") )
				choice = "3";
			if(theme.equals("green") )
				choice = "4";
			
			Map<String, String> cookies = getCookieMap(request);
			String user  = cookies.get("name");
	
				status = db.updateTheme(user, choice);
		}
		else
			status = Status.ERROR;
		
		try {
			if(status == Status.OK) {
				response.sendRedirect(response.encodeRedirectURL("/theme?success"));
			}
			else {
				response.sendRedirect(response.encodeRedirectURL("/theme?error=" + status.name()));
			}
		} 
		catch(Exception ex) {
				log.error("Unable to process login form.", ex);
		}
	}


	private void printForm(PrintWriter out)
	{
		assert out != null;
		
		out.println("<form action=\"/theme\" method=\"post\">");
		out.println("<table class=\"logintable\">");
		out.println("<tr>");
		out.println("<td>");
		out.println("<input type=\"radio\" name=\"theme\" value=\"gray\" /> gray");
		out.println("</td>");
		out.println("<td>");
		out.println("<input type=\"radio\" name=\"theme\" value=\"red\" /> red");
		out.println("</td>");
		out.println("<td>");
		out.println("<input type=\"radio\" name=\"theme\" value=\"blue\" /> blue");
		out.println("</td>");
		out.println("<td>");
		out.println("<input type=\"radio\" name=\"theme\" value=\"green\" /> green");
		out.println("</td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("<p><input type=\"submit\" value=\"Change\"></p>");
		out.println("</form>");
	}

}

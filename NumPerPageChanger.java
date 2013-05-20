import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet handle for setting number per page
 * 
 * @author Yaoli Zheng
 *
 */
@SuppressWarnings("serial")
public class NumPerPageChanger extends BaseServlet {
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
	{
		
		Map<String, String> cookies = getCookieMap(request);
		
		String login = cookies.get("login");
		String user  = cookies.get("name");
		if(login != null && login.equals("true") && user != null) {
			String theme = db.getTheme(user);
			prepareResponseWithTheme("Change number per page", response, theme);
		}
		else {
			prepareResponse("Change number per page", response);
		}
		PrintWriter out;
		String success = request.getParameter("success");
		String error = request.getParameter("error");
		try {
			out = response.getWriter();
			out.println("<div class=\"login\">");
			out.println("<div class=\"loginform\">");
			out.println("<p class=\"title\">Change number per page</p>");
			
			if(error != null) {
				out.println("<p style=\"color: blue;\">" + error +"</p>");
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
		String num = request.getParameter("num");
		Status status = null;
		Map<String, String> cookies = getCookieMap(request);
		String user  = cookies.get("name");

			status = db.updateNum(user, num);
		
			try {

				if(status == Status.OK) {
					response.sendRedirect(response.encodeRedirectURL("/num?success"));
				}
				else {
					response.sendRedirect(response.encodeRedirectURL("/num?error=" + status.name()));
				}
			} catch(Exception ex) {
				log.error("Unable to process login form.", ex);
			}
		}

	
	private void printForm(PrintWriter out)
	{
		assert out != null;
		
		out.println("<form action=\"/num\" method=\"post\">");
		out.println("<table class=\"logintable\">");
		out.println("\t<tr>");
		out.println("\t\t<td><select name=\"num\">");
		for(int i = 1; i <= 10; i++) {
			out.println("<option value =\"" + i + "\">" + i + "</option>");
		}

		out.println("</select></td>");
		out.println("\t</tr>");
		out.println("</table>");
		out.println("<p><input type=\"submit\" value=\"Change\"></p>");
		out.println("</form>");
	}

}

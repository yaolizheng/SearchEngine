import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet handle for adding new seed
 * 
 * @author Yaoli Zheng
 *
 */
@SuppressWarnings("serial")
public class NewSeedServlet extends BaseServlet {
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		
		Map<String, String> cookies = getCookieMap(request);
				
				String login = cookies.get("login");
				String user  = cookies.get("name");
				if(login != null && login.equals("true") && user != null) {
					String theme = db.getTheme(user);
					prepareResponseWithTheme("Add new seed", response, theme);
				}
				else {
					prepareResponse("Add new seed", response);
				}
			PrintWriter out;
			try {
				out = response.getWriter();
				out.println("<div style=\"text-align: center;\">");
				out.println("<div class=\"newseed\">");
				out.println("<div>Add new seed</div>");
				if(request.getParameter("error") != null) {
					out.println("<div>Error</div>");
				}
				if(request.getParameter("succ") != null) {
					out.println("<div>Adding seed successful</div>");
				}
				out.println("<form action=\"/seed\" method=\"post\">");
				out.println("<input type=\"text\" class=\"text\" name=\"newseed\"><input type=\"submit\" value=\"ADD\">");
				out.println("</form>");
				out.println("</div>");
				out.println("</div>");
			
			} catch (IOException e) {
				e.printStackTrace();
			}
			finishResponse(response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		
		String seed = request.getParameter("newseed");
		System.out.println(seed);
		prepareResponse("NewSeed", response);
		try {
			new WebCrawler(seed);
			response.sendRedirect(response.encodeRedirectURL("/seed?succ"));
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			try {
				response.sendRedirect(response.encodeRedirectURL("/seed?error"));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		finishResponse(response);
	}

}

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet handles for getting the visited pages
 * 
 * @author Yaoli Zheng
 *
 */
@SuppressWarnings("serial")
public class VisitedPageServlet extends BaseServlet{
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		String js = "";
		js += "<script language=\"javascript\">\n";
		js += "function selected()\n";
		js += "{\n";
		js += "var del=document.getElementsByName(\"delete\");\n";
		js += "for(var i=0;i<del.length;i++)\n";
		js += "{\n";
		js += "del[i].checked=!del[i].checked;\n";
		js += "}\n";
		js += "}\n";
		js += "</script>\n";
		prepareResponseWithJS("Visited Page", response, js);
		PrintWriter out = null;
		try {
			Map<String, String> cookies = getCookieMap(request);
			
			String login = cookies.get("login");
			String user  = cookies.get("name");
			
			if(login != null && login.equals("true") && user != null) {
				out = response.getWriter();
				
				if(request.getParameter("error") != null) {
					out.println("<div>Delete error</div>");
				}
				if(request.getParameter("succ") != null) {
					out.println("<div>Delete succedded</div>");
				}
				out.println("<div>VISITED PAGE:</div>");
				out.println(db.getVisitedPage(user));
			}
			else {
				response.sendRedirect("/index");
				
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finishResponse(response);
		
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		String delete[] = request.getParameterValues("delete");
		if(delete == null) {
			try {
				response.sendRedirect(response.encodeRedirectURL("/page?error"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			try {
				Map<String, String> cookies = getCookieMap(request);
				
				String login = cookies.get("login");
				String user  = cookies.get("name");
				
				if(login != null && login.equals("true") && user != null) {
					Status status = db.delVisitedPage(user, delete);
					if(status == Status.OK) {
						response.sendRedirect(response.encodeRedirectURL("/page?succ"));
					}	
				}
				else {
					response.sendRedirect("/index");
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	

}

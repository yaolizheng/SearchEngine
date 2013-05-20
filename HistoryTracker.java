import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet display the personal query history
 * 
 * @author Yaoli Zheng
 *
 */
@SuppressWarnings("serial")
public class HistoryTracker extends BaseServlet{
	
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
		prepareResponseWithJS("History", response, js);
		PrintWriter out = null;
		try {
			Map<String, String> cookies = getCookieMap(request);
			
			String login = cookies.get("login");
			String user  = cookies.get("name");
			
			if(login != null && login.equals("true") && user != null) {
				out = response.getWriter();
				
				if(request.getParameter("error") != null) {
					out.println("<div style=\"color: red\">Delete error</div>");
				}
				if(request.getParameter("succ") != null) {
					out.println("<div>Delete succeeded</div>");
				}
				out.println("<div>HISTORY:</div>");
				out.println(db.getHistory(user));
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
				response.sendRedirect(response.encodeRedirectURL("/history?error"));
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
					Status status = db.delHistory(user, delete);
					if(status == Status.OK) {
						response.sendRedirect(response.encodeRedirectURL("/history?succ"));
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

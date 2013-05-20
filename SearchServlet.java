import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet returns the results of search
 * 
 * @author Yaoli Zheng
 *
 */
@SuppressWarnings("serial")
public class SearchServlet  extends BaseServlet{
	
	private Search search;
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
	{
		
		String key = request.getParameter("key");
		
		String oldkey =key;
		
		
		String page = request.getParameter("page");
		String consecutive = request.getParameter("consecutive");
		String no = request.getParameter("no");
		String para = "";
		String regex = "[^\"]*?\"([^\"]+?)\"";
		Pattern pattern = Pattern.compile(regex);
		Matcher m = pattern.matcher(key);
		if(m.find()) {
			consecutive = m.group(1);
			key = key.replace("\"", "");
		}
		regex = "[Nn][Oo][Tt] ([^ ]+)";
		pattern = Pattern.compile(regex);
		m = pattern.matcher(key);
		if(m.find()) {
			no = m.group(1);
			key = key.replaceAll("(?i)not", "");
		}
		
		if(no != null && no.length() != 0) {

			para += "&no=" + no;
		}
		else {
			no = "";
		}
		if(consecutive != null && consecutive.length() != 0) {
			para += "&consecutive=" + consecutive;
		}
		else {
			consecutive = "";
		}
		if(page == null)
			page = "1";
		Map<String, String> cookies = getCookieMap(request);
		//check whether the user is login
		String login = cookies.get("login");
		String user  = cookies.get("name");
		String welcome ="<div class=\"welcome\"><a href = \"/login\">login</a>/<a href = \"/register\">register</a>/<a href = \"/advanced\">Advanced Search</a></div>";
		search = new Search(key, consecutive, no);
		if(login != null && login.equals("true") && user != null)
		{
			String theme = db.getTheme(user);
			prepareResponseWithTheme("Results", response, theme);
			db.saveHistory(user, key);
			search.setMaxLine(Integer.valueOf(db.getNumPerPage(user)));
			welcome = "<div class=\"welcome\">welcome," + user + "/last login:" + db.getLastlogin(user) + "/<a href = \"/login?logout\">logout</a>/<a href = \"/account\">Account</a>/<a href = \"/advanced\">Advanced Search</a></div>";
		}
		else {
			prepareResponse("Results", response);
		}
		long startTime = System.currentTimeMillis();
		
		String results = search.buildQueryList(Integer.valueOf(page));
		long endTime = System.currentTimeMillis();
		int num = search.getResultPage();
		PrintWriter out;
		try {
			out = response.getWriter();
		
			out.println("<div class=\"Top\">");
			out.println("<div class=\"return\"><a href=\"/index\">HOME</a></div>");
			out.println(welcome);
			out.println("</div>");
			out.println("<div class=\"resultSearchTag\">");
			out.println("<form action=\"/results\" method=\"post\">");
			out.println("<input type=\"text\" class=\"text\" name=\"key\" value=\"" + oldkey + "\"><input type=\"submit\" class=\"button\" value=\"\">");
			out.println("</form>");
			out.println("</div>");
			out.println("<div class=\"suggest\">");
			out.println("<p>Suggested Queries:</p>");
			out.println(db.getSuggestedQuery());
			out.println("</div>");
			out.println("<div class=\"context\">");
			out.println("<div class=\"tag\">");
			out.println("All Results(" + search.getResultNum()+" results in " + ((float)(endTime - startTime)/1000.0) +" seconds):</div>");
			out.println("<div class=\"results\">");
			out.println(results);
			out.println("</div>");
			out.println("<div class=\"page\">");
			int p = 10;
			int start = ((Integer.valueOf(page) - 1) / p) * p + 1;
			int end = start + p;
			if(Integer.valueOf(page) > 1)
				out.println("<a href=\"/results?key=" + key + "&page=" + String.valueOf(Integer.valueOf(page) - 1) + para + "\">Previous</a>");
			for(int i = start; i < end && i < num; i++) {	
				if(i == Integer.valueOf(page))
					out.println("<a href=\"/results?key=" + key + "&page=" + i + para +"\" style = \"color: red\">" + i + "</a>");
				else
					out.println("<a href=\"/results?key=" + key + "&page=" + i + para + "\">" + i + "</a>");
			}
			if(Integer.valueOf(page) < num - 1)
				out.println("<a href=\"/results?key=" + key + "&page=" + String.valueOf(Integer.valueOf(page) + 1) + para + "\">Next</a>");
			out.println("</div>");
			out.println("<div class=\"home\">");
			out.println("<img src=\"css/img/home.png\" alt=\"\" onclick=\"window.location='/index'\">");
			out.println("</div>");
			out.println("</div>");
		} catch (IOException e) {
			e.printStackTrace();
		}
		finishResponse(response);
	}
	

	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		
		this.doGet(request, response);
		
	}
}

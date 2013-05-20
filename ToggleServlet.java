import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet handles for setting partial searching toggle
 * 
 * @author Yaoli Zheng
 *
 */
@SuppressWarnings("serial")
public class ToggleServlet extends BaseServlet{
	public void doGet(HttpServletRequest request, HttpServletResponse response)
	{
		prepareResponse("toggle", response);
		String act = request.getParameter("act");
		if(act == null)
			act = "true";
		invertedIndex.setToggle(Boolean.valueOf(act));
		try {
			response.sendRedirect("/menu");
		} catch (IOException e) {
			e.printStackTrace();
		}
			finishResponse(response);
	}
}

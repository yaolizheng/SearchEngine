import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet handles for shutting down the server
 * 
 * @author Yaoli Zheng
 *
 */
@SuppressWarnings("serial")
public class ShutDownServlet extends BaseServlet {
	public void doGet(HttpServletRequest request, HttpServletResponse response)
	{
		log.info("shut down");
		db.clearPage();
		Driver.shutDown();
		System.exit(1);
	}

}

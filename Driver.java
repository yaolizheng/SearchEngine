import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletHandler;

/**
 * The Driver class contains the main method
 * 
 * @author Yaoli Zheng
 * 
 */
public class Driver {
	
	/** Start server on alternative port 8080 instead of default port 80. */
	private static int PORT = 8080;

	private static Server server;
	
	public static void shutDown() {
		try {
			server.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Logger log = Logger.getLogger(Driver.class.getName());
		PropertyConfigurator.configure(Driver.class.getResource("log4j.properties"));

		long start = System.currentTimeMillis();
		log.info("Start...");
		ArgsParser parser = new ArgsParser(args);
		String seed = parser.getValue("s");
		if (seed == "null") {
			System.out.println("Flag s does not exist or flag s has no associated value");
			return;
		}

		log.info("The seed of html is: " + seed);
		new WebCrawler(seed);
		long end = System.currentTimeMillis();
		log.info("Running time: " + (end - start));
		log.info("crawing seed finished, please open a browser and input \"http://127.0.0.1:8080/index\" to do the search");

		 server = new Server(PORT);
		 ResourceHandler resource = new ResourceHandler();

	     resource.setResourceBase(".");
	 
	     HandlerList handlers = new HandlerList();
	     ServletHandler handler = new ServletHandler();
	     handler.addServletWithMapping(IndexServlet.class,    "/index");
	     handler.addServletWithMapping(SearchServlet.class,    "/results");
	     handler.addServletWithMapping(LoginServlet.class,    "/login");
	     handler.addServletWithMapping(RegisterServlet.class,    "/register");
	     handler.addServletWithMapping(Account.class,    "/account");
	     handler.addServletWithMapping(PasswordChanger.class,    "/password");
	     handler.addServletWithMapping(HistoryTracker.class,    "/history");
	     handler.addServletWithMapping(VisitedPageServlet.class,    "/page");
	     handler.addServletWithMapping(RedirectServlet.class,    "/redirect");
	     handler.addServletWithMapping(NewSeedServlet.class,    "/seed");
	     handler.addServletWithMapping(ShutDownServlet.class,    "/shutdown");
	     handler.addServletWithMapping(ToggleServlet.class,    "/toggle");
	     handler.addServletWithMapping(TitleServlet.class,    "/title");
	     handler.addServletWithMapping(MenuServlet.class,    "/menu");
	     handler.addServletWithMapping(NumPerPageChanger.class,    "/num");
	     handler.addServletWithMapping(ThemeChangerServlet.class,    "/theme");
	     handler.addServletWithMapping(AdvancedSearchServlet.class,    "/advanced");
	     handlers.setHandlers(new Handler[] { resource, handler });
	     server.setHandler(handlers);
		
		 try {
		 	server.start();			
			server.join();
			
			log.info("Exiting...");
		 }
		 catch(Exception ex)
		 {
			log.fatal("Interrupted while running server.", ex);
			System.exit(-1);
		 }
	 }
}

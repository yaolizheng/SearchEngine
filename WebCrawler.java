import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * The WebCrawler class is to crawl webpages from seed and do the search
 * 
 * @author Yaoli Zheng
 *
 */
public class WebCrawler {
	protected static DatabaseHandler db = DatabaseHandler.getInstance();
	// the seed of webs
	private URL urlSeed;
	// the port for socket
	protected static final int PORT = 80;
	private static Logger log = Logger.getLogger(Driver.class.getName());
	// work queue
	private WorkQueue workers;
	// keep the number of current webs
	private int count;
	// the signal of killing all threads
	private int pending;
	// the list keeping all the urls which has been visited
	private ArrayList<String> urlList;
	//private InvertedIndex index;
	private InvertedIndex index = InvertedIndex.getInstance();
	private final int pageNum = 30;
	// keep the results for text
	//keep the results for html file

	Connection connection;
	
	/**
	 * Constructor of the class with seed
	 * 
	 * @param urlSeed		the seed of webs
	 * @throws Exception
	 */
	public WebCrawler(String urlSeed) {
		

		
		connection = db.openConnection();

			try {
				this.urlSeed = new URL(urlSeed);
			} catch(MalformedURLException ex) {
				log.error("please input the right url with protocol part!");
				System.exit(0);
			}
			workers = new WorkQueue(10);
			this.count = 0;
			this.pending = 0;
			urlList = new ArrayList<String>();
			//index = new InvertedIndex();
			long start = System.currentTimeMillis();
			this.HTMLFetcher();
			long end = System.currentTimeMillis();
			log.info("Running time for htmlFetcher: " + (end - start));
			List<String> suggestedQuery =  index.getSuggestedQuery();
			db.updateSuggestedQuery(connection, suggestedQuery);
			db.closeConnection(connection);
	}

	
	/**
	 * This method is to fetch html context from webs
	 *  
	 * @return	if it works return true
	 * @throws InterruptedException
	 */
	public boolean HTMLFetcher()
	{
		
		workers.execute(new SingleHTMLFetcher(this.urlSeed, this.urlSeed.toString()));
		while(getPending() > 0) {

			try {
				synchronized(this) {
					wait();
				}
			}
			catch(InterruptedException ignored) {		
			}
		}
		log.debug("Stop all thread!");
		try {
			workers.stopWorkers();
			return true;
		} catch(InterruptedException ex) {
			return false;
		}
	} 
	
	/**
	 * Increment the number of how many webs have been put into the work queue
	 */
	private synchronized void incrementCount()
	{
		count++;
	}
	
	/**
	 * Get the number of how many webs have been put into the work queue
	 */
	public synchronized int getCount()
	{
		return count;
	}
	
	/**
	 * Increment the pending
	 */
	private synchronized void incrementPending()
	{
		pending++;
	}
	
	/**
	 * Get the pending
	 */
	private synchronized int getPending()
	{
		return pending;
	}
	
	/**
	 * Decrement the pending
	 */
	private synchronized void decrementPending()
	{
		pending--;
		if(pending <= 0)
		{
			notifyAll();
		}
	}
	
	/**
	 * This thread is going to fetch the context from html and map it into invertedIndex
	 */
	private class SingleHTMLFetcher implements Runnable{
		
		private URL url;
		private String context;
		// the parent url which can let us know where this url comes from
		private String urlp;
		private TagStripper stripper;
		/**
		 * Constructor of SingleHTMLFetcher
		 * 
		 * @param url	the url of the webpage	
		 * @param urlp  the parent url of this url
		 */
		public SingleHTMLFetcher(URL url, String urlp) {
			this.url = url;
			incrementCount();
			incrementPending();
			this.urlp = urlp;
			urlList.add(url.toString());
			context = "";
		}
		
		/**
		 * The run method of this thread
		 */
		public void run()
		{
			int num = 0;
			try{
				log.debug("Dealing with: " +  url.toString() + "           From : " + urlp);
				
				Socket socket = new Socket(url.getHost(), PORT);
				PrintWriter writer = new PrintWriter(socket.getOutputStream());
				BufferedReader reader = 
					new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String request = "GET " + url.getPath() + " HTTP/1.1\n" +
								"Host: " + url.getHost() + "\n" +
								"Connection: close\n" + 
								"\r\n";
				writer.println(request);
				writer.flush();
		
				String line = reader.readLine();
				// filter the head message of the response
				while(line != null && line.length() != 0)
				{
					line = reader.readLine();
				}
				// keep the html context
				while (line != null) 
				{
					context += line + "\n";
					line = reader.readLine();
				}
				reader.close();
				writer.close();
				socket.close();
				//log.debug(context);
				stripper = new TagStripper(context);
				log.debug("Removing script and style from: " + url.toString());
				stripper.removeComments();
				stripper.removeScript();
				stripper.removeStyle();
				log.debug("Getting links from: " + url.toString());
				ArrayList<String> link = new ArrayList<String>();
				stripper.buildLinks(url.toString());
				
				link = stripper.getLinks();
				for(String tmp : link) {
					if(getCount() < pageNum ) {
						// find if the link has been already visited
						if(!urlList.contains(tmp)) {
							workers.execute(new SingleHTMLFetcher(new URL(tmp), this.url.toString()));
						}
					}
				}
				
				String title = stripper.getTitle();
				if(title == null)
					title = "NO title";
				title = title.replaceAll("&[^;]*;", "");
				log.debug("Removing tags from: " + url.toString());
				stripper.removeTags();
				stripper.removeSymbol();
				String snippet = stripper.getSnippet(title);
				db.savePageInfo(connection, title, snippet, url.toString());
				context = stripper.getContext();
				String[] words = context.toLowerCase().split("\\s");
				ArrayList<String> wordsList = new ArrayList<String>();
				for (int i = 0; i < words.length; i++) {
					// delete non-word character
					words[i] = words[i].replaceAll("\\W", "").replace("_",
							"");
					if(words[i].length() != 0)
						wordsList.add(words[i]);
				}
				for (String w : wordsList) {
					if (w.length() != 0) {
								try {
									index.addNum(w, url.toString(), num);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
						num++;
					}
				}
				
				log.debug("URL: " + url.toString() + " is finished!");
				decrementPending();
			}catch(Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	
	/**
	 * This method is going to print the result into files
	 * 
	 * @param fileName		the name of file	
	 * @param context		the context of result
	 */
	public void searchResultsPrint(String fileName, String context) {
		try {
			FileWriter fileWriter = new FileWriter(fileName);
			BufferedWriter writer = new BufferedWriter(fileWriter);
			writer.write(context);
			writer.close();
			fileWriter.close();
		} catch (IOException e) {
			System.out.println("IO is broken!");
		}
	}

}

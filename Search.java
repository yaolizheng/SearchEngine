import java.sql.Connection;
import java.util.ArrayList;
import java.util.TreeSet;

import org.apache.log4j.Logger;

/**
 * The search class is searching query from the invertedindex
 * 
 * @author Yaoli Zheng
 *
 */

public class Search {
	
	private String query;	
	private String consecutive;	
	private String no;	
	private InvertedIndex index = InvertedIndex.getInstance();
	private static Logger log = Logger.getLogger(Driver.class.getName());	
	private String htmlResult;
	private int maxLine;
	private int resultPage;
	protected static DatabaseHandler db = DatabaseHandler.getInstance();
	private int resultNum;
	
	public Search(String query, String consecutive, String no) {
		this.query = query;
		this.consecutive = consecutive;
		this.no = no;
		htmlResult = "";
		maxLine = 10;
		resultPage = 0;
		resultNum = 0;
	}
	
	public int getResultPage() {
		return resultPage;
	}
	
	public int getResultNum() {
		return resultNum;
	}
	
	public String getResults() {
		return htmlResult;
	}
	
	public void setMaxLine(int max) {
		maxLine = max;
	}
	/**
	 * This method is to build the results
	 * 
	 * @param page	the current page number
	 * @return		the search results
	 */
	public String buildQueryList(int page){
		// split the string into word by "\r\n"
		ArrayList<String> results = new ArrayList<String>();
		if(query != null && query.length() > 0 || (consecutive.length() == 0) || (consecutive.split("\\s").length == 1)) {
			//doing the normal search
			String[] words;
			if(query != null && query.length() > 0) {
				words = query.toLowerCase().split("\\s");
			}
			else {
				words = consecutive.toLowerCase().split("\\s");
			}
			ArrayList<String> wordsList = new ArrayList<String>();
			for (int i = 0; i < words.length; i++) {
				// delete non-word character
				words[i] = words[i].replaceAll("\\W", "").replace("_", "");
				if(words[i].length() != 0) {
					wordsList.add(words[i]);
				}
			}
			// search the keyword
			log.debug("Normal search");
			 results = index.numSearch(wordsList.toArray(new String[0]));
			 if(no.length() != 0) {
				 //doing the exclude search
				 log.debug("Exclued search");
				    words = no.toLowerCase().split("\\s");
					 wordsList = new ArrayList<String>();
					for (int i = 0; i < words.length; i++) {
						// delete non-word character
						words[i] = words[i].replaceAll("\\W", "").replace("_", "");
						if(words[i].length() != 0) {
							wordsList.add(words[i]);
						}
					}
				 results = index.noSearch(wordsList.toArray(new String[0]), results);
			 }
		}
		if(consecutive.length() != 0 && (consecutive.split("\\s").length > 1)) {
			//doing the consecutive search
			log.debug("consecutive search");
			String[] words = consecutive.toLowerCase().split("\\s");
			ArrayList<String> wordsList = new ArrayList<String>();
			for (int i = 0; i < words.length; i++) {
				// delete non-word character
				words[i] = words[i].replaceAll("\\W", "").replace("_", "");
				if(words[i].length() != 0) {
					wordsList.add(words[i]);
				}
			}
			// search the keyword
			results = index.consecutiveSearch(wordsList.toArray(new String[0]));
			 if(no.length() != 0) {
				 //doing the exclude search
				 log.debug("Exclued search");
				    words = no.toLowerCase().split("\\s");
					 wordsList = new ArrayList<String>();
					for (int i = 0; i < words.length; i++) {
						// delete non-word character
						words[i] = words[i].replaceAll("\\W", "").replace("_", "");
						if(words[i].length() != 0) {
							wordsList.add(words[i]);
						}
					}
				 results = index.noSearch(wordsList.toArray(new String[0]), results);
			 }
		}
		resultNum = results.size();
		if(results.size() == 0) {
			return "<div>Sorry, there is no result</div>";
		}
		resultPage = (results.size())/maxLine + 1;
		Connection connection = db.openConnection();
		int start = (page - 1) * maxLine;
		int end = page * maxLine;
		for (int i = start; i < end && i < results.size(); i++) {
			String split = results.get(i);
			String title = db.getTitle(connection, split);
			TreeSet<String> set= new TreeSet<String>();
			if(query != null && query.length() != 0) {
				String[] querys = query.split("\\s");
				for(String t: querys) {
					set.add(t);
				}
			}
			if(consecutive.length() != 0) {
				String[] consecutives = consecutive.split("\\s");
				for(String t: consecutives) {
					set.add(t);
				}
			}
			String[] newTitle = title.split("\\s");
			for(int k = 0; k < newTitle.length; k++) {
				for(String tmp : set) {
					if(newTitle[k].toLowerCase().equals(tmp.toLowerCase())) {
						newTitle[k] = "<span class=\"titlekeyword\">" + newTitle[k] + "</span>";
					}
				}
			}
			title = "";
			for(int k = 0; k < newTitle.length; k++) {
				title += newTitle[k] + " ";
			}
			htmlResult += "<a href=\"/redirect?url=" + split + "\" class=\"link\">" + title + "</a>\n";
			String clip = db.getSnippet(connection, split);
			String[] newClip = clip.split("\\s");
			for(int k = 0; k < newClip.length; k++) {
				for(String tmp : set) {
					if(newClip[k].toLowerCase().equals(tmp.toLowerCase())) {
						newClip[k] = "<span class=\"keyword\">" + newClip[k] + "</span>";
					}
				}
			}
			clip = "";
			for(int k = 0; k < newClip.length; k++) {
				clip += newClip[k] + " ";
			}
			htmlResult += "<p class=\"clip\">" + clip + "</p>\n";
		}
		db.closeConnection(connection);
		return htmlResult;
	}
}

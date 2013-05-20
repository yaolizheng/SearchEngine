import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Tag stripper class
 * 
 * @author Yaoli Zheng
 * 
 */
public class TagStripper {
	// keep the context in file
	private String context;
	// keep the links in file
	private ArrayList<String> links;
	// keep the relative links in file
	private ArrayList<String> relativeLinks;
	private static final String styleRegex = "<[sS][tT][yY][lL][eE][^>]*?>[\\w\\W]*?</[sS][tT][yY][lL][eE][^>]*?>";
	private static final String scriptRegex = "<[sS][cC][rR][iI][pP][tT][^>]*?>[\\w\\W]*?</[sS][cC][rR][iI][pP][tT][^>]*?>";
	private static final String tagRegex = "<[^<>]*>";
	private static final String commentRegex = "<!--[\\s\\S]*?-->";
	private static final String titleRegex = "<[tT][iI][tT][lL][eE][^>]*?>([\\w\\W]*?)</[tT][iI][tT][lL][eE][^>]*?>";
	private static final String symbolRegex = "&[^;]*;";

	/**
	 * Constructor of the class with input context
	 * 
	 * @param context
	 *            the input string
	 */
	public TagStripper(String context) {
		this.context = context;
		links = new ArrayList<String>();
		relativeLinks = new ArrayList<String>();
	}

	/**
	 * Display method
	 */
	public void display() {
		System.out.println("Urls:");
		System.out.println(links);
		System.out.println("Words:");
		System.out.println(context);
	}

	/**
	 * Get the context
	 * 
	 * @return context
	 */
	public String getContext() {
		return context;
	}
	
	/**
	 * Get links
	 * 
	 * @return links
	 */
	public ArrayList<String> getLinks() {
		return links;
	}

	/**
	 * Get relativeLinks
	 * 
	 * @return relativeLinks
	 */
	public ArrayList<String> getRelativeLinks() {
		return relativeLinks;
	}
	
	/**
	 * Method of removing comments
	 */
	public void removeComments() {
		context = context.replaceAll(commentRegex, "");
	}
	
	public String getTitle() {
		Pattern p = Pattern.compile(titleRegex);
		Matcher m = p.matcher(context);
		while (m.find()) {
			return m.group(1);
		}
		return null;
	}
	
	public String getSnippet(String title) {
		String snippet = context.replaceFirst(title, "");

		snippet = snippet.replaceAll("\\s{1,}", " ");
		
		if(snippet.length() >= 200) {
			snippet = snippet.substring(0, 200);
		}
		//log.debug("SSnippet:  " + snippet + " length:" + snippet.length());
		return snippet;
	}
	
	/**
	 * Method of removing special symbol
	 */
	public void removeSymbol() {
		context = context.replaceAll(symbolRegex, "");
	}
	
	/**
	 * Method of removing tags
	 */
	public void removeTags() {
		context = context.replaceAll(tagRegex, "");
	}

	/**
	 * Method of removing scripts
	 */
	public void removeScript() {
		context = context.replaceAll(scriptRegex, "");
	}

	/**
	 * Method of removing styles
	 */
	public void removeStyle() {
		context = context.replaceAll(styleRegex, "");
	}

	/**
	 * Get the links
	 */
	public void buildLinks(String link) {
		String regex = "[hH][rR][eE][fF]=\"([^\"]+)\"";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(context);
		URL domain =null;
		try {
			domain = new URL(link);
		} catch (MalformedURLException e) {
			
			}
		while (m.find()) {
			try {
				URL url = new URL(m.group(1));
				// if the link's protocol is "http" and its path contains
				// ".html" or ".htm" or no extension, put it into links
				if (url.getProtocol().equals("http")
						&& (url.getPath().indexOf(".html") != -1
								|| url.getPath().indexOf(".htm") != -1 || url
								.getPath().indexOf(".") == -1)) {
					links.add(m.group(1));
				}
			} catch (MalformedURLException e) {
				// if the link does not contain protocol,deal it as a relative url
				String rel = m.group(1);
				if (rel.indexOf("javascript") == -1) {
					try {
						URL relative = null;
						if(rel.indexOf("/") != 0) {
							relative = new URL(domain.getProtocol() + "://" + domain.getHost() + "/" + rel);
						}
						else
							relative = new URL(domain.getProtocol() + "://" + domain.getHost() + rel);
						if ((relative.getPath().indexOf(".html") != -1|| relative.getPath().indexOf(".htm") != -1 || relative.getPath().indexOf(".") == -1)) {
							links.add(relative.toString());
						
							
						}
					} catch (MalformedURLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
				}
			}
		}
	}

}

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.*;
import java.util.List;
import java.util.Properties;
import org.apache.log4j.Logger;


/**
 * Handles all database connections and requests. Only one database 
 * handler should be active at any time.
 * 
 * @author Yaoli Zheng
 * 
 */
public class DatabaseHandler 
{
	/** username for mySQL database */
	private String username;
	
	/** password for mySQL database */
	private String password;
	
	/** mySQL database to use */
	private String database;
	
	/** mySQL server hostname */
	private String hostname;
	
	/** properly formatted String for mySQL server */
	private String server;
	
	/** A log4j logger associated with the {@link LoginServer} class. */
	private static Logger log = Logger.getLogger(Driver.class);
	
	/** Single, static instance that should be used by all other classes. */
	private static final DatabaseHandler DBINSTANCE = new DatabaseHandler();

	/**
	 * Verifies the mySQL configuration, JDBC driver, and database
	 * connection. If any step fails, forces program to exit.
	 */
	DatabaseHandler() {
		// use Status enum to indicate specific error messages
		Status status = null;
		
		// test ability to load JDBC mySQL driver
		status = loadDriver();

		if(status != Status.OK) {
			log.fatal(status);
			System.exit(-status.code());
		}		
		
		// test ability to load database configuration
		status = loadConfig();
		
		if(status != Status.OK) {
			log.fatal(status);
			System.exit(-status.code());
		}
		
		// test database connection
		status = testConnection();
		
		if(status != Status.OK) {
			log.fatal(status);
			System.exit(-status.code());
		}
		System.out.println(status);
	}
	
	/**
	 * Used by other classes to get instance.
	 * There should be only one active DatabaseHandler instance at any
	 * time.
	 * 
	 * @return instance
	 */
	public static DatabaseHandler getInstance() {
		return DBINSTANCE;
	}

	/**
	 * Utility method that makes sure the provided String is not null
	 * or empty.
	 * 
	 * @param text
	 * @return true if String is not empty or null
	 */
	public static boolean validString(String text) {
		return (text != null) && !text.trim().isEmpty();
	}

	/**
	 * Loads the JDBC mySQL driver.
	 * @return if successful
	 */
	private Status loadDriver() {
		
		Status status = Status.ERROR;

		log.debug("Attempting to load mySQL JDBC driver...");
		
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			log.debug("Loading of the mySQL JDBC driver complete.");
			status = Status.OK;
		}
		catch(Exception ex) {
			log.error("Unable to load mySQL JDBC driver.", ex);
			status = Status.NO_DRIVER;
		}
		
		return status;
	}	
	
	/**
	 * Loads the database configuration from the database.properties file. 
	 * @return if successful
	 */
	private Status loadConfig() {
		Status status = Status.ERROR;
		Properties config = new Properties();

		try {			
			config.load(new FileReader("database.properties"));
			username = config.getProperty("username");
			password = config.getProperty("password");
			database = config.getProperty("database");
			hostname = config.getProperty("hostname");
			
			if(validString(username) && validString(password) 
					&& validString(database) && validString(hostname)) {
				log.debug("Using user " + username + " and database " + database + " on " + hostname + ".");
				server = "jdbc:mysql://" + hostname + "/" + database;
				status = Status.OK;
			}
			else {
				log.error("Invalid database configuration file. Please make sure to specify username, password, database, and hostname in database.properties.");
				status = Status.INVALID_CONFIG;
			}
		}
		catch(FileNotFoundException ex) {
			log.error("Unable to locate database.properties configuration file.", ex);
			status = Status.NO_CONFIG;
		}
		catch(IOException ex) {
			log.error("Unable to read database.properties configuration file.", ex);
			status = Status.NO_CONFIG;
		}
		catch(Exception ex) {
			log.error("Unknown exception occurred.", ex);
			status = Status.ERROR;
		}	
		
		return status;
	}

	/**
	 * Tests database configuration with simple SQL statement.
	 * @return  if successful
	 */
	private Status testConnection() {
		
		Status status = Status.ERROR;

		Connection connection = null;
		java.sql.Statement statement   = null;
		ResultSet results     = null;
		
		try {
			connection = DriverManager.getConnection(server, username, password);
			statement  = connection.createStatement();
			results    = statement.executeQuery("SHOW TABLES;");

			/*
			 * We should verify the table setup here, and possibly
			 * create the tables if needed.
			 */
			
			int num = 0;
			
			while(results.next())
				num++;
			
			log.debug("Database connection test complete. " + num + " tables found.");
			status = Status.OK;
		}
		catch(SQLException ex) {
			log.error("Unable to establish database connection.", ex);
			status = Status.CONNECTION_FAILED;
		}
		finally {
			try {
				statement.close();
				connection.close();
			}
			catch(Exception ex) {
				log.warn("Encounted exception while trying to close database connection.", ex);
			}
		}
		
		return status;
	}
	
	/**
	 * Returns an MD5 hash of the password as a hexidecimal String.
	 * 
	 * @param password
	 * @return hash
	 */
	private String getHash(String password) {
		
		String hash = password;
		
		try {
			/*
			 * You should use a stronger hash function with salting.
			 */
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(password.getBytes());
			
			BigInteger bigint = new BigInteger(1, md.digest());
			hash = bigint.toString(16);
			// be careful of losing the leading zero
			if(hash.length() % 2 != 0)
				hash = "0" + hash;
		}
		catch(Exception ex) {
			log.warn("Unable to hash password.");
		}
				
		return hash;
	}
	
	/**
	 * Tests if a user already exists in the database.
	 * 
	 * @param connection
	 * @param user
	 * @return true if user exists
	 */
	private boolean userExists(Connection connection, String user) {
		
		assert connection != null;
		
		try {
			/*
			 * Use PreparedStatement whenever placing user input into
			 * the SQL statement. Place a ? where user input should be
			 * inserted.
			 */
			String sql = "SELECT name FROM users WHERE name = ?";
			
			/*
			 * To include the user input, use the setString() method.
			 */
			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setString(1, user);
			
			ResultSet results = statement.executeQuery();
			if(results.next())
			{
				return true;
			}
		}
		catch(SQLException ex) {
			log.error("Encountered SQL exception while accessing database. Please check database configuration.", ex);
		}
		
		return false;
	}
	
	/**
	 * Tests if the page information already exists in the database.
	 * 
	 * @param connection
	 * @param url
	 * @return true if page exists
	 */
	private boolean pageExists(Connection connection, String url) {
		
		assert connection != null;
		
		try {

			String sql = "SELECT title FROM page WHERE url = ?";
			
			/*
			 * To include the user input, use the setString() method.
			 */
			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setString(1, url);
			
			ResultSet results = statement.executeQuery();
			if(results.next())
			{
				return true;
			}
		}
		catch(SQLException ex) {
			log.error("Encountered SQL exception while accessing database. Please check database configuration.", ex);
		}
		
		return false;
	}
	
	/**
	 * Open a connection to database
	 * 
	 * @return	the connection
	 */
	public Connection openConnection() {
		Connection connection = null;
		try {			
			connection = DriverManager.getConnection(server, username, password);
		}
		catch(SQLException ex) {
			log.error("Unable to establish database connection.", ex);
		}
		return connection;
	}
	
	/**
	 * Close a connection to database
	 * 
	 * @param connection
	 * @return
	 */
	public boolean closeConnection(Connection connection) {
		try {
			connection.close();
		}
		catch(SQLException ignored) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Save page information to the database
	 * 
	 * @param connection
	 * @param title
	 * @param snippet
	 * @param url
	 * @return	status
	 */
	public Status savePageInfo(Connection connection, String title, String snippet, String url) {
		try {
			
			if(pageExists(connection, url)) {
				return Status.DUPLICATE_INFO;
			}
			else {
				String sql = "INSERT INTO page (title, snippet, url) VALUES (?, ?, ?);";
				PreparedStatement statement = connection.prepareStatement(sql);
				statement.setString(1, title);
				statement.setString(2, snippet);
				statement.setString(3, url);
				
				statement.executeUpdate();
				statement.close();
				return Status.OK;
			}
		}
		catch(SQLException ex) {
			log.error("Encountered SQL exception while accessing database. Please check database configuration.", ex);
			return Status.ERROR;
		}
		
	}
	
	/**
	 * Get the title of page 
	 * 
	 * @param connection
	 * @param url
	 * @return	the title
	 */
	public String getTitle(Connection connection, String url) {
		try {

			String sql = "SELECT title FROM page WHERE url = ?";
			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setString(1, url);
			
			ResultSet results = statement.executeQuery();
			if(results.next()) {
				return results.getString("title");
			}
			statement.close();
		}
		catch(SQLException ex) {
			log.error("Encountered SQL exception while accessing database. Please check database configuration.", ex);
			return null;
		}
		return null;
	}
	
	/**
	 * Get the snippet
	 * @param connection
	 * @param url
	 * @return snippet
	 */
	public String getSnippet(Connection connection, String url) {
		try {
			String sql = "SELECT snippet FROM page WHERE url = ?";
			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setString(1, url);
			
			ResultSet results = statement.executeQuery();
			if(results.next()) {
				return results.getString("snippet");
			}
			statement.close();
		}
		catch(SQLException ex)
		{
			log.error("Encountered SQL exception while accessing database. Please check database configuration.", ex);
			return null;
		}
		return null;
	}
	
	/**
	 * Attempts to register a new user with our website.
	 * 
	 * @param newuser username of new user
	 * @param newpass password of new user
	 * @return if registration succeeded
	 */
	public Status registerUser(String newuser, String newpass)
	{
		Status status = Status.OK;
		
		String sql = null;		
		Connection connection = null;
		PreparedStatement statement = null;
		
		// test for null values
		if(newuser == null || newpass == null) {
			return Status.NULL_VALUES;
		}
		
		// make sure able to create database connection
		try {			
			connection = DriverManager.getConnection(server, username, password);
		}
		catch(SQLException ex) {
			log.error("Unable to establish database connection.", ex);
			return Status.CONNECTION_FAILED;
		}
		
		// test if username already exists
		if(userExists(connection, newuser)) {
			status = Status.DUPLICATE_USER;
			return status;
		}
		else {
			// attempt to insert new username and password into database
			try {				
				sql = "INSERT INTO users (name, password, lastlogin) VALUES (?, ?, ?);";
				statement = connection.prepareStatement(sql);
				statement.setString(1, newuser);
				statement.setString(2, getHash(newpass));
				statement.setString(3, null);
				statement.executeUpdate();
				
				status = Status.OK;
			}
			catch(SQLException ex) {
				log.error("Encountered SQL exception while registering user.", ex);
				status = Status.SQL_ERROR;
			}
		}
		
		try {
			statement.close();
			connection.close();
		}
		catch(SQLException ignored){}
		
		return status;
	}
	
	/**
	 * Update the password
	 * 
	 * @param user
	 * @param newpass
	 * @return status
	 */
	public Status updatePass(String user, String newpass) {
		Status status = Status.OK;
		
		String sql = null;		
		Connection connection = null;
		PreparedStatement statement = null;

		if(user == null || newpass == null) {
			return Status.NULL_VALUES;
		}

		try {			
			connection = DriverManager.getConnection(server, username, password);
		}
		catch(SQLException ex) {
			log.error("Unable to establish database connection.", ex);
			return Status.CONNECTION_FAILED;
		}
		
		try {				
			sql = "UPDATE users SET password = ? WHERE name = ?;";
			statement = connection.prepareStatement(sql);
			statement.setString(1, getHash(newpass));
			statement.setString(2, user);			
			statement.executeUpdate();
				
			status = Status.OK;
		}
		catch(SQLException ex) {
			log.error("Encountered SQL exception while registering user.", ex);
			status = Status.SQL_ERROR;
		}
	
		
		try {
			statement.close();
			connection.close();
		}
		catch(SQLException ignored){}
		
		return status;
	}
	
	

	/**
	 * Attempts to verify the provided username and password with database.
	 * 
	 * @param user
	 * @param pass
	 * @return if valid (user, pass) pair provided
	 */
	public Status verifyLogin(String user, String pass) {
		Status status = Status.ERROR;
		String sql = null;
		
		try {			
			Connection connection = DriverManager.getConnection(server, username, password);
			
			sql = "SELECT name FROM users WHERE name = ? AND password = ?";
			
			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setString(1, user);
			statement.setString(2, getHash(pass));
			
			log.debug("Executing statement " + sql);
			ResultSet results = statement.executeQuery();
			
			if(results.next()) {
				status = Status.OK;
			}
			else {
				status = Status.INCORRECT_LOGIN;
			}			
		}
		catch(Exception ex) {
			log.warn("Unable to verify user " + user + ".", ex);
			status = Status.CONNECTION_FAILED;
		}
		
		return status;
	}
	
	/**
	 * Save the history
	 * 
	 * @param user
	 * @param history
	 * @return	status
	 */
	public Status saveHistory(String user, String history) {
		Status status = Status.ERROR;
		String sql = null;
		
		try {			
			if(history != null && history.length() != 0) {
				Connection connection = DriverManager.getConnection(server, username, password);
				sql = "INSERT INTO history (name, query) VALUES (?, ?);";
				PreparedStatement statement = connection.prepareStatement(sql);
				statement.setString(1, user);
				statement.setString(2, history);
				
				statement.executeUpdate();
			}
			status = Status.OK;
			
		}
		catch(Exception ex) {
			log.warn("Unable add history.", ex);
			status = Status.CONNECTION_FAILED;
		}
		
		return status;
	}
	
	/**
	 * Delete the history
	 * 
	 * @param user
	 * @param id
	 * @return	status
	 */
	public Status delHistory(String user, String[] id) {
		Status status = Status.ERROR;
		String sql = null;
		PreparedStatement statement = null;
		try {			
			Connection connection = DriverManager.getConnection(server, username, password);
			for(String i : id) {
				sql = "DELETE FROM history WHERE id = ?";
			    statement = connection.prepareStatement(sql);
				statement.setString(1, i);
				statement.executeUpdate();
			}

			status = Status.OK;
			statement.close();
		}
		catch(Exception ex) {
			log.warn("Unable add history.", ex);
			status = Status.CONNECTION_FAILED;
		}
		
		return status;
	}
	
	/**
	 * Get the history
	 * 
	 * @param user
	 * @return	the history
	 */
	public String getHistory(String user) {
		ResultSet results = null;
		String history = "";
		String sql = null;		
		Connection connection = null;
		PreparedStatement statement = null;
		
		// test for null values
		if(user == null) {
			return null;
		}
		
		// make sure able to create database connection
		try {			
			connection = DriverManager.getConnection(server, username, password);
		}
		catch(SQLException ex) {
			log.error("Unable to establish database connection.", ex);
			return null;
		}

		try {				
			sql = "SELECT * FROM history WHERE name = ?;";
			statement = connection.prepareStatement(sql);
			statement.setString(1, user);			
			results = statement.executeQuery();
			history += "<form action=\"/history\" method=\"post\">\n";
			history += "<table   width=\"100%\" class=\"table\">\n";
			history += "<tr valign=\"middle\" align=\"center\">\n";
			history += "<th align=\"center\"><input type=\"checkbox\"  name=\"all\" onclick=\"selected()\">All</th>\n";
			history += "<th align=\"center\">history</th>\n";
			history += "<th align=\"center\">time</th>\n";
			history += "</tr>\n";
			while(results.next()) {
				String query = results.getString("query");
				String id = results.getString("id");
				String time = results.getString("time");
				history += "<tr align=\"center\" onmouseover=\"this.style.backgroundColor='#AAD5FF';\" onmouseout=\"this.style.backgroundColor='#ffffff';\">\n";
				history += "<td align=\"center\"><input type=\"checkbox\"  name=\"delete\" value=\"" + id +"\"></td>\n";
				history += "<td align=\"center\">" + query + "</td>\n";
				history += "<td align=\"center\">" + time + "</td>\n";
				history += "</tr>\n";
			}
			history += "</table>";
			history += "<p style = \"text-align: center\"><input type=\"submit\" value=\"delete\"></p>";
			history += "</form>\n";
		}
		catch(SQLException ex) {
				log.error("Encountered SQL exception while registering user.", ex);
				return null;
		}
	
		
		try {
			statement.close();
			connection.close();
		}
		catch(SQLException ignored){}
		
		return history;
	}
	
	/**
	 * Save the vistied page
	 * 
	 * @param user
	 * @param visitedPage
	 * @return	status
	 */
	public Status saveVisitedPage(String user, String visitedPage) {
		Status status = Status.ERROR;
		String sql = null;
		
		try {			
			Connection connection = DriverManager.getConnection(server, username, password);
			sql = "INSERT INTO visitedpage (name, page) VALUES (?, ?);";
			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setString(1, user);
			statement.setString(2, visitedPage);
			
			statement.executeUpdate();
			return Status.OK;
			
		}
		catch(Exception ex) {
			log.warn("Unable add visited page.", ex);
			status = Status.CONNECTION_FAILED;
		}
		
		return status;
	}
	
	/**
	 * Delete some visited pages
	 * 
	 * @param user
	 * @param id
	 * @return status
	 */
	public Status delVisitedPage(String user, String[] id) {
		Status status = Status.ERROR;
		String sql = null;
		PreparedStatement statement = null;
		try {			
			Connection connection = DriverManager.getConnection(server, username, password);
			for(String i : id) {
				sql = "DELETE FROM visitedpage WHERE id = ?";
			    statement = connection.prepareStatement(sql);
				statement.setString(1, i);
				statement.executeUpdate();
			}

			status = Status.OK;
			statement.close();
		}
		catch(Exception ex) {
			log.warn("Unable to delete visited page.", ex);
			status = Status.CONNECTION_FAILED;
		}
		
		return status;
	}
	
	/**
	 * Get the visited pages
	 * 
	 * @param user
	 * @return visited pages
	 */
	public String getVisitedPage(String user) {
		ResultSet results = null;
		String page = "";
		String sql = null;		
		Connection connection = null;
		PreparedStatement statement = null;
		
		// test for null values
		if(user == null) {
			return null;
		}
		
		// make sure able to create database connection
		try {			
			connection = DriverManager.getConnection(server, username, password);
		}
		catch(SQLException ex)
		{
			log.error("Unable to establish database connection.", ex);
			return null;
		}
		try {				
			sql = "SELECT * FROM visitedpage WHERE name = ?;";
			statement = connection.prepareStatement(sql);
			statement.setString(1, user);			
			results = statement.executeQuery();
			page += "<form action=\"/page\" method=\"post\">\n";
			page += "<table   width=\"100%\" class=\"table\">\n";
			page += "<tr valign=\"middle\" align=\"center\">\n";
			page += "<th align=\"center\"><input type=\"checkbox\"  name=\"all\" onclick=\"selected()\">All</th>\n";
			page += "<th align=\"center\">visited page</th>\n";
			page += "<th align=\"center\">time</th>\n";
			page += "</tr>\n";
			while(results.next()) {
				String query = results.getString("page");
				String id = results.getString("id");
				String time = results.getString("time");
				page += "<tr align=\"center\" onmouseover=\"this.style.backgroundColor='#AAD5FF';\" onmouseout=\"this.style.backgroundColor='#ffffff';\">\n";
				page += "<td align=\"center\"><input type=\"checkbox\"  name=\"delete\" value=\"" + id +"\"></td>\n";
				page += "<td align=\"center\">" + query + "</td>\n";
				page += "<td align=\"center\">" + time + "</td>\n";
				page += "</tr>\n";
		    }
			page += "</table>";
			page += "<p style = \"text-align: center\"><input type=\"submit\" value=\"delete\"></p>";
			page += "</form>\n";
		}
		catch(SQLException ex){
			log.error("Encountered SQL exception while registering user.", ex);
			return null;
		}
	
		
		try {
			statement.close();
			connection.close();
		}
		catch(SQLException ignored){}
		
		return page;
	}
	
	/**
	 * Update the suggested queries
	 * 
	 * @param connection
	 * @param results
	 * @return	status
	 */
	public Status updateSuggestedQuery (Connection connection, List<String> results) {
		assert connection != null;
		
		try {
 
			String sql = "DELETE FROM suggestedquery";
			PreparedStatement statement = connection.prepareStatement(sql);
			
			statement.executeUpdate();
			
			for(String tmp : results) {
				String[] s = tmp.split("\\|");
				sql = "INSERT INTO suggestedquery (query, number) VALUES (?, ?);";
				statement = connection.prepareStatement(sql);
				statement.setString(1, s[0]);
				statement.setString(2, s[1]);
				statement.executeUpdate();
			}
			return Status.OK;
		}
		catch(SQLException ex) {
			log.error("Encountered SQL exception while accessing database. Please check database configuration.", ex);
			return Status.ERROR;
		}
	}
	
	/**
	 * Get the time of last visiting
	 * @param user
	 * @return	the time
	 */
	public String getLastlogin(String user) {
		String login = "";
		
		ResultSet results = null;
		String sql = null;		
		Connection connection = null;
		PreparedStatement statement = null;

		try {			
			connection = DriverManager.getConnection(server, username, password);
		}
		catch(SQLException ex) {
			log.error("Unable to establish database connection.", ex);
			return null;
		}
		try {				
			sql = "SELECT lastlogin FROM users;";
			statement = connection.prepareStatement(sql);		
			results = statement.executeQuery();
			while(results.next()) {
				login = results.getString("lastlogin");
			}
		}
		catch(SQLException ex) {
			log.error("Encountered SQL exception while registering user.", ex);
			return null;
		}
		try {
			statement.close();
			connection.close();
		}
		catch(SQLException ignored){}
		if(login == null) {
			login = "first login";
		}
		return login;
	}
	
	/**
	 * Update the time of last visiting
	 * 
	 * @param user
	 * @param time
	 * @return	status
	 */
	public Status updateLastLogin (String user, String time) {
		Status status = Status.OK;
		String sql = null;		
		Connection connection = null;
		PreparedStatement statement = null;
		if(user == null) {
			return Status.NULL_VALUES;
		}
		try {			
			connection = DriverManager.getConnection(server, username, password);
		}
		catch(SQLException ex)
		{
			log.error("Unable to establish database connection.", ex);
			return Status.CONNECTION_FAILED;
		}
		try {				
			sql = "UPDATE users SET lastlogin = ? WHERE name = ?;";
			statement = connection.prepareStatement(sql);
			statement.setString(1, time);	
			statement.setString(2, user);	
			statement.executeUpdate();
			status = Status.OK;
		}
		catch(SQLException ex) {
			log.error("Encountered SQL exception while registering user.", ex);
			status = Status.SQL_ERROR;
		}
	
		
		try {
			statement.close();
			connection.close();
		}
		catch(SQLException ignored){}
		
		return status;
	}
	
	/**
	 * Get suggested queries
	 * 
	 * @return	the suggested queries
	 */
	public String getSuggestedQuery() {
		ResultSet results = null;
		String query = "";
		String sql = null;		
		Connection connection = null;
		PreparedStatement statement = null;
		try {			
			connection = DriverManager.getConnection(server, username, password);
		}
		catch(SQLException ex) {
			log.error("Unable to establish database connection.", ex);
			return null;
		}
		try {				
			sql = "SELECT * FROM suggestedquery;";
			statement = connection.prepareStatement(sql);		
			results = statement.executeQuery();
			while(results.next()) {
				String q = results.getString("query");
				query += "<p><a href=\"/results?key=" + q + "\">" + q + "</p>\n";
			}

		}
		catch(SQLException ex) {
				log.error("Encountered SQL exception while registering user.", ex);
		}
	
		
		try {
			statement.close();
			connection.close();
		}
		catch(SQLException ignored){}
		
		return query;

	}
	
	/**
	 * Get the setting of numbers per page
	 * 
	 * @param user
	 * @return	the setting
	 */
	public String getNumPerPage(String user) {
		String num = "";
		ResultSet results = null;
		String sql = null;		
		Connection connection = null;
		PreparedStatement statement = null;
		try {			
			connection = DriverManager.getConnection(server, username, password);
		}
		catch(SQLException ex) {
			log.error("Unable to establish database connection.", ex);
			return null;
		}
		try {				
			sql = "SELECT numperpage FROM users;";
			statement = connection.prepareStatement(sql);		
			results = statement.executeQuery();
			while(results.next()) {
				num = results.getString("numperpage");
			}
		}
		catch(SQLException ex) {
			log.error("Encountered SQL exception while registering user.", ex);
			return null;
		}
		try {
			statement.close();
			connection.close();
		}
		catch(SQLException ignored){}
		return num;
	}
	
	/**
	 * Update the number per page
	 * 
	 * @param user
	 * @param num
	 * @return status
	 */
	public Status updateNum(String user, String num) {
		Status status = Status.OK;
		String sql = null;		
		Connection connection = null;
		PreparedStatement statement = null;
		if(user == null) {
			return Status.NULL_VALUES;
		}
		try {			
			connection = DriverManager.getConnection(server, username, password);
		}
		catch(SQLException ex) {
			log.error("Unable to establish database connection.", ex);
			return Status.CONNECTION_FAILED;
		}

		try {				
			sql = "UPDATE users SET numperpage = ? WHERE name = ?;";
			statement = connection.prepareStatement(sql);
			statement.setString(1, num);	
			statement.setString(2, user);	
			statement.executeUpdate();
			status = Status.OK;
		}
		catch(SQLException ex) {
			log.error("Encountered SQL exception while registering user.", ex);
			status = Status.SQL_ERROR;
		}
	
		
		try {
			statement.close();
			connection.close();
		}
		catch(SQLException ignored){}
		
		return status;
	}
	
	/**
	 * Get the theme setting
	 * 
	 * @param user
	 * @return	the setting
	 */
	public String getTheme(String user) {
		String theme = "";
		
		ResultSet results = null;
		String sql = null;		
		Connection connection = null;
		PreparedStatement statement = null;
		try {			
			connection = DriverManager.getConnection(server, username, password);
		}
		catch(SQLException ex) {
			log.error("Unable to establish database connection.", ex);
			return null;
		}
		try {				
			sql = "SELECT theme FROM users;";
			statement = connection.prepareStatement(sql);		
			results = statement.executeQuery();
			while(results.next()) {
				theme = results.getString("theme");
			}
		}
		catch(SQLException ex) {
			log.error("Encountered SQL exception while registering user.", ex);
			return null;
		}
		try {
			statement.close();
			connection.close();
		}
		catch(SQLException ignored){}
		return theme;
	}
	
	/**
	 * Update the theme setting
	 * 
	 * @param user
	 * @param theme
	 * @return	status
	 */
	public Status updateTheme(String user, String theme) {
		Status status = Status.OK;
		String sql = null;		
		Connection connection = null;
		PreparedStatement statement = null;

		if(user == null) {
			return Status.NULL_VALUES;
		}
		try {			
			connection = DriverManager.getConnection(server, username, password);
		}
		catch(SQLException ex) {
			log.error("Unable to establish database connection.", ex);
			return Status.CONNECTION_FAILED;
		}

		try {				
			sql = "UPDATE users SET theme = ? WHERE name = ?;";
			statement = connection.prepareStatement(sql);
			statement.setString(1, theme);	
			statement.setString(2, user);	
			statement.executeUpdate();
			status = Status.OK;
		}
		catch(SQLException ex) {
			log.error("Encountered SQL exception while registering user.", ex);
			status = Status.SQL_ERROR;
		}
	
		
		try {
			statement.close();
			connection.close();
		}
		catch(SQLException ignored){}
		
		return status;
	}
	
	/**
	 * Clear the page information
	 * 
	 * @return	status
	 */
	public Status clearPage() {
		Status status = Status.OK;
		String sql = null;		
		Connection connection = null;
		PreparedStatement statement = null;
		try {			
			connection = DriverManager.getConnection(server, username, password);
		}
		catch(SQLException ex) {
			log.error("Unable to establish database connection.", ex);
			return Status.CONNECTION_FAILED;
		}
		try {				
			sql = "DELETE FROM page;";
			statement = connection.prepareStatement(sql);	
			statement.executeUpdate();
			status = Status.OK;
		}
		catch(SQLException ex) {
			log.error("Encountered SQL exception while registering user.", ex);
			status = Status.SQL_ERROR;
		}
	
		
		try {
			statement.close();
			connection.close();
		}
		catch(SQLException ignored){}
		
		return status;
	}
}

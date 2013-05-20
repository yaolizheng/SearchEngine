/**
 * A enum used for providing user-friendly error messages when something
 * goes wrong. Part of the {@link LoginServer} example.
 * 
 * <p>
 * See <a href="http://docs.oracle.com/javase/tutorial/java/javaOO/enum.html">
 * http://docs.oracle.com/javase/tutorial/java/javaOO/enum.html</a> and
 * <a href="http://docs.oracle.com/javase/6/docs/api/java/lang/Enum.html">
 * http://docs.oracle.com/javase/6/docs/api/java/lang/Enum.html</a> for
 * more on enums.
 */
public enum Status 
{
	// create different status types in NAME(code, message) format
	OK               ( 0, "No errors occured"),
	ERROR            ( 1, "Unknown error occured"),
	INVALID_CONFIG   ( 2, "Invalid database configuration file"),
	NO_CONFIG        ( 3, "No database configuration file found"),
	NO_DRIVER        ( 4, "Unable to load JDBC driver"),
	CONNECTION_FAILED( 5, "Failed to establish database connection"),
	SQL_ERROR        ( 6, "Encountered unknown SQL error"),
	DUPLICATE_USER   ( 7, "User with that name already exists"),
	PASSWORD_LENGTH  ( 8, "Invalid password length"),
	INCORRECT_LOGIN  ( 9, "Login credentials incorrect"),
	NULL_VALUES      (10, "Must provide both username and password"),
	DUPLICATE_INFO   (11, "Page information with that url already exists"),
	RETYPE_ERR	     (12, "Retype is not the same, please retry");
	
	// private members
	private final String message;
	private int code;
	
	// private enum constructor
	private Status(int code, String message)
	{
		this.code = code;
		this.message = message;
	}
	
	// returns associated error code
	public int code()
	{
		return this.code;
	}
	
	// returns String representation of status type
	public String toString()
	{
		return String.format("%s (error code %d).", message, code);
	}
}

/**
 * Arguments parser class
 * 
 * @author Yaoli Zheng
 * 
 */
public class ArgsParser {
	private String[] args;

	/**
	 * Constructor for setting the arguments
	 * 
	 * @param args
	 */
	public ArgsParser(String[] args) {
		this.args = args;
	}

	/**
	 * Overridden toString() method
	 */
	public String toString() {
		String tmp = "";
		for (int i = 0; i < args.length; i++)
			tmp += args[i] + " ";
		return tmp;
	}

	/**
	 * return the number of parameters
	 * 
	 * @return number of parameters
	 */
	public int numParameters() {
		return args.length;
	}

	/**
	 * return the number of flags
	 * 
	 * @return number of flags
	 */
	public int numFlags() {
		int count = 0;
		for (int i = 0; i < args.length; i++) {
			if (args[i].indexOf("-") != -1)
				count++;
		}
		return count;
	}

	/**
	 * test whether a specific flag exists
	 * 
	 * @param flag
	 * 
	 * @return true if the flag exists or false if the flag does not exist
	 */
	public boolean hasFlag(String flag) {
		for (int i = 0; i < args.length; i++) {
			if (args[i].indexOf("-" + flag) != -1)
				return true;
		}
		return false;
	}

	/**
	 * Test whether a specific flag has an associated value
	 * 
	 * @param flag
	 * 
	 * @return true if the flag has value or false if the flag does not have
	 *         value
	 */
	public boolean hasValue(String flag) {
		if (this.hasFlag(flag) == false) {
			return false;
		}
		for (int i = 0; i < args.length; i++) {
			if (args[i].indexOf("-" + flag) != -1) {
				if ((i + 1) < args.length) {
					if (args[i + 1].indexOf("-") != 0)
						return true;
				}
			}
		}
		return false;
	}

	/**
	 * Return the value associated with a specific flag
	 * 
	 * @param flag
	 * 
	 * @return the value of the flag or null if the flag does not have value
	 */
	public String getValue(String flag) {
		if (this.hasFlag(flag) == true && this.hasValue(flag) == true) {
			for (int i = 0; i < args.length; i++) {
				if (args[i].indexOf("-" + flag) != -1) {
					return args[i + 1];
				}
			}
		}
		return "null";
	}
}

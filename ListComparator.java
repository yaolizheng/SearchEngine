import java.util.Comparator;

/**
 * The RankComparator implements the compare method to compare the position of each words
 * 
 * @author Yaoli Zheng
 * 
 */

public class ListComparator implements Comparator<String> {
 
	public int compare(String a1, String a2) {
		//get the position
		String[] tmp1 = a1.split("\\|");
		String[] tmp2 = a2.split("\\|");
		if(Integer.valueOf(tmp1[1]) > Integer.valueOf(tmp2[1])) 
			return -1;
		else if(Integer.valueOf(tmp1[1]) < Integer.valueOf(tmp2[1])) {
			return 1;
		}
		else return 0;
	}
}

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

/**
 * The InvertedIndex class stores the word and this file's name it appears and
 * the position it appears
 * 
 * @author Yaoli Zheng
 * 
 */
public class InvertedIndex {

	// A HashMap to store the data, the keyword will be word and the value will
	// be another HashMap whose key word will be file name and value will be a
	// list to store the position
	private HashMap<String, HashMap<String, ArrayList<Integer>>> index;
	private static Logger log = Logger.getLogger(InvertedIndex.class.getName());
	// Custom lock
	private CustomLock lock;
	private HashMap<String, CustomLock> indexLock;
	private static final InvertedIndex INVERTEDINDEX = new InvertedIndex();
	private boolean toggle;

	/**
	 * Constructor of InvertedIndex
	 */
	public InvertedIndex() {
		log.debug("InvertedIndex created!");
		toggle = true;
		index = new HashMap<String, HashMap<String, ArrayList<Integer>>>();
		lock = new CustomLock();
		indexLock = new HashMap<String, CustomLock>();
	}

	
	public void setToggle(boolean partial)  {
		toggle = partial;
	}
	
	public boolean getToggle()  {
		return toggle;
	}
	
	public static InvertedIndex getInstance()
	{
		return INVERTEDINDEX;
	}
	
	
	/**
	 * Add the word as the key into the data
	 * 
	 * @param word
	 *            word in the file
	 */
	public void addWord(String word) {
		index.put(word, new HashMap<String, ArrayList<Integer>>());
	}

	/**
	 * Decide whether the word is in the data or not
	 * 
	 * @param word
	 *            word in the file
	 * @return true when the word is already in the HashMap
	 */
	public boolean hasWord(String word) {
		return index.containsKey(word);

	}

	/**
	 * Decide whether the word and the file are in the data or not
	 * 
	 * @param word
	 *            word in the file
	 * @param file
	 *            the name of file
	 * @return true when the word and the file are already in the HashMap
	 */
	public boolean hasFile(String word, String file) {
		HashMap<String, ArrayList<Integer>> hashMap;
		hashMap = index.get(word);
		return hashMap.containsKey(file);
	}

	/**
	 * Add the file name into the data
	 * 
	 * @param word
	 *            word in the file
	 * @param file
	 *            the name of file
	 */
	public void addFile(String word, String file) {
		HashMap<String, ArrayList<Integer>> HashMap = index.get(word);
		HashMap.put(file, new ArrayList<Integer>());
	}

	/**
	 * Add the position of the word in specific file into the data
	 * 
	 * @param word
	 *            word in file
	 * @param file
	 *            the name of file
	 * @param num
	 *            the position of word
	 */
	public void addNum(String word, String file, int num)
			throws InterruptedException {

		HashMap<String, ArrayList<Integer>> map;

		lock.acquireReadLock();
		try {
			map = index.get(word);
		} finally {
			lock.releaseReadLock();
		}

		if (map == null) {
			lock.acquireWriteLock();
			try {
				if (index.containsKey(word) == false) {
					map = new HashMap<String, ArrayList<Integer>>();
					index.put(word, map);
					indexLock.put(word, new CustomLock());
				} else {
					map = index.get(word);
				}
			} finally {
				lock.releaseWriteLock();
			}
		}

		CustomLock subLock = indexLock.get(word);
		subLock.acquireWriteLock();
		try {
			ArrayList<Integer> list = map.get(file);
			if (list == null) {
				list = new ArrayList<Integer>();
				list.add(num);
				map.put(file, list);
			} else {
				list.add(num);
			}
		} finally {
			subLock.releaseWriteLock();
		}
	}

	/**
	 * Doing the exclude search
	 * 
	 * @param query
	 * @param r
	 * @return	the search results
	 */
	public ArrayList<String> noSearch(String[] query, ArrayList<String> r) {

		for (String word : query) {
			for (Entry<String, HashMap<String, ArrayList<Integer>>> tmp : index
					.entrySet()) {
				if(toggle == true) {
					if (tmp.getKey().indexOf(word) == 0) {
						for (Entry<String, ArrayList<Integer>> v : tmp.getValue()
								.entrySet()) {
							if(r.contains(v.getKey()))
								r.remove(v.getKey());
						}
					}
				}
				else {
					if (tmp.getKey().equals(word)) {
						for (Entry<String, ArrayList<Integer>> v : tmp.getValue()
								.entrySet()) {
							if(r.contains(v.getKey()))
								r.remove(v.getKey());
						}
					}
				}

			}
		}
		return r;
	}
	
	/**
	 * The regular search
	 * 
	 * @param query
	 * @return	the search results
	 */
	public ArrayList<String> numSearch(String[] query) {
		ArrayList<String> results = new ArrayList<String>();
		HashMap<String, Integer> counter = new HashMap<String, Integer>();
		for (String word : query) {
			for (Entry<String, HashMap<String, ArrayList<Integer>>> tmp : index
					.entrySet()) {
				if(toggle == true) {
					if (tmp.getKey().indexOf(word) == 0) {
						for (Entry<String, ArrayList<Integer>> v : tmp.getValue()
								.entrySet()) {
							if(counter.containsKey(v.getKey())) {
								int count = counter.get(v.getKey());
								count += v.getValue().size();
								counter.put(v.getKey(), count);
							}
							else {
								counter.put(v.getKey(), v.getValue().size());
							}
						}
					}
				}
				else {
					if (tmp.getKey().equals(word)) {
						for (Entry<String, ArrayList<Integer>> v : tmp.getValue()
								.entrySet()) {
							if(counter.containsKey(v.getKey())) {
								log.debug(v.getKey());
								int count = counter.get(v.getKey());
								count += v.getValue().size();
								counter.put(v.getKey(), count);
							}
							else {
								counter.put(v.getKey(), v.getValue().size());
							}
						}
					}
				}

			}
		}
		
		for(Entry<String, Integer> t : counter.entrySet()) {
			log.debug("url: " + t.getKey() + "   count: " + t.getValue());
			results.add(t.getKey() + "|" + t.getValue());
		}
		Collections.sort(results, new ListComparator());
		ArrayList<String> result = new ArrayList<String>();
		for(String tmp : results) {
			result.add(tmp.split("\\|")[0]);
		}
		return result;
	}
	
	/**
	 * Get the suggested queries
	 * 
	 * @return the suggested queries
	 */
	public List<String> getSuggestedQuery()
	{
		ArrayList<String> query = new ArrayList<String>();
		HashMap<String, Integer> counter = new HashMap<String, Integer>();
			for (Entry<String, HashMap<String, ArrayList<Integer>>> tmp : index
					.entrySet()) {
					for (Entry<String, ArrayList<Integer>> v : tmp.getValue()
							.entrySet()) {
						if(counter.containsKey(tmp.getKey())) {
							int count = counter.get(tmp.getKey());
							count += v.getValue().size();
							counter.put(tmp.getKey(), count);
						}
						else {
							counter.put(tmp.getKey(), v.getValue().size());
						}
					}
		}
		
		for(Entry<String, Integer> t : counter.entrySet()) {
			query.add(t.getKey() + "|" + String.valueOf(t.getValue()));
		}
		Collections.sort(query, new ListComparator());
		int end = 10;
		if(query.size() < 10)
			end = query.size();
		return query.subList(0, end);
		
	}
	
	/**
	 * Write the data into the specific txt file
	 */
	public void fileWriter() {
		try {
			FileWriter fileWriter = new FileWriter("invertedindex.txt");
			BufferedWriter writer = new BufferedWriter(fileWriter);
			for (String word : index.keySet()) {
				writer.append(word + "\n");
				for (String file : index.get(word).keySet()) {
					writer.append(file);
					for (int num : index.get(word).get(file)) {
						writer.append(", " + num);
					}
					writer.append("\n");
				}
				writer.append("\n");
			}
			writer.close();
			fileWriter.close();
		} catch (IOException e) {
			System.out.println("IO is broken!");
		}
	}
	
	/**
	 * Doing the consecutive search
	 * 
	 * @param query
	 * @return	the search results
	 */
	public ArrayList<String> consecutiveSearch(String[] query) {
		
		int queryIndex = 0;
		ArrayList<String> results = new ArrayList<String>();
		// Map the information into a new map which has filename as key and
		// positions as value
		HashMap<String, ArrayList<String>> subMap = new HashMap<String, ArrayList<String>>();
		for (String word : query) {
			for (Entry<String, HashMap<String, ArrayList<Integer>>> tmp : index
					.entrySet()) {
				// doing the partial search
				if (tmp.getKey().equals(word)) {

					//log.debug("The partial search is: " + tmp.getKey());
					for (Entry<String, ArrayList<Integer>> v : tmp.getValue()
							.entrySet()) {
						if (!subMap.containsKey(v.getKey())) {
							subMap.put(v.getKey(), new ArrayList<String>());
						}
						ArrayList<String> list = subMap.get(v.getKey());
						for (int value : v.getValue()) {
							list.add(tmp.getKey() + "|" + value + "|"
									+ queryIndex);
						}
					}
				}
			}
			queryIndex++;
		}

		// to find the numbers of consecutive words matched
		for (Entry<String, ArrayList<String>> tmp : subMap.entrySet()) {

			ArrayList<String> list = tmp.getValue();
			
			ArrayList<String> count = new ArrayList<String>();
			// sort the list with the position from smaller to bigger
			Collections.sort(list, new CounterComparator());
			//log.debug(list);
			// keep the information
			ArrayList<String> words = new ArrayList<String>();
			ArrayList<Integer> position = new ArrayList<Integer>();
			ArrayList<Integer> qIndex = new ArrayList<Integer>();
			for (String l : list) {
				String[] split = l.split("\\|");
				qIndex.add(Integer.valueOf(split[2]));
				position.add(Integer.valueOf(split[1]));
				words.add(split[0]);
			}
			log.debug(words);
			log.debug(position);
			log.debug(qIndex);
			boolean add = true;
			if(position.size() == 1) {
			}
			for(int i = 0; i < position.size() - 1; i++) {
				if(add == true) {
					count.add(words.get(i) + "|" + qIndex.get(i) + "|" + position.get(i));
				}
				if((position.get(i).equals(position.get(i + 1) - 1) || position.get(i).equals(position.get(i + 1))) ) {
					count.add(words.get(i + 1) + "|" + qIndex.get(i + 1) + "|" + position.get(i + 1));
					log.debug(count);
					add = false;
				}
				else {
					log.debug(count);
					if(this.getMax(count, query.length)) {
						if(!results.contains(tmp.getKey()))
							results.add(tmp.getKey());
					}
					count.clear();
					add = true;
				}
				
				if(i ==( position.size() - 2)) {
					log.debug(count);
					if(this.getMax(count, query.length)) {
						if(!results.contains(tmp.getKey()))
							results.add(tmp.getKey());
					}
					count.clear();
				}
			}

		}
		return results;
	}
	
	
	/**
	 * Find numbers of consecutive words matched
	 * @param target	the words with consecutive position in the context
	 * @param length	the max length
	 * @return			the numbers of consecutive words matched
	 */
	public boolean getMax(ArrayList<String> target, int length) {
		int count = 1;
		int tmp1 = 0;
		int tmp2 = 0;
		for(int i = 0; i < target.size(); i++) {
			tmp1 = Integer.valueOf(target.get(i).split("\\|")[1]);
			tmp2 = Integer.valueOf(target.get(i).split("\\|")[2]);
			count = 1;
			for(int j = i + 1; j < target.size(); j++) {
				if(tmp1 == Integer.valueOf(target.get(j).split("\\|")[1]) - 1 && tmp2 == Integer.valueOf(target.get(j).split("\\|")[2]) - 1){
					tmp1 = Integer.valueOf(target.get(j).split("\\|")[1]);
					tmp2 = Integer.valueOf(target.get(j).split("\\|")[2]);
					count++;
				}
			}
		log.debug(count);
			if(count == length) {
				return true;
			}
		}
		return false;
	}
}

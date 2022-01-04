package lse;

import java.io.*;
import java.util.*;

/**
 * This class builds an index of keywords. Each keyword maps to a set of pages in
 * which it occurs, with frequency of occurrence in each page.
 *
 */
public class LittleSearchEngine {
	
	/**
	 * This is a hash table of all keywords. The key is the actual keyword, and the associated value is
	 * an array list of all occurrences of the keyword in documents. The array list is maintained in 
	 * DESCENDING order of frequencies.
	 */
	HashMap<String,ArrayList<Occurrence>> keywordsIndex;
	
	/**
	 * The hash set of all noise words.
	 */
	HashSet<String> noiseWords;
	
	/**
	 * Creates the keyWordsIndex and noiseWords hash tables.
	 */
	public LittleSearchEngine() {
		keywordsIndex = new HashMap<String,ArrayList<Occurrence>>(1000,2.0f);
		noiseWords = new HashSet<String>(100,2.0f);
	}
	
	/**
	 * Scans a document, and loads all keywords found into a hash table of keyword occurrences
	 * in the document. Uses the getKeyWord method to separate keywords from other words.
	 * 
	 * @param docFile Name of the document file to be scanned and loaded
	 * @return Hash table of keywords in the given document, each associated with an Occurrence object
	 * @throws FileNotFoundException If the document file is not found on disk
	 */
	public HashMap<String,Occurrence> loadKeywordsFromDocument(String docFile) 
	throws FileNotFoundException {

		//creates returned hashmap
		HashMap<String, Occurrence> docKeys = new HashMap<String, Occurrence>();

		//create new scanner
		Scanner scanDoc = new Scanner(new File(docFile));
		//scans through the words of the document
		while (scanDoc.hasNext()) {
			String word = scanDoc.next();

			//check if word is a keyword
			String keyWord = getKeyword(word);

			//if word is a keyword
			if(keyWord != null){
				//if hashmap contains keyword
				if(docKeys.containsKey(keyWord)){
					Occurrence current = docKeys.get(keyWord);
					//add +1 to the frequency of the keyword
					current.frequency += 1;
					//update the frequency of word
					docKeys.put(keyWord,current);
				}
				//if hashmap doesn't contain keyword
				else{
					//insert into hashmap
					docKeys.put(keyWord, new Occurrence(docFile, 1));
				}
			}
		}

		return docKeys;
	}
	
	/**
	 * Merges the keywords for a single document into the master keywordsIndex
	 * hash table. For each keyword, its Occurrence in the current document
	 * must be inserted in the correct place (according to descending order of
	 * frequency) in the same keyword's Occurrence list in the master hash table. 
	 * This is done by calling the insertLastOccurrence method.
	 * 
	 * @param kws Keywords hash table for a document
	 */
	public void mergeKeywords(HashMap<String,Occurrence> kws) {

		//for every key in the parameter hashmap
		for(String entry : kws.keySet()){
			//if the global hashmap doesn't contain the keyword
			if(!keywordsIndex.containsKey(entry)){
				//make new occurrence arraylist
				ArrayList<Occurrence> newKeyword = new ArrayList<Occurrence>();
				//put the keyword's occurence in the new occurrence arraylist
				newKeyword.add(kws.get(entry));
				//put the new keyword into the global hashmap
				keywordsIndex.put(entry, newKeyword);
			}
			else{
				//copy arraylist of occurrence from global hashmap of existing word
				ArrayList<Occurrence> newKeyword = keywordsIndex.get(entry);
				//add occurrence of keyword to the existing arraylist corresponding to that keyword in the global hashmap
				newKeyword.add(kws.get(entry));
				//rearrange global hashmap keyword's arraylist because you just inserted a new occurrence to the arraylist
				insertLastOccurrence(newKeyword);
				//update the occurrence arraylist for the specific word
				keywordsIndex.put(entry, newKeyword);
			}
		}

	}
	
	/**
	 * Given a word, returns it as a keyword if it passes the keyword test,
	 * otherwise returns null. A keyword is any word that, after being stripped of any
	 * trailing punctuation(s), consists only of alphabetic letters, and is not
	 * a noise word. All words are treated in a case-INsensitive manner.
	 * 
	 * Punctuation characters are the following: '.', ',', '?', ':', ';' and '!'
	 * NO OTHER CHARACTER SHOULD COUNT AS PUNCTUATION
	 * 
	 * If a word has multiple trailing punctuation characters, they must all be stripped
	 * So "word!!" will become "word", and "word?!?!" will also become "word"
	 * 
	 * See assignment description for examples
	 * 
	 * @param word Candidate word
	 * @return Keyword (word without trailing punctuation, LOWER CASE)
	 */
	public String getKeyword(String word) {

		//create potentially returned keyword
		String retString = word;

		//check if string has hyphen or apostrophe
		if(retString.contains("-") || retString.contains("'")){
			return null;
		}

		//if string ends with punctuation
		while(retString.endsWith(".")||retString.endsWith(",")||
				retString.endsWith("?")||retString.endsWith(":")||
				retString.endsWith(";")||retString.endsWith("!")){

					retString = word.substring(0,word.length()-1);
				}

		//if string still contains punctuation
		if(retString.contains(".")||retString.contains(",")||
			retString.contains("?")||retString.contains(":")||
			retString.contains(";")||retString.contains("!")){
				return null;
			}

		//check if word is a noise word (before making it all lowercase)
		if(noiseWords.contains(retString)){
			return null;
		}

		//make word all lower case
		retString = retString.toLowerCase();

		//check if word is a noise word
		if(noiseWords.contains(retString)){
			return null;
		}

		return retString;
	}
	
	/**
	 * Inserts the last occurrence in the parameter list in the correct position in the
	 * list, based on ordering occurrences on descending frequencies. The elements
	 * 0..n-2 in the list are already in the correct order. Insertion is done by
	 * first finding the correct spot using binary search, then inserting at that spot.
	 * 
	 * @param occs List of Occurrences
	 * @return Sequence of mid point indexes in the input list checked by the binary search process,
	 *         null if the size of the input list is 1. This returned array list is only used to test
	 *         your code - it is not used elsewhere in the program.
	 */
	public ArrayList<Integer> insertLastOccurrence(ArrayList<Occurrence> occs) {

		//if occs size is 1
		if(occs.size() == 1){
			return null;
		}

		//arraylist to hold all midpoints touched
		ArrayList<Integer> mids = new ArrayList<Integer>();

		//save and remove last occ (the one to be inserted) from occ
		Occurrence lastOcc = occs.get(occs.size()-1);
		occs.remove(occs.size()-1);

		//INDEXES for low, high
		int lo = occs.size()-1;
		int hi = 0;	
		int mid = 0;

		while(lo >= hi){
			//make new midpoint
			mid = hi + ((lo-hi)/2);

			//if frequency of target is less than frequency of midpoint
			if(lastOcc.frequency < occs.get(mid).frequency){
				hi = mid + 1;
				mids.add(mid);
				mid++;
			}
			//if frequency of target is larger than frequency of midpoint
			else if(lastOcc.frequency > occs.get(mid).frequency){
				lo = mid - 1;
				mids.add(mid);
			}
			//if frequency of target is equal to frequency of midpoint
			else if(lastOcc.frequency == occs.get(mid).frequency){
				mids.add(mid);
				break;
			}
		}

		//insert last occurrence into correct spot
		occs.add(mid, lastOcc);

		return mids;
	}
	
	/**
	 * This method indexes all keywords found in all the input documents. When this
	 * method is done, the keywordsIndex hash table will be filled with all keywords,
	 * each of which is associated with an array list of Occurrence objects, arranged
	 * in decreasing frequencies of occurrence.
	 * 
	 * @param docsFile Name of file that has a list of all the document file names, one name per line
	 * @param noiseWordsFile Name of file that has a list of noise words, one noise word per line
	 * @throws FileNotFoundException If there is a problem locating any of the input files on disk
	 */
	public void makeIndex(String docsFile, String noiseWordsFile) 
	throws FileNotFoundException {
		// load noise words to hash table
		Scanner sc = new Scanner(new File(noiseWordsFile));
		while (sc.hasNext()) {
			String word = sc.next();
			noiseWords.add(word);
		}
		
		// index all keywords
		sc = new Scanner(new File(docsFile));
		while (sc.hasNext()) {
			String docFile = sc.next();
			HashMap<String,Occurrence> kws = loadKeywordsFromDocument(docFile);
			mergeKeywords(kws);
		}
		sc.close();
	}
	
	/**
	 * Search result for "kw1 or kw2". A document is in the result set if kw1 or kw2 occurs in that
	 * document. Result set is arranged in descending order of document frequencies. 
	 * 
	 * Note that a matching document will only appear once in the result. 
	 * 
	 * Ties in frequency values are broken in favor of the first keyword. 
	 * That is, if kw1 is in doc1 with frequency f1, and kw2 is in doc2 also with the same 
	 * frequency f1, then doc1 will take precedence over doc2 in the result. 
	 * 
	 * The result set is limited to 5 entries. If there are no matches at all, result is null.
	 * 
	 * See assignment description for examples
	 * 
	 * @param kw1 First keyword
	 * @param kw1 Second keyword
	 * @return List of documents in which either kw1 or kw2 occurs, arranged in descending order of
	 *         frequencies. The result size is limited to 5 documents. If there are no matches, 
	 *         returns null or empty array list.
	 */
	public ArrayList<String> top5search(String kw1, String kw2) {
		//returned arraylist of document names
		ArrayList<String> top5 = new ArrayList<String>();

		ArrayList<Occurrence> occ1 = new ArrayList<Occurrence>();
		ArrayList<Occurrence> occ2 = new ArrayList<Occurrence>();
		
		//copy all of kw1 arraylist into occ1 arraylist
		if(keywordsIndex.containsKey(kw1)){
			for(Occurrence occ : keywordsIndex.get(kw1)){
				occ1.add(occ);
			}
		}
		//copy all of kw2 arraylist into occ2 arraylist
		if(keywordsIndex.containsKey(kw2)){
			for(Occurrence occ : keywordsIndex.get(kw2)){
				occ2.add(occ);
			}
		}

		//trim occ1 down to 5
		if(occ1.size() > 5){
			for(int i = occ1.size(); i > 5; i--){
				occ1.remove(i-1);
			}
		}
		//trim occ2 down to 5
		if(occ2.size() > 5){
			for(int i = occ2.size(); i > 5; i--){
				occ2.remove(i-1);
			}
		}

		//while both arraylists arent empty
		while(!occ1.isEmpty() && !occ2.isEmpty()){
			Occurrence juan = occ1.get(0);
			Occurrence jose = occ2.get(0);

			//if same document
			if(juan.document == jose.document){
				if(juan.frequency >= jose.frequency){
					top5.add(juan.document);
				}
				else if(juan.frequency < jose.frequency){
					top5.add(jose.document);
				}
				occ1.remove(0);
				occ2.remove(0);
			}
			//if different document
			else if(juan.document != jose.document){
				if(juan.frequency >= jose.frequency){
					top5.add(juan.document);
					occ1.remove(0);
				}
				else if(juan.frequency < jose.frequency){
					top5.add(jose.document);
					occ2.remove(0);
				}
			}
		}

		//if any of the arraylist still have elements left over
		if(!occ1.isEmpty()){
			while(!occ1.isEmpty()){
				Occurrence juan = occ1.get(0);
				top5.add(juan.document);
				occ1.remove(0);
			}
		}
		else if(!occ2.isEmpty()){
			while(!occ2.isEmpty()){
				Occurrence jose = occ2.get(0);
				top5.add(jose.document);
				occ2.remove(0);
			}
		}

		//trim top5 down to 5
		if(top5.size() > 5){
			for(int i = top5.size(); i > 5; i--){
				top5.remove(i-1);
			}
		}

		//check for duplicates
		for(int a = 0; a < top5.size(); a++){
			String crntDoc = top5.get(a);
			for(int b = a+1; b < top5.size(); b++){
				String compDoc = top5.get(b);
				if(crntDoc == compDoc){
					top5.remove(b);
				}
			}
		}

		return top5;
	
	}
}

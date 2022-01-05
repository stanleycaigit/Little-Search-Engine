package lse;

public class Occurrence {
	
	//Document in which a keyword occurs.
	String document;
	
	//The frequency (number of times) the keyword occurs in the above document.
	int frequency;
	
	public Occurrence(String doc, int freq) {
		document = doc;
		frequency = freq;
	}
	
	public String toString() {
		return "(" + document + "," + frequency + ")";
	}
}

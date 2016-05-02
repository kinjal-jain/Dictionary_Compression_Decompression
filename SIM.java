/* On my honor, I have neither given nor received unauthorized aid on this assignment */

/* Command To Compile 	::javac SIM.java
 *                    	::java  SIM 1
 *                   	::java  SIM 2
 */

/*	Author : Kinjal Jain	*/

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

class Compression {
	FileWriter fstream;
	BufferedWriter output;
	int count = 0;
	LinkedHashMap<String, Integer> wordCount = new LinkedHashMap<String, Integer>();
	HashMap<String, String> Dictionary = new HashMap<String, String>();
	String[] d = { "000", "001", "010", "011", "100", "101", "110", "111" };

	// Constructor for Compression, takes input file, generates output
	// file(compressed)
	@SuppressWarnings("static-access")
	Compression(String input, String out) throws IOException {
		LookUp L = new LookUp(input);
		Dictionary = L.Dictionary;
		String outResult = new String();
		fstream = new FileWriter(out);
		output = new BufferedWriter(fstream);
		BufferedReader reader2 = new BufferedReader(new FileReader(input));
		String l = null;
		int i = 0;

		while ((l = reader2.readLine()) != null) {
			String compressed = null;
			// Direct Matching
			if (Dictionary.containsValue(l)) {
				List<Object> l1 = getKeysFromValue(Dictionary, l);
				compressed = "00" + l1.get(0);
				// System.out.println("Direct Compress : " + compressed);
			} else { // Two bit mismatch
				compressed = xor(l, d);
				// System.out.println("Compressed String : " + compressed);
			}
			outResult += compressed;
		}
		while (outResult.length() % 32 != 0) {
			outResult += "1";
		}
		while (i < outResult.length()) {
			output.write(outResult.substring(i, i + 32));
			output.write("\r\n");
			i += 32;
		}
		output.write("xxxx");
		for (i = 0; i < 8; i++) {
			output.write("\r\n" + Dictionary.get(d[i]));
		}
		output.close();

	}

	/**
	 * Calculates the XOR for the given 32 bit binary based on the dictionary
	 * provided.
	 * 
	 * @param orig
	 *            - Original 32 bit binary String
	 * @param dict
	 *            - Dictionary
	 * @return compressed - Compressed String
	 */
	public String xor(String orig, String[] dict) {
		String dictVal = null;
		Integer[] mismatchCounter = new Integer[8];
		String[] xorToBinary = new String[8];
		for (int j = 0; j < 8; j++) {
			dictVal = Dictionary.get(dict[j]);
			if (orig.length() == dictVal.length()) {
				long mismatchInteger = Long.parseLong(orig, 2)
						^ Long.parseLong(dictVal, 2);
				xorToBinary[j] = Long.toBinaryString(mismatchInteger);
				while (xorToBinary[j].length() != 32) {
					xorToBinary[j] = "0" + xorToBinary[j];
				}
				mismatchCounter[j] = Long.bitCount(mismatchInteger);
				// System.out.println(mismatchCounter[j] + " <--> "
				// + xorToBinary[j] + " <--> " + dictVal + "<--> " + orig);
			}
		}
		String compressed = null;
		String firstIndex = null;
		String secondIndex = null;
		for (int i = 0; i < 8; i++) {
			if (mismatchCounter[i] == 2 && xorToBinary[i].contains("11")) {
				// System.out.println("XOR : " + xorToBinary[i]);
				// System.out.println("Index of Binary : "
				// + xorToBinary[i].indexOf("11"));
				firstIndex = Integer.toBinaryString((xorToBinary[i]
						.indexOf("11")));
				firstIndex = fiveBitGenerator(firstIndex);
				// System.out.println("First Index : " + firstIndex);
				compressed = "01" + firstIndex + dict[i];
				// System.out.println("Original : " + orig);
				// System.out.println("Compressed for Orig : " + compressed);
			} else if (mismatchCounter[i] == 2 && xorToBinary[i].contains("1")) {
				// System.out.println("XOR : " + xorToBinary[i]);
				// System.out.println("Index of Binary : "
				// + xorToBinary[i].indexOf("1"));
				// System.out.println("Index of Binary : "
				// + xorToBinary[i].lastIndexOf("1"));
				firstIndex = Integer.toBinaryString((xorToBinary[i]
						.indexOf("1")));
				firstIndex = fiveBitGenerator(firstIndex);
				secondIndex = Integer.toBinaryString((xorToBinary[i]
						.lastIndexOf("1")));
				secondIndex = fiveBitGenerator(secondIndex);
				compressed = "10" + firstIndex + secondIndex + dict[i];
				// System.out.println("First Index : " + firstIndex);
				// System.out.println("Second Index : " + secondIndex);
				// System.out.println("Original : " + orig);
				// System.out.println("Compressed for Orig : " + compressed);
			}
		}
		if (compressed == null) {
			compressed = "11" + orig;
		}
		return compressed;

	}

	/**
	 * Generates the five bit Index
	 * 
	 * @param index
	 * @return
	 */
	public String fiveBitGenerator(String index) {
		while (index.length() != 5) {
			index = "0" + index;
		}
		return index;
	}

	/**
	 * Returns the List of Object based on the Value, in the HashMap.
	 * 
	 * @param hm
	 *            - Hashmap
	 * @param value
	 *            - Value to be searched for the Key
	 * @return - List of Keys based on Values in the Hashmap.
	 */
	public List<Object> getKeysFromValue(HashMap<String, String> hm,
			Object value) {
		Set<String> ref = hm.keySet();
		Iterator<String> it = ref.iterator();
		List<Object> list = new ArrayList<Object>();

		while (it.hasNext()) {
			Object o = it.next();

			if (hm.get(o).equals(value)) {
				list.add(o);
			}
		}
		return list;
	}

} // Class Compression ends

/**
 * The Class writes the Decompression logic for the Compression/Decompression
 * Module.
 * 
 */
class Decompression {
	FileWriter fstream;
	BufferedWriter output;
	HashMap<String, Integer> wordCount = new HashMap<String, Integer>();
	HashMap<String, String> Dictionary = new HashMap<String, String>();
	String[] d = { "000", "001", "010", "011", "100", "101", "110", "111" };

	/*
	 * Constructor takes compressed input as one argument and generates a
	 * decompressed output
	 */
	Decompression(String input, String out) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(input));
		fstream = new FileWriter(out);
		output = new BufferedWriter(fstream);
		String line, readerIndex = null, read = null;
		String outResult = new String();

		while (!(line = reader.readLine()).equals("xxxx")) {
			outResult = outResult + line.trim();
		}

		for (int i = 0; i < 8; i++) {
			if ((line = reader.readLine()) != ("xxxx")) {
				Dictionary.put(d[i], line);
			}
		}
		int i = 0;
		String decompressed = "";
		while (i < outResult.length()) {
			readerIndex = outResult.substring(i, i + 2);
			i = i + 2;
			if (readerIndex.equals("00")) {
				read = outResult.substring(i, i + 3);
				// System.out.println(read);
				// System.out.println(Dictionary.get(read));
				decompressed = Dictionary.get(read);
				i = i + 3;
			} else if (readerIndex.equals("11")) {
				if ((outResult.length() - i) < 32) {
					break;
				}
				decompressed = outResult.substring(i, i + 32);
				i = i + 32;
			} else if (readerIndex.equals("01")) {
				String compressed = outResult.substring(i, i + 8);
				String firstIndex = compressed.substring(0, 5);
				String dict = compressed.substring(5, 8);
				String element = Dictionary.get(dict);
				decompressed = oneMismatchResolver(firstIndex, element);
				i = i + 8;
				// System.out.println("Compressed : " + compressed);
				// System.out.println("First Index : " + firstIndex);
				// System.out.println("Dictionary Element : " + dict);
				// System.out.println("Element : " + element);
				// output.write(set);
				// output.newLine();
			} else if (readerIndex.equals("10")) {
				String compressed = outResult.substring(i, i + 13);
				String firstIndex = compressed.substring(0, 5);
				String secondIndex = compressed.substring(5, 10);
				String dict = compressed.substring(10, 13);
				String element = Dictionary.get(dict);
				// System.out.println("Compressed : " + compressed);
				// System.out.println("First Index : " + firstIndex);
				// System.out.println("Second Index : " + secondIndex);
				// System.out.println("Dictionary Element : " + dict);
				// System.out.println("Element : " + element);
				decompressed = twoMismatchResolver(firstIndex, secondIndex,
						element);
				i = i + 13;
			}
			output.write(decompressed);
			output.newLine();

		}
		output.close();
		// System.out.println(Dictionary.values());
	}

	public String oneMismatchResolver(String index, String element) {
		int i = Integer.parseInt(index, 2);
		String mismatch = element.substring(i, i + 2);
		String resolved = "";
		switch (mismatch) {
		case "00":
			resolved = "11";
			break;
		case "01":
			resolved = "10";
			break;
		case "10":
			resolved = "01";
			break;
		case "11":
			resolved = "00";
			break;
		default:
			resolved = "";
		}
		String decompressed = element.substring(0, i) + resolved
				+ element.substring(i + 2);
		// System.out.println("mismatch : " + mismatch);
		// System.out.println("resolved : " + resolved);
		// System.out.println("Decompressed String 1 mismatch : " +
		// decompressed);
		return decompressed;
	}

	public String twoMismatchResolver(String fIndex, String sIndex,
			String element) {
		int i = Integer.parseInt(fIndex, 2);
		int j = Integer.parseInt(sIndex, 2);
		String fMismatch = ((element.substring(i, i + 1)).equals("0")) ? "1"
				: "0";
		String sMismatch = ((element.substring(j, j + 1)).equals("0")) ? "1"
				: "0";
		String decompressed = element.substring(0, i) + fMismatch
				+ element.substring(i + 1, j) + sMismatch
				+ element.substring(j + 1);
		// System.out.println(fMismatch);
		// System.out.println(sMismatch);
		// System.out.println("2 bit Decompressed String : " + decompressed);
		return decompressed;
	}

} // Class Decompression ends

class LookUp {
	LinkedHashMap<String, Integer> wordCount = new LinkedHashMap<String, Integer>();
	static HashMap<String, String> Dictionary = new HashMap<String, String>();
	FileWriter fstream;
	BufferedWriter output;

	/*
	 * Constructor takes input file, and generates the Dictionary for reference
	 * during compression
	 */
	LookUp(String input) throws IOException {
		String line;
		BufferedReader reader = new BufferedReader(new FileReader(input));
		String[] d = { "000", "001", "010", "011", "100", "101", "110", "111" };
		while ((line = reader.readLine()) != null) {
			String[] words = line.split(" ");
			for (String word : words) {
				if (wordCount.containsKey(word))
					wordCount.put(word, wordCount.get(word).intValue() + 1);
				else
					wordCount.put(word, 1);
			}
		}
		for (int i = 0; i < 8; i++) {
			Collection<Integer> wc = wordCount.values();
			List<Object> l = getKeysFromValue(wordCount, Collections.max(wc));
			Dictionary.put(d[i], (String) l.get(0));
			wordCount.remove(l.get(0));

		}
	}

	// Get the Key from a Value in a hashmap
	public List<Object> getKeysFromValue(HashMap<String, Integer> hm,
			Object value) {
		Set<String> ref = hm.keySet();
		Iterator<String> it = ref.iterator();
		List<Object> list = new ArrayList<Object>();

		while (it.hasNext()) {
			Object o = it.next();
			if (hm.get(o).equals(value)) {
				list.add(o);
			}
		}
		return list;
	}
} // Class LookUp Ends

public class SIM {

	@SuppressWarnings("unused")
	public static void main(String[] args) throws IOException {

		if (args[0].equals("1")) {
			Compression C = new Compression("original.txt", "cout.txt");
		}
		if (args[0].equals("2")) {
			Decompression D = new Decompression("compressed.txt", "dout.txt");
		}

	} // Main class ends

}
/* Author : Kinjal Jain */
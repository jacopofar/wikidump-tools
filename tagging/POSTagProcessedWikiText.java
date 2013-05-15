package tagging;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

/**
 * Process a wikipedia pre-processed dump, identifying parts of speech by applying a dictionary build by another utility from a wiktionary XML dump. 
 * The preprocessed code will be made of pure text, without mediawiki formatting, and page titles in lines in the form <doc id=... title=...>
 * The result will be a tab separated file in the form
 * TERM\tIDENTIFIEDPOS
 * with different POS separated with a comma and unknown ones marked as UNKNOWN.
 * 
 * Some terms in the dictionary are made of more than one word, the programs matches the longest char sequence.
 * 
 * 
 * */
public class POSTagProcessedWikiText {

	//let's count words (as dictionary words) frequency, to build some statistics
	static HashMap<String,Integer> frequencies=new HashMap<String,Integer>();
	static HashSet<String> placeholders=new HashSet<String>();
	/**
	 * A tool to tag a text using the POS dictionary. It's made for italian but should be easily adapted to other European languages.
	 * It splits the text in tokens (using spaces and punctuation) and assign to each token the list of possible tags.
	 * 
	 * Additionally, it creates the word frequency table
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {

		if(args.length!=3 && args.length!=4){
			System.err.println("Wrong usage, need 3 or 4 arguments:");
			System.err.println("The text to be tagged");
			System.err.println("The POS dictionary");
			System.err.println("The result file (it will be a TSV file)");
			System.err.println("[optional]the file where to save the word frequency table");
			System.exit(1);
		}
		String freqFile=null;
		if(args.length==4)
			freqFile=args[3];
		long start=System.currentTimeMillis();
		//alphabetic characters used in Europe
		//some libraries contains methods for this operation, this program has to be small and autonomous
		String characters="èéìùòàáßçāēīōūšžűőñôõøöêïëåşığĳœ";
		characters+=characters.toUpperCase();

		placeholders.add("OPAR");
		placeholders.add("CPAR");
		placeholders.add("DOT");
		placeholders.add("COMMA");
		placeholders.add("EXCLAMATION");
		placeholders.add("QUESTION");
		placeholders.add("PERCENT");
		placeholders.add("COLON");
		placeholders.add("SEMICOLON");
		placeholders.add("LINEEND");


		File file = new File(args[2]);
		DataOutputStream out = new DataOutputStream(new FileOutputStream(file));
		BufferedWriter wr=new BufferedWriter(new OutputStreamWriter(out));

		HashMap<String,String> POSdict=new HashMap<String,String>();
		//read the POS dictionary
		FileInputStream fstream = new FileInputStream(args[1]);
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine;
		int maxlen=-1;
		System.out.println("loading POS table...");
		while ((strLine = br.readLine()) != null){
			POSdict.put(markSigns(strLine.split("\t")[0]), strLine.split("\t")[1]);
			//we record the maximum length of an element to make matching faster (don't look for strings longest than maxlen)
			if(markSigns(strLine.split("\t")[0]).length()>maxlen) maxlen=markSigns(strLine.split("\t")[0]).length();
		}
		in.close();
		fstream.close();

		//add some composed words not present in the dictionary, for the Italian language
		POSdict.put(markSigns("dello"), "preposition");
		POSdict.put(markSigns("della"), "preposition");
		POSdict.put(markSigns("degli"), "preposition");
		POSdict.put(markSigns("dei"), "preposition,noun");
		POSdict.put(markSigns("delle"), "preposition");
		POSdict.put(markSigns("dalle"), "preposition");
		POSdict.put(markSigns("dalla"), "preposition");
		POSdict.put(markSigns("al"), "preposition");
		POSdict.put(markSigns("nelle"), "preposition");
		POSdict.put(markSigns("negli"), "preposition");
		POSdict.put(markSigns("nei"), "preposition,noun");
		POSdict.put(markSigns("sugli"), "preposition");
		POSdict.put(markSigns("all'"), "preposition");
		POSdict.put(markSigns("alle"), "preposition");
		POSdict.put(markSigns("allo"), "preposition");
		POSdict.put(markSigns("nel"), "preposition");
		POSdict.put(markSigns("si"), "pronoun");
		POSdict.put(markSigns("sul"), "preposition");
		POSdict.put(markSigns("sui"), "preposition");
		POSdict.put(markSigns("sull'"), "preposition");
		POSdict.put(markSigns("sulle"), "preposition");
		POSdict.put(markSigns("sulla"), "preposition");
		POSdict.put(markSigns("ai"), "preposition");
		POSdict.put(markSigns("dal"), "preposition");
		POSdict.put(markSigns("del"), "preposition");
		POSdict.put(markSigns("alla"), "preposition");
		POSdict.put(markSigns("allo"), "preposition");
		POSdict.put(markSigns("alle"), "preposition");
		POSdict.put(markSigns("agli"), "preposition");
		POSdict.put(markSigns("nella"), "preposition");
		POSdict.put(markSigns("nello"), "preposition");
		POSdict.put(markSigns("nell'"), "preposition");
		POSdict.put(markSigns("l'"), "article");
		POSdict.put(markSigns("dell'"), "preposition");
		POSdict.put(markSigns("dall'"), "preposition");
		System.out.println("POS list loaded! Start reading the text...");

		//open a textual representation of wikipedia (previously generated from an XML dump with the Python3 script)
		fstream = new FileInputStream(args[0]);
		in = new DataInputStream(fstream);
		br = new BufferedReader(new InputStreamReader(in));
		int words=0,unk=0,single=0,tok=0,pages=0;
		while ((strLine = br.readLine()) != null){
			if(strLine.contains("<doc id=")){
				pages++;
				System.out.println("words:"+words+" tokens:"+tok+" singleton:"+single+" unknown:"+unk+" pages:"+pages+" \t\t"+(pages*1000)/(System.currentTimeMillis()-start)+" pages per second");
				wr.write(strLine+"\n");
				continue;
			}
			if(strLine.length()<50){
				//if it's a list, don't skip elements
				if(!strLine.matches("[^\\(]+\\).+")){
					//avoid short lines, those are probably section titles
					//System.out.println("skip "+strLine);
					continue;
				}
			}
			strLine=markSigns(strLine)+" LINEEND";
			//remove all non alphanumeric characters (made for European languages and Turkish, works for Italian and English, not tested for other languages)
			strLine=strLine.replaceAll("[^'a-zA-Z"+characters+"0-9]", " ");
			//remove multiple spaces
			strLine=strLine.trim();
			strLine.replaceAll("[ ]+ ", " ");
			/*
			 * Now we "eat" the string finding recognizing the longest prefix, tag it (if known) and remove it until the line is empty.
			Some strings in the POS list contains others (e.g.:"stati uniti" contains "stati"), so we cannot just identify each word.
			I want the longest one, so start with the maxlen next characters and reduce the size until I get a match.
			If I can't find anything, take the string until the next space and mark it as UNKNOWN
			 */
			while(strLine.length()>0){
				while(strLine.startsWith(" "))strLine=strLine.substring(1);
				for(int l=Math.min(maxlen,strLine.length());l>=0;l--){
					if(l==0){
						//ate everything with no matches, mark the first word as UNKNOWN and remove it
						int size=strLine.split(" ")[0].length();
						//if it has no spaces, take the entire string
						if(size==0)size=strLine.length();
						//if it's a placeholder, don't tag it
						if(placeholders.contains(strLine.substring(0, size))){
							tok++;
							wr.write(strLine.substring(0, size)+"\n");
						}
						else{
							//unknown and not a placeholder
							unk++;
							words++;
							wr.write(strLine.substring(0, size)+"\tUNKNOWN\n");
							recordFreq(strLine.substring(0, size));
						}
						strLine=strLine.substring(size);

						break;
					}
					//avoid to split a word
					if(l!=strLine.length())
						if(strLine.charAt(l)!=' ') continue;
					String candidate=strLine.substring(0, l);
					if(POSdict.containsKey(candidate.toLowerCase())){
						String tags=POSdict.get(candidate.toLowerCase());
						wr.write(candidate+"\t"+tags+"\n");
						recordFreq(candidate);
						if(!tags.contains(",")) single++;
						strLine=strLine.substring(l);
						words++;
						break;
					}
				}
			}
		}
		wr.close();
		out.close();

		System.out.println("finished! It took "+(System.currentTimeMillis()-start)/1000+" seconds ("+(System.currentTimeMillis()-start)/60000+" minutes)");
		if(freqFile!=null){
			System.out.println("now let's build the token frequency table");
			file = new File("frequencies.txt");
			out = new DataOutputStream(new FileOutputStream(file));
			wr=new BufferedWriter(new OutputStreamWriter(out));

			for(Entry<String, Integer> e:frequencies.entrySet()){
				//ignore singletons
				if(e.getValue()<2) continue;
				wr.write(e.getKey()+"\t"+e.getValue()+"\n");
			}

			wr.close();
			out.close();
		}
	}

	static String markSigns(String text){
		text=text.replace("'", "' ");
		text=text.replace("(", " OPAR ");
		text=text.replace(")", " CPAR ");
		text=text.replace(".", " DOT ");
		text=text.replace(",", " COMMA ");
		text=text.replace("!", " EXCLAMATION ");
		text=text.replace("?", " QUESTION ");
		text=text.replace("%", " PERCENT ");
		text=text.replace(":", " COLON ");
		text=text.replace(";", " SEMICOLON ");
		text=text.trim();
		return text;
	}

	static void recordFreq(String token){
		if(frequencies.containsKey(token))
			frequencies.put(token, frequencies.get(token)+1);
		else
			frequencies.put(token,1);
	}
}

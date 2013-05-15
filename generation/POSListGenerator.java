package generation;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class POSListGenerator {

	private static BufferedWriter wr;

	/**
	 * Small tool to extract a list of word roles from an XML dump of en.wiktionary
	 * It's made for Italian but should work for other languages with mimimal changes.
	 * 
	 * Example output:
	 * 
	 * It reads the XML file line per line and parse with regular expressions, without using SAX
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		if(args.length!=3 && args.length!=4){
			System.err.println("Wrong usage, need 2 arguments:");
			System.err.println("The path of the XML dump of en.wiktionary");
			System.err.println("The path of the POS dictionary to be generated");
			System.exit(1);
		}
		String strLine;
		String title="";
		String content="";
		boolean insideArticle=false;
		File file = new File(args[1]);
		DataOutputStream out = new DataOutputStream(new FileOutputStream(file));
		wr=new BufferedWriter(new OutputStreamWriter(out));

		//parameters to inform the user about the work status
		long start=System.currentTimeMillis();
		long lastMessage=start;
		int saw=0;
		
		//open the en.wiktionary dump and read it line per line
		FileInputStream fstream = new FileInputStream(args[0]);
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		
		while ((strLine = br.readLine()) != null){
			
			if(lastMessage+5000<System.currentTimeMillis()){
				System.out.println("I saw "+saw+" pages ("+Math.round(((double)saw/(double)(System.currentTimeMillis()-start))*1000)+" pages per second)");
				lastMessage=System.currentTimeMillis();
			}
			if(strLine.contains("<title>")){
				title=strLine.split("<title>")[1].split("</title>")[0];
			}
			if(strLine.contains("<text ")){
				insideArticle=true;
				content="";
				try{content=strLine.split(">")[1];}catch(ArrayIndexOutOfBoundsException e){}
				continue;
			}
			if(strLine.contains("</text>")){
				insideArticle=false;
				content+=strLine.split("<")[0];
				managePage(content,title);
				saw++;
				continue;
			}
			if(insideArticle)content+=strLine;
		}
		wr.close();
		out.close();
		System.out.println("Finished! It took "+(System.currentTimeMillis()-start)/1000 +" seconds");
	}

	private static void managePage(String contenuto, String titolo) throws IOException {
		if(!contenuto.contains("==Italian==")) return;
		String voceIt=contenuto.split("==Italian==")[1];
		voceIt=voceIt.split("[^=]==[^=]")[0];
		Matcher m = Pattern.compile("===([a-zA-Z ]+)===").matcher(voceIt);

		String add="";
		while (m.find()) {
			String maybe=m.group().toLowerCase().replace("=", "");
			if(maybe.equals("adverb") && !add.contains(maybe))add+=","+maybe;
			if(maybe.equals("adjective") && !add.contains(maybe))add+=","+maybe;
			if(maybe.equals("noun") && !add.contains(maybe))add+=","+maybe;
			if(maybe.equals("verb") && !add.contains(maybe))add+=","+maybe;
			if(maybe.equals("proper noun") && !add.contains(maybe))add+=","+maybe;
			if(maybe.equals("conjunction") && !add.contains(maybe))add+=","+maybe;
			if(maybe.equals("verb form") && !add.contains(maybe))add+=","+"verb"; //don't care if it's a form. Moreover, is not always used
			if(maybe.equals("pronoun") && !add.contains(maybe))add+=","+maybe;
			if(maybe.equals("article") && !add.contains(maybe))add+=","+maybe;
			if(maybe.equals("preposition") && !add.contains(maybe))add+=","+maybe;
			if(maybe.equals("interjection") && !add.contains(maybe))add+=","+maybe;
		}
		if(add.length()>0){
			wr.write(titolo+"\t");
			add=add.substring(1);
			wr.write(add);
			wr.write("\n");
		}
		else
			System.out.println("--don't understand the word type (POS) of: "+titolo);
	}

}

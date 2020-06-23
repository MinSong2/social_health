package edu.yonsei.preprocess;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;

import edu.yonsei.util.Collection;
import edu.yonsei.util.Document;
import edu.yonsei.util.Sentence;
import edu.yonsei.util.Token;


/**
 * @author Reinald Kim Amplayo & Min Song
 */
public class PseudoPreprocess implements Preprocess {

	private TreeSet<String> stopSet;
	private Porter porter;
	Pipeline pipe = null;
	public PseudoPreprocess(Pipeline pipe) throws Exception
	{
		stopSet = new TreeSet<String>();
		porter = new Porter();
		this.pipe = pipe;
	}
	
	public boolean isStopword(String word)
	{
		return pipe.getStopWords().contains(word);
	}
	
	@Override
	public void initializeStopwords(String stopFile) throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void preprocess(Token t) {
		t.setLemma(t.getToken());
		t.setPOS("O");
		t.setNER("O");
		t.setStop(stopSet.contains(t.getToken()));
	
		//
	}

	@Override
	public void preprocess(Sentence s) {
		try {
			int count = 0;
			String sentence = s.getSentence();
			for (String _word : sentence.split("\\s+")) {
			    	String word = _word;
			    	String pos = "NN";
			    	if (_word.split("\\/").length > 1) {
			    		pos = _word.split("\\/")[1];
			    	}
			    	int length = _word.length();
			    	
			    	int offset = sentence.indexOf(_word,count);
			    	
			    	count += length;
			    	
			    	//System.out.println("Token " + word + " Pos " + pos + " Len " + length + " Offset " + offset);

			    	if (!StringUtils.endsWithAny(word, new String[] {null, ",", ".", "-", "\"", "?", "'", ";", ":"})) {
			    		Token t = new Token(word, word, pos, "O", word, stopSet.contains(word), "ko", false);
			    		s.add(t);
			    	}
			}
		} catch (Exception e) {
			System.out.println("There is an error " + e.getMessage());
		}
		
	}

	@Override
	public void preprocess(Document d) throws Exception {
	
		if (d.size() > 0) {
			for(int i = 0; i < d.size(); ++i) {
				Sentence s = d.get(i);
				preprocess(s);
				d.add(s);
			}
		} else {
			Sentence s = new Sentence(d.getDocument(), false);
			preprocess(s);
			d.add(s);
			
		}
	}

	@Override
	public void preprocess(Collection c) throws Exception {
		for(int i=0; i<c.size(); i++) {
			Document d = c.get(i);
			preprocess(d);
		}
	}
}

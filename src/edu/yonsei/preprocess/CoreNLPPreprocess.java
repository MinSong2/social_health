package edu.yonsei.preprocess;

import java.io.FileReader;
import java.util.Properties;
import java.util.Scanner;
import java.util.TreeSet;

import edu.yonsei.util.Collection;
import edu.yonsei.util.Document;
import edu.yonsei.util.Sentence;
import edu.yonsei.util.Token;

/**
 * @author Reinald Kim Amplayo & Min Song
 */
public class CoreNLPPreprocess implements Preprocess {
	
	private TreeSet<String> stopSet;
	private Porter porter;
	
	public CoreNLPPreprocess(String stopFile) throws Exception {
		stopSet = new TreeSet<String>();
		porter = new Porter();
		initializeStopwords(stopFile);
	}
	
	public void initializeStopwords(String stopFile) throws Exception {
		Scanner s = new Scanner(new FileReader(stopFile));
		while(s.hasNext())
			stopSet.add(s.next());
		s.close();
	}
	
	public boolean isStopword(String word)
	{
		return stopSet.contains(word);
	}
	
	public void preprocess(Token t) {
		edu.stanford.nlp.simple.Sentence sent = new edu.stanford.nlp.simple.Sentence(t.getToken());
		t.setLemma(sent.lemma(0));
		t.setPOS(sent.posTag(0));
		t.setNER(sent.nerTag(0));
		t.setStop(stopSet.contains(sent.lemma(0)) || stopSet.contains(t.getToken()));
	}
	
	public void preprocess(Sentence s) {
		edu.stanford.nlp.simple.Sentence sent = new edu.stanford.nlp.simple.Sentence(s.getSentence());
		
		preprocess(s, sent);
	}
	
	public void preprocess(Sentence s, edu.stanford.nlp.simple.Sentence sent) {
		Properties prop = new Properties();
		prop.setProperty("parse.maxlen", "100");
		try {
			s.setTree(sent.parse(prop));
			s.setParseTree(sent.parse(prop).toString());
			s.setDependencies(sent.dependencyGraph().toList().split("\n"));
			for(int i=0; i<sent.length(); i++) {
				boolean stop = stopSet.contains(sent.lemma(i)) || stopSet.contains(sent.originalText(i));
				String stem = porter.stripAffixes(sent.originalText(i));
				Token t = new Token(sent.originalText(i), sent.lemma(i), sent.posTag(i), sent.nerTag(i), stem, stop, "en", true);
				s.add(t);
			}
		} catch (Exception e) {
			System.out.println("Error " + e.getMessage());
		}
	}
	
	public void preprocess(Document d) throws Exception {
		edu.stanford.nlp.simple.Document doc = new edu.stanford.nlp.simple.Document(d.getDocument());
		for(edu.stanford.nlp.simple.Sentence sent : doc.sentences()) {
			Sentence s = new Sentence(sent.text(), false);
			preprocess(s, sent);
			d.add(s);
		}
	}
	
	public void preprocess(Collection c) throws Exception {
		for(int i=0; i<c.size(); i++) {
			Document d = c.get(i);
			preprocess(d);
		}
	}

}

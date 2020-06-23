package edu.yonsei.util;

import edu.yonsei.preprocess.CoreNLPPreprocess;
import edu.yonsei.preprocess.Pipeline;
import edu.yonsei.preprocess.PseudoPreprocess;
import edu.yonsei.preprocess.StanfordCoreNLPPreprocess;

/**
 * @author Reinald Kim Amplayo & Min Song
 */
public class Token {
	
	private String token, lemma, pos, ner, stem;
	private boolean isStop;
	private String mode;
	boolean isKomoran;
	
	public Token(String t, String l, String p, String n, String s, boolean i, String mode, boolean isKomoran) {
		token = t;
		lemma = l;
		pos = p;
		ner = n;
		stem = s;
		isStop = i;
		this.mode = mode;
		this.isKomoran = isKomoran;
	}
	
	public void preprocess(Pipeline pipe) throws Exception {
		if (mode.toLowerCase().startsWith("en")) {
			StanfordCoreNLPPreprocess cnlpp = new StanfordCoreNLPPreprocess(pipe);
			cnlpp.preprocess(this);
		} else if (mode.toLowerCase().startsWith("pseudo")) {
			PseudoPreprocess ppp = new PseudoPreprocess(pipe);
			ppp.preprocess(this);
		}
	}
	
	public Token(String t, Pipeline pipe) throws Exception {
		token = t;
		preprocess(pipe);
	}
	
	public String getStem() {
		return stem;
	}
	
	public String getToken() {
		return token;
	}
	
	public String getLemma() {
		return lemma;
	}
	
	public String getPOS() {
		return pos;
	}
	
	public String getNER() {
		return ner;
	}
	
	public boolean isStopword() {
		return isStop;
	}
	
	public void setLemma(String l) {
		lemma = l;
	}
	
	public void setPOS(String p) {
		pos = p;
	}
	
	public void setNER(String n) {
		ner = n;
	}
	
	public void setStop(boolean s) {
		isStop = s;
	}

}

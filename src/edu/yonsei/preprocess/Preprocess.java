package edu.yonsei.preprocess;

import edu.yonsei.util.Collection;
import edu.yonsei.util.Document;
import edu.yonsei.util.Sentence;
import edu.yonsei.util.Token;

public interface Preprocess {

	public void initializeStopwords(String stopFile) throws Exception;
	
	public void preprocess(Token t);
	
	public void preprocess(Sentence s);
	
	public void preprocess(Document d) throws Exception;
	
	public void preprocess(Collection c) throws Exception;
	
	public boolean isStopword(String word);
}

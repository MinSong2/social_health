package edu.yonsei.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import edu.yonsei.preprocess.CoreNLPPreprocess;
import edu.yonsei.preprocess.Pipeline;
import edu.yonsei.preprocess.PseudoPreprocess;
import edu.yonsei.preprocess.StanfordCoreNLPPreprocess;


/**
 * @author Reinald Kim Amplayo & Min Song
 */
public class Document extends ArrayList<Sentence> {
	
	private static final long serialVersionUID = 5923438214189501337L;
	private String document, classification;
	private Vector<Double> features;
	private Topic topic;
	private String docId;
    List<Integer> tokens;
    private double sentimentCore, sentimentLing, sentimentWord;
    
    public Document()
    {
    }
    
	public Document(String d) throws Exception {
		document = d;
		classification = null;
		topic = null;
		features = new Vector<Double>();
	}
	
	public Document(String d, String c) throws Exception {
		this(d);
		classification = c;
	}
	
	public void setDocId(String id)
	{
		docId = id;
	}
	
	public String getDocId()
	{
		return docId;
	}
	
	public void preprocess(String mode, boolean isKomoran, Pipeline pipe) throws Exception {

		if (mode.toLowerCase().startsWith("en")) {
			StanfordCoreNLPPreprocess cnlpp = new StanfordCoreNLPPreprocess(pipe);
			cnlpp.preprocess(this);
		} else if (mode.toLowerCase().startsWith("pseudo")) {
			PseudoPreprocess ppp = new PseudoPreprocess(pipe);
			ppp.preprocess(this);
		}
	}
	
	public Vector<Double> getFeatures() {
		return features;
	}
	
	public void setClassification(String c) {
		classification = c;
	}
	
	public String getClassification() {
		return classification;
	}
	

	public String getDocument() {
		return document;
	}
	
	public void setDocument(String d) {
		document = d;
	}
	
	public Topic getTopic() {
		if(topic == null) {
			System.err.println("No topic yet. Cannot initialize topic without a model.");
			return null;
		}
		return topic;
	}
	
	
	public void setFeatures(Vector<Double> f) {
		features = f;
	}

	public List<Integer> getTokens(){
        return tokens;
    }
    
    public int getToken(int index){
        return tokens.get(index);
    }
    
    public void setTokens(List<Integer> tokens){
        this.tokens = tokens;
    }
    
    public int getNumOfTokens(){
        return tokens.size();
    } 
    
}

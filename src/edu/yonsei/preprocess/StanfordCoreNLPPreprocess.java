package edu.yonsei.preprocess;

import java.io.FileReader;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.TreeSet;

import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.SentenceAnnotator;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;
import edu.yonsei.util.Collection;
import edu.yonsei.util.Document;
import edu.yonsei.util.Sentence;
import edu.yonsei.util.Token;


public class StanfordCoreNLPPreprocess implements Preprocess {

	private Annotation document;
	Pipeline pipe = null;
	
	public StanfordCoreNLPPreprocess(Pipeline pipe) 
	{
		this.pipe = pipe;
	}
	
	public void initializeStopwords(String stopFile) throws Exception 
	{
		// use Pipeline class
	}
	
	public boolean isStopword(String word)
	{
		return pipe.getStopWords().contains(word);
	}
	
	@Override
	public void preprocess(Token t) {
		edu.stanford.nlp.simple.Sentence sent = new edu.stanford.nlp.simple.Sentence(t.getToken());
		t.setLemma(sent.lemma(0));
		t.setPOS(sent.posTag(0));
		t.setNER(sent.nerTag(0));
		t.setStop(pipe.getStopWords().contains(sent.lemma(0)) || pipe.getStopWords().contains(t.getToken()));
	}

	@Override
	public void preprocess(Sentence s) {
		document = new Annotation(s.getSentence());
		pipe.getStanfordCoreNLP().annotate(document);
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		
		preprocess(s, sentences.get(0));
	}

	public void preprocess(Sentence s, edu.stanford.nlp.simple.Sentence sent) {
		//not implemented
	}
	
	public void preprocess(Sentence s, CoreMap sent) {
		try {
			s.setTree(sent.get(TreeAnnotation.class));
			//s.setParseTree(sent.get(TreeAnnotation.class).toString());
			//s.setDependencies(sent.get(CollapsedCCProcessedDependenciesAnnotation.class).toList().split("\n"));
			for(CoreLabel label : sent.get(TokensAnnotation.class)) {
				boolean stop = pipe.getStopWords().contains(label.lemma());
				String stem = pipe.getPorter().stripAffixes(label.originalText());
				Token t = new Token(label.originalText(), label.get(LemmaAnnotation.class), 
						label.get(PartOfSpeechAnnotation.class), label.get(NamedEntityTagAnnotation.class), stem, stop, "en", true);
				s.add(t);
			}
		} catch (Exception e) {
			System.out.println("Error " + e.getMessage());
		}
	}
	
	public void preprocess(Document d) throws Exception {
		document = new Annotation(d.getDocument());
		pipe.getStanfordCoreNLP().annotate(document);
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		for(CoreMap sent : sentences) {
			Sentence s = new Sentence(sent.toString(), false);
			
			preprocess(s, sent);
			d.add(s);
			
			//System.out.println("TEST " + s.getSentence());
		}
	}
	
	public void preprocess(Collection c) throws Exception {
		for(int i=0; i<c.size(); i++) {
			Document d = c.get(i);
			preprocess(d);
		}
	}
	
	//Sets text
	public void setText(String txt) {
		
	}
	
	//Splits a sequence of tokens into sentences
	public List<CoreMap> getSentences() {
		return document.get(SentencesAnnotation.class);
	}
		
	public List<CoreLabel> getTokens(CoreMap sentence) {
		return sentence.get(TokensAnnotation.class);
	}
	
	
	/* document level*/
	
	/** Implements both pronominal and nominal coreference resolution **/
	public Map<Integer, CorefChain> getDocumentChain() {
		return document.get(CorefChainAnnotation.class);
	}
	
	
	/* sentence level */
	
	/** Provides full syntactic analysis, using both the constituent and the dependency representations **/
	public Tree getSentenceTree(CoreMap sentence) {
		return sentence.get(TreeAnnotation.class);
	}
	
	/** Provides full syntactic analysis, using both the constituent and the dependency representations **/
	public SemanticGraph getSentenceSemanticGraph(CoreMap sentence) {
		return sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
	}

	
	/* token level */
	
	/** Returns original token string **/
	public String getTokenText(CoreLabel token) {
		return token.getString(TextAnnotation.class);
	}
	
	/** Labels tokens with their POS tag **/
	public String getTokenPOS(CoreLabel token) {
		return token.get(PartOfSpeechAnnotation.class);
	}
	
	/** Generates the word lemmas for all tokens in the corpus **/
	public String getTokenLemma(CoreLabel token) {
		return token.get(LemmaAnnotation.class);
	}
	
	/** Recognizes named and numerical entities **/
	public String getTokenNE(CoreLabel token) {
		return token.get(NamedEntityTagAnnotation.class);
	}
		
	
	public static void main(String[] args) throws Exception {
		Pipeline pipe = new Pipeline("data/util/stopwords.txt");
		StanfordCoreNLPPreprocess nlp = new StanfordCoreNLPPreprocess(pipe);
		String text = "A pVHL mutant containing a P154L substitution does not promote degradation of HIF1-Alpha";
		nlp.preprocess(new Document(text));
		for (CoreMap sentence : nlp.getSentences()) {
			System.out.println(nlp.getSentenceTree(sentence).toString());
			System.out.println(nlp.getSentenceTree(sentence));
			
			for (CoreLabel token : nlp.getTokens(sentence)) {
				System.out.println(nlp.getTokenLemma(token));
				System.out.println(nlp.getTokenText(token));
				System.out.println(nlp.getTokenPOS(token));
			}
		}
	
	}


}

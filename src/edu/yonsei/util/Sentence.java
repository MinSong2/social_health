package edu.yonsei.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.stanford.nlp.trees.Tree;

import edu.yonsei.preprocess.CoreNLPPreprocess;

import edu.yonsei.preprocess.Pipeline;
import edu.yonsei.preprocess.Preprocess;
import edu.yonsei.preprocess.PseudoPreprocess;
import edu.yonsei.preprocess.StanfordCoreNLPPreprocess;

import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.cmdline.parser.ParserTool;
import opennlp.tools.parser.Parser;
import opennlp.tools.util.Span;

/**
 * @author Reinald Kim Amplayo & Min Song
 */
public class Sentence extends ArrayList<Token> {

	private static final long serialVersionUID = 6274823696191382944L;
	private String sentence, parseTree;
	private String[] dependencies;
	private double sentimentCore, sentimentLing, sentimentWord;
	Tree tree = null;
	ArrayList<String> nounPhrases = new ArrayList();
	Preprocess processor = null;
	ArrayList<String> stopwords = null;

	boolean isKomoran = true;
	String mode = "en";
	
	public Sentence(String s) {
		sentence = s;
		parseTree = null;
		dependencies = null;
		sentimentCore = -2000000000;
		sentimentLing = -2000000000;
		sentimentWord = -2000000000;

	}
	
	public Sentence(String s, boolean isKomoran) {
		sentence = s;
		parseTree = null;
		dependencies = null;
		sentimentCore = -2000000000;
		sentimentLing = -2000000000;
		sentimentWord = -2000000000;
		
		this.isKomoran = isKomoran;
	}
	
	public Sentence(String s, String regex, int textPosition) {
		
		sentence = s.split(regex)[textPosition];
		parseTree = null;
		dependencies = null;
		sentimentCore = -2000000000;
		sentimentLing = -2000000000;
		sentimentWord = -2000000000;
	}
	
	public void setParseTree(String p) {
		parseTree = p;
	}

	public String getParseTree() {
		return parseTree;
	}

	public void setTree(Tree tree)
	{
		this.tree = tree;
	}
	
	public Tree getTree()
	{
		return tree;
	}
	
	public void setDependencies(String[] dep) {
		dependencies = dep;
	}
	
	public String[] getDependencies() {
		return dependencies;
	}
		
	public ArrayList<String> getNounPhrases()
	{
		return nounPhrases;
	}
	
	public void clearNounPhrases()
	{
		nounPhrases.clear();
	}
	
	public void setStopWord(ArrayList<String> stopwords)
	{
		//stopwordArray = stopwords.toArray(new CharSequence[stopwords.size()]);
		this.stopwords = stopwords;
	}
	
	public void preprocess(String mode, boolean isKomoran, Pipeline pipeline) throws Exception 
	{
		this.isKomoran = isKomoran;
		this.mode = mode;
		
		if (mode.toLowerCase().startsWith("en")) {
			processor = new StanfordCoreNLPPreprocess(pipeline);
			processor.preprocess(this);
		} else if (mode.toLowerCase().startsWith("pseudo")) {
			processor = new PseudoPreprocess(pipeline);
			processor.preprocess(this);
		}
	}
	
	public String getSentence() {
		return sentence;
	}
	
	public void setSentence(String s) {
		sentence = s;
	}
	

	
	
	public List<String> getNGrams(int n) {
		List<String> ret = new ArrayList<String>();
		for(int i=0; i<size()-n; i++) {
			String ngram = "";
			for(int j=i; j<i+n; j++) {
				Token token = get(j);
				if (get(j).isStopword()) continue;
				
				if (isKomoran && !mode.toLowerCase().startsWith("en")) {
					if (!token.isStopword() 
						&& (!token.getToken().startsWith("<") && !token.getToken().endsWith(">") &&
								(token.getPOS().startsWith("NN") || token.getPOS().startsWith("JJ") || token.getPOS().startsWith("V"))) ) {
						ngram += token.getLemma() + " ";
					}
				} else if (!isKomoran && !mode.toLowerCase().startsWith("en")) {
					if (!token.isStopword() 
							&& (!token.getToken().startsWith("Punctuation") && !token.getToken().endsWith("Josa") &&
									!token.getPOS().startsWith("Foreign")) ) {
							ngram += token.getLemma() + " ";
					}
				} else if (mode.toLowerCase().startsWith("en")) {
					if (!token.isStopword() 
							&& (!token.getToken().startsWith("<") && !token.getToken().endsWith(">") &&
									(token.getPOS().startsWith("NN"))) ) {
							ngram += token.getLemma() + " ";
						}
				}
			}
	
			ngram = ngram.trim();
			if (ngram.length() > 0 && ret.size() < 1) {
				ret.add(ngram);
			} else if (ngram.length() > 0 && ret.size() >= 1 
					&& !ret.get(ret.size()-1).equals(ngram) && !ret.get(ret.size()-1).contains(ngram)) { 
				ret.add(ngram);
			}
			//System.out.println("XXXXX " + ngram + " : " + ret.size());
			
		}
		
		return ret;
	}

	public List<String> getNNounGrams(int n) {
		List<String> ret = new ArrayList<String>();
		for(int i=0; i<size()-n; i++) {
			String ngram = "";
			for(int j=i; j<i+n; j++) {
				Token token = get(j);
				
				if (get(j).isStopword()) continue;
				if (token.getLemma() == null) continue;
				if (token.getLemma().trim().length() < 1) continue;
				
				//System.out.println("XXXXX " + get(j).isStopword() + " " + token.getLemma() + " :: " + token.getPOS());
							
				if (isKomoran && !mode.toLowerCase().startsWith("en")) {
					if (!token.isStopword() 
							&& (!token.getToken().startsWith("<") && !token.getToken().endsWith(">") &&
									(token.getPOS().startsWith("NN") || token.getPOS().startsWith("JJ"))) ) {
							ngram += token.getLemma().trim() + " ";
					}
				} else if (!isKomoran && !mode.toLowerCase().startsWith("en")) {
					if (!token.isStopword() 
							&& (!token.getToken().startsWith("Punctuation") && !token.getToken().endsWith("Josa") &&
									!token.getPOS().startsWith("Foreign")) ) {
							ngram += token.getLemma() + " ";
					}
				} else if (mode.toLowerCase().startsWith("en")) {
					if (!token.isStopword() 
							&& (!token.getToken().startsWith("<") && !token.getToken().endsWith(">") &&
									(token.getPOS().startsWith("NN"))) ) {
							ngram += token.getLemma() + " ";
						}
				}
			}	
			ngram = ngram.trim();
			if (ngram.length() > 0 && ret.size() < 1) {
				ret.add(ngram);
			} else if (ngram.length() > 0 && ret.size() >= 1 
					&& !ret.get(ret.size()-1).equals(ngram) && !ret.get(ret.size()-1).contains(ngram)) { 
				ret.add(ngram);
			}
			
		}
		
		return ret;
	}
	
	public List<String> getNNounGramsWithStopwords(int n) {
		List<String> ret = new ArrayList<String>();
		for(int i=0; i<size()-n; i++) {
			String ngram = "";
			for(int j=i; j<i+n; j++) {
				Token token = get(j);

				if (token.getLemma() == null) continue;
				if (token.getLemma().trim().length() < 1) continue;
				
				//System.out.println("XXXXX " + get(j).isStopword() + " " + token.getLemma() + " :: " + token.getPOS());
							
				if (isKomoran) {
					if (!token.isStopword() 
							&& (!token.getToken().startsWith("<") && !token.getToken().endsWith(">") &&
									(token.getPOS().startsWith("NN") || token.getPOS().startsWith("JJ"))) ) {
							ngram += token.getLemma().trim() + " ";
					}
				} else {
					if (!token.isStopword() 
							&& (!token.getToken().startsWith("<") && !token.getToken().endsWith(">"))
							&& (!token.getToken().startsWith("Punctuation") && !token.getToken().endsWith("Josa") &&
									!token.getPOS().startsWith("Foreign")) ) {
							ngram += token.getLemma() + " ";
					}
				}
			}	
			ngram = ngram.trim();
			if (ngram.length() > 0 && ret.size() < 1) {
				ret.add(ngram);
			} else if (ngram.length() > 0 && ret.size() >= 1 
					&& !ret.get(ret.size()-1).equals(ngram) && !ret.get(ret.size()-1).contains(ngram)) { 
				ret.add(ngram);
			}
			
		}
		
		return ret;
	}
	
	/**
	 * 
	 * @param tree
	 */
	public void getNounPhrases(Tree tree) 
	{   
        if (tree == null || tree.isLeaf()) {
            return;
         }
         //if node is a NP - Get the terminal nodes to get the words in the NP      
         if(tree.value().equals("NP") ) {
            List<Tree> leaves = tree.getLeaves();

            String phrase = "";
            for(Tree leaf : leaves) {
            	if (!leaf.toString().toLowerCase().equals("a") && !leaf.toString().toLowerCase().equals("an")
            			&& !leaf.toString().toLowerCase().equals("the")
            			&& !leaf.toString().toLowerCase().equals("et")
            			&& !leaf.toString().toLowerCase().equals("al")
            			&& !leaf.toString().toLowerCase().equals("al.")
            			) {
            		
            		
            		String _token = leaf.toString().trim().toLowerCase();		
	            	phrase += _token + " ";
            	}
            }
            
            if(phrase.split(" ").length > 1 && phrase.split(" ").length < 4) 
            {   	
    			String[] tokens = phrase.split("\\s+");
    			String new_phrase = "";
    			for (int j = 0; j < tokens.length; ++j) {
    				if (!stopwords.contains(tokens[j].trim())) {
    					new_phrase += tokens[j].trim() + " ";
    				} 
    			}    	
            	nounPhrases.add(new_phrase.trim());
            }
       }

       for(Tree child : tree.children()) {
    	   getNounPhrases(child);
       }
    }
    
	/**
	 * 
	 * @param chunker
	 */
	public void getNounPhraseByOpenNLP(ChunkerME chunker)
	{
		try {
			ArrayList<String> tagList = new ArrayList();
			ArrayList<String> n_sent = new ArrayList();
			
			for(int i = 0; i < size(); i++) {
				n_sent.add(get(i).getLemma());
				tagList.add(get(i).getPOS());
			}
			
			String[] a_sent = n_sent.toArray(new String[n_sent.size()]);
			String[] tags = tagList.toArray(new String[tagList.size()]);		
			String result[] = chunker.chunk(a_sent, tags);
			 
			String noun_phrase = "";
			int count = 0;
			for (String s : result) {
				
				if (s.equals("B-NP")) {
					if (noun_phrase.length() > 0) {
						nounPhrases.add(noun_phrase.trim());
						
						noun_phrase = "";
					}
					if (!stopwords.contains(a_sent[count].toLowerCase())) {
						noun_phrase += a_sent[count] + " ";
					}
				} else if (s.equals("I-NP")) {
					if (!stopwords.contains(a_sent[count].toLowerCase())) {
						noun_phrase += a_sent[count] + " ";
					}
				}	
				count++;
			}
			
			if (noun_phrase.length() > 0) {
				nounPhrases.add(noun_phrase.trim());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//recursively loop through tree, extracting noun phrases
	private void getNounPhrases(opennlp.tools.parser.Parse p) {

		if (p.getType().equals("NP")) { //NP=noun phrase
		    nounPhrases.add(p.getText());
		}
		    
		for (opennlp.tools.parser.Parse child : p.getChildren())
		         getNounPhrases(child);
	}
}



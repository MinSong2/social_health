package edu.yonsei.preprocess;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;
import java.util.Scanner;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;

public class EnglishPipeline extends Pipeline {

	//
	private Properties props;
	private StanfordCoreNLP pipeline = null;
	private ArrayList<String> stopSet;
	private Porter porter = null;
	ChunkerME chunkerModel = null;
	
	public EnglishPipeline() throws Exception
	{
		stopSet = new ArrayList<String>();
		porter = new Porter();
		
		props = new Properties();
		props.setProperty("parse.maxlen", "100");
		props.put("annotators", "tokenize, ssplit, pos, lemma");
		pipeline = new StanfordCoreNLP(props);
		
		// chunker
		try {
			InputStream is = new FileInputStream("model/en-chunker.bin");
			ChunkerModel chunker = new ChunkerModel(is);
			chunkerModel = new ChunkerME(chunker);
			
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	public EnglishPipeline(String stopFile) throws Exception
	{
		stopSet = new ArrayList<String>();
		porter = new Porter();
		try {
			initializeStopwords(stopFile);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		props = new Properties();
		//props.setProperty("parse.maxlen", "100");
		props.put("annotators", "tokenize, ssplit, pos, lemma");
		//props.put("annotators", "tokenize, ssplit, pos, lemma");
		pipeline = new StanfordCoreNLP(props);
		
		// chunker
		InputStream is = new FileInputStream("model/en-chunker.bin");
		ChunkerModel chunker = new ChunkerModel(is);
		chunkerModel = new ChunkerME(chunker);
	}
	
	public void initializeStopwords(String stopFile) throws Exception {
		Scanner s = new Scanner(new FileReader(stopFile));
		while(s.hasNext()) {
			String line = s.next();
			if (line.trim().length() < 1) continue;
			stopSet.add(line.trim().toLowerCase());
		}
		s.close();
		
		Collections.sort(stopSet);
	}
	
	public StanfordCoreNLP getStanfordCoreNLP()
	{
		return pipeline;
	}
	
	public Properties getProperty()
	{
		return props;
	}
	
	public ArrayList<String> getStopWords()
	{
		return stopSet;
	}
	
	public Porter getPorter()
	{
		return porter;
	}
	
	public ChunkerME  getChunker()
	{
		return chunkerModel;
	}
	
}

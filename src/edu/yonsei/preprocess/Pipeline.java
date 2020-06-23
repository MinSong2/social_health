package edu.yonsei.preprocess;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;
import java.util.Scanner;
import java.util.TreeSet;

import edu.stanford.nlp.pipeline.SentenceAnnotator;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.parser.Parser;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.util.InvalidFormatException;

public class Pipeline {

	public Pipeline() throws Exception
	{
	}
	
	public Pipeline(String stopFile) throws Exception
	{
	}

	public void initializeStopwords(String stopFile) throws Exception {
		// TODO Auto-generated method stub
		
	}

	public ArrayList<String> getStopWords() {
		// TODO Auto-generated method stub
		return null;
	}

	public StanfordCoreNLP getStanfordCoreNLP() {
		// TODO Auto-generated method stub
		return null;
	}

	public Porter getPorter() {
		// TODO Auto-generated method stub
		return null;
	}

	public SentenceDetectorME getSentenceDetector() {
		// TODO Auto-generated method stub
		return null;
	}

	public ChunkerME getChunker() {
		// TODO Auto-generated method stub
		return null;
	}

	
	

}

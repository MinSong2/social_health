package applications.social_health;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.Input2CharSequence;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TargetStringToFeatures;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceLowercase;
import cc.mallet.types.Alphabet;
import cc.mallet.types.FeatureSequence;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import edu.njit.util.SortByValueMap;
import edu.yonsei.util.TFIDFByMalletInstance;
import edu.yonsei.preprocess.EnglishPipeline;

import edu.yonsei.preprocess.Pipeline;
import edu.yonsei.topics.DMRTopicModel;
import edu.yonsei.topics.ParallelTopicModel;
import edu.yonsei.util.CooccurrenceHandler;
import edu.yonsei.util.Document;
import edu.yonsei.util.Sentence;
import gnu.trove.TObjectDoubleHashMap;

public class SocialHealthDataManager {

	// for creating mallet instances
	static Pattern tokenPattern = Pattern.compile("[\\p{L}\\p{N}\\p{Punct}\\S]+");

	CooccurrenceHandler coOccurrence = null;

	InstanceList instancesList = null;
	ArrayList<String> ngramList = new ArrayList();
	int nGramSize = 2;
	String language = "en";
	int docCount = 0;
	int sentCount = 0;
	
	HashMap<String,Integer> journalMap = new HashMap();
	
	public SocialHealthDataManager()
	{
		instancesList = new InstanceList(getPipe());
		coOccurrence = new CooccurrenceHandler(2);
	}
	
	public static Pipe getPipe() {
		Pipe instancePipe = new SerialPipes(new Pipe[] { 
				(Pipe) new Input2CharSequence("UTF-8"),
				(Pipe) new CharSequence2TokenSequence(tokenPattern),
				(Pipe) new TokenSequenceLowercase(),
				(Pipe) new TokenSequence2FeatureSequence() 
		});

		return instancePipe;
	}
	
	public void readCitingFile(String fileName) throws Exception
	{
		long startTime = System.currentTimeMillis();
		System.out.println("Process: " + fileName + "...");
		
		ArrayList<String> docList = new ArrayList();
		Scanner s = new Scanner(new File(fileName));
		while(s.hasNext()) {
			String line = s.nextLine();
			String[] split = line.split("\t");
			if (split.length < 2) continue;

			String doi  = split[0];
			String authors = split[1];
			String title = split[2];
			String journal = split[3];
			
			if (!journalMap.containsKey(journal)) {
				journalMap.put(journal,1);
			} else {
				journalMap.put(journal, journalMap.get(journal)+1);
			}
			
			String o_year = split[4]; 
			
			String abs = "";
			if (split.length > 5) {
				abs = split[5];
			}
			
			String year_info = "";
			if (split.length > 6) {
				year_info = split[6];
			}
			
			String text = title + " " + abs;
			docList.add(text.trim());
			
			System.out.println(doi + "\t" + authors + "\t" + title + "\t" + journal + "\t" + abs + "\t" + year_info);
			
		}
		s.close();
		
		// create Mallet instancelist
		ArrayList<Instance> instanceBuffer = new ArrayList<Instance>();

		long estimatedTime = System.currentTimeMillis() - startTime;
		double elapsedSeconds = estimatedTime / 1000.0;

		System.out.println("Time taken: " + elapsedSeconds + " :: " + docList.size());

		// configuration information
		String token_mode = "ngram"; // ngram or noun_phrase
		boolean isKomoran = true; // komoran or korean twitter

		String morphData = "";
		String stopword = "";
		if (System.getProperty("os.name").startsWith("Windows")) {
			morphData = "datas/";
			stopword = "data/stopwords/stopwords_en.txt";
		} else {
			morphData = "/home/tsmm/yTextMiner/datas/";
			stopword = "/home/tsmm/yTextMiner/data/util/stopwords.txt";
		}

		Pipeline pipe = null;
		if (language.equals("en")) {
			pipe = new EnglishPipeline(stopword);
		} 

		for (String text : docList) {
			Document document = new Document(text);
			
			document.preprocess(language, isKomoran, pipe);
			System.out.println("Document " + docCount);

			String doc = "";
			
			for (int j = 0; j < document.size(); ++j) {
				Sentence sentence = document.get(j);

				List<String> token_results = null;
				
				if (token_mode.equals("ngram")) {
					token_results = sentence.getNNounGrams(nGramSize);
				} else if (token_mode.equals("noun_phrase")) {
					sentence.clearNounPhrases();
					sentence.setStopWord(pipe.getStopWords());
					if (language.equals("en")) {
						sentence.getNounPhraseByOpenNLP(pipe.getChunker());
					} 
					token_results = sentence.getNounPhrases();
				}

				//System.out.println("token results: " + token_results);

				if (token_results.size() < 1)
					continue;

				String sent = "";
				for (int k = 0; k < token_results.size(); ++k) {
					String ngram = token_results.get(k);
					ngram = ngram.trim();

					if (!StringUtils.isAlphaSpace(ngram))
						continue;

					if (ngram.length() > 1) {
						ngram = ngram.replaceAll("\\s+", "_").toLowerCase() + " ";
						sent += ngram + " ";
					}
				}

				//doc += sent.trim() + " ";
				sentCount++;
				
				if (sent.trim().length() > 0) {
	
					doc += sent.trim() + " ";
				}
			}

			// insert document to instance
			Instance instance = new Instance(doc.trim(), null, String.valueOf(docCount), null);
			instanceBuffer.add(instance);
			
			docCount++;
			
			long estimatedTime1 = System.currentTimeMillis() - estimatedTime;
			elapsedSeconds = estimatedTime1 / 1000.0;
			System.out.println("Time taken for ngram and co-occurrence: " + elapsedSeconds);
		}

		instancesList.addThruPipe(instanceBuffer.iterator());		
	}
	
	public void readCitedFile(String fileName) throws Exception
	{
		HashMap<String,String> keywordList = new HashMap();
		long startTime = System.currentTimeMillis();
		System.out.println("Process: " + fileName + "...");
		
		HashMap<String,String> docList = new HashMap();
		Scanner s = new Scanner(new File(fileName));
		while(s.hasNext()) {
			String line = s.nextLine();
			line = line.replaceAll("\"", "");
			
			String[] split = line.split("\t");
			if (split.length < 11) continue;

			String authors = split[0];
			String title = split[2];
			String year = split[3];
			String journal = split[4];
			
			if (!journalMap.containsKey(journal)) {
				journalMap.put(journal,1);
			} else {
				journalMap.put(journal, journalMap.get(journal)+1);
			}
			
			String cited_by = split[11]; 
			
			String abs = "";
			if (split.length > 5) {
				abs = split[16];
			}

			String _author_keywords = split[17];
			String[] a_ks = _author_keywords.split(";");
			String author_keywords = "";
			for (String a : a_ks) {
				a = a.trim();
				a = a.replaceAll(" ", "_");
				author_keywords += a + " ";
			}
			author_keywords = author_keywords.trim();
			
			String _index_keywords = split[18];
			String[] i_ks = _index_keywords.split(";");
			String index_keywords = "";
			for (String i : i_ks) {
				i = i.trim();
				i = i.replaceAll(" ", "_");
				index_keywords += i + " ";
			}
			index_keywords = index_keywords.trim();
			
			String id = split[split.length-1];
			String keywords = author_keywords + " " + index_keywords;
			keywords = keywords.trim();
			if (keywords.length() > 0) {
				keywordList.put(id,keywords);
			}

			String text = title + " " + abs;
			docList.put(id,text.trim());
			
			System.out.println(title + "\t" + abs + "\t" + keywords);
			
		}
		s.close();
		
		// create Mallet instancelist
		ArrayList<Instance> instanceBuffer = new ArrayList<Instance>();

		long estimatedTime = System.currentTimeMillis() - startTime;
		double elapsedSeconds = estimatedTime / 1000.0;

		System.out.println("Time taken: " + elapsedSeconds + " :: " + docList.size());

		// configuration information
		String token_mode = "ngram"; // ngram or noun_phrase
		boolean isKomoran = true; // komoran or korean twitter

		String morphData = "";
		String stopword = "";
		if (System.getProperty("os.name").startsWith("Windows")) {
			morphData = "datas/";
			stopword = "data/stopwords/stopwords_en.txt";
		} else {
			morphData = "/home/tsmm/yTextMiner/datas/";
			stopword = "/home/tsmm/yTextMiner/data/util/stopwords.txt";
		}

		Pipeline pipe = null;
		if (language.equals("en")) {
			pipe = new EnglishPipeline(stopword);
		} 

		for (Map.Entry<String,String> entry : docList.entrySet()) {
			String id = entry.getKey();
			String text = entry.getValue();
			Document document = new Document(text);
			
			document.preprocess(language, isKomoran, pipe);
			System.out.println("Document " + docCount);

			String doc = "";
			
			for (int j = 0; j < document.size(); ++j) {
				Sentence sentence = document.get(j);

				List<String> token_results = null;
				
				if (token_mode.equals("ngram")) {
					token_results = sentence.getNNounGrams(nGramSize);
				} else if (token_mode.equals("noun_phrase")) {
					sentence.clearNounPhrases();
					sentence.setStopWord(pipe.getStopWords());
					if (language.equals("en")) {
						sentence.getNounPhraseByOpenNLP(pipe.getChunker());
					} 
					token_results = sentence.getNounPhrases();
				}

				//System.out.println("token results: " + token_results);

				if (token_results.size() < 1)
					continue;

				String sent = "";
				for (int k = 0; k < token_results.size(); ++k) {
					String ngram = token_results.get(k);
					ngram = ngram.trim();

					if (!StringUtils.isAlphaSpace(ngram))
						continue;

					if (ngram.length() > 1) {
						ngram = ngram.replaceAll("\\s+", "_").toLowerCase() + " ";
						sent += ngram + " ";
					}
				}

				//doc += sent.trim() + " ";
				sentCount++;
				
				if (sent.trim().length() > 0) {
	
					doc += sent.trim() + " ";
				}
			}
			
			if (keywordList.containsKey(id)) {
				doc += " " + keywordList.get(id);
			}

			// insert document to instance
			Instance instance = new Instance(doc.trim(), null, String.valueOf(docCount), null);
			instanceBuffer.add(instance);
			
			docCount++;
			
			long estimatedTime1 = System.currentTimeMillis() - estimatedTime;
			elapsedSeconds = estimatedTime1 / 1000.0;
			System.out.println("Time taken for ngram and co-occurrence: " + elapsedSeconds);
		}

		instancesList.addThruPipe(instanceBuffer.iterator());		
	}
	
	public void readFile(String fileName) throws Exception
	{
		HashMap<String,String> keywordList = new HashMap();
		long startTime = System.currentTimeMillis();
		System.out.println("Process: " + fileName + "...");
		
		HashMap<String,String> docList = new HashMap();
		
		try {
			Scanner s = new Scanner(new FileReader(new File(fileName)));
			s.nextLine();
			while(s.hasNext()) {
				String line = s.nextLine();
				line = line.replaceAll("\"", "");
				
				System.out.println("LINE " + line);
				
				String[] split = line.split("\t");
				if (split.length < 2) continue;
	
				String authors = split[0];
				String title = split[2];
				String year = split[3];
				String journal = split[4];
				
				if (!journalMap.containsKey(journal)) {
					journalMap.put(journal,1);
				} else {
					journalMap.put(journal, journalMap.get(journal)+1);
				}
				
				String cited_by = split[11]; 
				
				String abs = "";
				if (split.length > 5) {
					abs = split[16];
				}
	
				String _author_keywords = split[17];
				String[] a_ks = _author_keywords.split(";");
				String author_keywords = "";
				for (String a : a_ks) {
					a = a.trim();
					a = a.replaceAll(" ", "_");
					author_keywords += a + " ";
				}
				author_keywords = author_keywords.trim();
				
				String _index_keywords = split[18];
				String[] i_ks = _index_keywords.split(";");
				String index_keywords = "";
				for (String i : i_ks) {
					i = i.trim();
					i = i.replaceAll(" ", "_");
					index_keywords += i + " ";
				}
				index_keywords = index_keywords.trim();
				
				String id = split[split.length-1];
				String keywords = author_keywords + " " + index_keywords;
				keywords = keywords.trim();
				if (keywords.length() > 0) {
					keywordList.put(id,keywords);
				}
	
				String text = title + " " + abs;
				docList.put(id,text.trim());
				
				System.out.println(title + "\t" + abs + "\t" + keywords);
				
			}
			s.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// create Mallet instancelist
		ArrayList<Instance> instanceBuffer = new ArrayList<Instance>();

		long estimatedTime = System.currentTimeMillis() - startTime;
		double elapsedSeconds = estimatedTime / 1000.0;

		System.out.println("Time taken: " + elapsedSeconds + " :: " + docList.size());

		// configuration information
		String token_mode = "ngram"; // ngram or noun_phrase
		boolean isKomoran = true; // komoran or korean twitter

		String morphData = "";
		String stopword = "";
		if (System.getProperty("os.name").startsWith("Windows")) {
			morphData = "datas/";
			stopword = "data/stopwords/stopwords_en.txt";
		} else {
			morphData = "/home/tsmm/yTextMiner/datas/";
			stopword = "/home/tsmm/yTextMiner/data/util/stopwords.txt";
		}

		Pipeline pipe = null;
		if (language.equals("en")) {
			pipe = new EnglishPipeline(stopword);
		} 

		for (Map.Entry<String,String> entry : docList.entrySet()) {
			String id = entry.getKey();
			String text = entry.getValue();
			Document document = new Document(text);
			
			document.preprocess(language, isKomoran, pipe);
			System.out.println("Document " + docCount);

			String doc = "";
			
			for (int j = 0; j < document.size(); ++j) {
				Sentence sentence = document.get(j);

				List<String> token_results = null;
				
				if (token_mode.equals("ngram")) {
					token_results = sentence.getNNounGrams(nGramSize);
				} else if (token_mode.equals("noun_phrase")) {
					sentence.clearNounPhrases();
					sentence.setStopWord(pipe.getStopWords());
					if (language.equals("en")) {
						sentence.getNounPhraseByOpenNLP(pipe.getChunker());
					} 
					token_results = sentence.getNounPhrases();
				}

				//System.out.println("token results: " + token_results);

				if (token_results.size() < 1)
					continue;

				String sent = "";
				for (int k = 0; k < token_results.size(); ++k) {
					String ngram = token_results.get(k);
					ngram = ngram.trim();

					if (!StringUtils.isAlphaSpace(ngram))
						continue;

					if (ngram.length() > 1) {
						ngram = ngram.replaceAll("\\s+", "_").toLowerCase() + " ";
						sent += ngram + " ";
					}
				}

				//doc += sent.trim() + " ";
				sentCount++;
				
				if (sent.trim().length() > 0) {
	
					doc += sent.trim() + " ";
				}
			}
			
			if (keywordList.containsKey(id)) {
				doc += " " + keywordList.get(id);
			}

			// insert document to instance
			Instance instance = new Instance(doc.trim(), null, String.valueOf(docCount), null);
			instanceBuffer.add(instance);
			
			docCount++;
			
			long estimatedTime1 = System.currentTimeMillis() - estimatedTime;
			elapsedSeconds = estimatedTime1 / 1000.0;
			System.out.println("Time taken for ngram and co-occurrence: " + elapsedSeconds);
		}

		instancesList.addThruPipe(instanceBuffer.iterator());		
	}
	
	public void readPubMedFile(String fileName) throws Exception
	{
		HashMap<String,String> keywordList = new HashMap();
		long startTime = System.currentTimeMillis();
		System.out.println("Process: " + fileName + "...");
		
		HashMap<String,String> docList = new HashMap();
		
		try {
			Scanner s = new Scanner(new FileReader(new File(fileName)));
			s.nextLine();
			while(s.hasNext()) {
				String line = s.nextLine();
				line = line.replaceAll("\"", "");
				
				System.out.println("LINE " + line);
				
				String[] split = line.split("\t");
				if (split.length < 2) continue;
	
				String pmid = split[0];
				String text = split[split.length-1];

				docList.put(pmid,text.trim());
				
				System.out.println(pmid + "\t" + text);
				
			}
			s.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// create Mallet instancelist
		ArrayList<Instance> instanceBuffer = new ArrayList<Instance>();

		long estimatedTime = System.currentTimeMillis() - startTime;
		double elapsedSeconds = estimatedTime / 1000.0;

		System.out.println("Time taken: " + elapsedSeconds + " :: " + docList.size());

		// configuration information
		String token_mode = "ngram"; // ngram or noun_phrase
		boolean isKomoran = true; // komoran or korean twitter

		String morphData = "";
		String stopword = "";
		if (System.getProperty("os.name").startsWith("Windows")) {
			morphData = "datas/";
			stopword = "data/stopwords/stopwords_en.txt";
		} else {
			morphData = "/home/tsmm/yTextMiner/datas/";
			stopword = "/home/tsmm/yTextMiner/data/util/stopwords.txt";
		}

		Pipeline pipe = null;
		if (language.equals("en")) {
			pipe = new EnglishPipeline(stopword);
		} 

		for (Map.Entry<String,String> entry : docList.entrySet()) {
			String id = entry.getKey();
			String text = entry.getValue();
			Document document = new Document(text);
			
			document.preprocess(language, isKomoran, pipe);
			System.out.println("Document " + docCount);

			String doc = "";
			
			for (int j = 0; j < document.size(); ++j) {
				Sentence sentence = document.get(j);

				List<String> token_results = null;
				
				if (token_mode.equals("ngram")) {
					token_results = sentence.getNNounGrams(nGramSize);
				} else if (token_mode.equals("noun_phrase")) {
					sentence.clearNounPhrases();
					sentence.setStopWord(pipe.getStopWords());
					if (language.equals("en")) {
						sentence.getNounPhraseByOpenNLP(pipe.getChunker());
					} 
					token_results = sentence.getNounPhrases();
				}

				//System.out.println("token results: " + token_results);

				if (token_results.size() < 1)
					continue;

				String sent = "";
				for (int k = 0; k < token_results.size(); ++k) {
					String ngram = token_results.get(k);
					ngram = ngram.trim();

					if (!StringUtils.isAlphaSpace(ngram))
						continue;

					if (ngram.length() > 1) {
						ngram = ngram.replaceAll("\\s+", "_").toLowerCase() + " ";
						sent += ngram + " ";
					}
				}

				//doc += sent.trim() + " ";
				sentCount++;
				
				if (sent.trim().length() > 0) {
	
					doc += sent.trim() + " ";
				}
			}
			
			if (keywordList.containsKey(id)) {
				doc += " " + keywordList.get(id);
			}

			// insert document to instance
			Instance instance = new Instance(doc.trim(), null, String.valueOf(docCount), null);
			instanceBuffer.add(instance);
			
			docCount++;
			
			long estimatedTime1 = System.currentTimeMillis() - estimatedTime;
			elapsedSeconds = estimatedTime1 / 1000.0;
			System.out.println("Time taken for ngram and co-occurrence: " + elapsedSeconds);
		}

		instancesList.addThruPipe(instanceBuffer.iterator());		
	}
	
	public void collectTermStatistics(String prefix)
	{
		TFIDFByMalletInstance tfidf = new TFIDFByMalletInstance();
		int tfidf_threshold = 1;
		int freq_threshold = 2;
		int wordlength = 3;
		
		tfidf.genVocab(instancesList, prefix+"term_list.txt", tfidf_threshold, freq_threshold, wordlength);
	
		System.out.println("Done!");		
	}
	
	public void collectJournalStatistics(String prefix) throws IOException
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(prefix+"journals.txt")));
		
		HashMap<String,Integer> sorted = SortByValueMap.sortMapByValue(journalMap, false);
		for(Map.Entry<String, Integer> entry : sorted.entrySet()) {
			writer.write(entry.getKey() + "\t" + entry.getValue() + "\n");
		}
		writer.close();
		
	}
	
	public void runLDA(int noTopics, String topicFile, int numberWords, String modelFile) throws Exception
	{
		ParallelTopicModel model = new ParallelTopicModel(noTopics);
		model.setTopicDisplay(100, 50);
		model.setNumIterations(1500);
		model.setOptimizeInterval(1500/10);
		
		//model.setNumThreads(20);
		model.addInstances(instancesList);
		model.estimate();
		
		model.write(new File(modelFile));
		model.printTopWords(new File(topicFile), numberWords, false);
		
	
	}
	
	public void loadModel(String modelFile, String outputFile, String prefix) throws IOException
	{
		ParallelTopicModel model = null;
	       // Deserialization 
        try
        {    
            // Reading the object from a file 
            FileInputStream file = new FileInputStream(modelFile); 
            ObjectInputStream in = new ObjectInputStream(file); 
              
            // Method for deserialization of object 
            model = (ParallelTopicModel)in.readObject(); 
              
            
            in.close(); 
            file.close(); 
              
            System.out.println("Object has been deserialized "); 
        } catch(IOException ex) { 
            System.out.println("IOException is caught"); 
        } catch(ClassNotFoundException ex) 
        { 
        	System.out.println("ClassNotFoundException is caught"); 
        } 
        
        //model.printTopicWordWeights(new File(outputFile));
        
        File xml = new File(outputFile);
        String xml_file = xml.getName().replaceAll(".txt", "");
        PrintWriter printWriter = new PrintWriter(new File(xml_file + "_diagnostics.txt"));
        
        //model.printTopicWordWeights(printWriter);
        
        model.printDocumentTopics(new File(prefix + "doc_topic.txt"));
	}
	
	public void saveInstanceList(String instanceFile)
	{
		instancesList.save(new File(instanceFile));
	}
	
	public void loadInstanceList(String instanceFile)
	{
		instancesList = InstanceList.load(new File(instanceFile));
	}
	
	public void computeCooccurrences(File file) throws Exception {
		TObjectDoubleHashMap freq = getFrequency(instancesList);
		
		for (Instance instance : instancesList) {
			FeatureSequence original_tokens = (FeatureSequence) instance.getData();
			ArrayList<String> sentence = new ArrayList();
			for (int jj = 0; jj < original_tokens.getLength(); jj++) {
				String word = (String) original_tokens.getObjectAtPosition(jj);
				if (freq.get(word) < 50 && freq.get(word) >=5) {
					sentence.add(word);
				}
			}

			if (sentence.size() > 1) {
				coOccurrence.makeCooccurrenceVector(sentence);
			}
		}
		coOccurrence.printWithThreshold(file,3);
	}
	
	public static TObjectDoubleHashMap getFrequency (InstanceList data) {
		TObjectDoubleHashMap<String> freq = new TObjectDoubleHashMap<String> ();
		Alphabet alphabet = data.getAlphabet();
		for(int ii = 0; ii < alphabet.size(); ii++) {
			String word = alphabet.lookupObject(ii).toString();
			freq.put(word, 0);
		}

		for (Instance instance : data) {
			FeatureSequence original_tokens = (FeatureSequence) instance.getData();
			for (int jj = 0; jj < original_tokens.getLength(); jj++) {
				String word = (String) original_tokens.getObjectAtPosition(jj);
				freq.adjustValue(word, 1);
			}
		}
		
		System.out.println("Alphabet size: " + alphabet.size());
		System.out.println("Frequency size: " + freq.size());
		return freq;
	}
	
	public static void main(String[] args) throws Exception
	{
		String fileName = "./input/pubmed_results.txt";
		//mode is either pubmed, social_health, social_health_citing, social_health_cited
		String mode = "pubmed";
		
		SocialHealthDataManager manager = new SocialHealthDataManager();
		if (mode.equals("pubmed")) {
			manager.readPubMedFile(fileName);
		} else if (mode.equals("social_health")) {
			manager.readFile(fileName);
		} else if (mode.equals("social_health_cited")) {
			manager.readCitedFile(fileName);
		} else if (mode.equals("social_health_citing")) {
			manager.readCitingFile(fileName);
		}
		
		String prefix = "social_health_pubmed_";
		//manager.collectJournalStatistics(prefix);
		
		manager.collectTermStatistics(prefix);
		
		String cooccurrence_file = "social_health_cooccurrence.txt";
		manager.computeCooccurrences(new File(cooccurrence_file));
		
		String instanceFile = "social_health_pubmed.instance";
		manager.saveInstanceList(instanceFile);
		String topicFile = "social_health_pubmed_topics.txt";
		String modelFile = "social_health_pubmed.model";
		int numberWords = 30;
		manager.runLDA(20, topicFile, numberWords, modelFile);
		
		String outputFile = "social_health_pubmed_topic_term_weights.txt";
		manager.loadModel(modelFile, outputFile, prefix);
	}
}

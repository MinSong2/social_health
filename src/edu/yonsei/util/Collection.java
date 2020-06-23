package edu.yonsei.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Pattern;


import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.CharSequenceLowercase;
import cc.mallet.pipe.Input2CharSequence;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TargetStringToFeatures;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceLowercase;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.pipe.iterator.StringArrayIterator;
//import cc.mallet.topics.DMRTopicModel;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.FeatureSequence;
import cc.mallet.types.IDSorter;
import cc.mallet.types.InstanceList;


import edu.yonsei.preprocess.EnglishPipeline;

import edu.yonsei.preprocess.Pipeline;

import edu.yonsei.topics.DMRTopicModel;

public class Collection extends ArrayList<Document> {
	
	private static final long serialVersionUID = -283788963082278261L;
	private Topic[] topics;

	public Collection(List<String> list) throws Exception {
		topics = null;
		for(String str : list) {
			Document d = new Document(str);
			add(d);
		}
	}
	
	public Collection(List<String> docs, List<String> classes) throws Exception {
		for(int i=0; i<docs.size(); i++) {
			Document d = new Document(docs.get(i), classes.get(i));
			add(d);
		}
	}
	
	public Collection(List<Document> documents, boolean skip) {
		// TODO Auto-generated constructor stub
		topics = null;
		addAll(documents);
	}

	public int getCollectionSize()
	{
		return size();
	}

	
	public String getClassLabel(int index)
	{
		Document doc = get(index);
		
		return doc.getClassification();
	}
	
	public void preprocess(String mode, String stopword, String morphData) throws Exception {
    	Pipeline pipe = null;
    	if (mode.equals("en")) {
			pipe = new EnglishPipeline(stopword);
		} 
		boolean isKomoran = false;
		for(int i=0; i<size(); i++) {
			System.out.println("preprocessing document " + i);
			get(i).preprocess(mode,isKomoran,pipe);
		}
	}
	
	
	
	@SuppressWarnings("deprecation")
	public void createDMR(int noTopics, int iteration, int interval) throws Exception {
		ArrayList<Pipe> pipeList = new ArrayList<Pipe>();
		
		pipeList.add(new Input2CharSequence("UTF-8"));
		pipeList.add(new TargetStringToFeatures());
		pipeList.add(new CharSequence2TokenSequence(Pattern.compile("[\\p{L}\\p{N}_]+")));
		pipeList.add(new TokenSequenceLowercase());
		//pipeList.add(new TokenSequenceRemoveStopwords(new File("data/util/stopwords.txt"), "UTF-8", false, false, false));
		pipeList.add(new TokenSequence2FeatureSequence());
		
		InstanceList instances = new InstanceList(new SerialPipes(pipeList));
		
		ArrayList<cc.mallet.types.Instance> instanceBuffer = new ArrayList<cc.mallet.types.Instance>();
		
		for(int i=0; i<size(); i++) {
			String doc = get(i).getDocument();
			String feature = get(i).getClassification();
			
			instanceBuffer.add(new cc.mallet.types.Instance(doc, feature, i+"", null));
		}
		
		instances.addThruPipe(instanceBuffer.iterator());
		
		DMRTopicModel model = new DMRTopicModel(noTopics);
		
		model.setTopicDisplay(interval, 50);
		model.setNumIterations(iteration);
		model.setOptimizeInterval(iteration/10);
		
		model.addInstances(instances);
		model.estimate();
		
		model.write(new File("model/mallet/dmr.model"));
		Serialization.serialize(instances, "model/mallet/instances.model");
	}
	
	public void createDMRForPerplexity(int noTopics, int iteration, int interval, HashMap<String,String> testData) throws Exception {
		ArrayList<Pipe> pipeList = new ArrayList<Pipe>();
		
		pipeList.add(new Input2CharSequence("UTF-8"));
		pipeList.add(new TargetStringToFeatures());
		pipeList.add(new CharSequence2TokenSequence(Pattern.compile("[\\p{L}\\p{N}_]+")));
		pipeList.add(new TokenSequenceLowercase());
		//pipeList.add(new TokenSequenceRemoveStopwords(new File("data/util/stopwords.txt"), "UTF-8", false, false, false));
		pipeList.add(new TokenSequence2FeatureSequence());
		
		InstanceList instances = new InstanceList(new SerialPipes(pipeList));
		
		ArrayList<cc.mallet.types.Instance> instanceBuffer = new ArrayList<cc.mallet.types.Instance>();
		
		for(int i=0; i<size(); i++) {
			String doc = get(i).getDocument();
			String feature = get(i).getClassification();
			
			instanceBuffer.add(new cc.mallet.types.Instance(doc, feature, i+"", null));
		}
		
		instances.addThruPipe(instanceBuffer.iterator());
		
		DMRTopicModel model = new DMRTopicModel(noTopics);
		
		model.setTopicDisplay(interval, 50);
		model.setNumIterations(iteration);
		model.setOptimizeInterval(iteration/10);
		
		model.addInstances(instances);
		model.estimate();
		
		model.write(new File("model/mallet/dmr.model"));
		Serialization.serialize(instances, "model/mallet/instances.model");
		
		
		InstanceList testInstancesList = new InstanceList(new SerialPipes(pipeList));
		
		ArrayList<cc.mallet.types.Instance> testInstanceBuffer = new ArrayList<cc.mallet.types.Instance>();
		
		int i = 0;
		for(Map.Entry<String, String> entry : testData.entrySet()) {
			String doc = entry.getKey();
			String feature = entry.getValue();
			
			testInstanceBuffer.add(new cc.mallet.types.Instance(doc, feature, i+"", null));
			
			++i;
		}
		
		testInstancesList.addThruPipe(instanceBuffer.iterator());
        edu.yonsei.topics.MarginalProbEstimator evaluator = model.getProbEstimator();

        System.out.println("Loaded test instances");
        
        PrintStream docProbabilityStream = new PrintStream("docProbabilityFile.txt");
        double dTotalLogLikeliHood = evaluator.evaluateLeftToRight(instances, 10, false,
                docProbabilityStream);
        docProbabilityStream.close();
        
        int iTotalWords = 0;
        PrintStream doclengthsStream = new PrintStream("doclengths.txt");

        PrintWriter fResults_Perplexity = new PrintWriter("perplexity.txt");
        PrintStream fResults_Topics = new PrintStream("topics.txt");
          
        
        for (cc.mallet.types.Instance instance : testInstancesList) {
            if (!(instance.getData() instanceof FeatureSequence)) {
                System.err.println("DocumentLengths is only applicable to FeatureSequence objects "
                        + "(use --keep-sequence when importing)");
                System.exit(1);
            }

            FeatureSequence words = (FeatureSequence) instance.getData();
            doclengthsStream.println(words.size());
            iTotalWords += words.size();
        }
        doclengthsStream.close();
        double dPerplexity = Math.exp((-1.0 * dTotalLogLikeliHood) / iTotalWords);
        System.out.println("Perplexity:" + dPerplexity);
        fResults_Perplexity.println("TotalLogLikeliHood,iTotalWords,Perplexity");
        fResults_Perplexity.print(-1.0 * dTotalLogLikeliHood + ",");
        fResults_Perplexity.print(iTotalWords + ",");
        fResults_Perplexity.println(Double.toString(dPerplexity));
    
        fResults_Perplexity.close();
        fResults_Topics.close();
	}
	
	public void createDMRWithInstance(String instanceFile, int noTopics, int iteration, int interval) throws Exception
	{
		DMRTopicModel model = new DMRTopicModel(noTopics);
		
		model.setTopicDisplay(interval, 50);
		model.setNumIterations(iteration);
		model.setOptimizeInterval(iteration/10);
		
		InstanceList instances = InstanceList.load(new File(instanceFile));
		
		model.addInstances(instances);
		model.estimate();
		
		model.write(new File("model/mallet/dmr.model"));
	}
	
	public void createLDA(int noTopics, int iteration) throws Exception {
		ArrayList<Pipe> pipeList = new ArrayList<Pipe>();
		
		pipeList.add(new CharSequenceLowercase());
		pipeList.add(new CharSequence2TokenSequence(Pattern.compile("[\\p{L}\\p{N}_]+")));
		pipeList.add(new TokenSequenceRemoveStopwords(new File("data/util/stopwords.txt"), "UTF-8", false, false, false));
		pipeList.add(new TokenSequence2FeatureSequence());
		
		InstanceList instances = new InstanceList(new SerialPipes(pipeList));
		
		String[] array = new String[size()];
		for(int i=0; i<size(); i++)
			array[i] = get(i).getDocument();
		
		instances.addThruPipe(new StringArrayIterator(array));
		
		ParallelTopicModel model = new ParallelTopicModel(noTopics, 1.0, 0.01);
		
		model.addInstances(instances);
		model.setNumThreads(2);
		model.setNumIterations(iteration);

		model.estimate();
		
		PrintWriter pw = new PrintWriter("topic.xml");
		model.topicXMLReport(pw, 20);
		pw.close();
		
		model.printTopWords(new File("topwords.txt"), 30, true);
		
		model.write(new File("model/mallet/lda.model"));
		Serialization.serialize(instances, "model/mallet/instances.model");
		
		ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();
		topics = new Topic[noTopics];
		for(int i=0; i<noTopics; i++)
			topics[i] = new Topic(i, model.getAlphabet(), topicSortedWords.get(i).iterator());
	}
	
	
	public Topic[] getTopics() {
		return topics;
	}

}

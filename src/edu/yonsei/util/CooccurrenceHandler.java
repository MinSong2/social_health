package edu.yonsei.util;

import edu.njit.util.SortByValueMap;
import gnu.trove.TObjectIntHashMap;
import gnu.trove.TObjectIntIterator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.util.Pair;
import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;

public class CooccurrenceHandler {

	TObjectIntHashMap trie = null;
	
	ArrayList<String> wordList = new ArrayList();
	int coOccurrenceVectorSize = 2;
	public CooccurrenceHandler(int coOccurrenceVectorSize)
	{
		this.coOccurrenceVectorSize = coOccurrenceVectorSize;
		trie = new TObjectIntHashMap();
	}
	
	/**
	 * 
	 * @param words
	 */
	public void makeCooccurrenceVector(List<String> words)
	{
		ICombinatoricsVector<String> co_vector = Factory.createVector(words);
		
		// Create a simple combination generator to generate n-combinations of the initial vector
		Generator<String> gen = Factory.createSimpleCombinationGenerator(co_vector, coOccurrenceVectorSize);
		
		for (ICombinatoricsVector<String> combination : gen) {
			List<String> vec = combination.getVector();
			
			Pair<String,String> pair = Pair.create(vec.get(0), vec.get(1));
			
			if (!trie.containsKey(pair)) {
				trie.put(pair, new Integer(1));
			} else {
				trie.put(pair, (Integer)trie.get(pair)+1);
			}
		}
	}
	
	/**
	 * 
	 * @param inputFile
	 */
	public void makeCooccurrenceVector(String inputFile)
	{
		BufferedReader br = null;
		try {
			String line;
			br = new BufferedReader(new FileReader(inputFile));
			while ((line = br.readLine()) != null) {
				String[] lines = line.split("\\s+");
				List<String> _lines = Arrays.asList(lines); 
				makeCooccurrenceVector(_lines);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
		
	/**
	 * print helper function
	 */
	public void print()
	{
		TObjectIntIterator it = (TObjectIntIterator)trie.iterator();
		
		while (it.hasNext()) {
			it.advance();
				Pair pair = (Pair)it.key();
				Integer freq = it.value();
				System.out.println(pair.getFirst() + " : " + pair.getSecond() + " : " + freq);
			
		}
		//System.out.println(trie);
	}
	
	public TObjectIntHashMap getMap()
	{
		return trie;
	}
	
	/**
	 * print to file helper function
	 * @param outFile
	 */
	public void print(File outFile)
	{
		try {
			FileWriter fw = new FileWriter(outFile.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			TObjectIntIterator it = (TObjectIntIterator)trie.iterator();
			
			while (it.hasNext()) {
				it.advance();
				Pair pair = (Pair)it.key();
				Integer freq = it.value();
				//System.out.println(pair.getFirst() + "\t" + pair.getSecond() + "\t" + freq);
				if (freq >= 2) {
					bw.write(pair.getFirst() + "\t" + pair.getSecond() + "\t" + freq + "\n");
				}
			}
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void printWithThreshold(File outFile, int threshold)
	{
		try {
			FileWriter fw = new FileWriter(outFile.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			TObjectIntIterator it = (TObjectIntIterator)trie.iterator();
			
			while (it.hasNext()) {
				it.advance();
				Pair pair = (Pair)it.key();
				Integer freq = it.value();
				//System.out.println(pair.getFirst() + "\t" + pair.getSecond() + "\t" + freq);
				if (freq >= threshold) {
					if (!pair.getFirst().equals(pair.getSecond())) {
						bw.write(pair.getFirst() + "\t" + pair.getSecond() + "\t" + freq + "\n");
					}
				}
			}
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void printWithThresholdWithNoDuplicate(File outFile, int threshold)
	{
		try {
			FileWriter fw = new FileWriter(outFile.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			TObjectIntIterator it = (TObjectIntIterator)trie.iterator();
			
			HashMap<String,Integer> finalMap = new HashMap();
			
			while (it.hasNext()) {
				it.advance();
				Pair<String,String> pair = (Pair)it.key();
				Integer freq = it.value();
				String first =  pair.getFirst();
				String second = pair.getSecond();
				String key = first.trim() + "\t" + second.trim();
				String key1 = second.trim() + "\t" + first.trim();
				//System.out.println(pair.getFirst() + "\t" + pair.getSecond() + "\t" + freq);
				if (freq >= threshold) {
					if (!finalMap.containsKey(key) && (!finalMap.containsKey(key1))) {
						finalMap.put(key, freq);
					} else if (!finalMap.containsKey(key) && (finalMap.containsKey(key1))) {
						finalMap.put(key1, finalMap.get(key1)+freq);
					} 
					//else if (finalMap.containsKey(key) && (!finalMap.containsKey(key1))) {
						//finalMap.put(key, finalMap.get(key)+freq);
					//} 
					
					//if (finalMap.containsKey(key1)) {
						//System.out.println("Found " + key1);
						//finalMap.put(key, finalMap.get(key1)+freq);
					//}
					
				}
				
				
			}
			
			for (Map.Entry<String,Integer> entry : finalMap.entrySet()) {
				bw.write(entry.getKey() + "\t" + entry.getValue() + "\n");
			}
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public void printWithThresholdWithExcludedTerm(File outFile, String excludedTerm, 
			int threshold, ArrayList<String> keywords)
	{
		try {
			FileWriter fw = new FileWriter(outFile.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			TObjectIntIterator it = (TObjectIntIterator)trie.iterator();
			
			while (it.hasNext()) {
				it.advance();
				Pair pair = (Pair)it.key();
				Integer freq = it.value();
				//System.out.println(pair.getFirst() + "\t" + pair.getSecond() + "\t" + freq);
				if (freq >= threshold) {
					if (!pair.getFirst().equals(pair.getSecond()) && 
							(!pair.getFirst().equals(excludedTerm) && !pair.getSecond().equals(excludedTerm))
							) {
						if (keywords.contains(pair.getFirst()) || keywords.contains(pair.getSecond())) {
							bw.write(pair.getFirst() + "\t" + pair.getSecond() + "\t" + freq + "\n");
						}
					}
				}
			}
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void printWithThreshold(File outFile, String keyword, int threshold)
	{
		try {
			FileWriter fw = new FileWriter(keyword + outFile.getName());
			BufferedWriter bw = new BufferedWriter(fw);
			TObjectIntIterator it = (TObjectIntIterator)trie.iterator();
			
			HashMap<String,Integer> pairFreqMap = new HashMap();
			while (it.hasNext()) {
				it.advance();
				Pair pair = (Pair)it.key();
				Integer freq = it.value();
				//System.out.println(pair.getFirst() + "\t" + pair.getSecond() + "\t" + freq);
				if (freq >= threshold && (pair.getFirst().equals(keyword) || pair.getSecond().equals(keyword))
						) {
					String left = pair.getSecond() + "\t" + pair.getFirst();
					String right = pair.getFirst() + "\t" + pair.getSecond();
					if (!pair.getFirst().equals(pair.getSecond())) {
						String key = "";
						if (left.compareTo(right) < 0) {
							key = left;
						} else {
							key = right;
						}
						if (!pairFreqMap.containsKey(key)) {
							pairFreqMap.put(key, freq);
						}
					}
				}
			}
			
			System.out.println("Finish counting co-occurrence " + "...");
			
			HashMap<String,Integer> sorted = SortByValueMap.sortMapByValue(pairFreqMap, false);
			for (Map.Entry<String,Integer> entry : sorted.entrySet()) {	
				bw.write(entry.getKey() +"\t" + entry.getValue() + "\n");
			}
			
			
			bw.close();
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void print(File outFile, String termFile, int threshold)
	{
		try {
			
			HashMap<Pair<Integer,Integer>, Integer> idPairMap = new HashMap();
			TObjectIntIterator it = (TObjectIntIterator)trie.iterator();
			while (it.hasNext()) {
				it.advance();
				Pair pair = (Pair)it.key();
				Integer freq = it.value();
				//System.out.println(pair.getFirst() + "\t" + pair.getSecond() + "\t" + freq);
				if (freq >= threshold) {
					
					int f_idx = wordList.indexOf((String)pair.getFirst());
					if (f_idx < 0) {
						wordList.add((String)pair.getFirst());
						f_idx = wordList.size()-1;
					} 
					
					int s_idx = wordList.indexOf((String)pair.getSecond());
					if (s_idx < 0) {
						wordList.add((String)pair.getSecond());
						s_idx = wordList.size()-1;
					} 
					
					Pair<Integer,Integer> i_pair = null;
					if (f_idx > s_idx) {
						i_pair = new Pair(new Integer(f_idx), new Integer(s_idx));
					} else {
						i_pair = new Pair(new Integer(s_idx), new Integer(f_idx));
					}
					
					if (!idPairMap.containsKey(i_pair)) {
						idPairMap.put(i_pair, freq);
					} else {
						idPairMap.put(i_pair, idPairMap.get(i_pair)+freq);
					}
				}
			}
			
			FileWriter fw = new FileWriter(outFile.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			for (Map.Entry<Pair<Integer,Integer>, Integer> entry : idPairMap.entrySet()) {	
				bw.write(entry.getKey().getFirst() + "," + entry.getKey().getSecond() + "," + new Double(entry.getValue()).toString() + "\n");
			}		
			bw.close();
			
			FileWriter t_fw = new FileWriter(new File(termFile).getAbsoluteFile());
			BufferedWriter t_bw = new BufferedWriter(t_fw);
			for (int i = 0; i < wordList.size(); ++i) {
				t_bw.write(i + "," + wordList.get(i) + "\n");
			}
			t_bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void printWithLabel(File outFile)
	{
		try {
			FileWriter fw = new FileWriter(outFile.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			TObjectIntIterator it = (TObjectIntIterator)trie.iterator();
			
			bw.write("Source\tTarget\tCount\n");
			while (it.hasNext()) {
				it.advance();
				Pair pair = (Pair)it.key();
				Integer freq = it.value();
				System.out.println(pair.getFirst() + "\t" + pair.getSecond() + "\t" + freq);
				if (freq > 5) {
					bw.write(pair.getFirst() + "\t" + pair.getSecond() + "\t" + freq + "\n");
				}
			}
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * test function
	 */
	public void test()
	{
		// Create the initial vector
		ICombinatoricsVector<String> initialVector = Factory.createVector(
		      new String[] { "red", "black", "white", "green", "blue" } );

		// Create a simple combination generator to generate n-combinations of the initial vector
		Generator<String> gen = Factory.createSimpleCombinationGenerator(initialVector, 2);
		
		// Print all possible combinations
		for (ICombinatoricsVector<String> combination : gen) {
			System.out.println(combination.getVector());
			System.out.println(combination);
		}
	}
	
	public static void main(String[] args)
	{
		ArrayList words = new ArrayList();
		words.add("sdaaaa");
		words.add("aaaa");
		words.add("aadaa");
		words.add("adaaa");
		words.add("saaaa");
		words.add("sdaaaa");
		words.add("aafssaa");
		words.add("aaadsda");
		
		int co_occurence_vec_size = 2;
		CooccurrenceHandler ch = new CooccurrenceHandler(co_occurence_vec_size);
		ch.makeCooccurrenceVector(words);
		ch.print();
	}
}

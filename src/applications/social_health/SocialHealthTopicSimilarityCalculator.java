package applications.social_health;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.lang3.ArrayUtils;

import edu.njit.util.SortByValueMap;

public class SocialHealthTopicSimilarityCalculator {

	HashMap<String,HashMap<String,Double>> mapOne = new HashMap();
	HashMap<String,HashMap<String,Double>> mapTwo = new HashMap();
	
	public SocialHealthTopicSimilarityCalculator()
	{
	}
	
	public void readFirstTopicFile(String inputFile) throws Exception
	{
		HashMap<String,HashMap<String,Double>> topicTermVector = new HashMap();

		Scanner s = new Scanner(new FileReader(inputFile));
		while(s.hasNext()) {	
			String text = s.nextLine();	
										
			String[] split = text.split("\\s+");

			if (split.length < 3) continue;
			
			//System.out.println("LINE " + text);
			
			String _topic_id = split[0];
			String _term = split[1];
			_term = _term.trim();
			
			String _weight = split[2];
			Double weight = new Double(_weight.trim());
			
			if (!topicTermVector.containsKey(_topic_id.trim())) {
				HashMap<String,Double> map = new HashMap();
				map.put(_term,weight);
				topicTermVector.put(_topic_id, map);
			} else {
				topicTermVector.get(_topic_id.trim()).put(_term,new Double(_weight));
			}					
		}
		s.close();
		
		for (Map.Entry<String, HashMap<String,Double>> entry : topicTermVector.entrySet()) {
			String topic_id = entry.getKey();
			HashMap<String,Double> map = entry.getValue();
			HashMap<String,Double> sorted = SortByValueMap.sortMapByValue(map, false);
			
			HashMap<String,Double> new_map = new HashMap();
			int count = 0;
			int limit = 20;
			for (Map.Entry<String,Double> ent : sorted.entrySet()) {
				
				//System.out.println("LINE " + ent.getKey() + " : " + ent.getValue());
				
				if (limit < count) {
					break;
				}
				
				new_map.put(ent.getKey(), ent.getValue());
					
				count++;
			}
			
			mapOne.put(topic_id, new_map);
		}
	}
	
	public void readSecondTopicFile(String inputFile) throws Exception
	{
		HashMap<String,HashMap<String,Double>> topicTermVector = new HashMap();

		Scanner s = new Scanner(new FileReader(inputFile));
		while(s.hasNext()) {	
			String text = s.nextLine();	
										
			String[] split = text.split("\\s+");

			if (split.length < 3) continue;
			
			String _topic_id = split[0];
			String _term = split[1];
			_term = _term.trim();
			
			String _weight = split[2];
			Double weight = new Double(_weight.trim());
			
			if (!topicTermVector.containsKey(_topic_id.trim())) {
				HashMap<String,Double> map = new HashMap();
				map.put(_term,weight);
				topicTermVector.put(_topic_id, map);
			} else {
				topicTermVector.get(_topic_id.trim()).put(_term,new Double(_weight));
			}					
		}
		s.close();
		
		for (Map.Entry<String, HashMap<String,Double>> entry : topicTermVector.entrySet()) {
			String topic_id = entry.getKey();
			HashMap<String,Double> map = entry.getValue();
			HashMap<String,Double> sorted = SortByValueMap.sortMapByValue(map, false);
			
			HashMap<String,Double> new_map = new HashMap();
			int count = 0;
			int limit = 20;
			for (Map.Entry<String,Double> ent : sorted.entrySet()) {
				
				if (limit < count) {
					break;
				}
				
				new_map.put(ent.getKey(), ent.getValue());
				count++;
			}
			
			mapTwo.put(topic_id, new_map);
		}
	}
	
	public void computeSimilarity()
	{
		System.out.println("Positive topics ");
		for (Map.Entry<String, HashMap<String,Double>> entry : mapOne.entrySet()) {
			String topic_id = entry.getKey();
			HashMap<String,Double> vector_a = entry.getValue();
			HashSet<String> words = new HashSet(vector_a.keySet());
			
			for (Map.Entry<String, HashMap<String,Double>> ent : mapTwo.entrySet()) {
				String _topic_id = ent.getKey();
				HashMap<String,Double> vector_b = ent.getValue();
				words.addAll(vector_b.keySet());
				
				ArrayList<Double> a = new ArrayList();
				ArrayList<Double> b = new ArrayList();
				
				makeVector(a,b,words, vector_a, vector_b);
				
				Double[] d_a = a.toArray(new Double[a.size()]);
				double[] da = ArrayUtils.toPrimitive(d_a);
				
				Double[] d_b = b.toArray(new Double[b.size()]);
				double[] db = ArrayUtils.toPrimitive(d_b);
				
				double similarity = cosineSimilarity(da,db);
				if (similarity >= 0.3) {		
					System.out.println(topic_id + " : " + _topic_id  + "==" + similarity);
					//System.out.println(a);
					//System.out.println(b);
				}
			}
		}
	}
	
	public void computeWithinSimilarity()
	{
		System.out.println("Positive topics ");
		for (Map.Entry<String, HashMap<String,Double>> entry : mapOne.entrySet()) {
			String topic_id = entry.getKey();
			HashMap<String,Double> vector_a = entry.getValue();
			HashSet<String> words = new HashSet(vector_a.keySet());
			
			for (Map.Entry<String, HashMap<String,Double>> ent : mapOne.entrySet()) {
				String _topic_id = ent.getKey();
				
				if (topic_id == _topic_id) continue;
				
				HashMap<String,Double> vector_b = ent.getValue();
				words.addAll(vector_b.keySet());
				
				ArrayList<Double> a = new ArrayList();
				ArrayList<Double> b = new ArrayList();
				
				makeVector(a,b,words, vector_a, vector_b);
				
				Double[] d_a = a.toArray(new Double[a.size()]);
				double[] da = ArrayUtils.toPrimitive(d_a);
				
				Double[] d_b = b.toArray(new Double[b.size()]);
				double[] db = ArrayUtils.toPrimitive(d_b);
				
				double similarity = cosineSimilarity(da,db);
				if (similarity >= 0.3) {		
					System.out.println(topic_id + " : " + _topic_id  + "==" + similarity);
					//System.out.println(a);
					//System.out.println(b);
				}
			}
		}
	}
	
	private void makeVector(ArrayList<Double> a, ArrayList<Double> b,
			HashSet<String> words, HashMap<String,Double> vectorA, HashMap<String,Double> vectorB)
	{
		for (String word : words) {
			Double scoreA = vectorA.get(word);
			if (scoreA == null) {
				scoreA = 0.0;
			}
			a.add(scoreA);
			
			Double scoreB = vectorB.get(word);
			if (scoreB == null) {
				scoreB = 0.0;
			}
			b.add(scoreB);
		}
	}
	
	public double cosineSimilarity(double[] docVector1, double[] docVector2) {
        double dotProduct = 0.0;
        double magnitude1 = 0.0;
        double magnitude2 = 0.0;
        double cosineSimilarity = 0.0;

        for (int i = 0; i < docVector1.length; i++) //docVector1 and docVector2 must be of same length
        {
            dotProduct += docVector1[i] * docVector2[i];  //a.b
            magnitude1 += Math.pow(docVector1[i], 2);  //(a^2)
            magnitude2 += Math.pow(docVector2[i], 2); //(b^2)
        }

        magnitude1 = Math.sqrt(magnitude1);//sqrt(a^2)
        magnitude2 = Math.sqrt(magnitude2);//sqrt(b^2)

        if (magnitude1 != 0.0 | magnitude2 != 0.0) {
            cosineSimilarity = dotProduct / (magnitude1 * magnitude2);
        } else {
            return 0.0;
        }
        return cosineSimilarity;
    }
	
	public static void main(String[] args) throws Exception
	{
		String inputFile1 = "social_health_citing_topic_term_weights.txt";
		String inputFile = "social_health_topic_term_weights.txt";
		SocialHealthTopicSimilarityCalculator calculator = new SocialHealthTopicSimilarityCalculator();
		calculator.readFirstTopicFile(inputFile);
		//calculator.readSecondTopicFile(inputFile1);
		//calculator.computeSimilarity();
		calculator.computeWithinSimilarity();
	}
}

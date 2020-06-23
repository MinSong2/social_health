package applications.social_health;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import edu.njit.util.SortByValueMap;

public class PubMedResultParser {

    public static void main(String[] args) throws Exception {

    	HashMap<String,Integer> journalMap = new HashMap();
        String csvFile = "./input/csv-socialheal-set.csv";
        Reader in = new FileReader(csvFile);
        Iterable<CSVRecord> records = CSVFormat.RFC4180.withSkipHeaderRecord(false).parse(in);
        for (CSVRecord record : records) {
           String id = record.get(0);
           String journal = record.get(5);
           
           if (!journalMap.containsKey(journal)) {
        	   journalMap.put(journal, 1);
           } else {
        	   journalMap.put(journal, journalMap.get(journal) + 1);
           }
           System.out.println(id + " :: " + journal);
        }
        
        HashMap<String,Integer> sorted = SortByValueMap.sortMapByValue(journalMap, false);
        for (Map.Entry<String, Integer> ent : sorted.entrySet()) {
        	System.out.println(ent.getKey() + "\t" + ent.getValue());
        }
        	
    }

}

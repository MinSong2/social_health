package edu.yonsei.input;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class PubMedIDDataHandler extends DefaultHandler {
	

	private int    curIndex = 0;
	private String key      = null;

	boolean isIdList = false;
	
	String count = null;
	boolean isCount = false;
	private int numOccurrences = 0;
	private ArrayList<String> idList;
	public PubMedIDDataHandler() {
		super();
		idList = new ArrayList();
	}
	
	public int getNumOccurrencesProcessed() { return numOccurrences; }
	
	public void startElement(String namespaceURI, 
							 String localName,
							 String qualifiedName, 
							 Attributes atts) 
			throws SAXException 
	{
		if(localName.equals("Count")) {
			isCount = true;
			curIndex++;
		} else if(localName.equals("RetMax")) {
			
		} else if(localName.equals("RetStart")) {
			
		} else if(localName.equals("IdList")) {
			curIndex = 1;
			isIdList = true;
			key = atts.getValue("Id");
		} //else if(localName.equals("Id")) {
			//key = atts.getValue("Id");
			//System.out.println("Id: " + key);
		//}
	}
	
	public void endElement(String namespaceURI, 
						   String localName,
						   String qualifiedName) 
			throws SAXException 
	{
		if(localName.equals("IdList")) {
			isIdList = false;
			//System.out.println("Id: " + key);
			numOccurrences++;
		} else if(localName.equals("Id")) {
			
			idList.add(key);
			//System.out.println("Id: " + key);
			//numOccurrences++;
		} else if(localName.equals("Count")) {
			if (curIndex == 1) {
				System.out.println("XX: " + count);
			}
			isCount = false;
		}
	}	
	
	public ArrayList<String> getIdList() { return idList; }
	
	public void characters(char[] ch, int start, int length) 
	{
		StringBuffer s = new StringBuffer();
		for(int i=start; i<start+length; i++) {
			s.append(ch[i]);
		}
		
		key = s.toString();
		
		if (isCount && curIndex==1) {
			count = s.toString();
		}
	}

	public String getCount()
	{
		return count;
	}
 
}

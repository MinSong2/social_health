package edu.yonsei.input;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang.StringEscapeUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;


public class PubMedHandler extends DefaultHandler {
	
	private final Stack<String> tagsStack = new Stack<String>();
	  
    HashMap<String,Integer> mesh_map = new HashMap();
    
    ArrayList<PubMedBean> contents = new ArrayList();
    
	private int    curIndex = 0;
	private String key      = null;
	private String title = null;
	private int numOccurrences = 0;
	private LinkedList idList;
	private boolean isAbstract = false;
	private boolean isTitle = false;
	private String id = null;
	private boolean isFirstName = false;
	private boolean isLastName = false;
	private boolean isInitial = false;
	private boolean isAffiliation = false;
	private boolean isJournalTitle = false;
	private boolean isPID = false;
	private boolean isYear = false;
	boolean isAuthor = false;
	private boolean isJournalIssue = false;
	boolean isMedlineCitation = false;
	boolean isAuthorList = false;
	
	String firstName = null;
	String lastName = null;
	String initial = null;
	String affiliation = null;
	String journalTitle = null;
	ArrayList<Author> authors = null;
	Author author = null;
	String year = null;
	
	boolean isDesc = false;
	boolean isQualifier = false;
	String desc = null;
	String qual = null;
	HashMap<String,String> pidYearMap = null;
	
	public PubMedHandler() {
		super();
		authors = new ArrayList();
		key = new String();
		desc = new String();
		qual = new String();
		journalTitle = "";
		title = "";
		pidYearMap = new HashMap();
	}
	
	private void pushTag(String tag) {
        tagsStack.push(tag);
    }

    private String popTag() {
        return tagsStack.pop();
    }

    private String peekTag() {
        return tagsStack.peek();
    }
    
	public int getNumOccurrencesProcessed() { return numOccurrences; }
	
	public void startElement(String namespaceURI, 
							 String localName,
							 String qualifiedName, 
							 Attributes atts) 
			throws SAXException 
	{
		//pushTag(qualifiedName);
		
		if (localName.equals("PubmedArticleSet")) {
			
		} else if (localName.equals("PubmedArticle")) {
		} else if (localName.equals("MedlineCitation")) { //MedlineCitation
			curIndex = 0;
		} else if (localName.equals("PMID")) {
			if (id == null) {
				isPID = true;
			}
			
		} else if (localName.equals("Article")) {
			curIndex++;		
		} else if (localName.equals("Journal")) {
		
		} else if (localName.equals("JournalIssue")) {
			isJournalIssue = true;
		} else if (localName.equals("PubDate")) {
			
		} else if (localName.equals("MedlineDate")) {
			if (year == null) {
				isYear = true;
			}
		} else if (localName.equals("Year")) {
			if (year == null) {
				isYear = true;
			}
		} else if (localName.equals("Title")) {
			isJournalTitle = true;
		} else if (localName.equals("ISOAbbreviation")) {
			
		} else if (localName.equals("ArticleTitle")) {
			isTitle = true;
		} else if (localName.equals("Abstract")) {
			
		} else if (localName.equals("AbstractText")) {
			isAbstract = true;
		} else if (localName.equals("Affiliation")) {
			isAffiliation = true;
		} else if (localName.equals("AuthorList")) {
			isAuthorList = true;
		} else if (localName.equals("Author")) {
			author = new Author();
			isAuthor = true;
		} else if (localName.equals("LastName")) {
			isLastName = true;
		} else if (localName.equals("ForeName")) {
			isFirstName = true;
		} else if (localName.equals("Initials")) {
			isInitial = true;
		} else if (localName.equals("MeshHeadingList")) {
		
		} else if (localName.equals("MeshHeading")) {
			
		} else if (localName.equals("DescriptorName")) {
		    isDesc = true;
		} else if (localName.equals("QualifierName")) {
			isQualifier = true;
		} else {
			//System.out.println("Don't know what to do with start element:  " + localName);
		}
	}
	
	public void endElement(String namespaceURI, 
						   String localName,
						   String qualifiedName) 
			throws SAXException 
	{
		/**
        String tag = peekTag();
        if (!qualifiedName.equals(tag)) {
            //throw new InternalError();
        }
        
        popTag();
        String parentTag = peekTag();
        **/
		
		if (localName.equals("Initials")) {
			if (author != null) author.setInitial(initial);
			isInitial = false;
			
		} else if (localName.equals("ForeName")) {
			//if (author != null && firstName.length() > 0) author.setFirstName(firstName);
			isFirstName = false;
			//firstName = null;
		} else if (localName.equals("LastName")) {	
			//author.setLastName(lastName);
			//if (author != null && lastName.length() > 0) author.setLastName(lastName);
			isLastName = false;
			//lastName = null;
		} else if (localName.equals("Affiliation")) {
			
			if (affiliation != null && affiliation.length() > 0 && author != null) {
				affiliation = affiliation.replaceAll("null","");
				//System.out.println("Affiliation: " + affiliation);
				author.setAffiliation(StringEscapeUtils.escapeXml(affiliation.trim()));
			}
			
			affiliation = null;
			
			isAffiliation = false;	
		} else if (localName.equals("Author")) {

  			if (author != null) {
  				if (firstName != null && !firstName.equals("null")) {
  					firstName = firstName.replaceAll("null","");
  					author.setFirstName(firstName);
  				}
  				if (lastName != null && !lastName.equals("null")) {
  					lastName = lastName.replaceAll("null","");
  					author.setLastName(lastName);
  					//System.out.println("LastName: " + lastName);
  				}
				//System.out.println(" *** " + author.getLastName() + " : "  + author.getFirstName() + " : "+ author.affiliation());	
				authors.add(author);
			}
			
  			
  			
  			isAuthor = false;
			author = null;
			lastName = null;
			firstName = null;
		} else if (localName.equals("AuthorList")) {
			isAuthorList = false;	
		} else if(localName.equals("AbstractText")) {
			isAbstract = false;
			//System.out.println("Abs: " + key);
		} else if(localName.equals("Abstract")) {
			numOccurrences++;
		} else if (localName.equals("ArticleTitle")) {
			isTitle = false;
			//System.out.println("Title: " + title);
		} else if (localName.equals("ISOAbbreviation")) {
			//
		} else if(localName.equals("Title")) {
			isJournalTitle = false;
		} else if (localName.equals("Year")) {
			isYear = false;
		} else if (localName.equals("MedlineDate")) {
			isYear = false;
		} else if (localName.equals("PubDate")) {
			
		} else if (localName.equals("JournalIssue")) {
			isJournalIssue = false;
		} else if(localName.equals("Journal")) {
				
		} else if (localName.equals("Article")) {
			
		} else if(localName.equals("MedlineCitation")) {
		} else if (localName.equals("PMID")) {
			isPID = false;
		} else if (localName.equals("MeshHeadingList")) {
			
		} else if (localName.equals("MeshHeading")) {
			
		} else if (localName.equals("DescriptorName")) {
			if (desc.length() > 0) {
				addMesh(desc);
				//System.out.println("Mesh Descriptor: " + desc);
			}
			desc = "";
			isDesc = false;
			
		} else if (localName.equals("QualifierName")) {
			if (qual.length() > 0) {
				addMesh(qual);
				//System.out.println("Mesh Qualifier: " + qual);
			}
			qual = "";
			isQualifier = false;
			
		} else if (localName.equals("PubmedArticle")) {	
			try {
				
				//&, <, >, ", '
			
				PubMedBean pcb = new PubMedBean();
				if (key != null) {
					key = key.replaceAll("[\\t\\r\\n]", "");
					//pcb.setAbs(StringEscapeUtils.escapeXml(key.trim()));
					pcb.setAbs(key.trim().replaceAll("[&<>\"\']+", ""));
				}
				
				id = id.trim();
				year = year.trim();
				//System.out.println("PMID: " + id + " :: " + year);	
				
				pidYearMap.put(id, year);
				
				//pcb.setTitle(StringEscapeUtils.escapeXml(title.trim()));
				pcb.setTitle(title.trim().replaceAll("[&<>\"\']+", ""));
				pcb.setPMID(id);
				pcb.setYear(year);
				pcb.setAuthorList(new ArrayList<Author>(authors));
				pcb.setJournalTitle(StringEscapeUtils.escapeXml(journalTitle));	
				pcb.setMeshMap(new HashMap<String,Integer>(mesh_map));
				
				if (!contents.contains(pcb)) {
					contents.add(pcb);
				}
				
				//reset the variables
				id = null;
				key = "";
				title = "";
				journalTitle = "";
				year = null;

				authors.clear();
				mesh_map.clear();
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
		} else if (localName.equals("PubmedArticleSet")) {
			
		} else {
			//System.out.println("Don't know what to do with end element:  " + localName);
		}
	}	
	
	public LinkedList getIdList() { return idList; }
	
	public HashMap<String,String> getidYearMap() { return pidYearMap; }
	
	public void characters(char[] ch, int start, int length) 
	{
			StringBuffer s = new StringBuffer();
			for(int i=start; i<start+length; i++) {
				s.append(ch[i]);
			}
						
			if (isAbstract) {
				String abs = s.toString();
				key += abs + " ";
				//System.out.println("Abstract: " + key);
			} 
			if (isAffiliation) {
				if (s.toString() != null) {
					affiliation += s.toString().trim() + " ";		
					//System.out.println("Affiliation: " + affiliation);
				}
			} 
			
			if (isYear && year == null) {
				year = s.toString();
				//System.out.println("Year: " + year);
			}
			
			if (isJournalTitle) {
				journalTitle += s.toString();
				//System.out.println("Journal Title: " + journalTitle);
			} 
			
			if (isTitle) {
				String ti = s.toString();
				title += ti + " ";
				//System.out.println("Title: " + title);
			} 
			
			if (isFirstName && isAuthor) {
				
				firstName += s.toString().trim();
				//System.out.println("FirstName: " + firstName);
			} 
			if (isLastName && isAuthor) {
				////if (s.toString() != null) {
					lastName += s.toString().trim();
					//System.out.println("LastName: " + lastName);
				//}
			} 
			
			if (isInitial && initial == null) {
				//System.out.println("Initial: " + initial);
				initial = s.toString();
			} 
			if (isDesc) {
				if (desc.length() < 1) {
					desc = s.toString();
					desc = desc.trim();
					//System.out.println("DESCRIPTOR: " + desc);
				}
			} 
			if (isQualifier) {
				
				if (qual.length() < 1) {
					qual = s.toString();
					qual = qual.trim();
					//System.out.println("QUALIFIER: " + qual);
				}
			} 
			
			if (isPID && id == null) {
				id = s.toString();
				//System.out.println("ID: " + id);
			} 
	}

	public String getYear()
	{
		return year;
	}
	
	public String getTitle()
	{
		return title;
	}
	
	public String getAbstract()
	{
		return key;
	}
	
	public String getContent()
	{
		return title + ". " + key;
	}
	
	public ArrayList<PubMedBean> getContents()
	{
		return contents;
	}
		
	public void addMesh(String mesh)
	{
        if (mesh_map.containsKey(mesh)) { 
            // get number of occurrences for this word 
            // increment it  
            // and put back again  
        	mesh_map.put(StringEscapeUtils.escapeXml(mesh), mesh_map.get(mesh) + 1);
        } else { 
            // this is first time we see this word, set value '1' 
        	mesh_map.put(StringEscapeUtils.escapeXml(mesh), 1);
        } 	
	}
	
	public HashMap<String,Integer> getMeshMap()
	{
		return mesh_map;
	}
		
	public static void main(String [] args) throws IOException
	{
		XMLReader documentReader = null;
		try {
			documentReader = XMLReaderFactory.createXMLReader(
					"org.apache.xerces.parsers.SAXParser"
					);
		} 
		catch(SAXException e) {
			System.err.println("Couldn't create an XMLReader");
			e.printStackTrace();

		}
	
	    PubMedHandler ph = new PubMedHandler();
		
	    String inputFile = "pubmed_results.csv";
	    FileInputStream in = null;
		try {
			in = new FileInputStream(inputFile);
 
			InputSource input = new InputSource(in);	 
			documentReader.setContentHandler(ph);

			documentReader.parse(input);
			
			System.out.println("*********** " + ph.getContents().get(0).getPMID() + " " +ph.getContents().get(0).getTitle() +
					" **** " + ph.getContents().get(1).getPMID() + " " +ph.getContents().get(1).getTitle() );
			
			in.close();	
		} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}	
		
		FileWriter fstream = new FileWriter(args[1]);
		BufferedWriter out = new BufferedWriter(fstream);

		ArrayList<PubMedBean> beans = ph.getContents();
		for (PubMedBean bean : beans) {
			String pmid = bean.getPMID();
			String title = bean.getTitle();
			title = title.replaceAll("null", "");
			title = title.trim();
			String abs = bean.getAbs();
			abs = abs.replaceAll("null", "");
			String content = "";
			
			if (title.length() > 0) {
				content += title;
			}
			if (abs.trim().length() > 0) {
				content += " " + abs;
			}
			
			String author_list = "";
			ArrayList<Author> authors = bean.getAuthorList();
			for (Author a_author : authors) {
				
				String aff = a_author.affiliation();
				String a_author_first_name = a_author.getFirstName();
				String a_author_last_name = a_author.getLastName();
				
				if (aff == null) {
					aff = "NONE";
				}
				if (aff.length() < 1) {
					aff = "NONE";
				}
				
				if (a_author_last_name.length() > 0 && a_author_first_name.length() > 0) {
					author_list += a_author_first_name + " " + a_author_last_name + "\t" + aff + "\t";
				}
			}
			
			author_list = author_list.trim();
			content = content.trim();
			
			out.write(pmid + "\t" + author_list + "\t" + content + "\n");
		}
		
		out.close();
	}
}

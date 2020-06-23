 package edu.yonsei.input;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.TreeSet;

public class PubMedBean {

	LinkedList<String> titleList = null;
	String pmId = null;
	String title = null;
	String abs = null;
	LinkedList<String> pmcIds = null;
	String journalTitle = null;
	String year = null;
	String keywords = null;
	TreeSet<String> coAuthors = null;
	HashMap<String,Integer> meshList = null;
	ArrayList<String> meshs = null;
	ArrayList<Author> authorList = null;
	
	public PubMedBean()
	{	
		meshs = new ArrayList();
	}
	
	public void setPMID(String pmId)
	{
		this.pmId = pmId; 
	}
	
	public String getPMID()
	{
		return pmId;
	}
	
	public void setTitle(String title)
	{
		this.title = title;
	}
	
	public String getTitle()
	{
		return title;
	}

	public void setKeywords(String keywords)
	{
		this.keywords = keywords;
	}
	
	public String getKeywords()
	{
		return keywords;
	}
	
	public void setAbs(String abs)
	{
		this.abs = abs;
	}
	
	public String getAbs()
	{
		return abs;
	}
	
	public void setJournalTitle(String journalTitle)
	{
		this.journalTitle = journalTitle;
	}
	
	public String getJournalTitle()
	{
		return journalTitle;
	}
	
	public void setCoAuthors(TreeSet<String> coAuthors)
	{
		this.coAuthors = coAuthors;
	}
	
	public TreeSet<String> getCoAuthors()
	{
		return coAuthors;
	}
	
	public void setYear(String year)
	{
		this.year = year;
	}
	
	public String getYear()
	{
		return year;
	}
	
	public void setAuthorList(ArrayList<Author> authorList)
	{
		this.authorList = authorList;
	}
	
	public ArrayList<Author> getAuthorList()
	{
		return authorList;
	}
	
	public void setMeshMap(HashMap<String,Integer> meshList)
	{
		this.meshList = meshList;
	}
	
	public HashMap<String,Integer> getMeshMap()
	{
		return meshList;
	}
	
	public void setMeshList(ArrayList<String> meshs)
	{
		this.meshs.addAll(meshs);
	}
	
	public ArrayList<String> getMeshList()
	{
		return meshs;
	}
	
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("Title " + title + "\n");
		sb.append("PMID " + pmId + "\n");
		sb.append("ABSTRACT " + abs + "\n");
		sb.append("JOURNAL_TITLE " + journalTitle);
		return sb.toString();
	}
}

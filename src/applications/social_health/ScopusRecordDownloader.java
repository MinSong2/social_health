package applications.social_health;

import org.apache.commons.lang3.tuple.Pair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

public class ScopusRecordDownloader {

	public ScopusRecordDownloader()
	{
	}
	
	public Pair<String,String> search(String title, String author)
	{
		Pair<String,String> outcome = null;
		String url = "https://www.scopus.com/results/results.uri?sort=plf-f&src=s&sid=be79a42dedd2fb751da5bd403cf88cb0&sot=a"
				+ "&sdt=a&sl=91&s=TITLE-ABS-KEY%28" + title + "%29+AND+AUTHOR-NAME%28" + author + "%29&origin=searchadvanced&editSaveSearch=&txGid=8a898ffe6935a0a21739404da91f13e5";
			
		WebDriver driver = null;
		
		String abs = "";
		String year_info = "";
		try
		{
			// Initiate HtmlUnitDriver object.
			driver = new HtmlUnitDriver();

			driver.get(url);
				
			String result = driver.getPageSource();
			Document doc = Jsoup.parse(result);
	        Elements links = doc.select("a[href]");
	        for (Element link : links) {
	        	String class_name = link.attr("class");
	            if (class_name.equals("ddmDocTitle")) {
	            	System.out.println("LINK" + link.text());
	            	
	            	String linkHref = link.attr("href");
	            	
	            	driver = new HtmlUnitDriver();
	    			driver.get(linkHref);
	    			String summary_page = driver.getPageSource();
	    			//System.out.println("RESULT: " + summary_page);
	    			doc = Jsoup.parse(summary_page);
	    			Elements ids = doc.select("section[id]");
	    			for (Element id : ids) {
	    				if (id.attr("id").equals("abstractSection")) {
	    					abs = id.text();
	    					System.out.println("ABSTRACT: " + abs);
	    					
	    					break;
	    				}
	    			}
	    			
	    			//<span id="journalInfo" class="list-group-item">Volume 49, February 2019, Pages 240-244</span><span class="list-group-item"></span>
	    			Elements j_infos = doc.select("span[id]");
	    			for (Element info : j_infos) {
	    				if (info.attr("id").equals("journalInfo") && info.attr("class").equals("list-group-item")) {
	    					year_info = info.text();
	    					System.out.println("Year Info: " + year_info);
	    					
	    					break;
	    				}
	    			}
	    			
	    			
	            	break;
	            }
	        }
	        
	        outcome = Pair.of(abs, year_info);
			//System.out.println("RESULT: " + result);
	        
	        return outcome;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return outcome;
	}
	
	public static void main(String[] args)
	{
		String title = "Discovery";
		String author = "Song";
		ScopusRecordDownloader downloader = new ScopusRecordDownloader();
		downloader.search(title, author);
	}
}

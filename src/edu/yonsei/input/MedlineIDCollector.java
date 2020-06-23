package edu.yonsei.input;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang3.StringEscapeUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.xml.XmlPage;

public class MedlineIDCollector {

	protected static XMLReader documentReader = null;
	
	String count;
	private String m_queryTerm;
	private String m_url;
	private String m_retStart;
	private String m_retMax;
	private String m_input;
	private ArrayList m_ids;
	public MedlineIDCollector()
	{
		m_queryTerm = "term=";
		m_url = null;
		m_retStart = "retstart=";
		m_retMax = "retmax=";
		m_input = null;
		m_ids = new ArrayList();
	}
	
	public void setQueryTerm(String queryTerm)
	{
		m_queryTerm += queryTerm;
	}
	
	public void setUrl(String url)
	{
		m_url = url;
	}
	
	public void setRetStart(String retStart)
	{
		m_retStart += retStart;
	}
	
	public void setRetMax(String retMax)
	{
		m_retMax += retMax;
	}
	
	public void fetchID() throws Exception
	{

		String final_url = m_url + "&" + m_retStart + "&" + m_retMax + "&" + m_queryTerm ;
		//String final_url = m_url + "&" + m_retStart + "&" + m_retMax + "&" + m_queryTerm + "+AND+2017[pdat]";
		
		System.out.println("Final: " + final_url);
		// Create an instance of HttpClient.
	    HttpClient client = new HttpClient();

	    // Create a method instance.
	    GetMethod method = new GetMethod(m_url);
	    StringBuffer sb = new StringBuffer();
	    try {
			// Send data
		    URL _url = new URL(final_url);
		    URLConnection conn = _url.openConnection();
		    
		    //System.out.println("Header field: " + conn.getHeaderFields());
		    
	        InputStream raw = conn.getInputStream();
	        InputStream buffer = new BufferedInputStream(raw);
	        Reader r = new InputStreamReader(buffer);
	        int c;
	
	        while ((c = r.read()) != -1) {
	          //System.out.print((char) c);
	          sb.append((char)c);
	        } 

	     }
	      catch (MalformedURLException ex) {
	        System.err.println(" is not a parseable URL");
	      }
	      catch (IOException ex) {
	        System.err.println(ex);
	      }

	        m_input = sb.toString();
	        //System.out.println(m_input);
	  }

	public ArrayList<String> readFromCollection() throws FileNotFoundException, IOException {
	
	      
		try {
			documentReader = XMLReaderFactory.createXMLReader(
					"org.apache.xerces.parsers.SAXParser"
					);
		} 
		catch(SAXException e) {
			System.err.println("Couldn't create an XMLReader");
			e.printStackTrace();
			return null;
		}
	
	    PubMedIDDataHandler ph = new PubMedIDDataHandler();
		
		StringReader in = new StringReader(m_input);
		InputSource input = new InputSource(in);
		 
		documentReader.setContentHandler(ph);

		try {
			documentReader.parse(input);
		}
		catch(SAXException e) {
			System.err.println("Error during parse of xml document. " + e.getMessage());
			//e.printStackTrace();
		}
		
		in.close();
		
		m_ids.addAll(ph.getIdList());
		
		System.out.println("Count: " + ph.getCount());
		count = ph.getCount();
		
		return ph.getIdList();
		//System.out.println("Size " + wch.getFeatureSetList().size());
	}
	
	public String getId(int index) {
		return (String)m_ids.get(index);
	}
	
	public ArrayList<String> getIdList()
	{
		return m_ids;
	}
	
	public String getCount() { return count; }
	
	public int getIdCount() { return m_ids.size(); }
	
	public String retrieve()
	{
		String ids_str = new String();
		
		try {
		ArrayList<String> ids = readFromCollection();
		
		for (int i = 0; i< ids.size(); ++i) {
			if (i == ids.size()-1) {
				ids_str += ids.get(i);
			} else {
				ids_str += ids.get(i) + ",";
			}
		}
		} catch (Exception e) {
			System.out.println("Error in xml parsing: " + e.getMessage());
		}
    	String url_str = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&amp;retmode=xml&amp;rettype=abstract";
    	String final_url = url_str + "&id=" + ids_str;

		// Create an instance of HttpClient.
	    HttpClient client = new HttpClient();

	    // Create a method instance.
	    GetMethod method = new GetMethod(final_url);
	        
	    try {
			// Send data
		    URL _url = new URL(final_url);
		    URLConnection conn = _url.openConnection();
		    conn.setDoOutput(true);
		    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
		    //wr.write(value + "&" + query);
		    //wr.flush();
		    
	        // Get the response
	        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	        String line;
	        String responseString = new String();
	        while ((line = rd.readLine()) != null) {
	            // Process line...
	        	responseString += line;
	        }
	        wr.close();
	        rd.close();

	        //System.out.println(responseString);
	        
	        return responseString;

	    } catch (HttpException e) {
	      System.err.println("Fatal protocol violation: " + e.getMessage());
	      e.printStackTrace();
	    } catch (IOException e) {
	      System.err.println("Fatal transport error: " + e.getMessage());
	      e.printStackTrace();
	    } finally {
	      // Release the connection.
	      method.releaseConnection();
	    } 	
	    
	    return null;
	}
	
	public String retrieve(String id)
	{
		String ids_str = new String();
		
    	String url_str = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&amp;retmode=xml&amp;rettype=abstract";
    	String final_url = url_str + "&id=" + id;

		// Create an instance of HttpClient.
	    HttpClient client = new HttpClient();

	    // Create a method instance.
	    GetMethod method = new GetMethod(final_url);
	        
	    try {
			// Send data
		    URL _url = new URL(final_url);
		    URLConnection conn = _url.openConnection();
		    conn.setDoOutput(true);
		    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
		    //wr.write(value + "&" + query);
		    //wr.flush();
		    
	        // Get the response
	        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	        String line;
	        String responseString = new String();
	        while ((line = rd.readLine()) != null) {
	            // Process line...
	        	responseString += line;
	        }
	        wr.close();
	        rd.close();

	        //System.out.println(responseString);
	        
	        return responseString;

	    } catch (HttpException e) {
	      System.err.println("Fatal protocol violation: " + e.getMessage());
	      e.printStackTrace();
	    } catch (IOException e) {
	      System.err.println("Fatal transport error: " + e.getMessage());
	      e.printStackTrace();
	    } finally {
	      // Release the connection.
	      method.releaseConnection();
	    } 	
	    
	    return null;
	}
	
	/**
	 * 
	 * @param ids
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public String retrieveWithIdsOnline(List<String> ids) throws FileNotFoundException, IOException
	{
		String responseString = new String();
		String ids_str = new String();
		for (int i = 0; i< ids.size(); ++i) {
			if (i == ids.size()-1) {
				ids_str += ids.get(i);
			} else {
				ids_str += ids.get(i) + ",";
			}
		}
		
		// --- download the summary page from PubMed
	    String base = "https://www.ncbi.nlm.nih.gov/pubmed/";   // --- PubMed URL
	    String final_url = base + ids_str + "?report=xml&format=text&dispmax=200";            // --- display option: Summary

	    try (final WebClient a_webClient = new WebClient(BrowserVersion.CHROME)) {
	  		
	  		a_webClient.getOptions().setJavaScriptEnabled(true);
	  		a_webClient.getOptions().setThrowExceptionOnScriptError(false);
	  		a_webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
	
	  		WebRequest a_request = new WebRequest(new URL(final_url));
	        a_request.setCharset("utf-8");
	        HtmlPage a_page = a_webClient.getPage(a_request);
	          
	        a_webClient.waitForBackgroundJavaScript(5000);
	        a_webClient.setAjaxController(new NicelyResynchronizingAjaxController());
	  			
	        responseString = a_page.asXml();
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	    
	    return responseString;
	}
	
	public String retrieveWithIds(List<String> ids) throws FileNotFoundException, IOException
	{
	
		String ids_str = new String();
		for (int i = 0; i< ids.size(); ++i) {
			if (i == ids.size()-1) {
				ids_str += ids.get(i);
			} else {
				ids_str += ids.get(i) + ",";
			}
		}
		
		String responseString = "";
		
    	String url_str = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&amp;retmode=xml&amp;rettype=abstract";
    	String final_url = url_str + "&id=" + ids_str;

    	try {
	    	final WebClient a_webClient = new WebClient(BrowserVersion.CHROME);
	  		a_webClient.getOptions().setJavaScriptEnabled(true);
	  		a_webClient.getOptions().setThrowExceptionOnScriptError(false);
	  		a_webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
	
	  		WebRequest a_request = new WebRequest(new URL(final_url));
	        a_request.setCharset("utf-8");
	        XmlPage a_page = a_webClient.getPage(a_request);
	          
	        a_webClient.waitForBackgroundJavaScript(5000);
	        a_webClient.setAjaxController(new NicelyResynchronizingAjaxController());
	  			
	        responseString = a_page.asXml();
	        
	    } catch (HttpException e) {
	      System.err.println("Fatal protocol violation: " + e.getMessage());
	      e.printStackTrace();
	    } catch (IOException e) {
	      System.err.println("Fatal transport error: " + e.getMessage());
	      e.printStackTrace();
	    }
	    
	    return responseString;
	}
	
	
	public ArrayList<PubMedBean> retrievePubMedRecord(List<String> ids) throws Exception
	{
		String results = retrieveWithIds(ids);
		//String results = retrieveWithIdsOnline(ids);
		//results = StringEscapeUtils.unescapeXml(results);
		System.out.println("RESULT: " + results);
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

	    StringReader in = new StringReader(results);
		InputSource input = new InputSource(in);	 
		documentReader.setContentHandler(ph);

		try {
			documentReader.parse(input);
		}
		catch(SAXException e) {
			System.err.println("Error during parse of xml document.");
			e.printStackTrace();
		}
		
		ArrayList<PubMedBean> beans = ph.getContents();
		//System.out.println(beans.get(0).getAbs());
		
		in.close();
		
		return beans;
	}
	
	private void print_https_cert(HttpsURLConnection con)
	{

		if(con!=null){
		   try {
				System.out.println("Response Code : " + con.getResponseCode());
				System.out.println("Cipher Suite : " + con.getCipherSuite());
				System.out.println("\n");
	
				Certificate[] certs = con.getServerCertificates();
				for(Certificate cert : certs){
				   System.out.println("Cert Type : " + cert.getType());
				   System.out.println("Cert Hash Code : " + cert.hashCode());
				   System.out.println("Cert Public Key Algorithm : "
			                                    + cert.getPublicKey().getAlgorithm());
				   System.out.println("Cert Public Key Format : "
			                                    + cert.getPublicKey().getFormat());
				   System.out.println("\n");
				}

			} catch (SSLPeerUnverifiedException e) {
				e.printStackTrace();
			} catch (IOException e){
				e.printStackTrace();
			}
		}
	}

	private String printContent(HttpsURLConnection con)
	{	
		String results = "";
		if(con!=null){
			try {
			   System.out.println("****** Content of the URL ********");
			   BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
			   String input;
			   while ((input = br.readLine()) != null){
			      results += input;
			   }
			   br.close();

			} catch (IOException e) {
			   e.printStackTrace();
			}
		}
		return results;
	}

		   
	public static void main(String[] args) throws Exception {
		
		Random rnd = new Random();
    	
		
		MedlineIDCollector coll = new MedlineIDCollector();
		
		//http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?
		            //https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcg
		String url = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=pubmed&retmode=xml&usehistory=n";
		//"https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi??db=pubmed&retmode=xml&usehistory=n&retstart=0&retmax=20&term=cell"
		coll.setRetStart("0");
		coll.setRetMax("1000");
		//coll.setQueryTerm("cell[TIAB]");
		//
		coll.setQueryTerm("\"social%20health\"[Title]");
		coll.setUrl(url);
		
		coll.fetchID();
		ArrayList<String> ids = coll.readFromCollection();
		
		System.out.println("Count: " + coll.getCount());
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File("pubmed_results.txt")));
		
		for (int c = 0; c < ids.size(); c += (c+1) < ids.size() ? 200 : 1) {
    		if ((c+199) < ids.size()) {
    			
    			try {
	    			ArrayList<PubMedBean> result = coll.retrievePubMedRecord(ids.subList(c, (c+199)));
	    			
	    			System.out.println("RESULT " + result.size() + " : " + result.get(0).getAbs());
	    			
					for (int i = 0; i <result.size(); ++i) {
						PubMedBean bean = result.get(i);
						String pmid = bean.getPMID();
						String year = bean.getYear();
						year = year.replaceAll("[\\t\\r\\n]", "");
						
						String abs = bean.getAbs();
						String title = bean.getTitle();
						String content = "";
						if (abs != null) {
							if (abs.length() > 0) {
								content = title + " " + abs;
							}
						}
						String journal = bean.getJournalTitle();
						String meshes = "";
						HashMap<String,Integer> meshmap = bean.getMeshMap();
						for (Map.Entry<String, Integer> ent : meshmap.entrySet()) {
							meshes += ent.getKey() + "|";
						}
						
						writer.write(pmid + "\t" + year + "\t" + meshes + "\t" + content + "\n");
						
					}
					
    			} catch (Exception e) {
    				System.out.println(e.getMessage());
    				//There is a problem of getting data. We hit the pubmed again 
    				System.out.println("There is an error of getting records from PubMed. " + e.getMessage());
    				Thread.sleep(20000);
    				ArrayList<PubMedBean> result = coll.retrievePubMedRecord(ids.subList(c, (c+199)));
	    			
					for (int i = 0; i <result.size(); ++i) {
						PubMedBean bean = result.get(i);
						String pmid = bean.getPMID();
						String year = bean.getYear();
						String abs = bean.getAbs();
						String title = bean.getTitle();
						String content = "";
						if (abs != null) {
							if (abs.length() > 0) {
								content = title + " " + abs;
							}
						}
						String journal = bean.getJournalTitle();
						String meshes = "";
						HashMap<String,Integer> meshmap = bean.getMeshMap();
						for (Map.Entry<String, Integer> ent : meshmap.entrySet()) {
							meshes += ent.getKey() + "|";
						}
						
						writer.write(pmid + "\t" + year + "\t" + meshes + "\t" + content + "\n");
					}
					
    			} 
    		} else {
				//ArrayList<HashMap> result = getCitations(id_list.subList(c, (id_list.size())));
    			ArrayList<PubMedBean> result = coll.retrievePubMedRecord(ids.subList(c, (ids.size())));
    			
				for (int i = 0; i <result.size(); ++i) {
					PubMedBean bean = result.get(i);
					String pmid = bean.getPMID();
					String year = bean.getYear();
					String abs = bean.getAbs();
					String title = bean.getTitle();
					String content = "";
					if (abs != null) {
						if (abs.length() > 0) {
							content = title + " " + abs;
						}
					}
					String journal = bean.getJournalTitle();
					String meshes = "";
					HashMap<String,Integer> meshmap = bean.getMeshMap();
					for (Map.Entry<String, Integer> ent : meshmap.entrySet()) {
						meshes += ent.getKey() + "|";
					}
					
					writer.write(pmid + "\t" + year + "\t" + meshes + "\t" + content + "\n");
					
				}
    		}
    		
    		Integer sleepTime = rnd.nextInt(3000);
            Thread.sleep(sleepTime);
		}
		
		writer.close();
				
	}
	
}

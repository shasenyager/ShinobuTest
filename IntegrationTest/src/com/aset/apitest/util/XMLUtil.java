package com.aset.apitest.util;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class XMLUtil 
{
	public static String[] xmlAgencyTags = {"id", "title", "link", "description", "language","abrv", "level", "ttl", "pubdate", "lastbuilddate"};
	public static final int INDEX_PUBDATE = 8;
	public static final int INDEX_BUILDDATE = 9;
	
	// This method walks through xml document and retrieves data into multidimensional array
	public static String[][] retrieveData(String xml, String[] tags)
	{
		String[][] results = null;
		
		try
		{
			DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
	        DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
	        Document doc = docBuilder.parse(new InputSource(new StringReader(xml)));
	        
	        doc.getDocumentElement().normalize();

            NodeList listOfOrgs = doc.getElementsByTagName("organization");
            int totalOrgs = listOfOrgs.getLength();

            results = new String[totalOrgs][];
            
            for(int s = 0; s < listOfOrgs.getLength(); s++)
            {
                Node orgNode = listOfOrgs.item(s);
                if(orgNode.getNodeType() == Node.ELEMENT_NODE)
                {
                	String[] orgData = new String[tags.length];
                    Element orgElement = (Element)orgNode;

                    for(int i = 0; i < tags.length; i++)
                    {
	                    String data = orgElement.getElementsByTagName(tags[i]).item(0).getTextContent();
	                    orgData[i] = data;
                    }
                    results[s] = orgData;
                }   
            }
        }
		catch (SAXParseException err) 
        {
	        System.out.println ("** Parsing error" + ", line " + err.getLineNumber () + ", uri " + err.getSystemId ());
	        System.out.println(" " + err.getMessage ());
        }
		catch (SAXException e) 
		{
	        Exception x = e.getException ();
	        ((x == null) ? e : x).printStackTrace ();
		}
		catch (Throwable t) 
		{
			t.printStackTrace ();
        }
		
		return results;
	}
	
	public static String[][] convertAPIDateFormat(String[][] apiData)
	{
    	for (int i = 0; i < apiData.length; i++)
    	{
    		if (apiData[i][XMLUtil.INDEX_PUBDATE] != "" && apiData[i][XMLUtil.INDEX_PUBDATE] != null)
    			apiData[i][XMLUtil.INDEX_PUBDATE] = convertDateFormat(apiData[i][XMLUtil.INDEX_PUBDATE]);
    		if (apiData[i][XMLUtil.INDEX_BUILDDATE] != "" && apiData[i][XMLUtil.INDEX_BUILDDATE] != null) 
    			apiData[i][XMLUtil.INDEX_BUILDDATE] = convertDateFormat(apiData[i][XMLUtil.INDEX_BUILDDATE]);
    	}
    	
    	return apiData;
	}
	
	// This method converts date format from yyyy-mm-ddThh:mm:ss.SSS-GMT to yyyy-mm-dd hh:mm:ss.S
	// It assumes that millisecond is always 0
	private static String convertDateFormat(String apiTime) 
	{
		String convertedString = apiTime.replace('T', ' ');
		int index = convertedString.lastIndexOf("-");
		convertedString = convertedString.substring(0, index - 2);
		
		return convertedString;
	}
}

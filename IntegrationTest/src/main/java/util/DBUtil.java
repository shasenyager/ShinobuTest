package main.java.util;

import java.io.StringWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DBUtil 
{
	static final String OPEN_PARENTHESIS = "(";
	static final String CLOSE_PARENTHESIS = ")";
	
	static String dbDriver;
	static String dbUrl;
	static String dbUsername;
	static String dbPassword;
	
	static Connection connection = null;
	
	// This is used to map the xml tags in API result and DB result
	static Map<String, String> agencySchemaMap;
	
	public static void setUp(String driver, String url, String username, String password)
	{
		dbDriver = driver;
		dbUrl = url;
		dbUsername = username;
		dbPassword = password;
		
		agencySchemaMap = new HashMap<String, String>();
		agencySchemaMap.put("OrganizationID", "id");
		agencySchemaMap.put("Name", "title");
		agencySchemaMap.put("Website", "link");
		agencySchemaMap.put("Description", "description");
		agencySchemaMap.put("Language", "language");
		agencySchemaMap.put("Code", "abrv");
		agencySchemaMap.put("Level", "level");
		agencySchemaMap.put("TTL", "ttl");
		agencySchemaMap.put("PubDate", "pubdate");
		agencySchemaMap.put("LastBuildDate", "lastbuilddate");
	}
	
	// This method establishes the database connection
	public static void establishConnection()
	{
		if (dbDriver == null || dbUrl == null || dbUsername == null || dbPassword == null)
			System.out.println("DB driver class, URL, Username, or Password is null.  Cannot open DB connection." );
		else
		{
			try 
			{
				Class.forName(dbDriver);
			} 
			catch (ClassNotFoundException e) 
			{
				System.out.println("Driver class: " + dbDriver + " could not be found" );
				e.printStackTrace();
				return;
			}
		 
			try 
			{
				if (connection != null && !connection.isClosed())
					System.out.println("DB connection is still open. Close it first before opening the connection." );
				else
					connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
			} 
			catch (SQLException e) 
			{
				System.out.println("DB Connection Failed!");
				e.printStackTrace();
				return;
			}
		}
	}
	
	public static void closeConnection()
	{
		try 
		{
			if (connection != null && !connection.isClosed())
			{
				connection.close();
			}
		}
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
	}
	
	// This method executes the stored procedure and returns data in xml format as a string
	public static String executeStoredProcedure(String procedure, String[] parameters)
	{
		String result = null;
		
		if (connection != null)
		{
			try
			{
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			    DocumentBuilder builder = factory.newDocumentBuilder();
			    Document doc = builder.newDocument();
			    
				CallableStatement cs  = connection.prepareCall("CALL " + procedure + GetParameterString(parameters));
				ResultSet rs = cs.executeQuery();
				if (rs != null)
				{
					Element parent = doc.createElement("organizations");
			    	doc.appendChild(parent);
			    	
					try
					{
						ResultSetMetaData rsmd = rs.getMetaData();
					    int colCount = rsmd.getColumnCount();
		
					    while (rs.next()) 
					    {
					    	Element row = doc.createElement("organization");
					    	parent.appendChild(row);
							for (int i = 1; i <= colCount; i++) 
							{
								String columnName = rsmd.getColumnName(i);
								// convert the column name to match with API result's xml tag
								// if the mapping does not exist, use the column name (means that column name and tag name as the same)
								String newColumnName = agencySchemaMap.get(columnName);
								if (newColumnName != null)
									columnName = newColumnName;
								
								Object value = rs.getObject(i);
								Element node = doc.createElement(columnName);
								if (value == null)
									node.appendChild(doc.createTextNode(""));
								else
									node.appendChild(doc.createTextNode(value.toString()));
								row.appendChild(node);
							}
					    }
					    result = convertDocToString(doc);
					    
					} catch (TransformerConfigurationException e) 
					{
						e.printStackTrace();
					} catch (TransformerException e) 
					{
						e.printStackTrace();
					}
					finally 
					{
						rs.close();	 
					}			  
				}
			}
			catch (SQLException e) 
			{
				e.printStackTrace();
			} 
			catch (ParserConfigurationException e) 
			{
				e.printStackTrace();
			}	
			catch (Exception e) 
			{
				e.printStackTrace();
			}	
		}
		return result;
	}
	
	// This method converts Document object to a string and returns the string
	private static String convertDocToString(Document doc) throws TransformerException
	{
		try
	    {
	       DOMSource domSource = new DOMSource(doc);
	       StringWriter writer = new StringWriter();
	       StreamResult result = new StreamResult(writer);
	       TransformerFactory tf = TransformerFactory.newInstance();
	       Transformer transformer = tf.newTransformer();
	       transformer.transform(domSource, result);
	       return writer.toString();
	    }
	    catch(TransformerException ex)
	    {
	       ex.printStackTrace();
	       return null;
	    }
	}
	
	// This method creates parameter list as one string.  The returned parameter list will be used a part of stored procedure
	private static String GetParameterString(String[] parameters)
	{
		String parameterString = OPEN_PARENTHESIS;
		
		if (parameters.length == 0)
			parameterString = OPEN_PARENTHESIS + CLOSE_PARENTHESIS;
		else
		{
			parameterString = OPEN_PARENTHESIS + "'";
			for(int i = 0; i < parameters.length; i++)
			{
				parameterString = parameterString + parameters[i];
				if (i < parameters.length - 1)
				{
					parameterString = parameterString + ",";
				}
			}
			parameterString = parameterString + "'";
		}
		
		return parameterString + CLOSE_PARENTHESIS;
	}
}

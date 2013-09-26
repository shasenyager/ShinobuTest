package main.java;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import main.java.util.APIUtil;
import main.java.util.DBUtil;
import main.java.util.XMLUtil;

import org.testng.Assert;
import org.testng.annotations.*;
import org.xml.sax.SAXException;


public class GetOrgListTest 
{
	String dbResult = null;
	String apiResult = null;
	
	final String procedure = "spget_orglist";
	final String method = "get_orglist";
	final String paramName = "org_id";
	
	@BeforeSuite
	@Parameters(value={"dbDriver", "dbUrl", "dbUsername", "dbPassword", "apiURL", "authorization"})
    public static void oneTimeSetUp(String dbDriver, String dbUrl, String dbUsername, String dbPassword, String apiURL, String authorization) 
	{
		DBUtil.setUp(dbDriver, dbUrl, dbUsername, dbPassword);
		APIUtil.setUp(apiURL, authorization);
		DBUtil.establishConnection();
    }
    @AfterSuite
    public static void oneTimeTearDown() 
    {
    	DBUtil.closeConnection();
    }
    
	@DataProvider(name = "Data-Provider-Function")
	public Object[][] parameterTestProvider() 
	{
		return new Object[][]{{"0D5CF94C-D8DF-4066-B9A7-693A7A0A11C8"}};//,{"EC336387-D944-4312-ABEA-80D23B24F520"}, {"null"}, {"abcd"}};
	}
	
	@Test(dataProvider = "Data-Provider-Function")
    public void testGetOrglist(String paramValue) throws SAXException, IOException, ParserConfigurationException, TransformerException 
    {
		// call REST API
    	apiResult = APIUtil.callGetMethod(method, new String[]{paramName}, new String[]{paramValue});
    	// call stored procedure
    	dbResult = DBUtil.executeStoredProcedure(procedure, new String[]{paramValue});
    	// verify that the both results are not null (even if there is no results to return, it should not be null)
    	Assert.assertNotNull(dbResult, "ERROR: DB results are null");
    	Assert.assertNotNull(apiResult, "ERROR: API results are null");
    	
    	String[][] apiData = XMLUtil.retrieveData(apiResult, XMLUtil.xmlAgencyTags);
    	String[][] dbData = XMLUtil.retrieveData(dbResult, XMLUtil.xmlAgencyTags);
    	
    	// convert date format from "yyyy-mm-ddThh:mm:ss.SSS-GMT" to "yyyy-mm-dd hh:mm:ss.S"
    	XMLUtil.convertAPIDateFormat(apiData);
    	
    	Assert.assertEquals(apiData.length, dbData.length, "Data count of DB results and API results do not match");
    	Assert.assertTrue(compareResults(apiData, dbData), "Contents of DB results and API results do not match");
    }

	// This method compares 2 multidimensional arrays
	private Boolean compareResults(String[][] result1, String[][] result2)
	{
		for (int i = 0; i < result1.length; i++)
		{
			for (int s = 0; s < 10; s++)
			{
				if (!result1[i][s].equals(result2[i][s]))
				{
					System.out.println("It doesn't match up: " + result1[i][s] + ": " + result2[i][s]);
					return false;
				}
			}
		}
		return true;
	}
}

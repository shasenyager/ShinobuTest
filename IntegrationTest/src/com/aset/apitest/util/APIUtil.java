package com.aset.apitest.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class APIUtil 
{
	static String baseUrl;
	static String headerParamValue;
	static final String PRE_PARAMETER = "?";
	static final String SET_PARAMETER = "=";
	static final String AND_PARAMETER = "&";
	static final String HEADER_PARAM = "Authorization";
	
	public static void setUp(String url, String header)
	{
		baseUrl = url;
		headerParamValue = header;
	}
	
	// This method calls REST API (GET) with parameters and returns xml data as a string
	// method - method name to call
	// paramNames - list of parameter names
	// paramValues - list of parameter values
	public static String callGetMethod(String method, String[] paramNames, String[] paramValues)
	{
		String result = null;
		
		try
		{
			  URL url = new URL(baseUrl + method + PRE_PARAMETER + GetParameterString(paramNames, paramValues));
			  HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			  conn.setRequestMethod("GET");
			  conn.setRequestProperty(HEADER_PARAM, headerParamValue);
			
			  if (conn.getResponseCode() != 200) 
			  {
				  System.out.println("Return code is not 200: " + conn.getResponseCode());
			  }
			
			  // Buffer the result into a string
			  BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			  StringBuilder sb = new StringBuilder();
			  String line;
			  while ((line = rd.readLine()) != null) 
			  {
			    sb.append(line);
			  }
			  rd.close();
			
			  conn.disconnect();
			  
			  System.out.println(sb.toString());
			  result = sb.toString();
		}
		catch(IOException e)
		{
			System.out.println("Catched IOException: " + e);
		}
		
		return result;
	}
	
	// This method returns parameter names and values as one string. The returned string will be used for part of url
	// For example: paramName1=paramValue1&paramName2=paramValue2
	// paramNames - list of parameter names
	// paramValues - list of parameter values
	private static String GetParameterString(String[] paramNames, String[] paramValues)
	{
		String parameterString = "";
		
		for(int i = 0; i < paramNames.length; i++)
		{
			parameterString = parameterString + paramNames[i] + SET_PARAMETER + paramValues[i];
			if (i < paramNames.length - 1)
			{
				parameterString = parameterString + AND_PARAMETER;
			}
		}
		
		return parameterString;
	}
	
}

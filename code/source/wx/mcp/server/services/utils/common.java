package wx.mcp.server.services.utils;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import com.wm.app.b2b.server.Server;
import com.wm.lang.ns.DependencyManager;
import com.wm.lang.ns.NSInterface;
import com.wm.lang.ns.NSName;
import com.wm.lang.ns.NSNode;
import com.wm.lang.ns.openapi.NSProviderDescriptor;
import com.wm.lang.ns.rsd.RestTag;
import com.wm.app.b2b.server.ns.NSDependencyManager;
import com.wm.app.b2b.server.ns.Namespace;
import org.json.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.*;
import wx.mcp.server.services.custom.OAS2MCPConverter;
import wx.mcp.server.models.*;
// --- <<IS-END-IMPORTS>> ---

public final class common

{
	// ---( internal utility methods )---

	final static common _instance = new common();

	static common _newInstance() { return new common(); }

	static common _cast(Object o) { return (common)o; }

	// ---( server methods )---




	public static final void createCacheKeyFromSecurityContext (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(createCacheKeyFromSecurityContext)>> ---
		// @sigtype java 3.5
		// [i] field:0:required authType {"API_KEY","OAUTH"}
		// [i] field:0:required apiKey
		// [i] field:0:required aud
		// [i] field:0:required clientId
		// [i] field:0:required scopes
		// [i] field:0:required bearerToken
		// [o] field:0:required cacheKey
		// pipeline
				 
		IDataCursor cursor = pipeline.getCursor();
		String apiKey   = IDataUtil.getString(cursor, "apiKey");
		String aud      = IDataUtil.getString(cursor, "aud");
		String clientId = IDataUtil.getString(cursor, "clientId");
		String scopes   = IDataUtil.getString(cursor, "scopes");
		String authType = IDataUtil.getString(cursor, "authType");
		String bearerToken = IDataUtil.getString(cursor, "bearerToken");
		String cacheKey = null;
		
		// Validate authType
		if (authType == null || authType.trim().isEmpty()) {
		    throw new IllegalArgumentException("authType must be provided and non-empty (OAUTH or API_KEY)");
		}
		
		authType = authType.trim().toUpperCase();
		String[] scopeArray = null;
		String scopeString = null;
		String rawKey = null;
		switch (authType) {
		    case "API_KEY":
		        if (apiKey == null || apiKey.trim().isEmpty()) {
		            throw new IllegalArgumentException("API_KEY authentication requires 'apiKey' parameter");
		        }
		        cacheKey = "APIKEY_" + sha256(apiKey.trim());
		        break;
		
		    case "OAUTH":
		        if (bearerToken == null || bearerToken.trim().isEmpty()) {
		            throw new IllegalArgumentException("OAUTH authentication requires 'bearerToken' parameter");
		        }
		        cacheKey = "OAUTH_" + sha256(bearerToken.trim());		    	
		        if (aud == null || aud.trim().isEmpty() ||
		            clientId == null || clientId.trim().isEmpty() ||
		            scopes == null || scopes.trim().isEmpty()) {
		            throw new IllegalArgumentException("OAUTH authentication requires 'aud', 'clientId', and 'scopes' parameters");
		        }
		        // Sort scopes alphabetically
		        scopeArray = scopes.trim().split("\\s+");
		        Arrays.sort(scopeArray);
		        scopeString = String.join(" ", scopeArray);
		        rawKey = aud.trim() + "|" + clientId.trim() + "|" + scopeString;
		        cacheKey = "OAUTH_" + sha256(rawKey);
		        break;
		    case "INTERNAL":	    	
		        if (clientId == null || clientId.trim().isEmpty() ||
		            scopes == null || scopes.trim().isEmpty()) {
		            throw new IllegalArgumentException("INTERNAL authentical requires at least 'clientId', and 'scopes' parameters");
		        } 
		        // Sort scopes alphabetically
		        scopeArray = scopes.trim().split("\\s+");
		        Arrays.sort(scopeArray);
		        scopeString = String.join(" ", scopeArray);
		        rawKey = aud.trim() + "|" + clientId.trim() + "|" + scopeString;
		        cacheKey = "INTERNAL_" + sha256(rawKey);
		        break;
		    default:
		        throw new IllegalArgumentException("Invalid authType: " + authType + ". Only 'OAUTH' or 'API_KEY' are supported.");
		}
		
		// You may want to put cacheKey back into the pipeline if needed:
		IDataUtil.put(cursor, "cacheKey", cacheKey);
		cursor.destroy();
		// --- <<IS-END>> ---

                
	}



	public static final void createObjectPrefix (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(createObjectPrefix)>> ---
		// @sigtype java 3.5
		// [i] field:0:required objName
		// [o] field:0:required effectivePrefix
		// pipeline
		IDataCursor pipelineCursor = pipeline.getCursor();
		String	objName = IDataUtil.getString( pipelineCursor, "objName" );
		String effectivePrefix = "";
		 
		if( objName != null){
			if( objName.trim().length() > 0){
				effectivePrefix = objName + "_";
			}
		}
		IDataUtil.put( pipelineCursor, "effectivePrefix", effectivePrefix );
		pipelineCursor.destroy();
		// --- <<IS-END>> ---

                
	}



	public static final void listHasValue (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(listHasValue)>> ---
		// @sigtype java 3.5
		// [i] field:1:required theList
		// [i] field:0:required theValue
		// [o] object:0:required hasValue
		// pipeline
		IDataCursor pipelineCursor = pipeline.getCursor(); 
		String[] theList = IDataUtil.getStringArray(pipelineCursor, "theList");
		String theValue = IDataUtil.getString(pipelineCursor, "theValue");
		  
		boolean _bool = false;
		
		if (theList != null && theValue != null) {
		    for (String s : theList) {
		        if (theValue.equals(s)) {  // theValue is guaranteed non-null here
		            _bool = true;
		            break; // can stop early
		        }
		    }
		}
		
		IDataUtil.put(pipelineCursor, "hasValue", Boolean.valueOf(_bool));
		pipelineCursor.destroy();
		// --- <<IS-END>> ---

                
	}



	public static final void splitString (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(splitString)>> ---
		// @sigtype java 3.5
		// [i] field:0:required inputString
		// [i] field:0:required separator
		// [o] field:1:optional stringElements
		// pipeline
		IDataCursor pipelineCursor = pipeline.getCursor();
		String inputString = IDataUtil.getString( pipelineCursor, "inputString" );
		String separator = IDataUtil.getString( pipelineCursor, "separator" );
		
		String[] stringElements;
		if (inputString == null || separator == null || separator.isEmpty()) {
		    stringElements = new String[0];
		} else {
		    stringElements = inputString.split(separator);
		}
		
		IDataUtil.put( pipelineCursor, "stringElements", stringElements );
		pipelineCursor.destroy();
		// --- <<IS-END>> ---

                
	}



	public static final void yamlJsonMapper (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(yamlJsonMapper)>> ---
		// @sigtype java 3.5
		// [i] field:0:required inputString
		// [o] field:0:required outputJson
		// [o] field:0:required detectedFormat {"yaml","json"}
		try{
			IDataCursor pipelineCursor = pipeline.getCursor();
			String	inputString = IDataUtil.getString( pipelineCursor, "inputString" );
			String	detectedFormat = "json";
			String	outputJson = null;
			  
		    ObjectMapper jsonMapper = new ObjectMapper();
		    ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
		
			 // Register Java 8 date/time support on BOTH mappers
			 yamlMapper.registerModule(new JavaTimeModule());
			 jsonMapper.registerModule(new JavaTimeModule());
		
			 // Optional but common: write ISO-8601 strings for dates (not timestamps)
			 yamlMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
			 jsonMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		
		    try {
		    		jsonMapper.readTree(inputString);
		    		outputJson = inputString;
		        } 
		    catch (Exception e) {
		    			JsonNode node = yamlMapper.readTree(inputString);
		    			if(node.isValueNode()) {
		    			    throw new IllegalArgumentException("Input string is plain text, expected JSON or YAML");
		    			}
		            	detectedFormat = "yaml";
		            	outputJson = jsonMapper.writeValueAsString(node);
		            	
		    }
		    IDataUtil.put( pipelineCursor, "outputJson", outputJson );
			IDataUtil.put( pipelineCursor, "detectedFormat", detectedFormat );
			pipelineCursor.destroy();
		}catch(Exception e){
			throw new ServiceException(e);
		}
		// --- <<IS-END>> ---

                
	}

	// --- <<IS-START-SHARED>> ---
	private static void log(String msg) {
		// input
		IData input = IDataFactory.create();
		IDataCursor inputCursor = input.getCursor();
		IDataUtil.put( inputCursor, "message", msg );
		IDataUtil.put( inputCursor, "function", "[webMethods MCP Server]:" ); 
		inputCursor.destroy();
	
		try{
			Service.doInvoke( "pub.flow", "debugLog", input );
		}catch( Exception e){}
	}
	
	/**
	 * Validates whether the given string is a strictly valid URL.
	 * <p>
	 * This method checks both the syntactic correctness of the URL using {@link java.net.URL}
	 * and ensures it conforms to URI standards using {@link java.net.URI}. It will return {@code true}
	 * only if the input string can be successfully parsed as both a URL and a URI, which means it must
	 * be properly encoded (e.g., spaces must be replaced with {@code %20}).
	 * </p>
	 *
	 * @param urlString the string to validate as a URL
	 * @return {@code true} if the string is a strictly valid and properly encoded URL, {@code false} otherwise
	 * @throws NullPointerException if {@code urlString} is {@code null}
	 *
	 * @see java.net.URL
	 * @see java.net.URI
	 */
	public static boolean isStrictlyValidURL(String urlString) {
	    try {
	        URL url = new URL(urlString);
	        url.toURI();
	        return true;
	    } catch (Exception e) {
	        return false;
	    }
	}
	
	public static String sha256(String value) {
	    try {
	        MessageDigest digest = MessageDigest.getInstance("SHA-256");
	        byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
	        // Hex-String erzeugen
	        StringBuilder hexString = new StringBuilder();
	        for (byte b : hash) {
	            String hex = Integer.toHexString(0xff & b);
	            if (hex.length() == 1) hexString.append('0');
	            hexString.append(hex);
	        }
	        return hexString.toString();
	    } catch (NoSuchAlgorithmException e) {
	        throw new RuntimeException("SHA-256 nicht verf\u00FCgbar", e);
	    }
	}
		
	private static String subStringAfter(String input, String prefix){
		int index = input.indexOf(prefix);
	
		String result = input;
		if (index != -1) {
		    result = input.substring(index + prefix.length());
		}
		return result;
	}
	
	private static String encodeURIComponent(String s) {
	    if (s == null) return "";
	    try {
	        return java.net.URLEncoder.encode(s, "UTF-8")
	            .replaceAll("\\+", "%20")
	            .replaceAll("\\%21", "!")
	            .replaceAll("\\%27", "'")
	            .replaceAll("\\%28", "(")
	            .replaceAll("\\%29", ")")
	            .replaceAll("\\%7E", "~");
	    } catch (Exception e) {
	        return s;
	    }
	}
		
	// --- <<IS-END-SHARED>> ---
}


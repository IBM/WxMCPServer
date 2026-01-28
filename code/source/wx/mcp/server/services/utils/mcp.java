package wx.mcp.server.services.utils;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import com.wm.app.b2b.server.Server;
import com.wm.lang.ns.DependencyManager;
import com.wm.lang.ns.NSField;
import com.wm.lang.ns.NSInterface;
import com.wm.lang.ns.NSName;
import com.wm.lang.ns.NSNode;
import com.wm.lang.ns.NSPackage;
import com.wm.lang.ns.NSRecord;
import com.wm.lang.ns.NSService;
import com.wm.lang.ns.openapi.NSProviderDescriptor;
import com.wm.lang.ns.rsd.RestTag;
import com.wm.app.b2b.server.ns.NSDependencyManager;
import com.wm.app.b2b.server.ns.Namespace;
import org.json.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.net.MalformedURLException;
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
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import wx.mcp.server.services.custom.OAS2MCPConverter;
import wx.mcp.server.models.*;
// --- <<IS-END-IMPORTS>> ---

public final class mcp

{
	// ---( internal utility methods )---

	final static mcp _instance = new mcp();

	static mcp _newInstance() { return new mcp(); }

	static mcp _cast(Object o) { return (mcp)o; }

	// ---( server methods )---




	public static final void extractMCPArguments (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(extractMCPArguments)>> ---
		// @sigtype java 3.5
		// [i] record:0:required arguments
		// [i] field:0:required headerPrefix
		// [i] field:0:required queryPrefix
		// [i] field:0:required pathParamPrefix
		// [o] record:1:required queryParams
		// [o] - field:0:required name
		// [o] - field:0:required value
		// [o] record:1:required pathParams
		// [o] - field:0:required name
		// [o] - field:0:required value
		// [o] record:1:required headers
		// [o] - field:0:required name
		// [o] - field:0:required value
		IDataCursor pipelineCursor = pipeline.getCursor();
		  
		String headerPrefix = IDataUtil.getString(pipelineCursor, "headerPrefix");
		String pathParamPrefix = IDataUtil.getString(pipelineCursor, "pathParamPrefix");
		String queryPrefix = IDataUtil.getString(pipelineCursor, "queryPrefix");
		 
		// Get the "arguments" document from the pipeline
		IData arguments = IDataUtil.getIData(pipelineCursor, "arguments");
		if (arguments == null) {
		    pipelineCursor.destroy();
		    return;
		}
		
		IDataCursor argsCursor = arguments.getCursor();
		
		// Lists to collect matching IData elements
		java.util.List<IData> queryParamsList = new java.util.ArrayList<>();
		java.util.List<IData> headersList = new java.util.ArrayList<>();
		java.util.List<IData> pathParamsList = new java.util.ArrayList<>();
		java.util.List<String> keysToRemove = new java.util.ArrayList<>();
		
		System.out.println("+++++++++++++++++++++++++++++++++++++++");
		// Iterate through all children of "arguments"
		while (argsCursor.next()){
		    String key = argsCursor.getKey();
		    Object value = argsCursor.getValue();
		    System.out.println("Found key: " + key);
		    if (key.startsWith(queryPrefix)) {		
		    	IData param = IDataFactory.create();
		        IDataCursor paramCursor = param.getCursor();
		        IDataUtil.put( paramCursor, "name", subStringAfter(key, queryPrefix));
				IDataUtil.put( paramCursor, "value", value);        
		        paramCursor.destroy();
		        queryParamsList.add(param);
		        
		        // Remove the current element from "arguments"
		        System.out.println("Adding key to delete: " + key);
		        keysToRemove.add(key);
		    } else if (key.startsWith(headerPrefix)) {
		    	IData param = IDataFactory.create();
		        IDataCursor paramCursor = param.getCursor();
				IDataUtil.put( paramCursor, "name", subStringAfter(key, headerPrefix));				
				IDataUtil.put( paramCursor, "value", value);
		        paramCursor.destroy();
		        headersList.add(param);
		
		        // Remove the current element from "arguments"
		        System.out.println("Adding key to delete: " + key);
		        keysToRemove.add(key);
		    } else if (key.startsWith(pathParamPrefix)) {
		    	IData param = IDataFactory.create();
		        IDataCursor paramCursor = param.getCursor();
		        IDataUtil.put( paramCursor, "name", subStringAfter(key, pathParamPrefix));
				IDataUtil.put( paramCursor, "value", value); 
		        paramCursor.destroy();
		        pathParamsList.add(param);
		
		        // Remove the current element from "arguments"
		        System.out.println("Adding key to delete: " + key);
		        keysToRemove.add(key);
		    }
		    // All other keys are left untouched
		}
		argsCursor.destroy();	
		
		// Second pass: remove keys from arguments
		if (!keysToRemove.isEmpty()) {
		    argsCursor = arguments.getCursor();
		    while (argsCursor.next()) {
		        String key = argsCursor.getKey();
		        if (keysToRemove.contains(key)) {
		            argsCursor.delete();
		            //argsCursor = arguments.getCursor();
		            argsCursor.previous();
		        }
		    }
		    argsCursor.destroy();
		}
		
		// Put the results into the pipeline as arrays if not empty
		if (!queryParamsList.isEmpty()) {
			IData[] queryParams = queryParamsList.toArray(new IData[0]);
		    IDataUtil.put(pipelineCursor, "queryParams", queryParams);
		}
		if (!headersList.isEmpty()) {
			IData[] headers = headersList.toArray(new IData[0]);
		    IDataUtil.put(pipelineCursor, "headers", headers);
		}
		if (!pathParamsList.isEmpty()) {
			IData[] pathParams = pathParamsList.toArray(new IData[0]);
		    IDataUtil.put(pipelineCursor, "pathParams", pathParams);
		}		
		pipelineCursor.destroy();
		// --- <<IS-END>> ---

                
	}



	public static final void getMCPObjectName (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(getMCPObjectName)>> ---
		// @sigtype java 3.5
		// [i] field:1:required tags
		// [o] field:0:required mcpObjectName
		// pipeline
		IDataCursor pipelineCursor = pipeline.getCursor();
		String[] tags = IDataUtil.getStringArray(pipelineCursor, "tags");
		String mcpObjectName = null;
		
		if (tags != null) {
		    for (String tag : tags) {
		        if (tag != null && tag.startsWith("mcp.object.name:")) {
		            // extract part after the prefix
		            mcpObjectName = tag.substring("mcp.object.name:".length()).trim();
		            break; // stop after first match
		        }
		    }
		}
		
		// put result into pipeline
		IDataUtil.put(pipelineCursor, "mcpObjectName", mcpObjectName);
		pipelineCursor.destroy();
		// --- <<IS-END>> ---

                
	}



	public static final void verifyMCPClientConfig (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(verifyMCPClientConfig)>> ---
		// @sigtype java 3.5
		// [i] recref:0:required mcpClientConfig wx.mcp.server.doctypes:MCPClientConfig
		IDataCursor pipelineCursor = pipeline.getCursor();
		
		    // mcpClientConfig
		    IData mcpClientConfig = IDataUtil.getIData(pipelineCursor, "mcpClientConfig");
		    if (mcpClientConfig != null) {
		        IDataCursor mcpClientConfigCursor = mcpClientConfig.getCursor();
		        String authType = IDataUtil.getString(mcpClientConfigCursor, "authType");
		        String toolCatalogBaseURL = IDataUtil.getString(mcpClientConfigCursor, "toolCatalogBaseURL");
		
		        // Validate toolCatalogBaseURL
		        
		        /*
		        if (toolCatalogBaseURL == null || toolCatalogBaseURL.trim().isEmpty()) {
		            throw new ServiceException("\"tool_catalog_base_url\" must not be NULL or empty.");
		        }
		        if (!isStrictlyValidURL(toolCatalogBaseURL)) {
		        	throw new ServiceException("\"tool_catalog_base_url\" is not a valid URL.");
		        }
				*/
		        // Validate API Key Auth
		        if ("API_Key".equalsIgnoreCase(authType)) {
		            IData apiKey = IDataUtil.getIData(mcpClientConfigCursor, "apiKey");
		            if (apiKey == null) {
		                throw new ServiceException("API Key authentication requires an \"apiKey\" object.");
		            }
		            IDataCursor apiKeyCursor = apiKey.getCursor();
		            String key = IDataUtil.getString(apiKeyCursor, "key");
		            String headerName = IDataUtil.getString(apiKeyCursor, "headerName");
		            apiKeyCursor.destroy();
		
		            if (key == null || key.trim().isEmpty()) {
		                throw new ServiceException("API Key authentication requires a non-empty \"api_key\".");
		            }
		            if (headerName == null || headerName.trim().isEmpty()) {
		                throw new ServiceException("API Key authentication requires a non-empty \"api_key_headername\". Use \"x-Gateway-APIKey\" as default for webMethods.");
		            }
		        }
		        
		        // Validate THIRD_PARY JWKS URI settings and audience
		        if ("THIRD_PARTY".equalsIgnoreCase(authType)) {
		        	 IDataCursor cfgCursor = mcpClientConfig.getCursor();
		             
		             // Extract authServer document
		             IData authServer = IDataUtil.getIData(cfgCursor, "authServer");
		             if (authServer == null) {
		                 throw new ServiceException("Missing authServer configuration for THIRD_PARTY authorization.");
		             }
		             
		             IDataCursor authServerCursor = authServer.getCursor();
		             String jwksURI = IDataUtil.getString(authServerCursor, "jwksURI");
		             String audience = IDataUtil.getString(authServerCursor, "audience");
		             authServerCursor.destroy();
		             
		             // Validate JWKS URI format
		             if (jwksURI == null || jwksURI.trim().isEmpty()) {
		                 throw new ServiceException("JWKS URI cannot be null or empty for auth type \"THIRD_PARTY\". Use global variable \"wxmcp.jwks.uri\"");
		             }
		             try {
		                 new URL(jwksURI); // Valid URL test
		             } catch (MalformedURLException e) {
		                 throw new ServiceException("Invalid JWKS URI format: " + jwksURI);
		             }
		             
		             // Validate audience
		             if (audience == null || audience.trim().isEmpty()) {
		                 throw new ServiceException("Audience must not be null or empty for THIRD_PARTY authType.");
		             }
		        }
		
		        // Validate OAuth Auth
		        if ("OAUTH".equalsIgnoreCase(authType)) {
		        	/*
		            IData oauth = IDataUtil.getIData(mcpClientConfigCursor, "oauth");
		            if (oauth == null) {
		                throw new ServiceException("OAuth authentication requires an \"oauth\" object.");
		            }
		            IDataCursor oauthCursor = oauth.getCursor();
		            String clientID = IDataUtil.getString(oauthCursor, "clientID");
		            String tokenURL = IDataUtil.getString(oauthCursor, "tokenURL");
		            String audience = IDataUtil.getString(oauthCursor, "audience");
		            String scopes = IDataUtil.getString(oauthCursor, "scopes");
		            String bearerToken = IDataUtil.getString(oauthCursor, "bearerToken");
		            oauthCursor.destroy();
		            if (bearerToken == null || bearerToken.trim().isEmpty()) {
		                throw new ServiceException("OAuth authentication requires a non-empty \"oauth_bearer_token\".");
		            }		
		            */
		            /*
		            if (clientID == null || clientID.trim().isEmpty()) {
		                throw new ServiceException("OAuth authentication requires a non-empty \"oauth_client_id\".");
		            }
		            if (tokenURL == null || tokenURL.trim().isEmpty()) {
		                throw new ServiceException("OAuth authentication requires a non-empty \"oauth_token_url\".");
		            }
		            if (audience == null || audience.trim().isEmpty()) {
		                throw new ServiceException("OAuth authentication requires a non-empty \"oauth_audience\".");
		            }
		            if (scopes == null || scopes.trim().isEmpty()) {
		                throw new ServiceException("OAuth authentication requires a non-empty \"oauth_scopes\".");
		            }
		            */
		        }
		
		        mcpClientConfigCursor.destroy();
		    } else {
		        throw new ServiceException("\"mcpClientConfig\" object must not be NULL.");
		    }
		    pipelineCursor.destroy();
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
	        throw new RuntimeException("SHA-256 not available", e);
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


package wx.mcp.server.services;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
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
import wx.mcp.server.services.custom.OAS2MCPConverter;
import wx.mcp.server.models.*;
// --- <<IS-END-IMPORTS>> ---

public final class utils

{
	// ---( internal utility methods )---

	final static utils _instance = new utils();

	static utils _newInstance() { return new utils(); }

	static utils _cast(Object o) { return (utils)o; }

	// ---( server methods )---




	public static final void convertOASToMCP (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(convertOASToMCP)>> ---
		// @sigtype java 3.5
		// [i] field:0:required openAPIString
		// [i] field:0:required queryPrefix
		// [i] field:0:required headerPrefix
		// [i] field:0:required pathParamPrefix
		// [i] field:0:required mcpObjectName
		// [o] field:0:required toolJSONString
		IDataCursor pipelineCursor = pipeline.getCursor();
		String openAPIString = IDataUtil.getString(pipelineCursor, "openAPIString");
		String headerPrefix = IDataUtil.getString(pipelineCursor, "headerPrefix");
		String pathParamPrefix = IDataUtil.getString(pipelineCursor, "pathParamPrefix");
		String queryPrefix = IDataUtil.getString(pipelineCursor, "queryPrefix");
		String mcpObjectName = IDataUtil.getString(pipelineCursor, "mcpObjectName");
		
		OAS2MCPConverter mcpConverter = new OAS2MCPConverter();
		ListToolsResponse mcpTools = mcpConverter.generateMcpToolsFromOAS(openAPIString, headerPrefix, pathParamPrefix, queryPrefix, mcpObjectName);
		ObjectMapper jsonMapper = new ObjectMapper(new JsonFactory());
		jsonMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		jsonMapper.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
		 
		String result = null;
		try {
			result = jsonMapper.writeValueAsString(mcpTools);
		} catch(JsonProcessingException jpe) {
			throw new ServiceException(jpe);
		}
		IDataUtil.put(pipelineCursor, "toolJSONString", result);
		pipelineCursor.destroy();
		// --- <<IS-END>> ---

                
	}



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
		        break;		    	
		    	/*
		        if (aud == null || aud.trim().isEmpty() ||
		            clientId == null || clientId.trim().isEmpty() ||
		            scopes == null || scopes.trim().isEmpty()) {
		            throw new IllegalArgumentException("OAUTH authentication requires 'aud', 'clientId', and 'scopes' parameters");
		        }
		        // Sort scopes alphabetically
		        String[] scopeArray = scopes.trim().split("\\s+");
		        Arrays.sort(scopeArray);
		        String scopeString = String.join(" ", scopeArray);
		        String rawKey = aud.trim() + "|" + clientId.trim() + "|" + scopeString;
		        cacheKey = "OAUTH_" + sha256(rawKey);
		        break;
				*/
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



	public static final void extractOperationsFromOpenAPI (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(extractOperationsFromOpenAPI)>> ---
		// @sigtype java 3.5
		// [i] field:0:required openAPISpec
		// [o] field:0:required basePath
		// [o] field:0:required apiName
		// [o] field:0:required mcpObjectName
		// [o] object:0:required isIgnored
		// [o] record:1:required operations
		// [o] - field:0:required id
		// [o] - field:0:required method
		IDataCursor pipelineCursor = pipeline.getCursor();
		String openAPISpec = IDataUtil.getString(pipelineCursor, "openAPISpec");
		 
		try {
			JSONObject openAPI = new JSONObject(openAPISpec);
		
			// Extract basePath (OpenAPI 2.0) or servers[0].url (OpenAPI 3.0)
			String basePath = openAPI.optString("basePath", null);
			if (basePath == null && openAPI.has("servers")) {
				JSONArray servers = openAPI.getJSONArray("servers");
				if (servers.length() > 0) {
					basePath = servers.getJSONObject(0).optString("url", "");
				}
			}
			IDataUtil.put(pipelineCursor, "basePath", basePath != null ? basePath : "");
		
			// Extract apiName (use 'info.title' if available)
			String apiName = "";
			if (openAPI.has("info")) {
				apiName = openAPI.getJSONObject("info").optString("title", "");
			}
			IDataUtil.put(pipelineCursor, "apiName", apiName);
		
			 // --- Enhancement: Extract mcpObjectName and isIgnored ---
			String mcpObjectName = "";
			boolean isIgnored = false;
			// Check for tags at API level
			if (openAPI.has("tags")) {
				JSONArray tags = openAPI.getJSONArray("tags");
				for (int i = 0; i < tags.length(); i++) {
					JSONObject tagObj = tags.optJSONObject(i);
					if (tagObj != null && tagObj.has("name")) {
						String tagName = tagObj.getString("name");
						if (tagName.startsWith("mcp.object.name:")) {
							mcpObjectName = tagName.substring("mcp.object.name:".length()).trim();
						}
						if ("mcp.ignore".equals(tagName)) {
							isIgnored = true;
						}
					} else if (tags.get(i) instanceof String) {
						String tagName = tags.getString(i);
						if (tagName.startsWith("mcp.object.name:")) {
							mcpObjectName = tagName.substring("mcp.object.name:".length()).trim();
						}
						if ("mcp.ignore".equals(tagName)) {
							isIgnored = true;
						}
					}
				}
			}
			IDataUtil.put(pipelineCursor, "mcpObjectName", mcpObjectName);
			IDataUtil.put(pipelineCursor, "isIgnored", Boolean.valueOf(isIgnored));
		
			// Extract operations from 'paths'
			List<IData> operationsList = new ArrayList<>();
			Set<String> httpMethods = new HashSet<>(Arrays.asList(
			    "get", "put", "post", "delete", "options", "head", "patch", "trace"
			));
			if (openAPI.has("paths")) {
				JSONObject paths = openAPI.getJSONObject("paths");
				for (String path : paths.keySet()) {
					JSONObject methods = paths.getJSONObject(path);
					for (String method : methods.keySet()) {
						if (!httpMethods.contains(method.toLowerCase())) {
							continue; // skip non-method keys like 'summary', 'parameters'
						}
						JSONObject op = methods.getJSONObject(method);
						IData operation = IDataFactory.create();
						IDataCursor opCursor = operation.getCursor();
						
						// creating a default operationId if it is missing is done as well when creating the mcp tool specification
						// TODO: create a utility that is used both here and inside wx.mcp.server.services.custom.OAS2MCPConverter
						String operationId = op.optString("operationid");
						if (operationId == null || operationId.isBlank()) {
							String sanitizedPath = path.replaceAll("[{}\\/]", "_").replaceAll("_+", "_");
							operationId = method.toLowerCase() + "_" + sanitizedPath;
						}
						IDataUtil.put(opCursor, "id", operationId);
		//						IDataUtil.put(opCursor, "id", op.optString("operationId", path + "_" + method));
						IDataUtil.put(opCursor, "method", method.toLowerCase());
						IDataUtil.put(opCursor, "path", path);
						opCursor.destroy();
						operationsList.add(operation);
					}
				} 
			}
			IData[] operations = operationsList.toArray(new IData[0]);
			IDataUtil.put(pipelineCursor, "operations", operations);
		
		} catch (Exception e) {
			throw new ServiceException("Failed to parse OpenAPI spec: " + e.getMessage());
		} finally {
			pipelineCursor.destroy();
		}
		// --- <<IS-END>> ---

                
	}



	public static final void extractPathParamFromURL (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(extractPathParamFromURL)>> ---
		// @sigtype java 3.5
		// [i] field:0:required url
		// [i] field:0:optional dropPathElements {"/rest/v1"}
		// [o] field:0:required baseURL
		// [o] field:0:required pathParam
		// pipeline
		IDataCursor pipelineCursor = pipeline.getCursor();
		String	url = IDataUtil.getString( pipelineCursor, "url" );
		String	dropPathElements = IDataUtil.getString( pipelineCursor, "dropPathElements" );
		
		// Remove query parameters if present
		String noQuery = url.split("\\?")[0];
		
		// Find the last slash to extract the path parameter
		int lastSlash = noQuery.lastIndexOf('/');
		String pathParam = noQuery.substring(lastSlash + 1);
		
		// The base URL is everything before the last slash
		String baseURL = noQuery.substring(0, lastSlash);
		
		// Remove unneeded path elements
		if( dropPathElements != null){
		    if (baseURL.contains(dropPathElements)) {
		        baseURL = baseURL.replace(dropPathElements, "");
		    }
		}
		
		IDataUtil.put( pipelineCursor, "baseURL", baseURL );
		IDataUtil.put( pipelineCursor, "pathParam", pathParam);
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



	public static final void prepareHeaders (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(prepareHeaders)>> ---
		// @sigtype java 3.5
		// [i] record:1:required queryParams
		// [i] - field:0:required name
		// [i] - field:0:required value
		// [i] record:1:required pathParams
		// [i] - field:0:required name
		// [i] - field:0:required value
		// [i] record:1:required headers
		// [i] - field:0:required name
		// [i] - field:0:required value
		// [i] record:1:required additionalHeaders
		// [i] - field:0:required name
		// [i] - field:0:required value
		// [i] field:0:required basePath
		// [i] field:0:required relativePath
		// [o] field:0:required fullURL
		// [o] record:0:required effectiveHeaders
		IDataCursor pipelineCursor = pipeline.getCursor();
		 
		// 1. Merge headers and additionalHeaders into a single IData "headers"
		IData[] headersArr = IDataUtil.getIDataArray(pipelineCursor, "headers");
		IData[] additionalHeadersArr = IDataUtil.getIDataArray(pipelineCursor, "additionalHeaders");
		Map<String, String> headersMap = new LinkedHashMap<>();
		 
		// If headersArr is empty or null, just use additionalHeadersArr (if not empty)
		boolean headersArrEmpty = (headersArr == null || headersArr.length == 0);
		boolean additionalHeadersArrEmpty = (additionalHeadersArr == null || additionalHeadersArr.length == 0);
		
		if (!headersArrEmpty) {
			for (IData header : headersArr) {
				IDataCursor c = header.getCursor();
				String name = IDataUtil.getString(c, "name");
				String value = IDataUtil.getString(c, "value");
				if (name != null && value != null) headersMap.put(name, value);
				c.destroy();
			}
			if (!additionalHeadersArrEmpty) {
				for (IData header : additionalHeadersArr) {
					IDataCursor c = header.getCursor();
					String name = IDataUtil.getString(c, "name");
					String value = IDataUtil.getString(c, "value");
					if (name != null && value != null) headersMap.put(name, value);
					c.destroy();
				}
			}
		} else if (!additionalHeadersArrEmpty) {
			for (IData header : additionalHeadersArr) {
				IDataCursor c = header.getCursor();
				String name = IDataUtil.getString(c, "name");
				String value = IDataUtil.getString(c, "value");
				if (name != null && value != null) headersMap.put(name, value);
				c.destroy();
			}
		}
		
		IData headersOut = IDataFactory.create();
		IDataCursor headersOutCursor = headersOut.getCursor();
		for (Map.Entry<String, String> entry : headersMap.entrySet()) {
			IDataUtil.put(headersOutCursor, entry.getKey(), entry.getValue());
		}
		headersOutCursor.destroy();
		IDataUtil.put(pipelineCursor, "effectiveHeaders", headersOut);
		
		// 2. Build the full URL
		String basePath = IDataUtil.getString(pipelineCursor, "basePath");
		String relativePath = IDataUtil.getString(pipelineCursor, "relativePath");
		if (basePath == null) basePath = "";
		if (relativePath == null) relativePath = "";
		
		// Remove trailing slash from basePath and leading slash from relativePath
		if (basePath.endsWith("/")) basePath = basePath.substring(0, basePath.length() - 1);
		if (relativePath.startsWith("/")) relativePath = relativePath.substring(1);
		
		String url = basePath + "/" + relativePath;
		
		// Replace {path} parameters
		IData[] pathParamsArr = IDataUtil.getIDataArray(pipelineCursor, "pathParams");
		if (pathParamsArr != null) {
			for (IData param : pathParamsArr) {
				IDataCursor c = param.getCursor();
				String name = IDataUtil.getString(c, "name");
				String value = IDataUtil.getString(c, "value");
				if (name != null && value != null) {
					url = url.replace("{" + name + "}", encodeURIComponent(value));
				}
				c.destroy();
			}
		}
		
		// 3. Add query parameters
		IData[] queryParamsArr = IDataUtil.getIDataArray(pipelineCursor, "queryParams");
		StringBuilder queryString = new StringBuilder();
		if (queryParamsArr != null) {
			for (IData param : queryParamsArr) {
				IDataCursor c = param.getCursor();
				String name = IDataUtil.getString(c, "name");
				String value = IDataUtil.getString(c, "value");
				if (name != null && value != null) {
					if (queryString.length() == 0) {
						queryString.append("?");
					} else {
						queryString.append("&");
					}
					queryString.append(encodeURIComponent(name)).append("=").append(encodeURIComponent(value));
				}
				c.destroy();
			}
		}
		url += queryString.toString();
		
		IDataUtil.put(pipelineCursor, "fullURL", url);
		
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
		        if (toolCatalogBaseURL == null || toolCatalogBaseURL.trim().isEmpty()) {
		            throw new ServiceException("\"tool_catalog_base_url\" must not be NULL or empty.");
		        }
		        if (!isStrictlyValidURL(toolCatalogBaseURL)) {
		        	throw new ServiceException("\"tool_catalog_base_url\" is not a valid URL.");
		        }
		
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
		
		        // Validate OAuth Auth
		        if ("OAUTH".equalsIgnoreCase(authType)) {
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


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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
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
		    
		    String content = openAPIString;
		    JSONObject openApi = new JSONObject(content);
		    JSONObject components = null;
		    if (openApi.has("components")) {
		       components = openApi.getJSONObject("components");
		    }
		
		    JSONObject paths = openApi.getJSONObject("paths");
		    JSONArray tools = new JSONArray();
		    Set<String> httpMethods = Set.of("get", "post", "put", "delete", "patch", "head", "options");
		
		    for (String path : paths.keySet()) {
		        JSONObject methods = paths.getJSONObject(path);
		
		        for (String methodKey : methods.keySet()) {
		            if (!httpMethods.contains(methodKey.toLowerCase())) continue;
		
		            JSONObject op = methods.getJSONObject(methodKey);
		            if (!op.has("operationId")) continue;
		
		            String operationId = op.getString("operationId");
		            String summary = op.optString("summary", "No description");
		
		            // --- Build tool name and ID with mcpObjectName if present ---
		            String toolName;
		            if (mcpObjectName != null && !mcpObjectName.isEmpty()) {
		                toolName = mcpObjectName + "_" + operationId;
		            } else {
		                toolName = operationId;
		            }
		            String toolID;
		            if (mcpObjectName != null && !mcpObjectName.isEmpty()) {
		                toolID = mcpObjectName + "_" + operationId;
		            } else {
		                toolID = operationId;
		            }
		
		            // --- Input schema ---
		            JSONObject inputSchema = new JSONObject();
		            inputSchema.put("type", "object");
		            JSONObject inputProps = new JSONObject();
		            JSONArray requiredParams = new JSONArray();
		
		            // Collect parameters
		            JSONArray parameters = new JSONArray();
		            if (methods.has("parameters")) {
		                for (Object p : methods.getJSONArray("parameters")) {
		                    parameters.put(p);
		                }
		            }
		            if (op.has("parameters")) {
		                for (Object p : op.getJSONArray("parameters")) {
		                    parameters.put(p);
		                }
		            }
		
		            for (int i = 0; i < parameters.length(); i++) {
		                JSONObject param = parameters.getJSONObject(i);
		                // If the parameter is specified using an internal reference, this ref needs to be resolved before extracting the info
						if (param.has("$ref")) {
					        String ref = param.getString("$ref"); // e.g. "#/components/parameters/X-UnitTest-Param1"
					        String[] refParts = ref.replace("#/", "").split("/");
					
					        JSONObject resolved = openApi;
					        for (String part : refParts) {
					            resolved = resolved.getJSONObject(part);
					        }
					
					        extractParameterDetails(resolved, queryPrefix, headerPrefix, pathParamPrefix, components, inputProps, requiredParams);
					    } else {
		
					    	extractParameterDetails(param, queryPrefix, headerPrefix, pathParamPrefix, components, inputProps, requiredParams);
					    }
		            }
		
		         // --- Handle requestBody ---
		            
		            if (op.has("requestBody")) {
		                try {
		                    JSONObject bodySchema = op.getJSONObject("requestBody")
		                            .getJSONObject("content")
		                            .getJSONObject("application/json")
		                            .getJSONObject("schema");
		
		                    JSONObject resolvedBody = resolveSchema(bodySchema, components);
		                    JSONObject cleanedBody = cleanSchema(resolvedBody);
		
		                    // Ensure root schema is always an object
		                    if (!"object".equalsIgnoreCase(cleanedBody.optString("type"))) {
		                        JSONObject wrapper = new JSONObject();
		                        wrapper.put("type", "object");
		                        wrapper.put("properties", cleanedBody.optJSONObject("properties"));
		                        if (cleanedBody.has("required")) {
		                            wrapper.put("required", cleanedBody.getJSONArray("required"));
		                        }
		                        cleanedBody = wrapper;
		                    }
		
		                    // If no required array is specified, assume all properties are required
		                    if (!cleanedBody.has("required")) {
		                        JSONArray allRequired = new JSONArray();
		                        JSONObject bodyProps = cleanedBody.optJSONObject("properties");
		                        if (bodyProps != null) {
		                            for (String propName : bodyProps.keySet()) {
		                                allRequired.put(propName);
		                            }
		                        }
		                        cleanedBody.put("required", allRequired);
		                    }
		
		                    // Merge parameters properties with requestBody properties
		                    JSONObject mergedSchema = new JSONObject();
		                    mergedSchema.put("type", "object");
		
		                    // Merge inputProps (parameters)
		                    JSONObject mergedProps = new JSONObject();
		                    for (String key : inputProps.keySet()) {
		                        mergedProps.put(key, inputProps.get(key));
		                    }
		
		                    // Merge requestBody properties
		                    if (cleanedBody.has("properties")) {
		                        JSONObject bodyProps = cleanedBody.getJSONObject("properties");
		                        for (String key : bodyProps.keySet()) {
		                            mergedProps.put(key, bodyProps.get(key));
		                        }
		                    }
		                    mergedSchema.put("properties", mergedProps);
		
		                    // Merge required arrays (parameters + requestBody)
		                    JSONArray mergedRequired = new JSONArray();
		                    for (int i = 0; i < requiredParams.length(); i++) {
		                        mergedRequired.put(requiredParams.getString(i));
		                    }
		                    JSONArray bodyRequired = cleanedBody.optJSONArray("required");
		                    if (bodyRequired != null) {
		                        for (int i = 0; i < bodyRequired.length(); i++) {
		                            String req = bodyRequired.getString(i);
		                            if (!mergedRequired.toList().contains(req)) {
		                                mergedRequired.put(req);
		                            }
		                        }
		                    }
		
		                    if (mergedRequired.length() > 0) {
		                        mergedSchema.put("required", mergedRequired);
		                    }
		
		                    // Recursively propagate required from components if any
		                    propagateRequiredFromComponents(mergedSchema, components);
		
		                    inputSchema = mergedSchema;
		
		                } catch (Exception e) {
		                    System.err.println("Failed to resolve requestBody schema for "
		                                       + operationId + ": " + e.getMessage());
		                    // Fallback to parameters only schema if requestBody fails
		                    inputSchema.put("properties", inputProps);
		                    if (requiredParams.length() > 0) {
		                        inputSchema.put("required", requiredParams);
		                    }
		                }
		            } else {
		                // No requestBody, only parameters
		                inputSchema.put("properties", inputProps);
		                if (requiredParams.length() > 0) {
		                    inputSchema.put("required", requiredParams);
		                }
		            }
		
		
		            
		            /*
		            if (op.has("requestBody")) {
		                try {
		                    JSONObject bodySchema = op.getJSONObject("requestBody")
		                            .getJSONObject("content")
		                            .getJSONObject("application/json")
		                            .getJSONObject("schema");
		
		                    // Resolve $ref and merge OpenAPI-required info
		                    JSONObject resolvedBody = resolveSchema(bodySchema, components);
		                    JSONObject cleanedBody = cleanSchema(resolvedBody);
		
		                    // Ensure root schema is always an object
		                    if (!"object".equalsIgnoreCase(cleanedBody.optString("type"))) {
		                        JSONObject wrapper = new JSONObject();
		                        wrapper.put("type", "object");
		                        wrapper.put("properties", cleanedBody.optJSONObject("properties"));
		                        if (cleanedBody.has("required")) {
		                            wrapper.put("required", cleanedBody.getJSONArray("required"));
		                        }
		                        cleanedBody = wrapper;
		                    }
		
		                    // Preserve required array from OpenAPI if available
		                    if (resolvedBody.has("required")) {
		                        cleanedBody.put("required", resolvedBody.getJSONArray("required"));
		                    }
		                    
		                    if (cleanedBody.has("properties")) {
		                        JSONObject props = cleanedBody.getJSONObject("properties");
		                        JSONArray requiredArr = cleanedBody.optJSONArray("required");
		
		                        if (requiredArr == null) {
		                            requiredArr = new JSONArray();
		                            for (String propName : props.keySet()) {
		                                requiredArr.put(propName);
		                            }
		                            cleanedBody.put("required", requiredArr);
		                        } else {
		                            // Ensure all properties marked required in OpenAPI remain
		                            // and optionally add any missing ones
		                            for (String propName : props.keySet()) {
		                                if (!requiredArr.toList().contains(propName)) {
		                                    requiredArr.put(propName);
		                                }
		                            }
		                        }
		                    }
		
		                    // Recursively propagate required for nested $ref
		                    propagateRequiredFromComponents(cleanedBody, components);
		
		                    inputSchema = cleanedBody;
		
		                } catch (Exception e) {
		                    System.err.println("Failed to resolve requestBody schema for "
		                                       + operationId + ": " + e.getMessage());
		                }
		            } else {
		                inputSchema.put("properties", inputProps);
		                if (requiredParams.length() > 0) {
		                    inputSchema.put("required", requiredParams);
		                }
		            }
		
		*/
		
		         // --- Output schema ---
		            JSONObject outputSchema = new JSONObject();
		            JSONObject responses = op.getJSONObject("responses");  // <-- This must stay
		            boolean foundJsonResponse = false;
		
		            for (String status : responses.keySet()) {
		                JSONObject resp = responses.getJSONObject(status);
		                if (resp.has("content")) {
		                    JSONObject contentObj = resp.getJSONObject("content");
		                    if (contentObj.has("application/json")) {
		                        JSONObject schema = contentObj
		                                .getJSONObject("application/json")
		                                .getJSONObject("schema");
		                        JSONObject originalSchema = cleanSchema(resolveSchema(schema, components));
		                        JSONObject wrappedOutputSchema = new JSONObject();
		                        wrappedOutputSchema.put("type", "object");
		                        JSONObject props = new JSONObject();
		                        props.put("result", originalSchema);
		                        wrappedOutputSchema.put("properties", props);
		                        wrappedOutputSchema.put("required", new JSONArray().put("result"));
		                        outputSchema = wrappedOutputSchema;
		                        foundJsonResponse = true;
		                        break;
		                    }
		                }
		            }
		
		            if (!foundJsonResponse) {
		                outputSchema.put("type", "object");
		                outputSchema.put("properties", new JSONObject());
		            }
		
		            // --- Build tool object ---
		            JSONObject tool = new JSONObject();
		            tool.put("name", toolName);
		            tool.put("description", summary);
		            tool.put("inputSchema", inputSchema);
		            tool.put("outputSchema", outputSchema);
		
		            tools.put(tool);
		        }
		    }
		
		    JSONObject result = new JSONObject();
		    result.put("tools", tools);
		    IDataUtil.put(pipelineCursor, "toolJSONString", result.toString());
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
						IDataUtil.put(opCursor, "id", op.optString("operationId", path + "_" + method));
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
		
		        // Validate portalClientBaseURL
		        if (toolCatalogBaseURL == null || toolCatalogBaseURL.trim().isEmpty()) {
		            throw new ServiceException("\"tool_catalog_base_url\" must not be NULL or empty.");
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
	private static void extractParameterDetails(JSONObject param, String queryPrefix, String headerPrefix, String pathParamPrefix, JSONObject components, JSONObject inputProps, JSONArray requiredParams) {
		String name = param.getString("name");
	    String in = param.getString("in");
	    String propertyName;
	    switch (in) {
	        case "query":
	            propertyName = queryPrefix + name;
	            break;
	        case "header":
	            propertyName = headerPrefix + name;
	            break;
	        case "path":
	            propertyName = pathParamPrefix + name;
	            break;
	        default:
	            return;
	    }
	    JSONObject schema = resolveSchema(param.getJSONObject("schema"), components);
	    schema = cleanSchema(schema);
	
	    // If primitive type, only keep relevant keys
	    if (schema.has("type")
	            && !schema.getString("type").equals("object")
	            && !schema.getString("type").equals("array")) {
	        JSONObject primitive = new JSONObject();
	        primitive.put("type", schema.getString("type"));
	        if (schema.has("title")) primitive.put("title", schema.get("title"));
	        if (schema.has("maxLength")) primitive.put("maxLength", schema.get("maxLength"));
	        if (schema.has("minimum")) primitive.put("minimum", schema.get("minimum"));
	        if (schema.has("maximum")) primitive.put("maximum", schema.get("maximum"));
	        if (schema.has("enum")) primitive.put("enum", schema.get("enum"));
	        schema = primitive;
	    }
	    inputProps.put(propertyName, schema);
	    if (param.optBoolean("required", false)) {
	        requiredParams.put(propertyName);
	    }
	}
	
	/**
	 * Recursively traverses the schema and replaces/augments it with 'required' 
	 * properties from the referenced OpenAPI components (if missing).
	 * This ensures nested objects and arrays respect the original OpenAPI constraints.
	 */
	private static void propagateRequiredFromComponents(JSONObject schema, JSONObject components) {
	    if (schema == null) return;
	
	    // Handle $ref case
	    if (schema.has("$ref")) {
	        String ref = schema.getString("$ref");
	        String[] parts = ref.split("/");
	        if (parts.length >= 3 && "components".equals(parts[1]) && "schemas".equals(parts[2])) {
	            String name = parts[parts.length - 1];
	            if (components.has("schemas") && components.getJSONObject("schemas").has(name)) {
	                JSONObject compSchema = components.getJSONObject("schemas").getJSONObject(name);
	                if (compSchema.has("required") && !schema.has("required")) {
	                    schema.put("required", compSchema.getJSONArray("required"));
	                }
	            }
	        }
	    }
	
	    // Recurse into properties
	    if (schema.has("properties")) {
	        for (String key : schema.getJSONObject("properties").keySet()) {
	            JSONObject prop = schema.getJSONObject("properties").optJSONObject(key);
	            if (prop != null) {
	                propagateRequiredFromComponents(prop, components);
	            }
	        }
	    }
	
	    // Recurse into array items
	    if ("array".equals(schema.optString("type")) && schema.has("items")) {
	        JSONObject items = schema.optJSONObject("items");
	        if (items != null) {
	            propagateRequiredFromComponents(items, components);
	        }
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
	
	private static JSONObject components = new JSONObject();
	
	
	public static JSONObject resolveSchema(JSONObject schema, JSONObject components) {
	    if (schema.has("$ref")) {
	        String ref = schema.getString("$ref");
	        String[] parts = ref.replace("#/", "").split("/");
	
	        JSONObject current = components; // components comes from top-level OpenAPI
	
	        for (int i = 1; i < parts.length; i++) {
	            String part = parts[i];
	            if (current.has(part)) {
	                current = current.getJSONObject(part);
	            } else {
	                // Gracefully handle missing part of the ref
	                System.err.println("resolveSchema: Missing part '" + part + "' in $ref: " + ref);
	                return new JSONObject(); // return empty schema to avoid crash
	            }
	        }
	        return resolveSchema(current, components);
	    }
	
	    // Recursively resolve any nested schemas
	    JSONObject result = new JSONObject();
	    for (String key : schema.keySet()) {
	        Object val = schema.get(key);
	        if (val instanceof JSONObject) {
	            result.put(key, resolveSchema((JSONObject) val, components));
	        } else if (val instanceof JSONArray) {
	            JSONArray array = (JSONArray) val;
	            JSONArray newArray = new JSONArray();
	            for (int i = 0; i < array.length(); i++) {
	                Object item = array.get(i);
	                if (item instanceof JSONObject) {
	                    newArray.put(resolveSchema((JSONObject) item, components));
	                } else {
	                    newArray.put(item);
	                }
	            }
	            result.put(key, newArray);
	        } else {
	            result.put(key, val);
	        }
	    }
	    return result;
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
	
	/**
	 * Cleans a schema by removing properties with null values and fixing types.
	 * Also removes keys like "example" or "examples" if their value is null.
	 */
	private static JSONObject cleanSchema(JSONObject schema) {
	    JSONObject cleaned = new JSONObject();
	    for (String key : schema.keySet()) { 
	        Object val = schema.get(key);
	        // Remove nulls and "example"/"examples" with null value
	        if (val == null || JSONObject.NULL.equals(val)) {
	            continue;
	        }
	        if ((key.equals("example") || key.equals("examples")) && (val == null || JSONObject.NULL.equals(val))) {
	            continue;
	        }
	        if (val instanceof JSONObject) {
	            JSONObject sub = cleanSchema((JSONObject) val);
	            if (sub.length() > 0) cleaned.put(key, sub);
	        } else if (val instanceof JSONArray) {
	            JSONArray arr = (JSONArray) val;
	            JSONArray newArr = new JSONArray();
	            for (int i = 0; i < arr.length(); i++) {
	                Object item = arr.get(i);
	                if (item == null || JSONObject.NULL.equals(item)) continue;
	                if (item instanceof JSONObject) {
	                    JSONObject sub = cleanSchema((JSONObject) item);
	                    if (sub.length() > 0) newArr.put(sub);
	                } else {
	                    newArr.put(item);
	                }
	            }
	            if (newArr.length() > 0) cleaned.put(key, newArr);
	        } else {
	            cleaned.put(key, val);
	        }
	    }
	    // Fix type: if "type" is "object" but no "properties", remove "type"
	    if ("object".equals(cleaned.optString("type")) && !cleaned.has("properties")) {
	        cleaned.remove("type");
	    }
	    return cleaned;
	}
	// --- <<IS-END-SHARED>> ---
}


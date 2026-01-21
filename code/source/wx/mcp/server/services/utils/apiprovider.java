package wx.mcp.server.services.utils;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import com.wm.app.b2b.server.ServiceThread;
import com.wm.app.b2b.server.Server;
import com.wm.lang.ns.DependencyManager;
import com.wm.lang.ns.NSInterface;
import com.wm.lang.ns.NSName;
import com.wm.lang.ns.NSNode;
import com.wm.lang.ns.openapi.NSProviderDescriptor;
import com.wm.lang.ns.openapi.models.Operation;
import com.wm.lang.ns.openapi.models.PathItem;
import com.wm.lang.ns.rsd.RestTag;
import com.wm.net.HTTPMethod;
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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import wx.mcp.server.services.custom.OAS2MCPConverter;
import wx.mcp.server.models.*;
import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataUtil;
import com.wm.data.IDataFactory;
// --- <<IS-END-IMPORTS>> ---

public final class apiprovider

{
	// ---( internal utility methods )---

	final static apiprovider _instance = new apiprovider();

	static apiprovider _newInstance() { return new apiprovider(); }

	static apiprovider _cast(Object o) { return (apiprovider)o; }

	// ---( server methods )---




	public static final void checkOpenApiNodes (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(checkOpenApiNodes)>> ---
		// @sigtype java 3.5
		// [i] field:1:required nodePath
		// [o] field:1:required apiProviderPath
		// pipeline
		IDataCursor pipelineCursor = pipeline.getCursor();
		String[]	nodePaths = IDataUtil.getStringArray( pipelineCursor, "nodePath" );
		  
		ArrayList<String> nodeList = new ArrayList<String>();
		HashSet<String> inputPaths = new HashSet<String>();
		DependencyManager manager = NSDependencyManager.current();
		Namespace namespace = Namespace.current();
		if (nodePaths != null && nodePaths.length > 0) {
		    for (String path : nodePaths) {
		        if (path == null || path.isEmpty()) continue;
		
		        try {
		            NSName nsName = NSName.create(path);
		            NSNode node = namespace.getNode(nsName);
		            if (node instanceof NSInterface) {
		            	NSInterface folder = (NSInterface) node;
		                NSNode[] children = folder.getNodes();
		
		                if (children != null) {
		                    for (NSNode child : children) {
		                        String childName = child.getNSName().getFullName();
		                        nodeList.add(childName);
		                        inputPaths.add(childName);
		                    }
		                }
		            } else {
		                // If it's not a folder, still include it in the list
		                nodeList.add(path);
		                inputPaths.add(path);
		            }
		        } catch (Exception e) {
		            System.err.println("Error processing path: " + path);
		            e.printStackTrace();
		        }
		    }
		}
		
		HashSet<String> outputPaths = new HashSet<String>();
		for (String nodePath : inputPaths) {
		    if (nodePath == null || nodePath.isEmpty()) continue;
		
		    NSNode node = namespace.getNode(NSName.create(nodePath));
		    if (node == null) {
		        System.out.println("Node not found: " + nodePath);
		        continue;
		    }
		
		    IData results = null;
		    try {
		        results = manager.getDependent(node, null);
		    } catch (Exception e) {
		        System.err.println("Error retrieving dependents for: " + nodePath);
		        e.printStackTrace();
		        continue;
		    }
		
		    if (results != null) {
		        IDataCursor resultsCursor = results.getCursor();
		        IData[] referencedBy = IDataUtil.getIDataArray(resultsCursor, "referencedBy");
		        resultsCursor.destroy();
		
		        if (referencedBy != null) {
		            for (IData dependent : referencedBy) {
		                if (dependent == null) continue;
		
		                IDataCursor dependentCursor = dependent.getCursor();
		                String nodeName = IDataUtil.getString(dependentCursor, "name");
		                dependentCursor.destroy();
		
		                if (nodeName == null) continue;
		
		                nodeName = nodeName.trim();
		                String fqname = nodeName.contains("/") 
		                        ? nodeName.substring(nodeName.lastIndexOf('/') + 1) 
		                        : nodeName;
		
		                //System.out.println("Dependent name: " + fqname);
		
		                try {
		                    NSName nsName = NSName.create(fqname);
		                    NSNode dNode = namespace.getNode(nsName);
		
		                    if (dNode != null) {
		                        System.out.println("Class: " + dNode.getClass().getName());
		                        if (dNode instanceof com.wm.lang.ns.openapi.NSProviderDescriptor) {
		                        	outputPaths.add(fqname);
		                        }
		                    } else {
		                        //System.out.println("Dependent node not found: " + fqname);
		                    }
		                } catch (Exception e) {
		                    //System.err.println("Error processing dependent: " + fqname);
		                    e.printStackTrace();
		                }
		            }
		        } else {
		            System.out.println("No dependents found for: " + nodePath);
		        }
		    }
		}
		
		
		if (outputPaths.size() > 0) {
		    IDataUtil.put(pipelineCursor, "apiProviderPath", outputPaths.toArray(new String[0]));
		}
		pipelineCursor.destroy();
		// --- <<IS-END>> ---

                
	}



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
		// [i] field:0:required responseMode {"structured","text","both"}
		// [o] field:0:required toolJSONString
		IDataCursor pipelineCursor = pipeline.getCursor();
		String openAPIString = IDataUtil.getString(pipelineCursor, "openAPIString");
		String headerPrefix = IDataUtil.getString(pipelineCursor, "headerPrefix");
		String pathParamPrefix = IDataUtil.getString(pipelineCursor, "pathParamPrefix");
		String queryPrefix = IDataUtil.getString(pipelineCursor, "queryPrefix");
		String mcpObjectName = IDataUtil.getString(pipelineCursor, "mcpObjectName");
		String responseMode = IDataUtil.getString(pipelineCursor, "responseMode");
		
		OAS2MCPConverter mcpConverter = new OAS2MCPConverter();
		String result = mcpConverter.generateMcpToolStringFromOAS(openAPIString, headerPrefix, pathParamPrefix, queryPrefix, mcpObjectName, responseMode);
		
		IDataUtil.put(pipelineCursor, "toolJSONString", result);
		pipelineCursor.destroy();
		// --- <<IS-END>> ---

                
	}



	public static final void extractApiProvidersFromUrlTemplate (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(extractApiProvidersFromUrlTemplate)>> ---
		// @sigtype java 3.5
		// [i] field:1:required urlTemplates
		// [o] field:1:required apiProvidersPath
		// pipeline
		IDataCursor pipelineCursor = pipeline.getCursor();
		String[] urlTemplates = IDataUtil.getStringArray(pipelineCursor, "urlTemplates");
		pipelineCursor.destroy();
		
		String[] apiProvidersPath = null;
		if (urlTemplates != null && urlTemplates.length > 0) {
		    java.util.Set<String> uniqueProviders = new java.util.HashSet<String>();
		    
		    for (String template : urlTemplates) {
		        if (template != null && template.contains("_")) {
		            // Split by first underscore to get HTTP method and the rest
		            String[] parts = template.split("_", 2);
		            if (parts.length == 2) {
		                String rest = parts[1];
		                // Find first slash after apiProviderPath
		                int firstSlash = rest.indexOf("/");
		                if (firstSlash > 0) {
		                    String apiProviderPath = rest.substring(0, firstSlash);
		                    uniqueProviders.add(apiProviderPath);
		                }
		            }
		        }
		    }
		    
		    apiProvidersPath = uniqueProviders.toArray(new String[uniqueProviders.size()]);
		}
		
		// pipeline
		IDataCursor pipelineCursor_out = pipeline.getCursor();
		IDataUtil.put(pipelineCursor_out, "apiProvidersPath", apiProvidersPath);
		pipelineCursor_out.destroy();
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
						String operationId = op.optString("operationId");
						if (operationId == null || operationId.isBlank()) {
							String sanitizedPath = path.replaceAll("[{}\\/]", "_").replaceAll("_+", "_");
							operationId = method.toLowerCase() + "_" + sanitizedPath;
						}
						IDataUtil.put(opCursor, "id", operationId);
						// IDataUtil.put(opCursor, "id", op.optString("operationId", path + "_" + method));
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



	public static final void filterApiOperationsByScopes (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(filterApiOperationsByScopes)>> ---
		// @sigtype java 3.5
		// [i] field:1:required scopes
		// [i] field:0:required openAPISpec
		// [o] field:0:required scopedOpenAPISpec
		// [o] record:1:required outOfScopeOperations
		// [o] - field:0:required id
		// [o] - field:0:required path
		// [o] record:1:required inScopeOperations
		// [o] - field:0:required id
		// [o] - field:0:required path
		IDataCursor pipelineCursor = pipeline.getCursor();
		        String[] scopes = IDataUtil.getStringArray(pipelineCursor, "scopes");
		        String openAPISpec = IDataUtil.getString(pipelineCursor, "openAPISpec");
		        pipelineCursor.destroy();
		
		        if (openAPISpec == null || openAPISpec.trim().isEmpty()) {
		            outputEmptyResults(pipeline, openAPISpec);
		            return;
		        }
		
		        ObjectMapper mapper = new ObjectMapper();
		        JsonNode root;
		        try {
		            root = mapper.readTree(openAPISpec);
		        } catch (Exception e) {
		            throw new ServiceException("Invalid OpenAPI JSON: " + e.getMessage());
		        }
		
		        if (scopes == null || scopes.length == 0) {
		            outputEmptyResults(pipeline, openAPISpec);
		            return;
		        }
		
		        JsonNode pathsNode = root.path("paths");
		        if (!pathsNode.isObject()) {
		            outputEmptyResults(pipeline, openAPISpec);
		            return;
		        }
		
		        List<IData> inScopeList = new ArrayList<>();
		        List<IData> outOfScopeList = new ArrayList<>();
		        ObjectNode newPaths = mapper.createObjectNode();
		
		        // Valid HTTP methods only
		        String[] validHttpMethods = {"GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS"};
		
		        Iterator<Map.Entry<String, JsonNode>> pathIter = ((ObjectNode) pathsNode).fields();
		        while (pathIter.hasNext()) {
		            Map.Entry<String, JsonNode> pathEntry = pathIter.next();
		            String pathKey = pathEntry.getKey();
		            JsonNode methodsNode = pathEntry.getValue();
		            if (!methodsNode.isObject()) continue;
		
		            ObjectNode allowedMethods = mapper.createObjectNode();
		            Iterator<Map.Entry<String, JsonNode>> methodIter = ((ObjectNode) methodsNode).fields();
		            while (methodIter.hasNext()) {
		                Map.Entry<String, JsonNode> methodEntry = methodIter.next();
		                String httpMethodKey = methodEntry.getKey().toUpperCase();
		                
		                // Skip non-HTTP methods (summary, parameters, etc.)
		                boolean isValidHttpMethod = false;
		                for (String validMethod : validHttpMethods) {
		                    if (httpMethodKey.equals(validMethod)) {
		                        isValidHttpMethod = true;
		                        break;
		                    }
		                }
		                if (!isValidHttpMethod) {
		                    continue;
		                }
		
		                String httpMethod = httpMethodKey;
		                JsonNode operation = methodEntry.getValue();
		
		                String operationId = getOperationId(operation);
		                if (operationId == null) {
		                    throw new ServiceException("Missing operationId in " + httpMethod + " " + pathKey);
		                }
		
		                boolean isInScope = hasMatchingScope(operation, scopes, mapper);
		                String operationPath = httpMethod + "_" + pathKey;
		
		                IData opRecord = IDataFactory.create();
		                IDataCursor opCursor = opRecord.getCursor();
		                IDataUtil.put(opCursor, "id", operationId);
		                IDataUtil.put(opCursor, "path", operationPath);
		                opCursor.destroy();
		
		                if (isInScope) {
		                    allowedMethods.set(httpMethod.toLowerCase(), operation);
		                    inScopeList.add(opRecord);
		                } else {
		                    outOfScopeList.add(opRecord);
		                }
		            }
		
		            if (allowedMethods.size() > 0) {
		                newPaths.set(pathKey, allowedMethods);
		            }
		        }
		
		        ((ObjectNode) root).set("paths", newPaths);
		        String filteredSpec;
		        try {
		            filteredSpec = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
		        } catch (Exception e) {
		            throw new ServiceException("Failed to serialize filtered spec: " + e.getMessage());
		        }
		
		        IDataCursor outCursor = pipeline.getCursor();
		        IDataUtil.put(outCursor, "scopedOpenAPISpec", filteredSpec);
		        IDataUtil.put(outCursor, "inScopeOperations", inScopeList.toArray(new IData[0]));
		        IDataUtil.put(outCursor, "outOfScopeOperations", outOfScopeList.toArray(new IData[0]));
		        outCursor.destroy();
		// --- <<IS-END>> ---

                
	}



	public static final void getOASNodeDetails (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(getOASNodeDetails)>> ---
		// @sigtype java 3.5
		// [i] field:0:required oasNodePath
		// [o] field:0:required apiName
		// [o] field:0:required apiVersion
		// [o] field:0:required description
		// [o] field:0:required baseURL
		// [o] field:1:required tags
		// [o] recref:1:required operations wx.mcp.server.doctypes:Operation
		// pipeline
		IDataCursor pipelineCursor = pipeline.getCursor();
		String	oasNodePath = IDataUtil.getString( pipelineCursor, "oasNodePath" );
		 
		Namespace namespace = Namespace.current();
		NSName nsName = NSName.create(oasNodePath);
		NSNode node = namespace.getNode(nsName);
		 
		if( node instanceof com.wm.lang.ns.openapi.NSProviderDescriptor){
			NSProviderDescriptor oasProvider = (NSProviderDescriptor) node;
			IDataUtil.put( pipelineCursor, "apiName", oasProvider.getInfo().getTitle());
			IDataUtil.put( pipelineCursor, "apiVersion", oasProvider.getInfo().getVersion() );
			IDataUtil.put( pipelineCursor, "description", ((oasProvider.getInfo().getDescription()== null) ? "" : oasProvider.getInfo().getDescription()));
			String[] tags = new String[0];
			Map<String, RestTag> _tags = oasProvider.getRestTags();
			if( _tags != null){
				tags = _tags.keySet().toArray(new String[]{});
			}
			IDataUtil.put( pipelineCursor, "tags", tags );
			String baseURL = "";
			List<com.wm.lang.ns.openapi.models.Server> apiServers = oasProvider.getServers();
			for( com.wm.lang.ns.openapi.models.Server apiServer : apiServers){
				String desc = apiServer.getDescription();
				if( (desc != null) && desc.equals( "This is a system generated server")){
					baseURL = apiServer.getUrl();
				}
				
			}
			IDataUtil.put( pipelineCursor, "baseURL", baseURL);
			
			// Get all path items from OpenAPI provider
			Map<String, PathItem> pathMap = oasProvider.getPaths();
			ArrayList<IData> operations = new ArrayList<IData>();
			for (Map.Entry<String, PathItem> pathEntry : pathMap.entrySet()) {
				
				Map<HTTPMethod, Operation> operationsMap = pathEntry.getValue().getOperations();
				
				for (Map.Entry<HTTPMethod, Operation> operation : operationsMap.entrySet()) {
				    String httpMethod = operation.getKey().getName();
				    Operation op = operation.getValue();
				   
				    IData operationDoc = IDataFactory.create();
				    IDataCursor opCursor = operationDoc.getCursor();
				    
				    IDataUtil.put(opCursor, "id", op.getOperationId());
				    IDataUtil.put(opCursor, "urlTemplateId", httpMethod+"_" + oasNodePath + op.getPath());
				    
				    IDataUtil.put(opCursor, "httpMethod", httpMethod);
				    IDataUtil.put(opCursor, "path", op.getPath().toString());
		
				    IDataUtil.put(opCursor, "isFlow", op.getServiceNSName().toString());
				    
				    operations.add(operationDoc);
				       
				    opCursor.destroy();
			    
				}
			}
		
			IDataUtil.put( pipelineCursor, "operations", operations.toArray(new IData[]{}) );
			
		}
		pipelineCursor.destroy();
		// --- <<IS-END>> ---

                
	}



	public static final void getOpenApiSpec (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(getOpenApiSpec)>> ---
		// @sigtype java 3.5
		// [i] field:0:required radName
		// [o] field:0:required openApiString
		// 1. Prepare input IData for the service call (isolated from the main pipeline)
		IData input = IDataFactory.create();
		IDataCursor inputCursor = input.getCursor();
		
		// Access the incoming pipeline to read input parameters
		IDataCursor pipelineCursor = pipeline.getCursor();
		String radName = IDataUtil.getString(pipelineCursor, "radName");
		pipelineCursor.destroy();
		
		// Set parameters for the OpenAPI service
		IDataUtil.put(inputCursor, "radName", radName);
		IDataUtil.put(inputCursor, "openapi.json", "openapi.json"); 
		inputCursor.destroy();
		
		try {
		    // 2. Invoke the service
		    // Note: wm.server.openapi:getOpenAPIDoc is designed to return the 
		    // JSON as 'responseString' in the pipeline, even if it sets a response stream.
		    ServiceThread st = Service.doThreadInvoke("wm.server.openapi", "getOpenAPIDoc", Service.getSession(), input);
		    IData output = st.getIData();
		    // 3. Extract the result from the output pipeline
		    IDataCursor outputCursor = output.getCursor();
		    String openAPIString = IDataUtil.getString(outputCursor, "responseString");
		    outputCursor.destroy();
		
		    // 4. Write the result back into the main pipeline
		    pipelineCursor = pipeline.getCursor();
		    IDataUtil.put(pipelineCursor, "openApiString", openAPIString);
		    pipelineCursor.destroy();
		
		} catch (Exception e) {
		    // 5. Wrap and throw any occurring exceptions
		    throw new ServiceException("Failed to retrieve OpenAPI document: " + e.getMessage());
		}
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

	// --- <<IS-START-SHARED>> ---
	  
		    private static String getOperationId(JsonNode operation)
		    {
		        JsonNode opIdNode = operation.path("operationId");
		        return opIdNode.isTextual() ? opIdNode.asText() : null;
		    }
	
		    private static boolean hasMatchingScope(JsonNode operation, String[] allowedScopes, ObjectMapper mapper) {
		        JsonNode securityArray = operation.path("security");
		        if (!securityArray.isArray() || securityArray.size() == 0) return false;
	
		        for (JsonNode secObj : securityArray) {
		            // Handle ALL possible security scheme name variations
		            JsonNode oauth2Array = secObj.get("oauth2");
		            if (oauth2Array == null) oauth2Array = secObj.get("OAuth2");
		            if (oauth2Array == null) oauth2Array = secObj.get("OAUTH2");
		            
		            if (oauth2Array != null && oauth2Array.isArray()) {
		                for (JsonNode scopeNode : oauth2Array) {
		                    String scope = scopeNode.asText();
		                    for (String allowed : allowedScopes) {
		                        if (scope.equals(allowed)) return true;
		                    }
		                }
		            }
		        }
		        return false;
		    }
	
		    private static void outputEmptyResults(IData pipeline, String openAPISpec) {
		        IDataCursor outCursor = pipeline.getCursor();
		        IDataUtil.put(outCursor, "scopedOpenAPISpec", openAPISpec);
		        IDataUtil.put(outCursor, "inScopeOperations", new IData[0]);
		        IDataUtil.put(outCursor, "outOfScopeOperations", new IData[0]);
		        outCursor.destroy();
		    }
	
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


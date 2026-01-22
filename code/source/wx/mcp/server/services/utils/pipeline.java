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
import com.wm.lang.ns.NSField;
import com.wm.lang.ns.NSInterface;
import com.wm.lang.ns.NSName;
import com.wm.lang.ns.NSNode;
import com.wm.lang.ns.NSPackage;
import com.wm.lang.ns.NSRecord;
import com.wm.lang.ns.NSService;
import com.wm.lang.ns.NSSignature;
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
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import wx.mcp.server.services.custom.OAS2MCPConverter;
import wx.mcp.server.models.*;
// --- <<IS-END-IMPORTS>> ---

public final class pipeline

{
	// ---( internal utility methods )---

	final static pipeline _instance = new pipeline();

	static pipeline _newInstance() { return new pipeline(); }

	static pipeline _cast(Object o) { return (pipeline)o; }

	// ---( server methods )---




	public static final void createISPipelineForInvocation (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(createISPipelineForInvocation)>> ---
		// @sigtype java 3.5
		// [i] record:0:required arguments
		// [i] field:0:required isFlowPath
		// [i] record:1:optional queryParams
		// [i] - field:0:required name
		// [i] - field:0:required value
		// [i] record:1:optional pathParams
		// [i] - field:0:required name
		// [i] - field:0:required value
		// [i] record:1:optional headers
		// [i] - field:0:required name
		// [i] - field:0:required value
		// [o] record:0:required mcpPipeline
		// [o] - record:0:optional params
		// [o] -- record:0:optional path
		// [o] -- record:0:optional query
		// [o] -- record:0:optional header
		// [o] - record:0:required requestBody
		IDataCursor pipelineCursor = pipeline.getCursor();
		    
		// --- Extract input arrays ---
		IData[] queryParams = IDataUtil.getIDataArray(pipelineCursor, "queryParams");
		IData[] pathParams = IDataUtil.getIDataArray(pipelineCursor, "pathParams");
		IData[] headers = IDataUtil.getIDataArray(pipelineCursor, "headers");
		
		// --- arguments and flow path ---
		IData arguments = IDataUtil.getIData(pipelineCursor, "arguments");
		String isFlowPath = IDataUtil.getString(pipelineCursor, "isFlowPath");
		
		// create requestBody
		IData requestBody = IDataFactory.create();
		String fieldName = null;
		
		// --- Flow introspection to determine fieldName under requestBody ---
		try {
		    if (isFlowPath != null && !isFlowPath.trim().isEmpty()) {
		        NSName nsName = NSName.create(isFlowPath);
		        NSService service = Namespace.getService(nsName);
		
		        if (service != null) {
		            NSSignature sig = service.getSignature();
		            if (sig != null) {
		                NSRecord inSpecRecord = sig.getInput();
		                if (inSpecRecord != null) {
		                    NSField[] fields = inSpecRecord.getFields();
		                    for (NSField field : fields) {
		                        if ("requestBody".equals(field.getName())) {
		                            if (field instanceof NSRecord) {
		                                NSField[] subFields = ((NSRecord) field).getFields();
		                                if (subFields != null && subFields.length > 0) {
		                                    fieldName = subFields[0].getName();
		                                }
		                            }
		                            break;
		                        }
		                    }
		                }
		            }
		        }else{
		        	throw new ServiceException( "IS Flow (MCP Tool) does not exist: " + isFlowPath);
		        }
		    }
		} catch (Exception e) {
		    throw new ServiceException( "Error during introspecting signature of IS Flow (MCP Tool): " + isFlowPath);
		}
		
		if (fieldName == null) fieldName = "request";
		
		// --- Copy arguments to requestBody/<fieldName> ---
		IData targetDoc = IDataFactory.create();
		if (arguments != null) {
		    IDataUtil.append(arguments, targetDoc);
		}
		
		IDataCursor rbCursor = requestBody.getCursor();
		IDataUtil.put(rbCursor, fieldName, targetDoc);
		rbCursor.destroy();
		
		// --- Construct mcpPipeline ---
		IData mcpPipeline = IDataFactory.create();
		IDataCursor mcpPipelineCursor = mcpPipeline.getCursor();
		
		IData params = IDataFactory.create();
		IDataCursor paramsCursor = params.getCursor();
		
		// --- Populate query params ---
		boolean hasQueryParams = false;
		if (queryParams != null && queryParams.length > 0) {
		    IData query = IDataFactory.create();
		    IDataCursor qCur = query.getCursor();
		    for (IData pair : queryParams) {
		        IDataCursor c = pair.getCursor();
		        String name = IDataUtil.getString(c, "name");
		        String value = IDataUtil.getString(c, "value");
		        if (name != null && value != null && !name.trim().isEmpty() && !value.trim().isEmpty()) {
		            IDataUtil.put(qCur, name, value);
		            hasQueryParams = true;
		        }
		        c.destroy();
		    }
		    qCur.destroy();
		    if (hasQueryParams) {
		        IDataUtil.put(paramsCursor, "query", query);
		    }
		}
		
		// --- Populate path params ---
		boolean hasPathParams = false;
		if (pathParams != null && pathParams.length > 0) {
		    IData path = IDataFactory.create();
		    IDataCursor pCur = path.getCursor();
		    for (IData pair : pathParams) {
		        IDataCursor c = pair.getCursor();
		        String name = IDataUtil.getString(c, "name");
		        String value = IDataUtil.getString(c, "value");
		        if (name != null && value != null && !name.trim().isEmpty() && !value.trim().isEmpty()) {
		            IDataUtil.put(pCur, name, value);
		            hasPathParams = true;
		        }
		        c.destroy();
		    }
		    pCur.destroy();
		    if (hasPathParams) {
		        IDataUtil.put(paramsCursor, "path", path);
		    }
		}
		
		// --- Populate header params ---
		boolean hasHeaderParams = false;
		if (headers != null && headers.length > 0) {
		    IData header = IDataFactory.create();
		    IDataCursor hCur = header.getCursor();
		    for (IData pair : headers) {
		        IDataCursor c = pair.getCursor();
		        String name = IDataUtil.getString(c, "name");
		        String value = IDataUtil.getString(c, "value");
		        if (name != null && value != null && !name.trim().isEmpty() && !value.trim().isEmpty()) {
		            IDataUtil.put(hCur, name, value);
		            hasHeaderParams = true;
		        }
		        c.destroy();
		    } 
		    hCur.destroy();
		    if (hasHeaderParams) {
		        IDataUtil.put(paramsCursor, "header", header);
		    }
		}
		
		paramsCursor.destroy();
		
		IDataUtil.put(mcpPipelineCursor, "params", params);
		IDataUtil.put(mcpPipelineCursor, "requestBody", requestBody);
		mcpPipelineCursor.destroy();
		  
		IDataUtil.put(pipelineCursor, "mcpPipeline", mcpPipeline);
		pipelineCursor.destroy();
		// --- <<IS-END>> ---

                
	}



	public static final void extractResponseFromPipeline (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(extractResponseFromPipeline)>> ---
		// @sigtype java 3.5
		// [i] record:0:required pipeline
		// [o] record:0:required toolResponse
		// [o] field:0:required toolResponseCode
		IDataCursor pipelineCursor = pipeline.getCursor();
		
		// Input pipeline
		IData _pipeline = IDataUtil.getIData(pipelineCursor, "pipeline");
		
		// Output toolResponse - DIRECTLY contains response payload
		IData toolResponse = IDataFactory.create();
		IDataCursor toolResponseCursor = toolResponse.getCursor();
		
		String toolResponseCode = null;
		
		if (_pipeline != null) {
		    IDataCursor pipeCursor = _pipeline.getCursor();
		    
		    // Iterate through HTTP status codes (200, 201, 400, etc.)
		    while (pipeCursor.next()) {
		        String httpCodeKey = pipeCursor.getKey();
		        
		        if (httpCodeKey.matches("\\d{3}")) {
		            Object statusObj = pipeCursor.getValue();
		            if (statusObj instanceof IData) {
		                IData statusData = (IData) statusObj;
		                IDataCursor statusCursor = statusData.getCursor();
		                
		                // Look for children under HTTP code
		                while (statusCursor.next()) {
		                    String childKey = statusCursor.getKey();
		                    Object childObj = statusCursor.getValue();
		                    
		                    boolean copiedData = false;
		                    
		                    if (childObj instanceof IData) {
		                        // CASE 1: Single IData \u2192 copy ALL fields directly to toolResponse
		                        IData singleData = (IData) childObj;
		                        IDataCursor singleCursor = singleData.getCursor();
		                        while (singleCursor.next()) {
		                            String fieldName = singleCursor.getKey();
		                            Object fieldValue = singleCursor.getValue();
		                            IDataUtil.put(toolResponseCursor, fieldName, fieldValue);
		                        }
		                        singleCursor.destroy();
		                        copiedData = true;
		                        
		                    } else if (childObj instanceof IData[]) {
		                        // CASE 2: IData[] \u2192 copy as-is
		                        IData[] arrayData = (IData[]) childObj;
		                        IDataUtil.put(toolResponseCursor, "$rootArray", arrayData);
		                        copiedData = true;
		                        
		                    } else if (childObj instanceof Object[]) {
		                        // CASE 3: Mixed Object[] \u2192 copy as-is
		                        Object[] mixedArray = (Object[]) childObj;
		                        IDataUtil.put(toolResponseCursor, "$rootArray", mixedArray);
		                        copiedData = true;
		                    }
		                    
		                    if (copiedData) {
		                        toolResponseCode = httpCodeKey;
		                        break; // First successful extraction found
		                    }
		                }
		                statusCursor.destroy();
		                
		                if (toolResponseCode != null) {
		                    break; // Stop after successful extraction
		                }
		            }
		        }
		    }
		    pipeCursor.destroy();
		}
		
		toolResponseCursor.destroy();
		IDataUtil.put(pipelineCursor, "toolResponse", toolResponse);
		IDataUtil.put(pipelineCursor, "toolResponseCode", toolResponseCode);
		pipelineCursor.destroy();
		// --- <<IS-END>> ---

                
	}



	public static final void getFlowDetails (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(getFlowDetails)>> ---
		// @sigtype java 3.5
		// [i] field:0:required isFlow
		// [o] record:0:optional is
		// [o] - field:0:required ifcname
		// [o] - field:0:required svcname
		// pipeline
		IDataCursor pipelineCursor = pipeline.getCursor();
		String isFlow = IDataUtil.getString(pipelineCursor, "isFlow");
		
		String ifcname = null;
		String svcname = null;
		
		if (isFlow != null && isFlow.contains(":")) {
		    String[] parts = isFlow.split(":", 2);
		    ifcname = parts[0].trim();
		    svcname = parts[1].trim();
		} else {
		    throw new ServiceException("fullName must be in format 'ifcname:svcname'");
		}
		
		IData is = IDataFactory.create();
		IDataCursor isCursor = is.getCursor();
		IDataUtil.put(isCursor, "ifcname", ifcname);
		IDataUtil.put(isCursor, "svcname", svcname);
		isCursor.destroy();
		
		IDataUtil.put(pipelineCursor, "is", is);
		pipelineCursor.destroy();
		// --- <<IS-END>> ---

                
	}



	public static final void invokeFlowService (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(invokeFlowService)>> ---
		// @sigtype java 3.5
		// [i] record:0:required inputPipeline
		// [i] record:0:optional flowDetails
		// [i] - field:0:required ifcname
		// [i] - field:0:required svcname
		// [o] record:0:required outputPipeline
		// pipeline
		IDataCursor pipelineCursor = pipeline.getCursor();
		
		// flowDetails
		IData flowDetails = IDataUtil.getIData(pipelineCursor, "flowDetails");
		String ifcname = null;
		String svcname = null;
		
		if (flowDetails != null) {
		    IDataCursor flowDetailsCursor = flowDetails.getCursor();
		    ifcname = IDataUtil.getString(flowDetailsCursor, "ifcname");
		    svcname = IDataUtil.getString(flowDetailsCursor, "svcname");
		    flowDetailsCursor.destroy();
		}
		
		// inputPipeline (original source)
		IData inputPipeline = IDataUtil.getIData(pipelineCursor, "inputPipeline");
		
		// --- create an isolated IData for invocation ---
		IData input = IDataFactory.create();
		IDataCursor inputCursor = input.getCursor();
		
		if (inputPipeline != null) {
		    // shallow clone: replicates the top-level fields
		    IDataUtil.merge(inputPipeline, input);
		}
		
		inputCursor.destroy();
		
		IData output = null;
		
		try {
		    // invoke dynamically
		    ServiceThread st = Service.doThreadInvoke(ifcname, svcname, Service.getSession(), input);
		    output = st.getIData();
		} catch (Exception e) {
		    throw new ServiceException("Failed to invoke flow service " + ifcname + ":" + svcname + ": " + e.getMessage());
		}
		
		// outputPipeline
		IDataUtil.put(pipelineCursor, "outputPipeline", output);
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


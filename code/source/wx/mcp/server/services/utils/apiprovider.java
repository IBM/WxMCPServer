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
import wx.mcp.server.services.custom.OAS2MCPConverter;
import wx.mcp.server.models.*;
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


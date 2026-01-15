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

public final class oauth

{
	// ---( internal utility methods )---

	final static oauth _instance = new oauth();

	static oauth _newInstance() { return new oauth(); }

	static oauth _cast(Object o) { return (oauth)o; }

	// ---( server methods )---




	public static final void extractBearerToken (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(extractBearerToken)>> ---
		// @sigtype java 3.5
		// [i] field:0:required bearerToken
		// [o] field:0:required token
		// pipeline
		IDataCursor pipelineCursor = pipeline.getCursor();
		String bearerToken = IDataUtil.getString(pipelineCursor, "bearerToken");
		
		String token = bearerToken;
		if (token != null) {
		    token = token.trim().toLowerCase();
		    if (token.startsWith("bearer ")) {
		        token = token.substring(7);
		    }
		}
		
		IDataUtil.put(pipelineCursor, "token", token);
		pipelineCursor.destroy();
		// --- <<IS-END>> ---

                
	}



	public static final void introspectToken (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(introspectToken)>> ---
		// @sigtype java 3.5
		// [i] field:0:required token
		// [i] field:0:optional token_type_hint
		// [o] field:0:required active
		// [o] field:0:optional token_type
		// [o] field:0:optional scope
		// [o] field:0:optional client_id
		// [o] field:0:optional owner_id
		// [o] field:0:optional iat
		// [o] field:0:optional exp
		// pipeline
		IDataCursor pipelineCursor = pipeline.getCursor();
		String	token = IDataUtil.getString( pipelineCursor, "token" );
		String	token_type_hint = IDataUtil.getString( pipelineCursor, "token_type_hint" );
		
		
		// input
		IData input = IDataFactory.create();
		IDataCursor inputCursor = input.getCursor();
		IDataUtil.put( inputCursor, "token", token );
		IDataUtil.put( inputCursor, "token_type_hint", token_type_hint );
		inputCursor.destroy();
		
		// output
		IData 	output = IDataFactory.create();
		try{
			output = Service.doInvoke( "pub.oauth", "introspectToken", input );
		}catch( Exception e){}
		IDataCursor outputCursor = output.getCursor();
		String	active = IDataUtil.getString( outputCursor, "active" );
		String	token_type = IDataUtil.getString( outputCursor, "token_type" );
		String	scope = IDataUtil.getString( outputCursor, "scope" );
		String	client_id = IDataUtil.getString( outputCursor, "client_id" );
		String	owner_id = IDataUtil.getString( outputCursor, "owner_id" );
		String	iat = IDataUtil.getString( outputCursor, "iat" );
		String	exp = IDataUtil.getString( outputCursor, "exp" );
		outputCursor.destroy();
		
		// pipeline
		IDataCursor pipelineCursor_1 = pipeline.getCursor();
		IDataUtil.put( pipelineCursor, "active", active);
		IDataUtil.put( pipelineCursor, "token_type", token_type );
		IDataUtil.put( pipelineCursor, "scope", scope );
		IDataUtil.put( pipelineCursor, "client_id", client_id );
		IDataUtil.put( pipelineCursor, "owner_id", owner_id );
		IDataUtil.put( pipelineCursor, "iat", iat );
		IDataUtil.put( pipelineCursor, "exp", exp );
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


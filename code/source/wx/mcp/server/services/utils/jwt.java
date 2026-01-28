package wx.mcp.server.services.utils;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import java.util.Arrays;
import org.json.JSONArray;
import java.util.*;
import java.security.*;
import java.security.spec.*;
import java.util.Base64;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import org.json.*;
// --- <<IS-END-IMPORTS>> ---

public final class jwt

{
	// ---( internal utility methods )---

	final static jwt _instance = new jwt();

	static jwt _newInstance() { return new jwt(); }

	static jwt _cast(Object o) { return (jwt)o; }

	// ---( server methods )---




	public static final void verifyJWTToken (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(verifyJWTToken)>> ---
		// @sigtype java 3.5
		// [i] field:0:required aud
		// [i] field:0:required jwksContent
		// [i] field:0:required bearerToken
		// [o] object:0:required isValid
		// [o] field:0:optional reason
		// [o] field:0:required issuer
		// [o] field:0:required subject
		// [o] field:0:required clientId
		// [o] field:0:required scopes
		IDataCursor pipelineCursor = pipeline.getCursor();
		String aud = IDataUtil.getString(pipelineCursor, "aud");
		String jwksContent = IDataUtil.getString(pipelineCursor, "jwksContent");
		String bearerToken = IDataUtil.getString(pipelineCursor, "bearerToken");
		pipelineCursor.destroy();
		
		boolean isValid = false;
		String reason = "";
		String issuer = "";
		String subject = "";
		String clientId = "";
		String scopes = "";
		
		try {
			if (bearerToken == null || bearerToken.trim().isEmpty())
				throw new Exception("Missing bearerToken input.");
		
			// Normalize header prefix ("bearer" or "Bearer")
			String trimmed = bearerToken.trim();
			if (trimmed.toLowerCase(Locale.ROOT).startsWith("bearer "))
				trimmed = trimmed.substring(7).trim();
			String token = trimmed;
		
			String[] parts = token.split("\\.");
			if (parts.length != 3)
				throw new Exception("Invalid JWT format. Expected 3 parts (header.payload.signature).");
		
			// Decode Base64Url parts
			String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
			String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
			String signatureB64 = parts[2];
		
			JSONObject header, payload;
			try {
				header = new JSONObject(headerJson);
				payload = new JSONObject(payloadJson);
			} catch (JSONException je) {
				throw new Exception("Invalid JSON in JWT header or payload.");
			}
		
			String kid = header.optString("kid", null);
			if (kid == null)
				throw new Exception("JWT header missing 'kid'.");
		
			// Parse JWKS
			if (jwksContent == null || jwksContent.isEmpty())
				throw new Exception("Missing JWKS content.");
		
			JSONObject jwks = new JSONObject(jwksContent);
			JSONArray keys = jwks.optJSONArray("keys");
			if (keys == null)
				throw new Exception("Invalid JWKS format: missing 'keys' array.");
		
			JSONObject jwk = null;
			for (int i = 0; i < keys.length(); i++) {
				JSONObject key = keys.getJSONObject(i);
				if (kid.equals(key.optString("kid"))) {
					jwk = key;
					break;
				}
			}
			if (jwk == null)
				throw new Exception("Matching key not found in JWKS for kid=" + kid);
		
			String kty = jwk.optString("kty", "");
			if (!"RSA".equalsIgnoreCase(kty))
				throw new Exception("Unsupported key type: " + kty);
		
			// Build RSA Public Key
			String nStr = jwk.getString("n");
			String eStr = jwk.getString("e");
			byte[] nBytes = Base64.getUrlDecoder().decode(nStr);
			byte[] eBytes = Base64.getUrlDecoder().decode(eStr);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			PublicKey publicKey = keyFactory.generatePublic(
					new RSAPublicKeySpec(new BigInteger(1, nBytes), new BigInteger(1, eBytes))
			);
		
			// Validate signature
			Signature sig = Signature.getInstance("SHA256withRSA");
			sig.initVerify(publicKey);
			sig.update((parts[0] + "." + parts[1]).getBytes(StandardCharsets.UTF_8));
		
			boolean verified = false;
			try {
				byte[] sigBytes = Base64.getUrlDecoder().decode(signatureB64);
				verified = sig.verify(sigBytes);
			} catch (IllegalArgumentException iae) {
				throw new Exception("Invalid Base64 encoding in signature.");
			}
			if (!verified)
				throw new Exception("Invalid JWT signature.");
		
			// Validate expiration
			long now = System.currentTimeMillis() / 1000L;
			if (payload.has("exp")) {
				long exp = payload.getLong("exp");
				if (now > exp)
					throw new Exception("Token expired.");
			}
		
			// Validate audience if given
			if (aud != null && payload.has("aud")) {
				boolean audMatch = false;
				Object audClaim = payload.get("aud");
				if (audClaim instanceof String) {
					if (aud.equals(audClaim))
						audMatch = true;
				} else if (audClaim instanceof JSONArray) {
					JSONArray arr = (JSONArray) audClaim;
					for (int i = 0; i < arr.length(); i++) {
						if (aud.equals(arr.getString(i))) {
							audMatch = true;
							break;
						}
					}
				}
				if (!audMatch)
					throw new Exception("Audience validation failed. Found audience \"" + audClaim 
							 + "\", expected \"" + aud + "\". Check global variable \"wxmcp.jwt.audience\"");
			}
		
			// Extract issuer and subject (always extract, even if validation fails later)
			if (payload.has("iss")) {
				issuer = payload.getString("iss");
			}
			if (payload.has("sub")) {
				subject = payload.getString("sub");
			}
		
			StringBuilder scopesBuilder = new StringBuilder();
			boolean foundScopes = false;
		
			// Check multiple scope claim names first
			String[] scopeClaimNames = {"scope", "scp", "scopes", "permissions"};
			for (String claimName : scopeClaimNames) {
			    if (payload.has(claimName)) {
			        Object scopeObj = payload.get(claimName);
			        if (scopeObj instanceof String) {
			            String[] splitScopes = ((String)scopeObj).trim().split("\\s+");
			            for (String s : splitScopes) {
			                if (!s.isEmpty()) {
			                    if (scopesBuilder.length() > 0) scopesBuilder.append(" ");
			                    scopesBuilder.append(s);
			                }
			            }
			        } else if (scopeObj instanceof JSONArray) {
			            JSONArray arr = (JSONArray) scopeObj;
			            for (int i = 0; i < arr.length(); i++) {
			                String s = arr.optString(i, null);
			                if (s != null && !s.isEmpty()) {
			                    if (scopesBuilder.length() > 0) scopesBuilder.append(" ");
			                    scopesBuilder.append(s);
			                }
			            }
			        }
			        foundScopes = true;
			        break;
			    }
			}
		
			// Also check Azure "roles" claim if no scopes found yet
			if (!foundScopes && payload.has("roles")) {
			    JSONArray rolesArr = payload.getJSONArray("roles");
			    for (int i = 0; i < rolesArr.length(); i++) {
			        String role = rolesArr.optString(i, null);
			        if (role != null && !role.isEmpty()) {
			            if (scopesBuilder.length() > 0) scopesBuilder.append(" ");
			            scopesBuilder.append(role);
			        }
			    }
			}
		
			scopes = scopesBuilder.toString();
		
			isValid = true;
			reason = "Token valid.";
			
			// Try to extract client ID
			String[] clientClaims = {"client_id", "cid", "azp"};
			for (String claim : clientClaims) {
			    if (payload.has(claim)) {
			        clientId = payload.getString(claim);
			        break;
			    }
			}
		}
		catch (Exception e) {
			isValid = false;
			reason = e.getMessage() != null ? e.getMessage() : "Unknown validation error.";
		}
		
		// Output results
		IDataCursor out = pipeline.getCursor();
		IDataUtil.put(out, "isValid", isValid);
		IDataUtil.put(out, "reason", reason);
		IDataUtil.put(out, "issuer", issuer);
		IDataUtil.put(out, "subject", subject);
		IDataUtil.put(out, "clientId", clientId);
		IDataUtil.put(out, "scopes", scopes);
		
		out.destroy();
		// --- <<IS-END>> ---

                
	}
}


package wx.mcp.server.services.custom.helpers;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;

public final class MediaTypeHelper {
	public static Map.Entry<String, MediaType> chooseMediaType(Content content) {
        if (content == null || content.isEmpty()) {
            return null;
        }

        Map.Entry<String, MediaType> best = null;
        int bestScore = Integer.MIN_VALUE;

        for (Map.Entry<String, MediaType> e : content.entrySet()) {
            String key = e.getKey();
            String base = baseType(key);
            int score = score(base, e.getValue());

            // Prefer earlier entries if scores tie (stable choice)
            if (score > bestScore) {
                bestScore = score;
                best = e;
            }
        }

        // Fallback: first entry to avoid losing the body entirely
        return best != null ? best : content.entrySet().stream().findFirst().orElse(null);
    }

    /** Lowercase media type without parameters (strip after ';'). */
    private static String baseType(String mt) {
        if (mt == null)
            return "";
        int semi = mt.indexOf(';');
        String base = (semi >= 0 ? mt.substring(0, semi) : mt);
        return base.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Assign a preference score to common/structured types; prefer entries with a
     * schema.
     */
    private static int score(String base, MediaType mt) {
        int s;
        if ("application/json".equals(base)) {
            s = 100;
        } else if (base.endsWith("+json")) { // vendor or structured json (e.g., application/problem+json)
            s = 90;
        } else if ("application/x-www-form-urlencoded".equals(base)) {
            s = 80;
        } else if ("multipart/form-data".equals(base)) {
            s = 70;
        } else if ("text/plain".equals(base)) {
            s = 60;
        } else if ("application/octet-stream".equals(base)) {
            s = 50;
        } else {
            s = 0; // unknown/less preferred
        }

        // Slightly prefer entries that actually have a schema
        if (mt != null && mt.getSchema() != null) {
            s += 5;
        }
        return s;
    }
}

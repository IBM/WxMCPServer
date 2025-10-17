package wx.mcp.server.services.custom.helpers;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;

public final class MediaTypeHelper {
    public static Map.Entry<String, MediaType> chooseMediaType(Content content) {
        List<Pattern> order = List.of(
                Pattern.compile("(?i)^application/json$"),
                Pattern.compile("(?i)\\+json$"),
                Pattern.compile("(?i)^application/x-www-form-urlencoded$"),
                Pattern.compile("(?i)^multipart/form-data$"),
                Pattern.compile("(?i)^text/plain$"),
                Pattern.compile("(?i)^application/octet-stream$"));
        for (Pattern p : order) {
            for (Map.Entry<String, MediaType> e : content.entrySet()) {
                if (p.matcher(e.getKey()).find())
                    return e;
            }
        }
        return content.entrySet().stream().findFirst().orElse(null);
    }
}

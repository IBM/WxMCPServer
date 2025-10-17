package wx.mcp.server.services.custom.helpers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;

public final class SchemaHelper {

    private static final Logger logger = LoggerFactory.getLogger("wx.mcp.server");
    private static final Set<String> excludedKeysLong = Set.of("exampleSetFlag", "examples", "types", "$id", "$schema",
            "xml", "writeOnly", "readOnly", "description");
    private static final Set<String> excludedKeys = Set.of("exampleSetFlag", "types", "$id", "$schema", "xml",
            "writeOnly", "readOnly");

    public static boolean isObjectWithProps(Schema<?> s) {
        return s != null && "object".equals(s.getType())
                && s.getProperties() != null && !s.getProperties().isEmpty();
    }

    public static boolean isOctetStream(String mt) {
        return mt != null && mt.toLowerCase(Locale.ROOT).startsWith("application/octet-stream");
    }

    public static Schema<?> schemaForParameter(Parameter p) {
        if (p.getSchema() != null)
            return p.getSchema();
        Content content = p.getContent();
        if (content != null && !content.isEmpty()) {
            // pick any media type present (commonly application/json)
            for (Map.Entry<String, MediaType> e : content.entrySet()) {
                MediaType mt = e.getValue();
                if (mt != null && mt.getSchema() != null)
                    return mt.getSchema();
            }
        }
        return null;
    }

    /** Convert an OAS Schema node into a detached map for subsequent cleaning. */
    public static Map<String, Object> toSchemaMap(ObjectMapper mapper, Schema<?> schema) {
        if (schema == null)
            return new LinkedHashMap<>();
        return mapper.convertValue(schema, new TypeReference<Map<String, Object>>() {
        });
    }

    /**
     * Remove nulls, empty collections, and internal flags you don’t want in the
     * schema payload
     */
    public static void cleanMap(Map<String, Object> m, boolean isLargeSpec) {
        if (m == null)
            return;
        // remove nulls recursively
        List<String> toRemove = new ArrayList<>();
        for (Map.Entry<String, Object> e : m.entrySet()) {
            Object v = e.getValue();
            if (v == null) {
                toRemove.add(e.getKey());
            } else if (v instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> child = (Map<String, Object>) v;
                cleanMap(child, isLargeSpec);
                // drop empty children
                // if (child.isEmpty())
                //     toRemove.add(e.getKey());
            } else if (v instanceof Collection) {
                Collection<?> c = (Collection<?>) v;
                if (c.isEmpty()) {
                    // empty collections could be removed
                    // toRemove.add(e.getKey());
                } else {
                    for (Object item : c) {
                        if (item instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> child = (Map<String, Object>) item;
                            cleanMap(child, isLargeSpec);
                        }
                    }
                }

                // Collection<?> c = (Collection<?>) v;
                // if (c.isEmpty())
                // toRemove.add(e.getKey());
            }
        }
        toRemove.forEach(m::remove);

        // remove a few fields that are irrelevant/noisy for schema
        if (isLargeSpec) {
            m.keySet().removeAll(excludedKeysLong);
        } else {
            m.keySet().removeAll(excludedKeys);
        }
    }
}

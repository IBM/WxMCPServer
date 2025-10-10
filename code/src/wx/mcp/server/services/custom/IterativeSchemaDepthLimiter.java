package wx.mcp.server.services.custom;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IterativeSchemaDepthLimiter {

    private static final Logger logger = LoggerFactory.getLogger(IterativeSchemaDepthLimiter.class.getName());

    public static void limitSchemaDepth(OpenAPI openAPI, int maxDepth) {
        if (openAPI.getComponents() == null || openAPI.getComponents().getSchemas() == null)
            return;

        var schemas = openAPI.getComponents().getSchemas();

        for (var entry : schemas.entrySet()) {
            var rootSchema = entry.getValue();
            var stack = new ArrayDeque<SchemaTraversalNode>();
            stack.push(new SchemaTraversalNode(rootSchema, 0, entry.getKey()));

            while (!stack.isEmpty()) {
                var node = stack.pop();
                var schema = node.schema;
                var depth = node.depth;
                var path = node.path;

                if (depth >= maxDepth) {
                    logger.warn("Pruned schema at depth %d: %s".formatted(depth, path));
                    continue;
                }

                if ("object".equals(schema.getType()) && schema.getProperties() != null) {
                    var newProps = new HashMap<String, Schema<?>>();
                    for (var propEntry : schema.getProperties().entrySet()) {
                        var propName = propEntry.getKey();
                        var propSchema = propEntry.getValue();
                        var propPath = path + "." + propName;

                        if (depth + 1 < maxDepth) {
                            stack.push(new SchemaTraversalNode(propSchema, depth + 1, propPath));
                            newProps.put(propName, propSchema);
                        } else {
                            logger.info("Pruned property at depth %d: %s".formatted(depth + 1, propPath));
                        }
                    }
                    // schema.setProperties(newProps);
                    schema.setProperties((Map<String, Schema>) (Map<?, ?>) newProps);
                }

                if ("array".equals(schema.getType()) && schema.getItems() != null) {
                    var itemPath = path + "[]";
                    if (depth + 1 < maxDepth) {
                        stack.push(new SchemaTraversalNode(schema.getItems(), depth + 1, itemPath));
                    } else {
                        logger.info("Pruned array items at depth %d: %s".formatted(depth + 1, itemPath));
                        schema.setItems(null);
                    }
                }
            }
        }
    }

    private static class SchemaTraversalNode {
        Schema<?> schema;
        int depth;
        String path;

        SchemaTraversalNode(Schema<?> schema, int depth, String path) {
            this.schema = schema;
            this.depth = depth;
            this.path = path;
        }
    }

}

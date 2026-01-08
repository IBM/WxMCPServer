package wx.mcp.server.services.custom;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class IterativeSchemaDepthLimiter {

    public static void limitSchemaDepthOnlyCyclic(OpenAPI openAPI, int maxDepth) {
        if (openAPI.getComponents() == null || openAPI.getComponents().getSchemas() == null)
            return;

        var schemas = openAPI.getComponents().getSchemas();

        for (var entry : schemas.entrySet()) {
            var rootSchema = entry.getValue();
            var stack = new ArrayDeque<SchemaTraversalNode>();
            var visitedRefs = new HashSet<String>(); // Track visited $refs
            stack.push(new SchemaTraversalNode(rootSchema, 0, entry.getKey()));
            while (!stack.isEmpty()) {
                var node = stack.pop();
                var schema = node.schema;
                var depth = node.depth;
                var path = node.path;

                String ref = schema.get$ref();
                if (ref != null) {
                    if (visitedRefs.contains(ref)) {
                        continue;
                    }
                    visitedRefs.add(ref);
                }

                if (depth >= maxDepth) {
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
                            System.err.println("Pruned property at depth %d: %s".formatted(depth + 1, propPath));
                        }
                    }
                    // schema.setProperties((Map<String, Schema<?>>) (Map<?, ?>) newProps);
                    Map<String, Schema> castedProps = new HashMap<>();
                    for (Map.Entry<String, Schema<?>> schemaEntry : newProps.entrySet()) {
                        castedProps.put(schemaEntry.getKey(), (Schema) schemaEntry.getValue());
                    }
                    schema.setProperties(castedProps);
                }

                if ("array".equals(schema.getType()) && schema.getItems() != null) {
                    var itemPath = path + "[]";
                    if (depth + 1 < maxDepth) {
                        stack.push(new SchemaTraversalNode(schema.getItems(), depth + 1, itemPath));
                    } else {
                        schema.setItems(null);
                    }
                }
            }
        }
    }
    
    public static void limitSchemaDepth(OpenAPI openAPI, int maxDepth) {
        if (openAPI.getComponents() == null || openAPI.getComponents().getSchemas() == null)
            return;

        var schemas = openAPI.getComponents().getSchemas();

        for (var entry : schemas.entrySet()) {
            var rootSchema = entry.getValue();
            var stack = new ArrayDeque<SchemaTraversalNode>();
            var visitedRefs = new HashSet<String>(); // Track visited $refs
            stack.push(new SchemaTraversalNode(rootSchema, 0, entry.getKey()));

            while (!stack.isEmpty()) {
                var node = stack.pop();
                var schema = node.schema;
                var depth = node.depth;
                var path = node.path;

                String ref = schema.get$ref();
                if (ref != null) {
                    if (visitedRefs.contains(ref)) {
                        continue;
                    }
                    visitedRefs.add(ref);
                }
                
                if (depth >= maxDepth) {
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
                            System.err.println("Pruned property at depth %d: %s".formatted(depth + 1, propPath));
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

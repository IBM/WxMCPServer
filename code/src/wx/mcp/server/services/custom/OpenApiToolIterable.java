package wx.mcp.server.services.custom;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.PathItem.HttpMethod;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.parameters.Parameter;
import wx.mcp.server.models.Tool;
import wx.mcp.server.services.custom.helpers.McpToolBuilder;
import wx.mcp.server.services.custom.helpers.ParameterHelper;

public final class OpenApiToolIterable {

    private static final Logger logger = LoggerFactory.getLogger("wx.mcp.server");

    private OpenApiToolIterable() {
    }

    /**
     * Create an Iterable<Tool> that lazily yields one Tool per (path, httpMethod,
     * operation).
     */
    public static Iterable<Tool> from(OpenAPI openAPI, String headerPrefix, String pathParamPrefix,
            String queryPrefix, String mcpObjectName, boolean isLargeSpec) {
        return () -> new OpenApiToolIterator(openAPI, headerPrefix, pathParamPrefix, queryPrefix, mcpObjectName, isLargeSpec);
    }

    private static final class OpenApiToolIterator implements Iterator<Tool> {
        private final Iterator<Map.Entry<String, PathItem>> pathIt;
        private String currentPath;
        private PathItem currentPathItem;
        private Iterator<Map.Entry<HttpMethod, Operation>> opIt = Collections.emptyIterator();
        private Tool next; // cache for next() / hasNext() protocol
        private String headerPrefix;
        private String pathParamPrefix;
        private String queryPrefix;
        private String mcpObjectName;
        private boolean isLargeSpec = true;

        OpenApiToolIterator(OpenAPI openAPI, String headerPrefix, String pathParamPrefix,
                String queryPrefix, String mcpObjectName, boolean isLargeSpec) {
            this.headerPrefix = headerPrefix;
            this.pathParamPrefix = pathParamPrefix;
            this.queryPrefix = queryPrefix;
            this.mcpObjectName = mcpObjectName;
            this.isLargeSpec = isLargeSpec;
            Paths paths = (openAPI != null) ? openAPI.getPaths() : null;
            this.pathIt = (paths == null)
                    ? Collections.<Map.Entry<String, PathItem>>emptyIterator()
                    : paths.entrySet().iterator();
        }

        @Override
        public boolean hasNext() {
            if (next != null)
                return true;

            // Advance until we find the next operation
            while (true) {
                if (opIt.hasNext()) {
                    Map.Entry<HttpMethod, Operation> e = opIt.next();
                    Operation op = e.getValue();

                    // 1) Merge path-level + op-level params (op overrides)
                    List<Parameter> mergedParams = ParameterHelper.effectiveParameters(currentPathItem, op);
                    
                    next = McpToolBuilder.buildMcpTool(currentPath, e.getKey(), e.getValue(), headerPrefix, pathParamPrefix, queryPrefix,
                            mcpObjectName, mergedParams, isLargeSpec);
                    if (next != null)
                        return true; // skip nulls if your mapping filters some ops
                } else if (pathIt.hasNext()) {
                    Map.Entry<String, PathItem> pEntry = pathIt.next();
                    currentPath = pEntry.getKey();
                    currentPathItem = pEntry.getValue();
                    Map<HttpMethod, Operation> map = (currentPathItem == null) ? null
                            : currentPathItem.readOperationsMap();
                    opIt = (map == null) ? Collections.<Map.Entry<HttpMethod, Operation>>emptyIterator()
                            : map.entrySet().iterator();
                } else {
                    return false; // no more paths/ops
                }
            }
        }

        @Override
        public Tool next() {
            if (!hasNext())
                throw new NoSuchElementException();
            Tool out = next;
            next = null;
            return out;
        }
    }
}
package wx.mcp.server.services.custom.helpers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.parameters.Parameter;

public final class ParameterHelper {
    public static List<Parameter> effectiveParameters(PathItem pathItem, Operation op) {
        Map<String, Parameter> byKey = new LinkedHashMap<>();

        if (pathItem != null && pathItem.getParameters() != null) {
            for (Parameter p : pathItem.getParameters()) {
                if (p != null)
                    byKey.put(key(p), p);
            }
        }
        if (op != null && op.getParameters() != null) {
            for (Parameter p : op.getParameters()) {
                if (p != null)
                    byKey.put(key(p), p); // overrides
            }
        }
        return new ArrayList<>(byKey.values());
    }

    private static String key(Parameter p) {
        return (p.getName() == null ? "" : p.getName()) + "|" + (p.getIn() == null ? "" : p.getIn());
    }
}

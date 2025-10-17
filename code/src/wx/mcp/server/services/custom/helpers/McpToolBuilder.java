package wx.mcp.server.services.custom.helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.PathItem.HttpMethod;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import wx.mcp.server.models.InputSchema;
import wx.mcp.server.models.OutputSchema;
import wx.mcp.server.models.Properties;
import wx.mcp.server.models.PropertiesProperty;
import wx.mcp.server.models.Tool;

public final class McpToolBuilder {

    private static final Logger logger = LoggerFactory.getLogger("wx.mcp.server");

    private static final Set<PathItem.HttpMethod> allowedMethods = EnumSet.of(
            PathItem.HttpMethod.GET,
            PathItem.HttpMethod.POST,
            PathItem.HttpMethod.PUT,
            PathItem.HttpMethod.DELETE,
            PathItem.HttpMethod.PATCH,
            PathItem.HttpMethod.HEAD,
            PathItem.HttpMethod.OPTIONS);

    /**
     * Your mapping from OpenAPI Operation to MCP Tool.
     * Implement this to construct a Tool with the info you need.
     */
    public static Tool buildMcpTool(String path, HttpMethod method, Operation op, String headerPrefix,
            String pathParamPrefix,
            String queryPrefix, String mcpObjectName, List<Parameter> mergedParameters, boolean isLargeSpec) {
        if (!allowedMethods.contains(method)) {
            logger.debug("\tmethod {} is NOT in the allowed list", method);
            return null;
        }
        // Example sketch — replace with your actual mapping:
        Tool t = new Tool();
        t.setDescription(op.getSummary() != null ? op.getSummary() : op.getDescription());
        logger.debug("inside to-mcp-tool for path {}\t\t method: {}\t\tTool Name: {} with {} path params", path,
                method.name(), t.getName(), mergedParameters.size());

        String operationId = op.getOperationId();
        // generate an operationId, if it is missing
        if (operationId == null || operationId.isBlank()) {
            String sanitizedPath = path.replaceAll("[{}\\/]", "_").replaceAll("_+", "_");
            operationId = method.name().toLowerCase() + "_" + sanitizedPath;
            // return;
        }

        String toolName = null;
        if (!StringHelper.isNullOrEmpty(mcpObjectName)) {
            toolName = mcpObjectName + "_" + operationId;
        } else {
            toolName = operationId;
        }
        t.setName(toolName);

        // MCP TOOL INPUT SCHEMA ------------------

        logger.debug("working on the input schema");
        ObjectMapper objectMapper = new ObjectMapper();

        // Naming strategy for all properties (headers, path, query, body)
        // currently, body properties are not renamed
        NameMapper nameMapper = new NameMapper(NameMapper.DEFAULT_STRATEGY,
                headerPrefix, pathParamPrefix, queryPrefix);

        InputSchema inputSchema = new InputSchema();
        inputSchema.setType("object");

        // Collect mcp tool properties and required lists
        wx.mcp.server.models.Properties inputProps = new wx.mcp.server.models.Properties();
        List<String> required = new ArrayList<>();

        // Add parameter-derived properties from path/operation
        InputSchema paramSchema = buildInputSchema(mergedParameters, nameMapper, objectMapper, isLargeSpec);
        inputProps.getAdditionalProperties().putAll(paramSchema.getProperties().getAdditionalProperties());
        required.addAll(paramSchema.getRequired());

        // Add request-body-derived properties (flatten top-level only)
        addRequestBodyFlattenTopLevelProps(op, nameMapper, objectMapper, inputProps, required, isLargeSpec);

        // 4) Finalize InputSchema
        inputSchema.setProperties(inputProps);
        inputSchema.setRequired(required);

        // Attach to tool
        t.setInputSchema(inputSchema);

        // MCP TOOL OUTPUT SCHEMA ------------------

        OutputSchema out = buildOutputSchemaSuccessOnly(op, objectMapper, /* addOneOf = */ true, isLargeSpec);
        t.setOutputSchema(out);

        // ...
        return t;
    }

    /**
     * Build an MCP tool {@code InputSchema} (JSON Schema) from merged+dereferenced
     * OpenAPI parameters.
     * <p>
     * - Produces a type {@code object} schema with one property per parameter.<br>
     * - Applies the provided {@link NameMapper} for deterministic, collision-free
     * names.<br>
     * - Pulls a parameter's schema either from {@code parameter.schema} or the
     * first entry in
     * {@code parameter.content}.<br>
     * - Adds helpful metadata (description, x-in, deprecated) to each property.
     */
    private static InputSchema buildInputSchema(List<Parameter> mergedParams,
            NameMapper names,
            ObjectMapper mapper,
            boolean isLargeSpec) {
        InputSchema input = new InputSchema();
        input.setType("object");

        // Guard: empty (always produce a valid empty object schema)
        if (mergedParams == null || mergedParams.isEmpty()) {
            input.setProperties(new Properties());
            input.setRequired(Collections.emptyList());
            return input;
        }

        Properties props = new Properties();
        List<String> required = new ArrayList<>();

        for (Parameter param : mergedParams) {
            if (param == null)
                continue;

            // 1) Determine the final property name via the naming strategy
            String propName = names.map(param.getName(), param.getIn());

            // 2) Obtain a schema for the parameter
            Schema<?> s = SchemaHelper.schemaForParameter(param);

            // 3) Convert schema → map, and clean it (no model mutation)
            Map<String, Object> m = SchemaHelper.toSchemaMap(mapper, s);
            SchemaHelper.cleanMap(m, isLargeSpec);
            // normalizeNullable(m); // (optional) if your consumer expects JSON Schema
            // unions

            // 4) Build the MCP side property
            PropertiesProperty pp = new PropertiesProperty();
            if (param.getDescription() != null)
                pp.setAdditionalProperty("description", param.getDescription());
            // if (param.getIn() != null)
            // pp.setAdditionalProperty("x-in", param.getIn());
            if (Boolean.TRUE.equals(param.getDeprecated()))
                pp.setAdditionalProperty("deprecated", true);

            // Copy the cleaned schema facets (type, format, enum, items, min*/max*, etc.)
            m.forEach(pp::setAdditionalProperty);

            // Sensible fallback when no schema info exists
            if (!pp.getAdditionalProperties().containsKey("type")) {
                pp.setAdditionalProperty("type", "string");
            }

            props.setAdditionalProperty(propName, pp);

            if (Boolean.TRUE.equals(param.getRequired())) {
                required.add(propName);
            }
        }

        input.setProperties(props);
        input.setRequired(required);
        return input;
    }

    private static OutputSchema buildOutputSchemaSuccessOnlyWithResponseWrapper(Operation operation,
            ObjectMapper mapper,
            boolean addOneOf,
            boolean isLargeSpec) {

        OutputSchema out = new OutputSchema();
        out.setType("object");
        out.setRequired(List.of("result"));

        wx.mcp.server.models.Properties props = new wx.mcp.server.models.Properties();
        wx.mcp.server.models.PropertiesProperty resultProp = new wx.mcp.server.models.PropertiesProperty();

        Map<String, ApiResponse> responses = (Map<String, ApiResponse>) operation.getResponses();
        if (responses == null || responses.isEmpty()) {
            resultProp.setAdditionalProperty("type", "object");
            resultProp.setAdditionalProperty("properties", new LinkedHashMap<>());
            props.setAdditionalProperty("result", resultProp);
            out.setProperties(props);
            return out;
        }

        Map<String, ApiResponse> success = new LinkedHashMap<>();
        for (Map.Entry<String, ApiResponse> e : responses.entrySet()) {
            if (isSuccessStatusKey(e.getKey())) {
                success.put(e.getKey(), e.getValue());
            }
        }

        List<String> orderedCodes = success.keySet().stream()
                .filter(code -> code.matches("\\d+"))
                .sorted((a, b) -> Integer.compare(Integer.parseInt(b), Integer.parseInt(a)))
                .collect(Collectors.toList());

        List<Map<String, Object>> oneOfList = new ArrayList<>();

        for (String code : orderedCodes) {
            ApiResponse apiResp = success.get(code);
            if (apiResp == null)
                continue;

            Content content = apiResp.getContent();
            // this would ignore empty responses
            // if (content == null || content.isEmpty())
            // continue;
            if (content == null || content.isEmpty()) {
                // Emit an empty object schema for responses with no content
                Map<String, Object> emptySchema = Map.of(
                        "type", "object",
                        "properties", Map.of());

                Map<String, Object> responseWrapper = new LinkedHashMap<>();
                responseWrapper.put("type", "object");
                responseWrapper.put("properties", Map.of(
                        "response_" + code.toLowerCase(Locale.ROOT), emptySchema));
                responseWrapper.put("required", List.of("response_" + code.toLowerCase(Locale.ROOT)));

                oneOfList.add(responseWrapper);
                continue;
            }

            Map.Entry<String, MediaType> chosen = MediaTypeHelper.chooseMediaType(content);
            if (chosen == null)
                continue;

            MediaType mt = chosen.getValue();
            Schema<?> schema = (mt != null) ? mt.getSchema() : null;
            if (schema == null)
                continue;

            Map<String, Object> schemaMap = SchemaHelper.toSchemaMap(mapper, schema);
            SchemaHelper.cleanMap(schemaMap, isLargeSpec);

            // Wrap schema inside a named property like "response_200"
            Map<String, Object> responseWrapper = new LinkedHashMap<>();
            responseWrapper.put("type", "object");

            Map<String, Object> innerProps = new LinkedHashMap<>();
            innerProps.put("response_" + code.toLowerCase(Locale.ROOT), schemaMap);
            responseWrapper.put("properties", innerProps);
            responseWrapper.put("required", List.of("response_" + code.toLowerCase(Locale.ROOT)));

            oneOfList.add(responseWrapper);
        }

        resultProp.setAdditionalProperty("oneOf", oneOfList);
        props.setAdditionalProperty("result", resultProp);
        out.setProperties(props);

        return out;
    }

    private static OutputSchema buildOutputSchemaSuccessOnly(Operation operation,
            ObjectMapper mapper,
            boolean addOneOf,
            boolean isLargeSpec) {

        OutputSchema out = new OutputSchema();
        out.setType("object");

        // Set "result" as required
        out.setRequired(List.of("result"));

        // Prepare the "result" property
        wx.mcp.server.models.Properties props = new wx.mcp.server.models.Properties();
        wx.mcp.server.models.PropertiesProperty resultProp = new wx.mcp.server.models.PropertiesProperty();

        List<Map<String, Object>> oneOfList = new ArrayList<>();

        Map<String, ApiResponse> responses = (Map<String, ApiResponse>) operation.getResponses();
        if (responses == null || responses.isEmpty()) {
            resultProp.setAdditionalProperty("type", "object");
            resultProp.setAdditionalProperty("properties", new LinkedHashMap<>());
            props.setAdditionalProperty("result", resultProp);
            out.setProperties(props);
            return out;
        }

        Map<String, ApiResponse> success = responses.entrySet().stream()
                .filter(e -> isSuccessStatusKey(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        for (Map.Entry<String, ApiResponse> entry : success.entrySet()) {
            ApiResponse apiResp = entry.getValue();
            Content content = apiResp.getContent();
            if (content == null || content.isEmpty())
                continue;

            Map.Entry<String, MediaType> chosen = MediaTypeHelper.chooseMediaType(content);
            if (chosen == null)
                continue;

            MediaType mt = chosen.getValue();
            Schema<?> schema = (mt != null) ? mt.getSchema() : null;
            if (schema == null)
                continue;

            Map<String, Object> schemaMap = SchemaHelper.toSchemaMap(mapper, schema);
            SchemaHelper.cleanMap(schemaMap, isLargeSpec);

            Map<String, Object> oneOfEntry = new LinkedHashMap<>();
            oneOfEntry.put("type", "object");
            oneOfEntry.put("properties", schemaMap);
            oneOfList.add(oneOfEntry);
        }

        resultProp.setAdditionalProperty("oneOf", oneOfList);
        props.setAdditionalProperty("result", resultProp);
        out.setProperties(props);

        return out;
    }

    private static OutputSchema previousBuildOutputSchemaSuccessOnly(Operation operation,
            ObjectMapper mapper,
            boolean addOneOf,
            boolean isLargeSpec) {
        OutputSchema out = new OutputSchema();
        out.setType("object");

        wx.mcp.server.models.Properties props = new wx.mcp.server.models.Properties();
        List<String> required = new ArrayList<>();

        // ApiResponses responses = operation.getResponses();
        Map<String, ApiResponse> responses = (Map<String, ApiResponse>) operation.getResponses();
        if (responses == null || responses.isEmpty()) {
            out.setProperties(props);
            out.setRequired(required);
            return out;
        }

        // Collect success responses (2xx + "2XX"/"2xx")
        Map<String, ApiResponse> success = new LinkedHashMap<>();
        for (Map.Entry<String, ApiResponse> e : responses.entrySet()) {
            if (isSuccessStatusKey(e.getKey())) {
                success.put(e.getKey(), e.getValue());
            }
        }
        if (success.isEmpty()) {
            out.setProperties(props);
            out.setRequired(required);
            return out;
        }

        // sort the response codes... non numeric codes will be filtered
        List<String> orderedCodes = success.keySet().stream()
                .filter(code -> code.matches("\\d+")) // optional: filter only numeric codes
                .sorted((a, b) -> Integer.compare(Integer.parseInt(b), Integer.parseInt(a)))
                .collect(Collectors.toList());

        // If you don't use oneOf, optionally choose ONE "primary" success to require
        // Optional<String> primary = addOneOf ? Optional.empty()
        // : OutputSchemaUtils.selectPrimarySuccess(orderedCodes);

        List<String> emittedPropNames = new ArrayList<>();

        for (String code : orderedCodes) {
            ApiResponse apiResp = success.get(code);
            if (apiResp == null)
                continue;

            Content content = apiResp.getContent();
            if (content == null || content.isEmpty())
                continue;

            Map.Entry<String, MediaType> chosen = MediaTypeHelper.chooseMediaType(content); // reuse your helper
            if (chosen == null)
                continue;

            String mtKey = chosen.getKey();
            MediaType mt = chosen.getValue();
            Schema<?> schema = (mt != null) ? mt.getSchema() : null;
            if (schema == null) {
                // handle 204 / no schema: emit a null or empty object schema
                wx.mcp.server.models.PropertiesProperty empty = new wx.mcp.server.models.PropertiesProperty();
                empty.setAdditionalProperty("type", "null");
                String propName = "response_" + code.toLowerCase(Locale.ROOT);
                props.setAdditionalProperty(propName, empty);
                emittedPropNames.add(propName);
                continue;
            }

            // Convert -> clean
            Map<String, Object> m = SchemaHelper.toSchemaMap(mapper, schema);
            SchemaHelper.cleanMap(m, isLargeSpec);
            // normalizeNullable(m); // optional

            wx.mcp.server.models.PropertiesProperty pp = new wx.mcp.server.models.PropertiesProperty();
            if (!m.containsKey("description") && apiResp.getDescription() != null) {
                pp.setAdditionalProperty("description", apiResp.getDescription());
            }
            m.forEach(pp::setAdditionalProperty);

            // Fallback type
            if (!pp.getAdditionalProperties().containsKey("type")) {
                if (mtKey.toLowerCase(Locale.ROOT).startsWith("text/")) {
                    pp.setAdditionalProperty("type", "string");
                } else if (mtKey.equalsIgnoreCase("application/octet-stream")) {
                    pp.setAdditionalProperty("type", "string");
                    pp.setAdditionalProperty("format", "binary");
                } else {
                    pp.setAdditionalProperty("type", "object");
                }
            }

            String propName = "response_" + code.toLowerCase(Locale.ROOT);
            props.setAdditionalProperty(propName, pp);
            emittedPropNames.add(propName);

            // If not using oneOf, you may mark one primary success as required
            // if (primary.isPresent() && primary.get().equals(code)) {
            // required.add(propName);
            // }
        }

        out.setProperties(props);
        out.setRequired(required);

        // Add top-level oneOf to enforce "exactly one success branch" if requested
        if (addOneOf && !emittedPropNames.isEmpty()) {
            List<Map<String, Object>> oneOf = new ArrayList<>();
            for (String name : emittedPropNames) {
                Map<String, Object> branch = new LinkedHashMap<>();
                branch.put("required", List.of(name));
                oneOf.add(branch);
            }
            out.setAdditionalProperty("oneOf", oneOf);
        }

        return out;
    }

    /** Success if a specific 2xx code or the OAS range key "2XX"/"2xx". */
    private static boolean isSuccessStatusKey(String key) {
        if (key == null)
            return false;
        String k = key.trim();
        if (k.equalsIgnoreCase("2XX"))
            return true; // OAS range key
        // exact 3-digit code
        if (k.length() == 3 && k.chars().allMatch(Character::isDigit)) {
            int code = Integer.parseInt(k);
            return code >= 200 && code <= 299;
        }
        return false;
    }

    /**
     * Pick ONE primary success to mark as required.
     * Preference: 200 → 201 → 202 → 204 → first available.
     * If you don't want any required responses, just return Optional.empty().
     */
    private static Optional<String> selectPrimarySuccess(List<String> orderedCodes) {
        List<String> pref = List.of("200", "201", "202", "204");
        for (String p : pref) {
            if (orderedCodes.contains(p))
                return Optional.of(p);
        }
        // Or no "primary":
        // return Optional.empty();
        // Else: fall back to the first 2xx key
        return orderedCodes.isEmpty() ? Optional.empty() : Optional.of(orderedCodes.get(0));
    }

    /**
     * Adds requestBody schema into InputSchema properties with a "shallow
     * flattening" policy:
     *
     * - If the chosen requestBody schema is an object with a non-empty `properties`
     * map:
     * → add EACH top-level property to the tool's InputSchema.properties (no body
     * prefix).
     * → nested structure under each property is preserved (no deep flattening).
     * → top-level InputSchema.required includes the body's required fields
     * only if requestBody.required == true (keeps semantics consistent).
     *
     * - Otherwise (array, primitive, empty object, free-form object):
     * → add ONE property named "body" (or whatever your naming strategy maps) with
     * that schema.
     *
     * This avoids turning an optional entire body into multiple required top-level
     * fields.
     */
    static void addRequestBodyFlattenTopLevelProps(Operation operation,
            NameMapper nameMapper,
            ObjectMapper mapper,
            Properties inputSchemaProps,
            List<String> inputSchemaRequired,
            boolean isLargeSpec) {

        RequestBody rb = operation.getRequestBody();
        if (rb == null)
            return;

        Content content = rb.getContent();
        if (content == null || content.isEmpty())
            return;

        Map.Entry<String, MediaType> chosen = MediaTypeHelper.chooseMediaType(content);
        if (chosen == null) {
            // Fallback: one string "body" property
            final String finalName = nameMapper.map("body", "body");
            var pp = new PropertiesProperty();
            pp.setAdditionalProperty("type", "string");
            inputSchemaProps.setAdditionalProperty(finalName, pp);
            if (Boolean.TRUE.equals(rb.getRequired()))
                inputSchemaRequired.add(finalName);
            return;
        }

        final String mediaTypeKey = chosen.getKey();
        final MediaType mt = chosen.getValue();
        final Schema<?> bodySchema = (mt != null) ? mt.getSchema() : null;

        if (bodySchema != null && SchemaHelper.isObjectWithProps(bodySchema)) {
            Map<String, Schema> bodyProps = bodySchema.getProperties(); // <- fixed

            if (bodyProps != null && !bodyProps.isEmpty()) {
                for (Map.Entry<String, Schema> e : bodyProps.entrySet()) {
                    String rawPropName = e.getKey();
                    Schema<?> propSchema = e.getValue(); // widen per value

                    String finalName = nameMapper.map(rawPropName, "body");

                    Map<String, Object> m = SchemaHelper.toSchemaMap(mapper, propSchema);
                    SchemaHelper.cleanMap(m, isLargeSpec);
                    // normalizeNullable(m); // optional

                    PropertiesProperty pp = new PropertiesProperty();

                    if (!m.containsKey("description") && propSchema.getDescription() != null) {
                        pp.setAdditionalProperty("description", propSchema.getDescription());
                    }

                    m.forEach(pp::setAdditionalProperty);

                    if (!pp.getAdditionalProperties().containsKey("type")) {
                        pp.setAdditionalProperty("type", "string");
                    }

                    inputSchemaProps.setAdditionalProperty(finalName, pp);
                }
            }

            if (Boolean.TRUE.equals(rb.getRequired())) {
                List<String> req = bodySchema.getRequired();
                if (req != null) {
                    for (String rawReq : req) {
                        String mapped = nameMapper.map(rawReq, "body");
                        inputSchemaRequired.add(mapped);
                    }
                }
            }

        } else {
            // NON-FLATTEN: add one "body" property (array, primitive, free-form, or object
            // without explicit props)
            String finalName = nameMapper.map("body", "body");

            Map<String, Object> m = SchemaHelper.toSchemaMap(mapper, bodySchema);
            SchemaHelper.cleanMap(m, isLargeSpec);
            // normalizeNullable(m); // optional

            var pp = new wx.mcp.server.models.PropertiesProperty();
            m.forEach(pp::setAdditionalProperty);

            if (!pp.getAdditionalProperties().containsKey("type")) {
                if (SchemaHelper.isOctetStream(mediaTypeKey)) {
                    pp.setAdditionalProperty("type", "string");
                    pp.setAdditionalProperty("format", "binary");
                } else {
                    pp.setAdditionalProperty("type", "string");
                }
            }

            inputSchemaProps.setAdditionalProperty(finalName, pp);
            if (Boolean.TRUE.equals(rb.getRequired()))
                inputSchemaRequired.add(finalName);
        }
    }

}

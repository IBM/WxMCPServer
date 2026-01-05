package wx.mcp.server.services.custom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import wx.mcp.server.models.InputSchema;
import wx.mcp.server.models.ListToolsResponse;
import wx.mcp.server.models.OutputSchema;
import wx.mcp.server.models.Properties;
import wx.mcp.server.models.PropertiesProperty;
import wx.mcp.server.models.Tool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OAS2MCPConverter {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger("wx.mcp.server");

	private static final String APPLICATION_JSON = "application/json";
	private static final String X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";
	private static final String[] SUPPORTED_CONTENT_TYPES = { APPLICATION_JSON };

	private static final int MAX_NESTED_SCHEMA_DEPTH = 4;
	private static final int DEFAULT_NESTED_SCHEMA_DEPTH = 6;
	private static final int MAX_OPENAPI_STRING_SIZE = 4000000;

	private static final Set<PathItem.HttpMethod> allowedMethods = EnumSet.of(PathItem.HttpMethod.GET,
			PathItem.HttpMethod.POST, PathItem.HttpMethod.PUT, PathItem.HttpMethod.DELETE, PathItem.HttpMethod.PATCH,
			PathItem.HttpMethod.HEAD, PathItem.HttpMethod.OPTIONS);

	private static final Set<String> excludedKeys = Set.of("exampleSetFlag", "types", "$id", "$schema", "xml",
			"writeOnly", "readOnly");

	/**
	 * @param openAPIString
	 * @param headerPrefix
	 * @param pathParamPrefix
	 * @param queryPrefix
	 * @param mcpObjectName
	 * @return
	 */
	public String generateMcpToolStringFromOAS(String openAPIString, String headerPrefix, String pathParamPrefix,
			String queryPrefix, String mcpObjectName, String responseMode) {
		int openApiStringSize = 0;
		String toolsAString = null;
		try {
			if (isNullOrEmpty(openAPIString) || isNullOrEmpty(headerPrefix) || isNullOrEmpty(pathParamPrefix)
					|| isNullOrEmpty(queryPrefix)) {
				logger.error("One or more required parameters are null or empty.");
				throw new IllegalArgumentException("One or more required parameters are null or empty.");
			}

			// parse the OpenAPI spec and resolve all internal references
			ParseOptions options = new ParseOptions();
			options.setResolve(true);
			options.setResolveFully(true);

			openApiStringSize = openAPIString.getBytes(StandardCharsets.UTF_8).length;
			logger.debug("starting OpenAPI spec parsing (utf8 byte Size: {})", openApiStringSize);
			SwaggerParseResult parseResult = new OpenAPIParser().readContents(openAPIString, null, options);

			OpenAPI openAPI = parseResult.getOpenAPI();
			// IterativeSchemaDepthLimiter.limitSchemaDepthOnlyCyclic(openAPI, 6);
			boolean isLargeSpec = openApiStringSize > MAX_OPENAPI_STRING_SIZE ? true : false;
			if (isLargeSpec) {
				logger.debug("OpenAPI Schema nested schema depth limited to {} levels due to specification size",
						MAX_NESTED_SCHEMA_DEPTH);
				IterativeSchemaDepthLimiter.limitSchemaDepthOnlyCyclic(openAPI, MAX_NESTED_SCHEMA_DEPTH);
			} else {
				logger.debug("OpenAPI Schema limited to {} levels to avoid infinite nesting",
						DEFAULT_NESTED_SCHEMA_DEPTH);
				IterativeSchemaDepthLimiter.limitSchemaDepthOnlyCyclic(openAPI, DEFAULT_NESTED_SCHEMA_DEPTH);
			}

			Iterable<Tool> toolsProducer = OpenApiToolIterable.from(openAPI, headerPrefix, pathParamPrefix, queryPrefix,
					mcpObjectName, isLargeSpec, responseMode);
			toolsAString = McpToolWriter.listToolsToString(null, null, toolsProducer);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}

		return toolsAString;
	}

	/**
	 * @param openAPIString
	 * @param headerPrefix
	 * @param pathParamPrefix
	 * @param queryPrefix
	 * @param mcpObjectName
	 * @return
	 */
	public ListToolsResponse generateMcpToolsFromOAS(String openAPIString, String headerPrefix, String pathParamPrefix,
			String queryPrefix, String mcpObjectName) {

		ListToolsResponse mcpTools = new ListToolsResponse();
		if (mcpTools.getTools() == null) {
			mcpTools.setTools(new ArrayList<Tool>());
		}
		ObjectMapper mapper = new ObjectMapper();
		try {
			if (isNullOrEmpty(openAPIString) || isNullOrEmpty(headerPrefix) || isNullOrEmpty(pathParamPrefix)
					|| isNullOrEmpty(queryPrefix)) {
				logger.error("One or more required parameters are null or empty.");
				throw new IllegalArgumentException("One or more required parameters are null or empty.");
			}

			// parse the OpenAPI spec and resolve all internal references
			ParseOptions options = new ParseOptions();
			options.setResolve(true);
			options.setResolveFully(true);

			SwaggerParseResult parseResult = new OpenAPIParser().readContents(openAPIString, null, options);
			OpenAPI openAPI = parseResult.getOpenAPI();

			int openApiStringSize = openAPIString.getBytes(StandardCharsets.UTF_8).length;
			if (openApiStringSize > MAX_OPENAPI_STRING_SIZE) {
				logger.info("OpenAPI Schema nested depth parsing is limited to " + MAX_NESTED_SCHEMA_DEPTH
						+ " due to the size of the specification");
				IterativeSchemaDepthLimiter.limitSchemaDepth(openAPI, MAX_NESTED_SCHEMA_DEPTH);
			} else {
				IterativeSchemaDepthLimiter.limitSchemaDepth(openAPI, DEFAULT_NESTED_SCHEMA_DEPTH);
			}

			if (openAPI == null) {
				logger.error("Failed to parse OpenAPI specification.\n{}", parseResult.getMessages());
				throw new Exception("Failed to parse OpenAPI specification.\n" + parseResult.getMessages());
			}

			Paths oasPaths = openAPI.getPaths();
			oasPaths.forEach((oasPath, pathItem) -> {
				// read parameters common to all operations in this 'path'
				logger.debug("working on {}", oasPath);
				List<Parameter> pathItemParameters = handleParameterPrefixes(pathItem.getParameters(), queryPrefix,
						headerPrefix, pathParamPrefix);

				pathItem.readOperationsMap().forEach((httpMethod, operation) -> {
					logger.debug("\tmethod {}", httpMethod);

					String summary = defaultIfBlank(operation.getSummary(), "No summary");

					String operationId = operation.getOperationId();

					// generate an operationId, if it is missing
					// creating a default operationId if it is missing is done as well when creating
					// the mcp tool specification
					// TODO: create a utility that is used both here and inside the wM IS Java
					// service
					// wx.mcp.server.services.utils:extractOperationsFromOpenAPI
					if (operationId == null || operationId.isBlank()) {
						String sanitizedPath = oasPath.replaceAll("[{}\\/]", "_").replaceAll("_+", "_");
						operationId = httpMethod.name().toLowerCase() + "_" + sanitizedPath;
						// return;
					}
					if (!allowedMethods.contains(httpMethod)) {
						logger.debug("\tmethod {} is in the allowed list", httpMethod);
						return;
					}

					// --- Build tool name and ID with mcpObjectName if present ---
					String toolName;
					if (!isNullOrEmpty(mcpObjectName)) {
						toolName = mcpObjectName + "_" + operationId;
					} else {
						toolName = operationId;
					}

					// Create MCP Tool
					Tool mcpTool = new Tool();
					mcpTool.setName(toolName);
					mcpTool.setDescription(summary);

					// MCP TOOL INPUT SCHEMA ------------------
					logger.debug("working on the input schema");
					InputSchema inputSchema = new InputSchema();
					inputSchema.setType("object");
					Properties inputSchemaProps = new Properties();

					// Operation-level parameters
					List<Parameter> operationParams = handleParameterPrefixes(operation.getParameters(), queryPrefix,
							headerPrefix, pathParamPrefix);
					// merge both the 'path' parameters and the 'operation' ones
					List<Parameter> allParams = new ArrayList<Parameter>();
					allParams.addAll(pathItemParameters);
					allParams.addAll(operationParams);

					// get the required parameters
					List<String> requiredParams = allParams.stream().filter(Parameter::getRequired)
							.map(Parameter::getName).collect(Collectors.toList());

					allParams.forEach((param) -> {
						logger.debug("\t\tparameter {}", param.getName());
						Schema<?> schema = param.getSchema();
						PropertiesProperty prop = new PropertiesProperty();

						processSchema(mapper, schema, prop);
						inputSchemaProps.setAdditionalProperty(param.getName(), prop);
					});

					// request body properties will be added to those parameters found previously
					if (operation.getRequestBody() != null && operation.getRequestBody().getContent() != null) {
						Content content = operation.getRequestBody().getContent();
						boolean containsSupportedContent = content.keySet().stream()
								.anyMatch(key -> Arrays.stream(SUPPORTED_CONTENT_TYPES).anyMatch(key::contains));
						// if (!content.containsKey(APPLICATION_JSON) ) {
						if (!containsSupportedContent) {
							Set<String> keySet = content.keySet();
							String unhandledKeys = keySet.stream().collect(Collectors.joining(","));
							logger.debug(
									"\t\tMCP Tool will not be generated -> request body contains only unhandled keys: {}",
									unhandledKeys);
							return;
						} else {
							if (content.containsKey(APPLICATION_JSON)) {
								MediaType appJsonMT = content.get(APPLICATION_JSON);
								Schema<?> appJsonSchema = appJsonMT.getSchema();
								Map<String, Schema> properties = appJsonSchema.getProperties();

								if (properties != null) {
									properties.forEach((propName, propSchema) -> {
										PropertiesProperty toolProp = new PropertiesProperty();
										processSchema(mapper, propSchema, toolProp);
										inputSchemaProps.setAdditionalProperty(propName, toolProp);
									});
								} else {
									// TODO: ADD WARNING/ERROR/EXCEPTION
								}
								List<String> required = appJsonSchema.getRequired();
								if (required != null && !required.isEmpty()) {
									requiredParams.addAll(required);
								}
							} else if (content.containsKey(X_WWW_FORM_URLENCODED)) {
								MediaType xWwwMT = content.get(X_WWW_FORM_URLENCODED);
								Schema<?> xWwwSchema = xWwwMT.getSchema();
								Map<String, Schema> properties = xWwwSchema.getProperties();

								properties.forEach((propName, propSchema) -> {
									PropertiesProperty toolProp = new PropertiesProperty();
									processSchema(mapper, propSchema, toolProp);
									inputSchemaProps.setAdditionalProperty(propName, toolProp);
								});
								List<String> required = xWwwSchema.getRequired();
								if (required != null && !required.isEmpty()) {
									requiredParams.addAll(required);
								}
							} else {
								logger.error(
										"MPC Tool will not be generated -> conflict while resolving the request body");
							}
						}
					}

					inputSchema.setProperties(inputSchemaProps);
					if (!requiredParams.isEmpty()) {
						inputSchema.setRequired(requiredParams);
					}
					mcpTool.setInputSchema(inputSchema);
					// MCP TOOL INPUT SCHEMA
					// ----------------------------------------------------------------------------

					// MCP TOOL OUTPUT SCHEMA
					// ---------------------------------------------------------------------------
					logger.debug("working on the output schema");
					OutputSchema outputSchema = new OutputSchema();
					Properties resultProperty = new Properties();
					PropertiesProperty oneOfProperty = new PropertiesProperty();
					List<PropertiesProperty> listOfProps = new ArrayList<PropertiesProperty>();

					oneOfProperty.setAdditionalProperty("oneOf", listOfProps);
					resultProperty.setAdditionalProperty("result", oneOfProperty);

					// there will only be one result element in the created output schema.
					// the result element will contain all elements defined in the different
					// responses
					outputSchema.setRequired(Collections.singletonList("result"));
					outputSchema.setType("object");

					ApiResponses pathItemResponses = operation.getResponses();
					pathItemResponses.forEach((status, apiResponse) -> {
						logger.debug("response {}", status);
						Content respStatusContent = apiResponse.getContent();
						if (respStatusContent != null) {
							if (respStatusContent.containsKey(APPLICATION_JSON)) {
								MediaType mediaType = respStatusContent.get(APPLICATION_JSON);
								Schema<?> schema = mediaType.getSchema();
								PropertiesProperty currentResponseProps = new PropertiesProperty();
								processSchema(mapper, schema, currentResponseProps);
								listOfProps.add(currentResponseProps);
								logger.debug("\t\thandled application/json response schema");
							} else {
								Set<String> keySet = respStatusContent.keySet();
								String unhandledKeys = keySet.stream().collect(Collectors.joining(","));
								logger.debug(
										"MCP Tool will not be generated -> response does only contain unhandled content-types {}",
										unhandledKeys);
							}
						}
					});
					outputSchema.setProperties(resultProperty);
					mcpTool.setOutputSchema(outputSchema);
					// MCP TOOL OUTPUT SCHEMA ------------------

					mcpTools.getTools().add(mcpTool);
				});
			});

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}
		return mcpTools;
	}

	private void processSchema(ObjectMapper mapper, Schema<?> propSchema, PropertiesProperty toolProp) {
		if (propSchema == null)
			return;

		// Convert the schema to a Map<String, Object>
		Map<String, Object> schemaMap = mapper.convertValue(propSchema, new TypeReference<Map<String, Object>>() {
		});

		// Clean the map (e.g., remove nulls and excluded keys)
		cleanMap(schemaMap);

		// Add cleaned properties to toolProp
		schemaMap.forEach(toolProp::setAdditionalProperty);
	}

	private static void cleanSchema(Schema<?> schema) {
		if (schema == null)
			return;

		// Remove excluded keys from extensions
		if (schema.getExtensions() != null) {
			schema.getExtensions().keySet().removeIf(excludedKeys::contains);
		}

		// Recursively clean nested schemas
		if (schema.getProperties() != null) {
			for (Schema<?> nested : schema.getProperties().values()) {
				cleanSchema(nested);
			}
		}

		if (schema.getItems() != null) {
			cleanSchema(schema.getItems());
		}
	}

	/**
	 * Recursively remove those entries with a null value and those included in the
	 * excludedKeys
	 * 
	 * @param map
	 */
	@SuppressWarnings("unchecked")
	public static void cleanMap(Map<String, Object> map) {
		if (map == null)
			return;

		// Remove excluded keys and null values
		map.entrySet().removeIf(entry -> entry.getValue() == null || excludedKeys.contains(entry.getKey()));

		for (Map.Entry<String, Object> entry : map.entrySet()) {
			Object value = entry.getValue();

			if (value instanceof Map<?, ?> nestedMap) {
				cleanMap((Map<String, Object>) nestedMap);
			} else if (value instanceof Schema<?> schema) {
				cleanSchema(schema);
			} else if (value instanceof List<?> list) {
				cleanList(list);
			}
		}

	}

	private static void cleanList(List<?> list) {
		if (list == null || list.isEmpty())
			return;

		// Remove unwanted elements from the list
		list.removeIf(item -> {
			if (item == null)
				return true;

			if (item instanceof String str) {
				return excludedKeys.contains(str);
			}

			if (item instanceof Map<?, ?> map) {
				logger.debug("map item from list will be cleaned");
				cleanMap((Map<String, Object>) map);
			} else if (item instanceof Schema<?> schema) {
				logger.debug("schema item from list will be cleaned");
				cleanSchema(schema);
			}

			return false;
		});
	}

	// Utility method
	private boolean isNullOrEmpty(String str) {
		return str == null || str.trim().isEmpty();
	}

	private static String defaultIfBlank(String value, String defaultValue) {
		return (value == null || value.isBlank()) ? defaultValue : value;
	}

	private List<Parameter> handleParameterPrefixes(List<Parameter> params, String queryPrefix, String headerPrefix,
			String pathParamPrefix) {

		if (params == null || params.isEmpty()) {
			return Collections.emptyList();
		}

		for (Parameter param : params) {
			String name = param.getName();
			String in = param.getIn();

			String propertyName = switch (in) {
			case "query" -> name.startsWith(queryPrefix) ? name : queryPrefix + name;
			case "header" -> name.startsWith(headerPrefix) ? name : headerPrefix + name;
			case "path" -> name.startsWith(pathParamPrefix) ? name : pathParamPrefix + name;
			default -> null;
			};

			if (propertyName != null) {
				param.setName(propertyName);
			}
		}
		return params;
	}
}

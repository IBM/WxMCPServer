
package wx.mcp.server.models;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * Definition for a tool the client can call.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "_meta",
    "annotations",
    "description",
    "inputSchema",
    "name",
    "outputSchema",
    "title"
})
@Generated("jsonschema2pojo")
public class Tool {

    /**
     * See [General fields: `_meta`](/specification/2025-06-18/basic/index#meta) for notes on `_meta` usage.
     * 
     */
    @JsonProperty("_meta")
    @JsonPropertyDescription("See [General fields: `_meta`](/specification/2025-06-18/basic/index#meta) for notes on `_meta` usage.")
    private Meta meta;
    /**
     * Additional properties describing a Tool to clients.
     * 
     * NOTE: all properties in ToolAnnotations are **hints**.
     * They are not guaranteed to provide a faithful description of
     * tool behavior (including descriptive properties like `title`).
     * 
     * Clients should never make tool use decisions based on ToolAnnotations
     * received from untrusted servers.
     * 
     */
    @JsonProperty("annotations")
    @JsonPropertyDescription("Additional properties describing a Tool to clients.\n\nNOTE: all properties in ToolAnnotations are **hints**.\nThey are not guaranteed to provide a faithful description of\ntool behavior (including descriptive properties like `title`).\n\nClients should never make tool use decisions based on ToolAnnotations\nreceived from untrusted servers.")
    private ToolAnnotations annotations;
    /**
     * A human-readable description of the tool.
     * 
     * This can be used by clients to improve the LLM's understanding of available tools. It can be thought of like a "hint" to the model.
     * 
     */
    @JsonProperty("description")
    @JsonPropertyDescription("A human-readable description of the tool.\n\nThis can be used by clients to improve the LLM's understanding of available tools. It can be thought of like a \"hint\" to the model.")
    private String description;
    /**
     * A JSON Schema object defining the expected parameters for the tool.
     * (Required)
     * 
     */
    @JsonProperty("inputSchema")
    @JsonPropertyDescription("A JSON Schema object defining the expected parameters for the tool.")
    private InputSchema inputSchema;
    /**
     * Intended for programmatic or logical use, but used as a display name in past specs or fallback (if title isn't present).
     * (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Intended for programmatic or logical use, but used as a display name in past specs or fallback (if title isn't present).")
    private String name;
    /**
     * An optional JSON Schema object defining the structure of the tool's output returned in
     * the structuredContent field of a CallToolResult.
     * 
     */
    @JsonProperty("outputSchema")
    @JsonPropertyDescription("An optional JSON Schema object defining the structure of the tool's output returned in\nthe structuredContent field of a CallToolResult.")
    private OutputSchema outputSchema;
    /**
     * Intended for UI and end-user contexts — optimized to be human-readable and easily understood,
     * even by those unfamiliar with domain-specific terminology.
     * 
     * If not provided, the name should be used for display (except for Tool,
     * where `annotations.title` should be given precedence over using `name`,
     * if present).
     * 
     */
    @JsonProperty("title")
    @JsonPropertyDescription("Intended for UI and end-user contexts \u2014 optimized to be human-readable and easily understood,\neven by those unfamiliar with domain-specific terminology.\n\nIf not provided, the name should be used for display (except for Tool,\nwhere `annotations.title` should be given precedence over using `name`,\nif present).")
    private String title;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    /**
     * See [General fields: `_meta`](/specification/2025-06-18/basic/index#meta) for notes on `_meta` usage.
     * 
     */
    @JsonProperty("_meta")
    public Meta getMeta() {
        return meta;
    }

    /**
     * See [General fields: `_meta`](/specification/2025-06-18/basic/index#meta) for notes on `_meta` usage.
     * 
     */
    @JsonProperty("_meta")
    public void setMeta(Meta meta) {
        this.meta = meta;
    }

    /**
     * Additional properties describing a Tool to clients.
     * 
     * NOTE: all properties in ToolAnnotations are **hints**.
     * They are not guaranteed to provide a faithful description of
     * tool behavior (including descriptive properties like `title`).
     * 
     * Clients should never make tool use decisions based on ToolAnnotations
     * received from untrusted servers.
     * 
     */
    @JsonProperty("annotations")
    public ToolAnnotations getAnnotations() {
        return annotations;
    }

    /**
     * Additional properties describing a Tool to clients.
     * 
     * NOTE: all properties in ToolAnnotations are **hints**.
     * They are not guaranteed to provide a faithful description of
     * tool behavior (including descriptive properties like `title`).
     * 
     * Clients should never make tool use decisions based on ToolAnnotations
     * received from untrusted servers.
     * 
     */
    @JsonProperty("annotations")
    public void setAnnotations(ToolAnnotations annotations) {
        this.annotations = annotations;
    }

    /**
     * A human-readable description of the tool.
     * 
     * This can be used by clients to improve the LLM's understanding of available tools. It can be thought of like a "hint" to the model.
     * 
     */
    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    /**
     * A human-readable description of the tool.
     * 
     * This can be used by clients to improve the LLM's understanding of available tools. It can be thought of like a "hint" to the model.
     * 
     */
    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * A JSON Schema object defining the expected parameters for the tool.
     * (Required)
     * 
     */
    @JsonProperty("inputSchema")
    public InputSchema getInputSchema() {
        return inputSchema;
    }

    /**
     * A JSON Schema object defining the expected parameters for the tool.
     * (Required)
     * 
     */
    @JsonProperty("inputSchema")
    public void setInputSchema(InputSchema inputSchema) {
        this.inputSchema = inputSchema;
    }

    /**
     * Intended for programmatic or logical use, but used as a display name in past specs or fallback (if title isn't present).
     * (Required)
     * 
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * Intended for programmatic or logical use, but used as a display name in past specs or fallback (if title isn't present).
     * (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    /**
     * An optional JSON Schema object defining the structure of the tool's output returned in
     * the structuredContent field of a CallToolResult.
     * 
     */
    @JsonProperty("outputSchema")
    public OutputSchema getOutputSchema() {
        return outputSchema;
    }

    /**
     * An optional JSON Schema object defining the structure of the tool's output returned in
     * the structuredContent field of a CallToolResult.
     * 
     */
    @JsonProperty("outputSchema")
    public void setOutputSchema(OutputSchema outputSchema) {
        this.outputSchema = outputSchema;
    }

    /**
     * Intended for UI and end-user contexts — optimized to be human-readable and easily understood,
     * even by those unfamiliar with domain-specific terminology.
     * 
     * If not provided, the name should be used for display (except for Tool,
     * where `annotations.title` should be given precedence over using `name`,
     * if present).
     * 
     */
    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    /**
     * Intended for UI and end-user contexts — optimized to be human-readable and easily understood,
     * even by those unfamiliar with domain-specific terminology.
     * 
     * If not provided, the name should be used for display (except for Tool,
     * where `annotations.title` should be given precedence over using `name`,
     * if present).
     * 
     */
    @JsonProperty("title")
    public void setTitle(String title) {
        this.title = title;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Tool.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("meta");
        sb.append('=');
        sb.append(((this.meta == null)?"<null>":this.meta));
        sb.append(',');
        sb.append("annotations");
        sb.append('=');
        sb.append(((this.annotations == null)?"<null>":this.annotations));
        sb.append(',');
        sb.append("description");
        sb.append('=');
        sb.append(((this.description == null)?"<null>":this.description));
        sb.append(',');
        sb.append("inputSchema");
        sb.append('=');
        sb.append(((this.inputSchema == null)?"<null>":this.inputSchema));
        sb.append(',');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null)?"<null>":this.name));
        sb.append(',');
        sb.append("outputSchema");
        sb.append('=');
        sb.append(((this.outputSchema == null)?"<null>":this.outputSchema));
        sb.append(',');
        sb.append("title");
        sb.append('=');
        sb.append(((this.title == null)?"<null>":this.title));
        sb.append(',');
        sb.append("additionalProperties");
        sb.append('=');
        sb.append(((this.additionalProperties == null)?"<null>":this.additionalProperties));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = ((result* 31)+((this.outputSchema == null)? 0 :this.outputSchema.hashCode()));
        result = ((result* 31)+((this.inputSchema == null)? 0 :this.inputSchema.hashCode()));
        result = ((result* 31)+((this.meta == null)? 0 :this.meta.hashCode()));
        result = ((result* 31)+((this.name == null)? 0 :this.name.hashCode()));
        result = ((result* 31)+((this.annotations == null)? 0 :this.annotations.hashCode()));
        result = ((result* 31)+((this.description == null)? 0 :this.description.hashCode()));
        result = ((result* 31)+((this.additionalProperties == null)? 0 :this.additionalProperties.hashCode()));
        result = ((result* 31)+((this.title == null)? 0 :this.title.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Tool) == false) {
            return false;
        }
        Tool rhs = ((Tool) other);
        return (((((((((this.outputSchema == rhs.outputSchema)||((this.outputSchema!= null)&&this.outputSchema.equals(rhs.outputSchema)))&&((this.inputSchema == rhs.inputSchema)||((this.inputSchema!= null)&&this.inputSchema.equals(rhs.inputSchema))))&&((this.meta == rhs.meta)||((this.meta!= null)&&this.meta.equals(rhs.meta))))&&((this.name == rhs.name)||((this.name!= null)&&this.name.equals(rhs.name))))&&((this.annotations == rhs.annotations)||((this.annotations!= null)&&this.annotations.equals(rhs.annotations))))&&((this.description == rhs.description)||((this.description!= null)&&this.description.equals(rhs.description))))&&((this.additionalProperties == rhs.additionalProperties)||((this.additionalProperties!= null)&&this.additionalProperties.equals(rhs.additionalProperties))))&&((this.title == rhs.title)||((this.title!= null)&&this.title.equals(rhs.title))));
    }

}

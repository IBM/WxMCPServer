
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
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "destructiveHint",
    "idempotentHint",
    "openWorldHint",
    "readOnlyHint",
    "title"
})
@Generated("jsonschema2pojo")
public class ToolAnnotations {

    /**
     * If true, the tool may perform destructive updates to its environment.
     * If false, the tool performs only additive updates.
     * 
     * (This property is meaningful only when `readOnlyHint == false`)
     * 
     * Default: true
     * 
     */
    @JsonProperty("destructiveHint")
    @JsonPropertyDescription("If true, the tool may perform destructive updates to its environment.\nIf false, the tool performs only additive updates.\n\n(This property is meaningful only when `readOnlyHint == false`)\n\nDefault: true")
    private boolean destructiveHint;
    /**
     * If true, calling the tool repeatedly with the same arguments
     * will have no additional effect on the its environment.
     * 
     * (This property is meaningful only when `readOnlyHint == false`)
     * 
     * Default: false
     * 
     */
    @JsonProperty("idempotentHint")
    @JsonPropertyDescription("If true, calling the tool repeatedly with the same arguments\nwill have no additional effect on the its environment.\n\n(This property is meaningful only when `readOnlyHint == false`)\n\nDefault: false")
    private boolean idempotentHint;
    /**
     * If true, this tool may interact with an "open world" of external
     * entities. If false, the tool's domain of interaction is closed.
     * For example, the world of a web search tool is open, whereas that
     * of a memory tool is not.
     * 
     * Default: true
     * 
     */
    @JsonProperty("openWorldHint")
    @JsonPropertyDescription("If true, this tool may interact with an \"open world\" of external\nentities. If false, the tool's domain of interaction is closed.\nFor example, the world of a web search tool is open, whereas that\nof a memory tool is not.\n\nDefault: true")
    private boolean openWorldHint;
    /**
     * If true, the tool does not modify its environment.
     * 
     * Default: false
     * 
     */
    @JsonProperty("readOnlyHint")
    @JsonPropertyDescription("If true, the tool does not modify its environment.\n\nDefault: false")
    private boolean readOnlyHint;
    /**
     * A human-readable title for the tool.
     * 
     */
    @JsonProperty("title")
    @JsonPropertyDescription("A human-readable title for the tool.")
    private String title;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    /**
     * If true, the tool may perform destructive updates to its environment.
     * If false, the tool performs only additive updates.
     * 
     * (This property is meaningful only when `readOnlyHint == false`)
     * 
     * Default: true
     * 
     */
    @JsonProperty("destructiveHint")
    public boolean isDestructiveHint() {
        return destructiveHint;
    }

    /**
     * If true, the tool may perform destructive updates to its environment.
     * If false, the tool performs only additive updates.
     * 
     * (This property is meaningful only when `readOnlyHint == false`)
     * 
     * Default: true
     * 
     */
    @JsonProperty("destructiveHint")
    public void setDestructiveHint(boolean destructiveHint) {
        this.destructiveHint = destructiveHint;
    }

    /**
     * If true, calling the tool repeatedly with the same arguments
     * will have no additional effect on the its environment.
     * 
     * (This property is meaningful only when `readOnlyHint == false`)
     * 
     * Default: false
     * 
     */
    @JsonProperty("idempotentHint")
    public boolean isIdempotentHint() {
        return idempotentHint;
    }

    /**
     * If true, calling the tool repeatedly with the same arguments
     * will have no additional effect on the its environment.
     * 
     * (This property is meaningful only when `readOnlyHint == false`)
     * 
     * Default: false
     * 
     */
    @JsonProperty("idempotentHint")
    public void setIdempotentHint(boolean idempotentHint) {
        this.idempotentHint = idempotentHint;
    }

    /**
     * If true, this tool may interact with an "open world" of external
     * entities. If false, the tool's domain of interaction is closed.
     * For example, the world of a web search tool is open, whereas that
     * of a memory tool is not.
     * 
     * Default: true
     * 
     */
    @JsonProperty("openWorldHint")
    public boolean isOpenWorldHint() {
        return openWorldHint;
    }

    /**
     * If true, this tool may interact with an "open world" of external
     * entities. If false, the tool's domain of interaction is closed.
     * For example, the world of a web search tool is open, whereas that
     * of a memory tool is not.
     * 
     * Default: true
     * 
     */
    @JsonProperty("openWorldHint")
    public void setOpenWorldHint(boolean openWorldHint) {
        this.openWorldHint = openWorldHint;
    }

    /**
     * If true, the tool does not modify its environment.
     * 
     * Default: false
     * 
     */
    @JsonProperty("readOnlyHint")
    public boolean isReadOnlyHint() {
        return readOnlyHint;
    }

    /**
     * If true, the tool does not modify its environment.
     * 
     * Default: false
     * 
     */
    @JsonProperty("readOnlyHint")
    public void setReadOnlyHint(boolean readOnlyHint) {
        this.readOnlyHint = readOnlyHint;
    }

    /**
     * A human-readable title for the tool.
     * 
     */
    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    /**
     * A human-readable title for the tool.
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
        sb.append(ToolAnnotations.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("destructiveHint");
        sb.append('=');
        sb.append(this.destructiveHint);
        sb.append(',');
        sb.append("idempotentHint");
        sb.append('=');
        sb.append(this.idempotentHint);
        sb.append(',');
        sb.append("openWorldHint");
        sb.append('=');
        sb.append(this.openWorldHint);
        sb.append(',');
        sb.append("readOnlyHint");
        sb.append('=');
        sb.append(this.readOnlyHint);
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
        result = ((result* 31)+(this.idempotentHint? 1 : 0));
        result = ((result* 31)+(this.openWorldHint? 1 : 0));
        result = ((result* 31)+((this.additionalProperties == null)? 0 :this.additionalProperties.hashCode()));
        result = ((result* 31)+((this.title == null)? 0 :this.title.hashCode()));
        result = ((result* 31)+(this.destructiveHint? 1 : 0));
        result = ((result* 31)+(this.readOnlyHint? 1 : 0));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ToolAnnotations) == false) {
            return false;
        }
        ToolAnnotations rhs = ((ToolAnnotations) other);
        return ((((((this.idempotentHint == rhs.idempotentHint)&&(this.openWorldHint == rhs.openWorldHint))&&((this.additionalProperties == rhs.additionalProperties)||((this.additionalProperties!= null)&&this.additionalProperties.equals(rhs.additionalProperties))))&&((this.title == rhs.title)||((this.title!= null)&&this.title.equals(rhs.title))))&&(this.destructiveHint == rhs.destructiveHint))&&(this.readOnlyHint == rhs.readOnlyHint));
    }

}

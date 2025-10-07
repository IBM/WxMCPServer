
package wx.mcp.server.models;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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
 * ListToolsResponse
 * <p>
 * The server's response to a tools/list request from the client.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "_meta",
    "nextCursor",
    "tools"
})
@Generated("jsonschema2pojo")
public class ListToolsResponse {

    /**
     * See [General fields: `_meta`](/specification/2025-06-18/basic/index#meta) for notes on `_meta` usage.
     * 
     */
    @JsonProperty("_meta")
    @JsonPropertyDescription("See [General fields: `_meta`](/specification/2025-06-18/basic/index#meta) for notes on `_meta` usage.")
    private Meta meta;
    /**
     * An opaque token representing the pagination position after the last returned result.
     * If present, there may be more results available.
     * 
     */
    @JsonProperty("nextCursor")
    @JsonPropertyDescription("An opaque token representing the pagination position after the last returned result.\nIf present, there may be more results available.")
    private String nextCursor;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("tools")
    private List<Tool> tools = new ArrayList<Tool>();
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
     * An opaque token representing the pagination position after the last returned result.
     * If present, there may be more results available.
     * 
     */
    @JsonProperty("nextCursor")
    public String getNextCursor() {
        return nextCursor;
    }

    /**
     * An opaque token representing the pagination position after the last returned result.
     * If present, there may be more results available.
     * 
     */
    @JsonProperty("nextCursor")
    public void setNextCursor(String nextCursor) {
        this.nextCursor = nextCursor;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("tools")
    public List<Tool> getTools() {
        return tools;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("tools")
    public void setTools(List<Tool> tools) {
        this.tools = tools;
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
        sb.append(ListToolsResponse.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("meta");
        sb.append('=');
        sb.append(((this.meta == null)?"<null>":this.meta));
        sb.append(',');
        sb.append("nextCursor");
        sb.append('=');
        sb.append(((this.nextCursor == null)?"<null>":this.nextCursor));
        sb.append(',');
        sb.append("tools");
        sb.append('=');
        sb.append(((this.tools == null)?"<null>":this.tools));
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
        result = ((result* 31)+((this.nextCursor == null)? 0 :this.nextCursor.hashCode()));
        result = ((result* 31)+((this.additionalProperties == null)? 0 :this.additionalProperties.hashCode()));
        result = ((result* 31)+((this.tools == null)? 0 :this.tools.hashCode()));
        result = ((result* 31)+((this.meta == null)? 0 :this.meta.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ListToolsResponse) == false) {
            return false;
        }
        ListToolsResponse rhs = ((ListToolsResponse) other);
        return (((((this.nextCursor == rhs.nextCursor)||((this.nextCursor!= null)&&this.nextCursor.equals(rhs.nextCursor)))&&((this.additionalProperties == rhs.additionalProperties)||((this.additionalProperties!= null)&&this.additionalProperties.equals(rhs.additionalProperties))))&&((this.tools == rhs.tools)||((this.tools!= null)&&this.tools.equals(rhs.tools))))&&((this.meta == rhs.meta)||((this.meta!= null)&&this.meta.equals(rhs.meta))));
    }

}

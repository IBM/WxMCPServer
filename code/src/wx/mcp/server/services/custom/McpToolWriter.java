package wx.mcp.server.services.custom;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.io.SegmentedStringWriter;
import com.fasterxml.jackson.core.util.BufferRecycler;
import com.fasterxml.jackson.core.util.JsonRecyclerPools;
import com.fasterxml.jackson.core.util.RecyclerPool;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

import wx.mcp.server.models.Meta;
import wx.mcp.server.models.Tool;

public class McpToolWriter {

    /**
     * Streams a huge tools array into a single String (no giant List in memory).
     */
    public static String listToolsToString(Meta meta,
            String nextCursor,
            Iterable<Tool> toolsProducer) throws Exception {
        ObjectMapper mapper = newMapper();

        // Acquire a BufferRecycler from a pool
        RecyclerPool<BufferRecycler> pool = JsonRecyclerPools.defaultPool();
        BufferRecycler br = pool.acquireAndLinkPooled();

        String json;
        SegmentedStringWriter ssw = new SegmentedStringWriter(br);
        try (JsonGenerator g = mapper.getFactory().createGenerator(ssw)) {
            g.writeStartObject();

            if (meta != null) {
                g.writeFieldName("_meta");
                mapper.writeValue(g, meta);
            }
            if (nextCursor != null) {
                g.writeStringField("nextCursor", nextCursor);
            }

            g.writeFieldName("tools");
            g.writeStartArray();
            for (Tool t : toolsProducer) {
                mapper.writeValue(g, t); // serialize one Tool at a time
            }
            g.writeEndArray();

            g.writeEndObject();
        }
        json = ssw.getAndClear();

        // Return recycler to the pool
        pool.releasePooled(br);

        return json; // final String (must fit in heap)
    }

    private static ObjectMapper newMapper() {
        JsonFactory jf = JsonFactory.builder()
                // Optional: reduces symbol/intern table pressure for very large, diverse field
                // sets
                .disable(JsonFactory.Feature.CANONICALIZE_FIELD_NAMES)
                .disable(JsonFactory.Feature.INTERN_FIELD_NAMES)
                .build();

        return JsonMapper.builder(jf)
                .defaultPropertyInclusion(JsonInclude.Value.construct(
                        JsonInclude.Include.NON_EMPTY, JsonInclude.Include.NON_EMPTY))
                .disable(SerializationFeature.INDENT_OUTPUT) // keep output compact
                .disable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS) // avoid sorting overhead
                .build();
    }
}
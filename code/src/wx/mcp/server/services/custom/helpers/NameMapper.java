package wx.mcp.server.services.custom.helpers;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Helper that applies a {@link NamingStrategy} and guarantees collision-free, deterministic
 * tool argument names for a single operation/tool build.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Apply the provided {@link NamingStrategy} to derive a base name.</li>
 *   <li>Ensure uniqueness (e.g., if {@code id} already exists, produce {@code id_2}, {@code id_3}, ...).</li>
 *   <li>Memoize results per ({@code name}, {@code in}) pair for consistency within a build.</li>
 * </ul>
 * <p>
 * <b>Lifecycle:</b> Create a new instance per tool/operation build. It is not thread-safe.
 * <p>
 */
public final class NameMapper {

  private final NamingStrategy strategy;
  private final String headerPrefix;
  private final String pathPrefix;
  private final String queryPrefix;

  // Tracks used output names to guarantee uniqueness (in insertion order).
  private final Set<String> used = new LinkedHashSet<>();

  // Memoizes mapping decisions per (name|in) key for stable reuse during a single build.
  private final Map<String, String> cacheByKey = new LinkedHashMap<>();

  /**
   * Construct a new mapper with the given strategy and prefixes.
   *
   * @param strategy     The naming strategy to use (must not be {@code null}).
   * @param headerPrefix Prefix for {@code in="header"} (e.g., "hdr_").
   * @param pathPrefix   Prefix for {@code in="path"}   (e.g., "path_").
   * @param queryPrefix  Prefix for {@code in="query"}  (e.g., "q_").
   */
  public NameMapper(NamingStrategy strategy,
                    String headerPrefix,
                    String pathPrefix,
                    String queryPrefix) {
    if (strategy == null) throw new IllegalArgumentException("strategy must not be null");
    this.strategy = strategy;
    this.headerPrefix = headerPrefix == null ? "" : headerPrefix;
    this.pathPrefix   = pathPrefix   == null ? "" : pathPrefix;
    this.queryPrefix  = queryPrefix  == null ? "" : queryPrefix;
  }

  /**
   * Map a ({@code rawName}, {@code in}) pair to a unique, namespaced argument name.
   * Results are memoized so repeated calls with the same inputs yield the same output.
   *
   * @param rawName Original parameter name (may be {@code null}).
   * @param in      Parameter location: "header", "path", "query", "cookie" (may be {@code null}).
   * @return A non-null, collision-free mapped name. If the underlying strategy returns an
   *         already-used name, a numeric suffix is appended (e.g., {@code _2}, {@code _3}, ...).
   */
  public String map(String rawName, String in) {
    final String key = (rawName == null ? "" : rawName) + "|" + (in == null ? "" : in);
    String cached = cacheByKey.get(key);
    if (cached != null) return cached;

    String base = strategy.apply(in, rawName, headerPrefix, pathPrefix, queryPrefix);
    if (base == null) base = "";
    if (base.isEmpty() && rawName != null) base = rawName; // last resort

    String unique = base;
    if (!used.add(unique)) {
      int i = 2;
      String attempt = base + "_" + i;
      while (!used.add(attempt)) {
        attempt = base + "_" + (++i);
      }
      unique = attempt;
    }
    cacheByKey.put(key, unique);
    return unique;
  }

  /**
   * Clear internal state (used names and memoized entries).
   */
  public void reset() {
    used.clear();
    cacheByKey.clear();
  }

  /**
   * A simple default strategy: add {@code header}, {@code path}, {@code query} prefixes by location.
   * <pre>
   *   header → headerPrefix + raw
   *   path   → pathPrefix   + raw
   *   query  → queryPrefix  + raw
   *   other  → raw
   * </pre>
   */
  public static final NamingStrategy DEFAULT_STRATEGY = (in, raw, h, p, q) -> {
    String name = raw == null ? "" : raw;
    if ("header".equals(in)) return h + name;
    if ("path".equals(in))   return p + name;
    if ("query".equals(in))  return q + name;
    return name; // cookie or unknown
  };
}
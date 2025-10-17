package wx.mcp.server.services.custom.helpers;

/**
 * Strategy for mapping an OpenAPI parameter's location ({@code in}) and original
 * {@code name} to a tool argument (property) name.
 * <p>
 * Typical implementations add a namespace/prefix based on {@code in} ("header", "path", "query")
 * and return the resulting name. The strategy does not handle uniqueness: that is the
 * responsibility of {@link NameMapper}.
 * <p>
 * <b>Thread-safety:</b> Implementations should be stateless or otherwise thread-safe.
 */
@FunctionalInterface
public interface NamingStrategy {

  /**
   * Map an OpenAPI parameter to its tool-side argument name.
   *
   * @param in            The parameter location: "header", "path", "query" or "cookie".
   *                      May be {@code null} if the source is non-conforming or unknown.
   * @param raw           The original parameter name from the OpenAPI document. May be {@code null}.
   * @param headerPrefix  The prefix to apply for {@code in="header"} (e.g., "hdr_").
   * @param pathPrefix    The prefix to apply for {@code in="path"}   (e.g., "path_").
   * @param queryPrefix   The prefix to apply for {@code in="query"}  (e.g., "q_").
   * @return The mapped (possibly prefixed) argument name. Implementations should not return
   *         {@code null}; use an empty string if no name can be derived.
   */
  String apply(String in, String raw, String headerPrefix, String pathPrefix, String queryPrefix);
}
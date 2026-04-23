package proxy.src.Models;

public record RequestBuilder(StringBuilder headersBuilder, String hostHeader, User user) {
}

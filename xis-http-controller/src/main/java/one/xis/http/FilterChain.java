package one.xis.http;

public interface FilterChain {
    void doFilter(HttpRequest request, HttpResponse response);
}

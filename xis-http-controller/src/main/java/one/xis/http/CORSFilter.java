package one.xis.http;

import lombok.NonNull;
import one.xis.context.DefaultComponent;

@DefaultComponent
class CORSFilter implements HttpFilter {
    
    @Override
    public void doFilter(@NonNull HttpRequest request, @NonNull HttpResponse response, @NonNull FilterChain chain) {
        String origin = request.getHeader("Origin");
        if (origin != null) {
            // Only set CORS headers if this is a cross-origin request
            response.addHeader("Access-Control-Allow-Origin", origin);
            response.addHeader("Access-Control-Allow-Credentials", "true");
            response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.addHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
            
            if (HttpMethod.OPTIONS == request.getHttpMethod()) {
                response.setStatusCode(204); // No Content
                return; // Don't continue chain for preflight
            }
        }
        chain.doFilter(request, response);
    }
}

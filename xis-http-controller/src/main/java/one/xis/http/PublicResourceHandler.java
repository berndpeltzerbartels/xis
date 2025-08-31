package one.xis.http;

import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Handler für die Auslieferung von Public Resources, inkl. 304-Handling.
 */
public class PublicResourceHandler {
    private final List<String> publicPaths;
    private static final String CACHE_CONTROL_HEADER = "Cache-Control";
    private static final String CACHE_CONTROL_VALUE = "private, max-age=3600";
    private final long startTime = System.currentTimeMillis();

    public PublicResourceHandler(List<String> publicPaths) {
        this.publicPaths = publicPaths;
    }

    public boolean handle(HttpRequest request, HttpResponse response) {
        String requestPath = request.getPath();
        if (isPathTraversal(requestPath)) {
            handleForbidden(response);
            return true;
        }
        URL resourceUrl = findResourceUrl(requestPath);
        if (resourceUrl == null) {
            return false;
        }
        try {
            URLConnection connection = resourceUrl.openConnection();
            long lastModified = connection.getLastModified();
            String ifModifiedSince = request.getHeader("If-Modified-Since");
            if (isNotModified(lastModified, ifModifiedSince)) {
                handleNotModified(response, lastModified);
                return true;
            }
            handleOk(response, connection, lastModified, resourceUrl);
            return true;
        } catch (Exception e) {
            handleError(response, e);
            return true;
        }
    }

    /**
     * Prüft auf Path Traversal (.. oder ähnliche Konstrukte)
     */
    private boolean isPathTraversal(String path) {
        if (path == null) return false;
        if (path.contains("..") || path.contains("\\") || path.contains(":") || path.startsWith("//")) {
            return true;
        }
        // Optional: Normalisierung und weitere Checks
        return false;
    }

    /**
     * Sucht die Resource unter allen publicPaths + requestPath
     */
    private URL findResourceUrl(String requestPath) {
        String normalizedRequestPath = requestPath.startsWith("/") ? requestPath : "/" + requestPath;
        for (String publicPath : publicPaths) {
            if (publicPath.startsWith("/")) {
                publicPath = publicPath.substring(1);
            }
            if (publicPath.endsWith("/")) {
                publicPath = publicPath.substring(0, publicPath.length() - 1);
            }
            String combinedPath = publicPath + normalizedRequestPath;
            URL url = getClass().getClassLoader().getResource(combinedPath);
            if (url != null) {
                return url;
            }
        }
        return null;
    }

    private void handleError(HttpResponse response, Exception e) {
        response.setStatusCode(500);
        response.setBody(("Error loading resource: " + e.getMessage()).getBytes());
    }

    private boolean isNotModified(long lastModified, String ifModifiedSince) {
        if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {
            long ifModifiedSinceEpoch = parseHttpDate(ifModifiedSince);
            return lastModified > 0 && lastModified <= ifModifiedSinceEpoch && ifModifiedSinceEpoch >= startTime;
        }
        return false;
    }

    private void handleNotModified(HttpResponse response, long lastModified) {
        response.setStatusCode(304);
        response.addHeader("Last-Modified", formatHttpDate(lastModified));
        response.addHeader(CACHE_CONTROL_HEADER, CACHE_CONTROL_VALUE);
    }

    private void handleOk(HttpResponse response, URLConnection connection, long lastModified, URL resourceUrl) throws Exception {
        String contentType = getContentType(resourceUrl);
        if (contentType != null) {
            response.addHeader("Content-Type", contentType);
        }
        response.addHeader("Last-Modified", formatHttpDate(lastModified));
        response.addHeader(CACHE_CONTROL_HEADER, CACHE_CONTROL_VALUE);
        response.setStatusCode(200);
        response.setBody(connection.getInputStream().readAllBytes());
    }

    private String getContentType(URL resourceUrl) {
        try {
            return Files.probeContentType(new File(resourceUrl.getFile()).toPath());
        } catch (Exception e) {
            return null;
        }
    }

    private long parseHttpDate(String httpDate) {
        try {
            return java.time.ZonedDateTime.parse(httpDate, DateTimeFormatter.RFC_1123_DATE_TIME)
                    .toInstant().toEpochMilli();
        } catch (Exception e) {
            return 0;
        }
    }

    private String formatHttpDate(long epochMilli) {
        return DateTimeFormatter.RFC_1123_DATE_TIME
                .format(Instant.ofEpochMilli(epochMilli).atZone(ZoneId.of("GMT")));
    }

    private void handleForbidden(HttpResponse response) {
        response.setStatusCode(403);
        response.setBody("Forbidden".getBytes());
    }
}

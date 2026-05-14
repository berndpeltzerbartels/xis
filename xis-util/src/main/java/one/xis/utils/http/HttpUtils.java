package one.xis.utils.http;

import lombok.NonNull;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HttpUtils {

    public static String appendQueryParameters(@NonNull String url, @NonNull Map<String, Object> parameters) {
        StringBuilder sb = new StringBuilder(url);
        if (!url.contains("?")) {
            sb.append("?");
        } else if (!url.endsWith("&")) {
            sb.append("&");
        }

        boolean first = true;
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            if (first) {
                first = false;
            } else {
                sb.append("&");
            }
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }

        return sb.toString();
    }


    /**
     * Localizes the given URL by removing protocol and host parts,
     *
     * @param url the URL to localize
     * @return the localized URL
     */
    public static String localizeUrl(@NonNull String url) {
        String trimmedUrl = url.trim();

        // Verhindert Javascript- oder Daten-URLs
        if (trimmedUrl.matches("(?i)^(javascript|data|vbscript):.*")) {
            return "/";
        }

        // Behandelt protokollrelative URLs wie //example.com/path
        if (trimmedUrl.startsWith("//")) {
            int pathStart = trimmedUrl.indexOf('/', 2);
            return pathStart != -1 ? trimmedUrl.substring(pathStart) : "/";
        }

        // Behandelt absolute URLs wie http://example.com/path
        int protocolEnd = trimmedUrl.indexOf("://");
        if (protocolEnd != -1) {
            int pathStart = trimmedUrl.indexOf('/', protocolEnd + 3);
            return pathStart != -1 ? trimmedUrl.substring(pathStart) : "/";
        }

        // Nimmt an, dass es sich bereits um einen lokalen Pfad handelt.
        // Stellt sicher, dass er mit einem Slash beginnt.
        if (!trimmedUrl.startsWith("/")) {
            return "/" + trimmedUrl;
        }

        return trimmedUrl;
    }

    public static Map<String, String> parseQueryParameters(@NonNull String url) {
        int queryStart = url.indexOf('?');
        String query = url.substring(queryStart + 1);
        String[] pairs = query.split("&");
        Map<String, String> params = new java.util.HashMap<>();
        for (String pair : pairs) {
            int equalsIndex = pair.indexOf('=');
            if (equalsIndex > 0) {
                String key = pair.substring(0, equalsIndex);
                String value = URLDecoder.decode(pair.substring(equalsIndex + 1), StandardCharsets.UTF_8);
                params.put(key, value);
            }
        }
        return params;
    }


    public static HttpURLConnection createPostConnectionFormUrlEncoded(@NonNull String url, @NonNull String requestBody) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setConnectTimeout(10000); // Verbindungs-Timeout: 10 Sekunden
            connection.setReadTimeout(15000); // Lese-Timeout: 15 Sekunden
            connection.getOutputStream().write(requestBody.getBytes());
            return connection;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create HTTP POST connection", e);
        }
    }

    public static HttpURLConnection createGetConnection(@NonNull String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000); // Verbindungs-Timeout: 10 Sekunden
            connection.setReadTimeout(15000); // Lese-Timeout: 15 Sekunden
            return connection;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create HTTP GET connection", e);
        }
    }
}

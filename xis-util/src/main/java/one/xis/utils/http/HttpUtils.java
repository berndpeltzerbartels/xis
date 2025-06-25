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

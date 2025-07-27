package one.xis.http.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
class HttpClientImpl implements HttpClient {
    private static final long DEFAULT_CONNECTION_TIMEOUT_MILLIS = 10000L; // Verbindungs-Timeout: 10 Sekunden
    private static final long READ_TIMEOUT_MILLIS = 10000L; // Lese-Timeout: 10 Sekunden

    private String serverUrl;

    @Override
    public HttpResponse doGet(String url, Map<String, String> headers) throws HttpClientException {
        try {
            URL urlObj = new URL(serverUrl + url);
            HttpURLConnection connection = getHttpURLConnection("GET", headers, urlObj);

            int responseCode = connection.getResponseCode();
            String responseBody = new String(connection.getInputStream().readAllBytes());

            return new HttpResponse(responseBody, responseCode);
        } catch (Exception e) {
            throw new HttpClientException("Error during GET request", e);
        }
    }

    @Override
    public HttpResponse doPost(String url, String requestBody, Map<String, String> headers) throws HttpClientException {
        try {
            if (headers.keySet().stream().map(String::toLowerCase).noneMatch(h -> h.equals("content-length")) && requestBody != null) {
                headers.put("Content-Length", String.valueOf(requestBody.length()));
            }
            URL urlObj = url.startsWith("http") ? new URL(url) : new URL(serverUrl + url);
            HttpURLConnection connection = getHttpURLConnection("POST", headers, urlObj);
            connection.setDoOutput(true);

            // Write request body
            if (requestBody != null && !requestBody.isEmpty()) {
                connection.getOutputStream().write(requestBody.getBytes());
            }

            int responseCode = connection.getResponseCode();
            String responseBody = new String(connection.getInputStream().readAllBytes());

            return new HttpResponse(responseBody, responseCode);
        } catch (Exception e) {
            throw new HttpClientException("Error during POST request", e);
        }
    }


    private static HttpURLConnection getHttpURLConnection(String httpMethod, Map<String, String> headers, URL urlObj) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
        connection.setRequestMethod(httpMethod);
        connection.setConnectTimeout((int) DEFAULT_CONNECTION_TIMEOUT_MILLIS);
        connection.setReadTimeout((int) READ_TIMEOUT_MILLIS);

        // Set headers
        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                connection.setRequestProperty(header.getKey(), header.getValue());
            }
        }
        return connection;
    }


}

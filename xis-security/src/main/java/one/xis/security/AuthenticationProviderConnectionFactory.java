package one.xis.security;

import lombok.NonNull;
import one.xis.context.XISComponent;

import java.net.HttpURLConnection;

@XISComponent
class AuthenticationProviderConnectionFactory {

    HttpURLConnection createPostConnectionFormUrlEncoded(@NonNull String url, @NonNull String requestBody) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new java.net.URL(url).openConnection();
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
}

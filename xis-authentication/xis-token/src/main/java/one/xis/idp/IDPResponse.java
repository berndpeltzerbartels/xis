package one.xis.idp;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.Data;
import one.xis.auth.token.ApiTokens;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class IDPResponse {
    private final ApiTokens apiTokens;

    public Map<String, Object> toOAuth2Response() {
        Map<String, Object> jsonResponse = new LinkedHashMap<>(); // LinkedHashMap behält die Reihenfolge bei
        jsonResponse.put("access_token", apiTokens.getAccessToken());
        jsonResponse.put("refresh_token", apiTokens.getRenewToken());
        jsonResponse.put("expires_in", apiTokens.getAccessTokenExpiresIn().toSeconds());
        jsonResponse.put("token_type", "Bearer");
        if (apiTokens.getRenewTokenExpiresIn() != null) {
            jsonResponse.put("refresh_expires_in", apiTokens.getRenewTokenExpiresIn().toSeconds());
        }
        return jsonResponse;
    }


    public static IDPResponse fromOAuth2Json(String oauth2Json) {
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(oauth2Json, JsonObject.class);

        String accessToken = jsonObject.get("access_token").getAsString();
        String refreshToken = jsonObject.has("refresh_token") ? jsonObject.get("refresh_token").getAsString() : null;
        long expiresIn = jsonObject.get("expires_in").getAsLong();

        Duration refreshTokenDuration = null;
        if (jsonObject.has("refresh_expires_in")) {
            refreshTokenDuration = Duration.ofSeconds(jsonObject.get("refresh_expires_in").getAsLong());
        }

        ApiTokens apiTokens = new ApiTokens(
                accessToken,
                Duration.ofSeconds(expiresIn),
                refreshToken,
                refreshTokenDuration
        );

        return new IDPResponse(apiTokens);
    }
}

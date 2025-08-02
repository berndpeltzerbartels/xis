package one.xis.idp;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.Data;
import one.xis.auth.ApiTokens;

import java.time.Duration;

@Data
public class IDPResponse {
    private final String idToken;
    private final ApiTokens apiTokens;

    public static IDPResponse fromOAuth2Json(String oauth2Json) {
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(oauth2Json, JsonObject.class);

        String idToken = jsonObject.get("id_token").getAsString();
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

        return new IDPResponse(idToken, apiTokens);
    }
}

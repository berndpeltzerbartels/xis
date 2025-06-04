package one.xis.security;

import com.google.gson.Gson;
import one.xis.context.XISComponent;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@XISComponent
public class TokenManagerImpl implements TokenManager {

    private final String secret;
    private final long tokenAliveTimeSeconds;
    private final long renewTokenAliveTimeSeconds;
    private final Gson gson = new Gson();

    private static final byte[] HEADER = """
            {"alg":"HS256","typ":"JWT"}""".getBytes();

    public TokenManagerImpl() {
        this.secret = SecurityUtil.createRandomKey(32);
        this.tokenAliveTimeSeconds = Duration.of(15, ChronoUnit.MINUTES).getSeconds();
        this.renewTokenAliveTimeSeconds = Duration.of(1, ChronoUnit.HOURS).getSeconds();
    }

    @Override
    public TokenResult createTokens(TokenRequest request) {
        Instant now = Instant.now();
        Instant tokenExpiration = now.plusSeconds(request.tokenAliveTimeSeconds());
        Instant renewTokenExpiration = now.plusSeconds(request.renewTokenAliveTimeSeconds());
        String token = createToken(request, tokenExpiration);
        String renewToken = createToken(request, renewTokenExpiration);

        return new TokenResult(
                token,
                tokenExpiration,
                renewToken,
                renewTokenExpiration
        );
    }

    @Override

    public TokenAttributes decodeToken(String token) throws InvalidTokenException {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) throw new InvalidTokenException("Invalid accessToken format");

            String headerPayload = parts[0] + "." + parts[1];
            String signature = parts[2];

            String expectedSig = sign(headerPayload);
            if (!Objects.equals(expectedSig, signature)) {
                throw new InvalidTokenException("Invalid signature");
            }

            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            Map<String, Object> claimsRaw = parseJson(payloadJson);
            String userId = (String) claimsRaw.get("sub");
            List<String> roles = (List<String>) claimsRaw.getOrDefault("roles", List.of());

            long exp = ((Number) claimsRaw.get("exp")).longValue();
            Instant expiresAt = Instant.ofEpochSecond(exp);

            Map<String, String> claims = new HashMap<>();
            for (Map.Entry<String, Object> entry : claimsRaw.entrySet()) {
                claims.put(entry.getKey(), String.valueOf(entry.getValue()));
            }

            return new TokenAttributes(userId, roles, claims, expiresAt);
        } catch (InvalidTokenException e) {
            throw e; // nicht doppelt wrappen
        } catch (Exception e) {
            throw new InvalidTokenException("Failed to decode accessToken", e);
        }
    }


    @Override
    public TokenResult renew(String token) throws InvalidTokenException {
        TokenAttributes attributes = decodeToken(token);
        TokenRequest request = new TokenRequest(attributes.userId(), attributes.roles(), attributes.claims(), tokenAliveTimeSeconds, renewTokenAliveTimeSeconds);
        return createTokens(request);
    }

    private String createToken(TokenRequest request, Instant expiresAt) {
        String header = Base64.getUrlEncoder().withoutPadding().encodeToString(HEADER);
        Map<String, Object> payload = new LinkedHashMap<>(request.claims());
        payload.put("sub", request.userId());
        payload.put("roles", request.roles());
        payload.put("exp", expiresAt.toEpochMilli());
        String payloadJson = toJson(payload);
        String payloadEncoded = Base64.getUrlEncoder().withoutPadding().encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
        String tokenData = header + "." + payloadEncoded;
        String signature = sign(tokenData);
        return tokenData + "." + signature;
    }

    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] sig = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(sig);
        } catch (Exception e) {
            throw new RuntimeException("Failed to sign JWT", e);
        }
    }

    private String toJson(Map<String, Object> map) {
        return gson.toJson(map);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJson(String json) {
        return gson.fromJson(json, Map.class);
    }
}

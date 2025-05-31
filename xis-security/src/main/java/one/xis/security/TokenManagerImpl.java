package one.xis.security;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@RequiredArgsConstructor
public class TokenManagerImpl implements TokenManager {

    private final String secret;
    private final long tokenAliveTimeSeconds;
    private final long renewTokenAliveTimeSeconds;
    private final Gson gson = new Gson();

    private static final byte[] HEADER = """
            {"alg":"HS256","typ":"JWT"}""".getBytes();

    public TokenManagerImpl(int tokenAliveTimeSeconds, int renewTokenAliveTimeSeconds) {
        this(generateSecret(), tokenAliveTimeSeconds, renewTokenAliveTimeSeconds);
    }

    @Override
    public TokenResult createTokens(TokenRequest request) {
        String token = createToken(request, tokenAliveTimeSeconds);
        String renewToken = createToken(request, renewTokenAliveTimeSeconds);
        return new TokenResult(
                token,
                Instant.now().plusSeconds(tokenAliveTimeSeconds),
                renewToken,
                Instant.now().plusSeconds(renewTokenAliveTimeSeconds)
        );
    }

    @Override

    public TokenAttributes decodeToken(String token) throws InvalidTokenException {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) throw new InvalidTokenException("Invalid token format");

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
            throw new InvalidTokenException("Failed to decode token", e);
        }
    }


    @Override
    public TokenResult renew(String token) throws InvalidTokenException {
        TokenAttributes attributes = decodeToken(token);
        TokenRequest request = new TokenRequest(attributes.userId(), attributes.roles(), attributes.claims(), tokenAliveTimeSeconds, renewTokenAliveTimeSeconds);
        return createTokens(request);
    }

    private String createToken(TokenRequest request, long validitySeconds) {
        String header = Base64.getUrlEncoder().withoutPadding().encodeToString(HEADER);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", request.userId());
        payload.put("roles", request.roles());
        payload.put("exp", LocalDateTime.now().plusSeconds(validitySeconds).toEpochSecond(ZoneOffset.UTC));
        request.claims().forEach((key, value) -> {
            if (!key.equals("roles") && !key.equals("sub") && !key.equals("exp")) {
                payload.put(key, value);
            }
        });
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

    private static String generateSecret() {
        byte[] keyBytes = new byte[32];
        new SecureRandom().nextBytes(keyBytes);
        return Base64.getEncoder().withoutPadding().encodeToString(keyBytes);
    }

    // Placeholder: replace with real JSON logic
    private String toJson(Map<String, Object> map) {
        return gson.toJson(map);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJson(String json) {
        return gson.fromJson(json, Map.class);
    }
}

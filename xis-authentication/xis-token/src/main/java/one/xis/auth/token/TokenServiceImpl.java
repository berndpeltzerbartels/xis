package one.xis.auth.token;


import com.google.gson.Gson;
import one.xis.auth.InvalidTokenException;
import one.xis.context.XISComponent;
import one.xis.security.SecurityUtil;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@XISComponent
class TokenServiceImpl implements TokenService {

    private static final byte[] HEADER = """
            {"alg":"HS256","typ":"JWT"}""".getBytes();

    private final String signingKey = SecurityUtil.createRandomKey(32); // TODO: make configurable
    private final Gson gson = new Gson();

    @Override
    public ApiTokens newTokens(TokenCreationAttributes tokenCreationAttributes, TokenCreationAttributes renewTokenCreationAttributes) {
        String accessToken = createToken(tokenCreationAttributes);
        String renewToken = createToken(renewTokenCreationAttributes);
        return new ApiTokens(accessToken, tokenCreationAttributes.expiresIn(), renewToken, renewTokenCreationAttributes.expiresIn());
    }

    @Override
    public TokenAttributes decodeToken(String token) throws InvalidTokenException {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) throw new InvalidTokenException("Invalid accessToken format");

            String headerPayload = parts[0] + "." + parts[1];
            String signature = parts[2];

            String expectedSig = signAndEncodeBase64(headerPayload);
            if (!Objects.equals(expectedSig, signature)) {
                throw new InvalidTokenException("Invalid signature");
            }

            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            Map<String, Object> claimsRaw = parseJson(payloadJson);
            String userId = (String) claimsRaw.get("sub");
            List<String> roles = (List<String>) claimsRaw.getOrDefault("roles", List.of());

            long exp = ((Number) claimsRaw.get("exp")).longValue();
            Instant expiresAt = Instant.ofEpochSecond(exp);
            if (expiresAt.isBefore(Instant.now())) {
                throw new InvalidTokenException("Token has expired");
            }
            Map<String, Object> claims = new HashMap<>();
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
    public ApiTokens renewTokens(String token, Duration tokenExpiresIn, Duration renewTokenExpiresIn) throws InvalidTokenException {
        TokenAttributes attributes = decodeToken(token);
        TokenCreationAttributes tokenCreationAttributes = new TokenCreationAttributes(
                attributes.userId(),
                attributes.roles(),
                attributes.claims(),
                tokenExpiresIn
        );
        TokenCreationAttributes renewTokenCreationAttributes = new TokenCreationAttributes(
                attributes.userId(),
                attributes.roles(),
                attributes.claims(),
                renewTokenExpiresIn
        );
        String accessToken = createToken(tokenCreationAttributes);
        String renewToken = createToken(renewTokenCreationAttributes);
        return new ApiTokens(accessToken, tokenExpiresIn, renewToken, renewTokenExpiresIn);
    }

    private String createToken(TokenCreationAttributes attributes) {
        String headerBase64 = Base64.getUrlEncoder().withoutPadding().encodeToString(HEADER);
        Map<String, Object> payload = new LinkedHashMap<>(attributes.claims());
        payload.put("sub", attributes.userId());
        payload.put("roles", attributes.roles());
        payload.put("exp", Instant.now().plus(attributes.expiresIn()).getEpochSecond());
        String payloadJson = toJson(payload);
        String payloadBase64 = Base64.getUrlEncoder().withoutPadding().encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
        String tokenData = headerBase64 + "." + payloadBase64;
        String signatureBase64 = signAndEncodeBase64(tokenData);
        return headerBase64 + "." + payloadBase64 + "." + signatureBase64;
    }

    private String signAndEncodeBase64(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(signingKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
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


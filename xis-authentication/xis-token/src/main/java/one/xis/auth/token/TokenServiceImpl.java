package one.xis.auth.token;


import com.google.gson.Gson;
import one.xis.auth.InvalidTokenException;
import one.xis.auth.JsonWebKey;
import one.xis.auth.UserInfo;
import one.xis.context.XISComponent;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@XISComponent
class TokenServiceImpl implements TokenService {

    // Header für RS256
    private static final byte[] HEADER = """
            {"alg":"RS256","typ":"JWT"}""".getBytes();

    private final KeyPair keyPair;
    private final Gson gson;

    public TokenServiceImpl(Gson gson) {
        this.gson = gson;
        // In einer echten Anwendung sollten die Schlüssel aus einem sicheren Speicher
        // (z.B. Vault, JKS) geladen und nicht bei jedem Start neu erstellt werden.
        this.keyPair = generateRsaKeyPair();
    }

    @Override
    public ApiTokens newTokens(UserInfo userInfo) {
        return newTokens(userInfo.getUserId(),
                userInfo.getRoles(),
                userInfo.getClaims());
    }

    @Override
    public ApiTokens newTokens(String userId, Collection<String> roles, Map<String, Object> claims) {
        return newTokens(new TokenCreationAttributes(userId, roles, claims, Duration.ofHours(15)),
                new TokenCreationAttributes(userId, roles, claims, Duration.ofDays(5)));
    }

    @Override
    public ApiTokens newTokens(TokenCreationAttributes tokenCreationAttributes, TokenCreationAttributes renewTokenCreationAttributes) {
        String accessToken = createToken(tokenCreationAttributes);
        String renewToken = createToken(renewTokenCreationAttributes);
        return new ApiTokens(accessToken, tokenCreationAttributes.expiresIn(), renewToken, renewTokenCreationAttributes.expiresIn());
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

    /**
     * Erstellt einen öffentlichen JSON Web Key (JWK) aus dem Public Key des Schlüsselpaars.
     * Dieser JWK kann sicher über einen öffentlichen Endpunkt (z.B. /.well-known/jwks.json)
     * bereitgestellt werden, damit andere Services die Token verifizieren können.
     *
     * @return Eine Map, die den öffentlichen JWK darstellt.
     */
    @Override
    public JsonWebKey getPublicJsonWebKey() {
        RSAPublicKey publicKey = (RSAPublicKey) this.keyPair.getPublic();

        JsonWebKey jwk = new JsonWebKey();
        jwk.setKeyType("RSA");
        jwk.setAlgorithm("RS256");
        jwk.setPublicKeyUse("sig"); // "sig" für Signatur-Verwendung
        jwk.setKeyId("xis-signing-key-rsa-1"); // Eindeutige Key-ID

        // Modulus und Exponent müssen Base64URL-kodiert sein.
        String n = Base64.getUrlEncoder().withoutPadding().encodeToString(publicKey.getModulus().toByteArray());
        String e = Base64.getUrlEncoder().withoutPadding().encodeToString(publicKey.getPublicExponent().toByteArray());

        jwk.setRsaModulus(n);
        jwk.setRsaExponent(e);

        return jwk;
    }

    @Override
    public TokenAttributes decodeToken(String token) throws InvalidTokenException {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) throw new InvalidTokenException("Invalid accessToken format");

            String headerPayload = parts[0] + "." + parts[1];
            byte[] signatureBytes = Base64.getUrlDecoder().decode(parts[2]);

            // Verifiziere die Signatur mit dem öffentlichen Schlüssel
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(keyPair.getPublic());
            signature.update(headerPayload.getBytes(StandardCharsets.UTF_8));

            if (!signature.verify(signatureBytes)) {
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
            throw e;
        } catch (Exception e) {
            throw new InvalidTokenException("Failed to decode accessToken", e);
        }
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
            // Signiere mit dem privaten Schlüssel
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(keyPair.getPrivate());
            signature.update(data.getBytes(StandardCharsets.UTF_8));
            byte[] sig = signature.sign();
            return Base64.getUrlEncoder().withoutPadding().encodeToString(sig);
        } catch (Exception e) {
            throw new RuntimeException("Failed to sign JWT", e);
        }
    }

    private KeyPair generateRsaKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            return keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Could not generate RSA key pair", e);
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
package one.xis.auth;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.security.SecurityUtil;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.RSAPublicKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

@XISComponent
@RequiredArgsConstructor
class TokenServiceImpl implements TokenService {

    private static final String HEADER_TEMPLATE = """
            {"alg":"RS256","typ":"JWT", "kid": "${KEY_ID}"}""";

    private final Gson gson;

    /**
     * @param userInfo
     * @return
     */
    @Override
    public ApiTokens newTokens(UserInfo userInfo, String issuer, String keyId, KeyPair keyPair) {
        Duration expiresIn = Duration.ofMinutes(5);
        Duration renewExpiresIn = Duration.ofMinutes(30);

        AccessTokenClaims accessTokenClaims = new AccessTokenClaims();
        accessTokenClaims.setUsername(userInfo.getUserId());
        accessTokenClaims.setJwtId(SecurityUtil.createRandomKey(12));
        accessTokenClaims.setIssuer(issuer);
        accessTokenClaims.setResourceAccess(new AccessTokenClaims.ResourceAccess(new AccessTokenClaims.ResourceAccess.Account(userInfo.getRoles())));
        accessTokenClaims.setRealmAccess(new AccessTokenClaims.RealmAccess(userInfo.getRoles()));
        accessTokenClaims.setExpiresAtSeconds(Instant.now().plus(expiresIn).getEpochSecond());
        accessTokenClaims.setIssuedAtSeconds(Instant.now().getEpochSecond());
        accessTokenClaims.setNotBeforeSeconds(Instant.now().getEpochSecond());
        accessTokenClaims.setClientId("xis-api");

        RenewTokenClaims renewTokenClaims = new RenewTokenClaims();
        renewTokenClaims.setUserId(userInfo.getUserId());
        renewTokenClaims.setIssuer(issuer);
        renewTokenClaims.setClientId("xis-api");
        renewTokenClaims.setExpiresAtSeconds(Instant.now().plus(renewExpiresIn).getEpochSecond());
        renewTokenClaims.setIssuedAtSeconds(Instant.now().getEpochSecond());
        renewTokenClaims.setNotBeforeSeconds(Instant.now().getEpochSecond());

        String accessToken = createToken(accessTokenClaims, keyId, keyPair);
        String renewToken = createToken(renewTokenClaims, keyId, keyPair);
        return new ApiTokens(accessToken, expiresIn, renewToken, renewExpiresIn);
    }


    @Override
    public String createToken(TokenClaims claims, String keyId, KeyPair keyPair) {
        return createToken(gson.toJson(claims), keyId, keyPair);
    }

    @Override
    public AccessTokenClaims decodeAccessToken(String token, JsonWebKey jwk) throws InvalidTokenException, TokenExpiredException {
        return decodeToken(token, AccessTokenClaims.class, jwk);

    }

    @Override
    public IDTokenClaims decodeIdToken(String token, JsonWebKey jwk) throws InvalidTokenException {
        return decodeToken(token, IDTokenClaims.class, jwk);
    }

    @Override
    public RenewTokenClaims decodeRenewToken(String token, JsonWebKey jwk) throws InvalidTokenException {
        return decodeToken(token, RenewTokenClaims.class, jwk);
    }

    @Override
    public String extractKeyId(String token) throws InvalidTokenException {
        JsonObject header = extractHeader(token);
        if (header.has("kid")) {
            return header.get("kid").getAsString();
        } else {
            throw new InvalidTokenException("Token header does not contain 'kid'");
        }
    }

    @Override
    public String extractIssuer(String token) throws InvalidTokenException {
        return gson.fromJson(extractPayload(token), JsonObject.class).get("iss").getAsString();
    }

    @Override
    public String extractUserId(String token) throws InvalidTokenException {
        return gson.fromJson(extractPayload(token), JsonObject.class).get("sub").getAsString();
    }

    // Extrahiert den Header als JsonObject
    private JsonObject extractHeader(String token) throws InvalidTokenException {
        String[] parts = token.split("\\.");
        if (parts.length != 3) throw new InvalidTokenException("Invalid token format");
        String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
        return gson.fromJson(headerJson, JsonObject.class);
    }

    private String extractPayload(String token) throws InvalidTokenException {
        String[] parts = token.split("\\.");
        if (parts.length != 3) throw new InvalidTokenException("Invalid token format");
        return new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
    }

    // Extrahiert die Claims
    private <C extends TokenClaims> C extractClaims(String token, Class<C> claimsClass) throws InvalidTokenException {
        String[] parts = token.split("\\.");
        if (parts.length != 3) throw new InvalidTokenException("Invalid token format");
        String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        return gson.fromJson(payloadJson, claimsClass);
    }

    // Erzeugt den PublicKey aus JWK
    private PublicKey createPublicKey(JsonWebKey jwk) throws Exception {
        byte[] nBytes = Base64.getUrlDecoder().decode(jwk.getRsaModulus());
        byte[] eBytes = Base64.getUrlDecoder().decode(jwk.getRsaExponent());
        BigInteger modulus = new BigInteger(1, nBytes);
        BigInteger exponent = new BigInteger(1, eBytes);
        RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(spec);
    }

    private <C extends TokenClaims> C decodeToken(String token, Class<C> claimsClass, JsonWebKey jwk) throws InvalidTokenException {
        try {
            JsonObject header = extractHeader(token); // TODO validate header
            C claims = extractClaims(token, claimsClass);
            validateClaims(claims);
            PublicKey publicKey = createPublicKey(jwk);

            String[] parts = token.split("\\.");
            String headerPayload = parts[0] + "." + parts[1];
            byte[] signatureBytes = Base64.getUrlDecoder().decode(parts[2]);

            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(publicKey);
            signature.update(headerPayload.getBytes(StandardCharsets.UTF_8));
            if (!signature.verify(signatureBytes)) {
                throw new InvalidTokenException("Invalid signature");
            }
            return claims;
        } catch (InvalidTokenException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidTokenException("Failed to decode accessToken", e);
        }
    }

    private <C extends TokenClaims> void validateClaims(C claims) throws InvalidTokenException {

        // Pflichtfelder prüfen
        if (claims.getIssuer() == null || claims.getIssuer().isBlank()) {
            throw new InvalidTokenException("Token issuer is missing or empty");
        }
        if (claims.getExpiresAtSeconds() == null) {
            throw new InvalidTokenException("Token expiration (exp) is missing");
        } else if (claims.getExpiresAtSeconds() <= 0) {
            throw new InvalidTokenException("Token expiration (exp) is invalid");
        }
        if (claims.getIssuedAtSeconds() == null) {
            throw new InvalidTokenException("Token issued-at (iat) is missing");
        }
        if (claims.getClientId() == null || claims.getClientId().isBlank()) {
            throw new InvalidTokenException("Token client_id is missing or empty");
        }

        // Ablauf prüfen
        Instant expiresAt = Instant.ofEpochSecond(claims.getExpiresAtSeconds());
        if (expiresAt.isBefore(Instant.now())) {
            throw new TokenExpiredException();
        }

        // Optionale Felder validieren, falls vorhanden
        if (claims.getNotBeforeSeconds() != null) {
            Instant notBefore = Instant.ofEpochSecond(claims.getNotBeforeSeconds());
            if (notBefore.isAfter(Instant.now())) {
                throw new InvalidTokenException("Token not valid yet (nbf in the future)");
            }
        }
        if (claims.getIssuedAtSeconds() > Instant.now().getEpochSecond()) {
            throw new InvalidTokenException("Token issued-at (iat) is in the future");
        }
    }

    private String createToken(String payloadJson, String keyId, KeyPair keyPair) {
        String headerBase64 = Base64.getUrlEncoder().withoutPadding().encodeToString(createHeader(keyId));
        String payloadBase64 = Base64.getUrlEncoder().withoutPadding().encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
        String tokenData = headerBase64 + "." + payloadBase64;
        String signatureBase64 = signAndEncodeBase64(tokenData, keyPair);
        return headerBase64 + "." + payloadBase64 + "." + signatureBase64;
    }

    private String signAndEncodeBase64(String data, KeyPair keyPair) {
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

    private byte[] createHeader(String keyId) {
        return HEADER_TEMPLATE.replace("${KEY_ID}", keyId).getBytes(StandardCharsets.UTF_8);
    }
}
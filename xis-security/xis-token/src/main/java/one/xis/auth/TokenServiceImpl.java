package one.xis.auth;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import one.xis.context.XISComponent;
import one.xis.security.SecurityUtil;
import one.xis.server.LocalUrlHolder;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Collection;

@XISComponent
class TokenServiceImpl implements TokenService {

    private static final String KEY_ID = "xis-signing-key-rsa-1"; // Eindeutige Key-ID für den JWK
    // Header für RS256
    private static final byte[] HEADER = """
            {"alg":"RS256","typ":"JWT", "kid": "${KEY_ID}"}""".replace("${KEY_ID}", KEY_ID).getBytes(StandardCharsets.UTF_8);

    private final KeyPair keyPair;
    private final Gson gson;
    private final LocalUrlHolder localUrlHolder;
    private final Collection<JsonWebKeyProvider> jsonWebKeyProviders;

    private JsonWebKey jsonWebKey;

    public TokenServiceImpl(Gson gson, LocalUrlHolder localUrlHolder, Collection<JsonWebKeyProvider> jsonWebKeyProviders) {
        this.gson = gson;
        this.localUrlHolder = localUrlHolder;
        this.jsonWebKeyProviders = jsonWebKeyProviders;
        // In einer echten Anwendung sollten die Schlüssel aus einem sicheren Speicher
        // (z.B. Vault, JKS) geladen und nicht bei jedem Start neu erstellt werden.
        this.keyPair = generateRsaKeyPair();
    }


    /**
     * @param userInfo
     * @return
     */
    @Override
    public ApiTokens newTokens(UserInfo userInfo) {
        Duration expiresIn = Duration.ofHours(5);
        Duration renewExpiresIn = Duration.ofDays(1);

        AccessTokenClaims accessTokenClaims = new AccessTokenClaims();
        accessTokenClaims.setUsername(userInfo.getUserId());
        accessTokenClaims.setJwtId(SecurityUtil.createRandomKey(12));
        accessTokenClaims.setIssuer(localUrlHolder.getUrl());
        accessTokenClaims.setResourceAccess(new AccessTokenClaims.ResourceAccess(new AccessTokenClaims.ResourceAccess.Account(userInfo.getRoles())));
        accessTokenClaims.setRealmAccess(new AccessTokenClaims.RealmAccess(userInfo.getRoles()));
        accessTokenClaims.setExpiresAtSeconds(Instant.now().plus(expiresIn).getEpochSecond());
        accessTokenClaims.setIssuedAtSeconds(Instant.now().getEpochSecond());
        accessTokenClaims.setNotBeforeSeconds(Instant.now().getEpochSecond());
        accessTokenClaims.setClientId("xis-api");

        RenewTokenClaims renewTokenClaims = new RenewTokenClaims();
        renewTokenClaims.setUserId(userInfo.getUserId());
        renewTokenClaims.setIssuer(localUrlHolder.getUrl());
        renewTokenClaims.setClientId("xis-api");
        renewTokenClaims.setExpiresAtSeconds(Instant.now().plus(renewExpiresIn).getEpochSecond());
        renewTokenClaims.setIssuedAtSeconds(Instant.now().getEpochSecond());
        renewTokenClaims.setNotBeforeSeconds(Instant.now().getEpochSecond());

        String accessToken = createToken(accessTokenClaims);
        String renewToken = createToken(renewTokenClaims);
        return new ApiTokens(accessToken, expiresIn, renewToken, renewExpiresIn);
    }


    @Override
    public ApiTokens renewTokens(String token, Duration tokenExpiresIn, Duration renewTokenExpiresIn) throws InvalidTokenException {
        RenewTokenClaims oldRenewToken = decodeRenewToken(token);

        // Neue AccessTokenClaims erstellen
        AccessTokenClaims accessTokenClaims = new AccessTokenClaims();
        accessTokenClaims.setUsername(oldRenewToken.getUserId());
        accessTokenClaims.setJwtId(SecurityUtil.createRandomKey(12));
        accessTokenClaims.setIssuer(localUrlHolder.getUrl());
        // Rollen können hier ggf. aus einer UserInfo-Quelle geladen werden, falls nötig
        accessTokenClaims.setExpiresAtSeconds(Instant.now().plus(tokenExpiresIn).getEpochSecond());
        accessTokenClaims.setIssuedAtSeconds(Instant.now().getEpochSecond());
        accessTokenClaims.setNotBeforeSeconds(Instant.now().getEpochSecond());
        accessTokenClaims.setClientId("xis-api");

        // Neues RenewTokenClaims erstellen
        RenewTokenClaims renewTokenClaims = new RenewTokenClaims();
        renewTokenClaims.setUserId(oldRenewToken.getUserId());
        renewTokenClaims.setIssuer(localUrlHolder.getUrl());
        renewTokenClaims.setClientId("xis-api");
        renewTokenClaims.setExpiresAtSeconds(Instant.now().plus(renewTokenExpiresIn).getEpochSecond());
        renewTokenClaims.setIssuedAtSeconds(Instant.now().getEpochSecond());
        renewTokenClaims.setNotBeforeSeconds(Instant.now().getEpochSecond());

        String accessToken = createToken(accessTokenClaims);
        String renewToken = createToken(renewTokenClaims);

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
        if (jsonWebKey == null) {
            jsonWebKey = createJsonWebKey();
        }
        return jsonWebKey;
    }

    private JsonWebKey createJsonWebKey() {
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
    public String createToken(TokenClaims claims) {
        return createToken(gson.toJson(claims));
    }

    @Override
    public AccessTokenClaims decodeAccessToken(String token) throws InvalidTokenException {
        return decodeToken(token, AccessTokenClaims.class);
    }

    @Override
    public IDTokenClaims decodeIdToken(String token) throws InvalidTokenException {
        return decodeToken(token, IDTokenClaims.class);
    }

    @Override
    public RenewTokenClaims decodeRenewToken(String token) throws InvalidTokenException {
        return decodeToken(token, RenewTokenClaims.class);
    }

    // Extrahiert den Header als JsonObject
    private JsonObject extractHeader(String token) throws InvalidTokenException {
        String[] parts = token.split("\\.");
        if (parts.length != 3) throw new InvalidTokenException("Invalid token format");
        String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
        return gson.fromJson(headerJson, JsonObject.class);
    }

    // Extrahiert die Claims
    private <C extends TokenClaims> C extractClaims(String token, Class<C> claimsClass) throws InvalidTokenException {
        String[] parts = token.split("\\.");
        if (parts.length != 3) throw new InvalidTokenException("Invalid token format");
        String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        return gson.fromJson(payloadJson, claimsClass);
    }

    // Findet das passende JWK
    private JsonWebKey findJwk(String issuer, String kid) throws InvalidTokenException {
        return jsonWebKeyProviders.stream()
                .map(provider -> provider.getKeysForIssuer(issuer).get(kid))
                .filter(collection -> collection != null && !collection.isEmpty())
                .flatMap(Collection::stream)
                .findFirst()
                .orElseThrow(() -> new InvalidTokenException("No matching JWK for kid: " + kid + " and issuer: " + issuer));
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
    
    private <C extends TokenClaims> C decodeToken(String token, Class<C> claimsClass) throws InvalidTokenException {
        try {
            JsonObject header = extractHeader(token);
            C claims = extractClaims(token, claimsClass);
            validateClaims(claims);
            String issuer = claims.getIssuer();
            String kid = header.get("kid").getAsString();

            JsonWebKey jwk = findJwk(issuer, kid);
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
            throw new InvalidTokenException("Token has expired");
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

    private String createToken(String payloadJson) {
        String headerBase64 = Base64.getUrlEncoder().withoutPadding().encodeToString(HEADER);
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


}
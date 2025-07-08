package one.xis.idpclient;


import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import one.xis.auth.JsonWebKey;
import one.xis.auth.UserInfo;
import one.xis.auth.UserInfoImpl;
import one.xis.auth.token.ApiTokens;
import one.xis.auth.token.StateParameter;
import one.xis.context.XISDefaultComponent;
import one.xis.context.XISInit;
import one.xis.context.XISInject;
import one.xis.ipdclient.*;
import one.xis.security.AuthenticationException;
import one.xis.security.UserInfoService;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.text.ParseException;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;

@XISDefaultComponent
@RequiredArgsConstructor
class IDPClientServiceImpl implements IDPClientService {

    @XISInject
    private Collection<IDPClientConfig> idpClientConfigs;

    @XISInject(optional = true)
    private UserInfoService<UserInfo> userInfoService;

    @XISInject
    private IDPClientFactory idpClientFactory;

    private final Map<String, IDPClient> idpClients = new HashMap<>();
    private final Map<String, IDPPublicKeys> publicKeys = new HashMap<>();

    @XISInit
    void init() {
        idpClientConfigs.stream().map(idpClientFactory::createConfiguredIDPClient)
                .forEach(idpClient -> idpClients.put(idpClient.getIdpId(), idpClient));
    }

    @Override
    public String loginFormUrl(String idpId, String redirectUri) {
        var idpClientConfig = getIDPClientConfig(idpId);
        var idpClient = getIDPClient(idpId);
        var state = StateParameter.create(redirectUri);
        return idpClient.getAuthorizationEndpoint() + "?" +
                "response_type=code" +
                "&client_id=" + encode(idpClientConfig.getClientId(), UTF_8) +
                "&redirect_uri=" + encode(redirectUri, UTF_8) +
                "&scope=openid" +
                "&state=" + encode(state, UTF_8);
    }

    @Override
    public ApiTokens fetchNewTokens(String idpId, String code, String state) {
        StateParameter.decodeAndVerify(state);
        var idpClient = getIDPClient(idpId);
        return idpClient.fetchNewTokens(code);
    }

    @Override
    public ApiTokens fetchRenewedTokens(String idpId, String refreshToken) {
        var idpClient = getIDPClient(idpId);
        return idpClient.fetchRenewedTokens(refreshToken);
    }

    /**
     * Fetches user information from the IDP's user-info endpoint using the provided access token.
     * After fetching, it triggers the optional UserProvisioningService to create or update
     * the user in the local application database.
     *
     * @param idpId       The identifier of the Identity Provider.
     * @param accessToken The access token to authorize the request to the user-info endpoint.
     * @return The user information retrieved from the IDP, potentially enriched by the UserProvisioningService.
     */
    @Override
    public UserInfoImpl fetchUserInfoFromIdp(String idpId, String accessToken) {
        var idpClient = getIDPClient(idpId);
        var userInfo = idpClient.fetchUserInfo(accessToken);
        return provisionUserIfServicePresent(userInfo, idpId);
    }


    @Override
    public UserInfoImpl verifyAndDecodeToken(String idpId, String accessToken) {
        try {
            var signedJWT = SignedJWT.parse(accessToken);
            var kid = signedJWT.getHeader().getKeyID();
            if (kid == null) {
                throw new AuthenticationException("JWT header does not contain a 'kid' (Key ID).");
            }

            var idpPublicKeys = getPublicKeys(idpId);
            var publicKey = idpPublicKeys.getKey(kid)
                    .orElseThrow(() -> new AuthenticationException("Public key with kid '" + kid + "' not found for IDP '" + idpId + "'."));

            JWSVerifier verifier = new RSASSAVerifier((RSAPublicKey) publicKey);
            if (!signedJWT.verify(verifier)) {
                throw new AuthenticationException("JWT signature verification failed.");
            }

            var claims = signedJWT.getJWTClaimsSet();
            var idpClientConfig = getIDPClientConfig(idpId);

            // Verify token expiration
            var expirationTime = claims.getExpirationTime();
            if (expirationTime == null || expirationTime.before(new java.util.Date())) {
                throw new AuthenticationException("Token is expired.");
            }

            // Verify token issuer
            var expectedIssuer = getIDPClient(idpId).getIssuer();
            if (expectedIssuer != null && !expectedIssuer.equals(claims.getIssuer())) {
                throw new AuthenticationException("Invalid token issuer. Expected '" + expectedIssuer + "', but got '" + claims.getIssuer() + "'.");
            }

            var userInfo = idpClientConfig.extractUserInfo(claims);
            return provisionUserIfServicePresent(userInfo, idpId);

        } catch (ParseException e) {
            throw new AuthenticationException("Failed to parse JWT.", e);
        } catch (JOSEException e) {
            throw new AuthenticationException("Failed to verify JWT signature.", e);
        }
    }

    private UserInfoImpl provisionUserIfServicePresent(UserInfoImpl userInfo, String idpId) {
        if (userInfoService != null) {
            userInfoService.saveUserInfo(userInfo, idpId);
        }
        return userInfo;
    }

    private IDPClient getIDPClient(String idpId) {
        var idpClient = idpClients.get(idpId);
        if (idpClient == null) {
            throw new IllegalArgumentException("IDP client with ID '" + idpId + "' not found");
        }
        return idpClient;
    }

    private IDPPublicKeys getPublicKeys(String idpId) {
        return publicKeys.computeIfAbsent(idpId, id -> {
            var idpClient = getIDPClient(id);
            var publicKeyResponse = idpClient.fetchPublicKeys();
            var keysByKid = publicKeyResponse.getKeys().stream()
                    .collect(Collectors.toMap(
                            JsonWebKey::getKeyId,
                            this::toRsaPublicKey,
                            (existing, replacement) -> replacement
                    ));
            return new IDPPublicKeys(keysByKid);
        });
    }

    private IDPClientConfig getIDPClientConfig(String idpId) {
        return idpClientConfigs.stream()
                .filter(config -> config.getIdpId().equals(idpId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("IDP client config with ID '" + idpId + "' not found"));
    }

    private RSAPublicKey toRsaPublicKey(JsonWebKey jwk) {
        try {
            byte[] modulusBytes = Base64.getUrlDecoder().decode(jwk.getRsaModulus());
            byte[] exponentBytes = Base64.getUrlDecoder().decode(jwk.getRsaExponent());

            var modulus = new BigInteger(1, modulusBytes);
            var exponent = new BigInteger(1, exponentBytes);

            var spec = new RSAPublicKeySpec(modulus, exponent);
            var factory = KeyFactory.getInstance("RSA");
            return (RSAPublicKey) factory.generatePublic(spec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new AuthenticationException("Failed to construct RSA public key from JWK for kid: " + jwk.getKeyId(), e);
        }
    }
}
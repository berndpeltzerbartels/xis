package one.xis.idp;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.security.*;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static java.time.temporal.ChronoUnit.MINUTES;

@XISComponent
@RequiredArgsConstructor
class LocalIDPServiceImpl implements LocalIDPService {

    private final LocalIDPUserService idpUserService;
    private final LocalIDPCodeStore idpCodeStore = new LocalIDPCodeStore();
    private final String secret = SecurityUtil.createRandomKey(32);
    private final Duration lifetime = Duration.of(15, MINUTES);
    private final Duration refreshLifetime = Duration.of(30, MINUTES);

    @Override
    public String login(LocalIDPLogin login) throws InvalidCredentialsException {
        if (!idpUserService.checkCredentials(login.getUsername(), login.getPassword())) {
            throw new InvalidCredentialsException();
        }
        String code = UUID.randomUUID().toString();
        idpCodeStore.store(code, login.getUsername());
        return code;
    }

    @Override
    public LocalIDPTokens issueToken(String code, String state) throws AuthenticationException {
        String userId = idpCodeStore.getUserIdForCode(code);
        if (userId == null) {
            throw new InvalidStateParameterException();
        }
        return generateTokenResponse(userId, state);
    }


    @Override
    public LocalIDPTokens refresh(String refreshToken) throws InvalidTokenException, AuthenticationException {
        String userId = verifyRefreshToken(refreshToken);
        return generateTokenResponse(userId, null);
    }


    @Override
    public UserInfo content(String accessToken) throws InvalidTokenException {
        try {
            var claims = Jwts.parser()
                    .setSigningKey(secret.getBytes(StandardCharsets.UTF_8))
                    .parseClaimsJws(accessToken)
                    .getBody();
            String userId = claims.getSubject();
            List<String> roles = (List<String>) claims.get("roles");
            Map<String, Object> additionalClaims = (Map<String, Object>) claims.get("claims");

            UserInfoImpl userInfo = new UserInfoImpl();
            userInfo.setUserId(userId);
            userInfo.setRoles(new HashSet<>(roles));
            userInfo.setClaims(additionalClaims);
            return userInfo;
        } catch (Exception e) {
            throw new InvalidTokenException(accessToken);
        }
    }


    public void checkRedirectUrl(String redirectUrl) throws InvalidRedirectUrlException {
        if (idpUserService.getAllowedRedirectUrls().stream().noneMatch(redirectUrl::startsWith)) {
            throw new InvalidRedirectUrlException(redirectUrl);
        }
    }

    private LocalIDPTokens generateTokenResponse(String userId, String state) throws AuthenticationException {
        long now = System.currentTimeMillis();
        Date expiry = Date.from(Instant.now().plus(lifetime));
        Date expiryRefresh = Date.from(Instant.now().plus(refreshLifetime));

        UserInfo userInfo = idpUserService.getUserInfo(userId);

        String jwt = Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(userId)
                .claim("roles", userInfo.getRoles())
                .claim("claims", userInfo.getClaims())
                .setIssuedAt(new Date(now))
                .setExpiration(expiry)
                .signWith(SignatureAlgorithm.HS256, secret.getBytes(StandardCharsets.UTF_8))
                .compact();

        String refreshToken = Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(userId)
                .claim("type", "refresh")
                .setIssuedAt(new Date(now))
                .setExpiration(expiryRefresh)
                .signWith(SignatureAlgorithm.HS256, secret.getBytes(StandardCharsets.UTF_8))
                .compact();

        LocalIDPTokens response = new LocalIDPTokens();
        response.setAccessToken(jwt);
        response.setRefreshToken(refreshToken);
        response.setExpiresIn(lifetime);
        response.setRefreshTokenExpiresIn(refreshLifetime);
        response.setState(state);
        return response;
    }

    private String verifyRefreshToken(String refreshToken) throws InvalidTokenException {
        try {
            var claims = Jwts.parser()
                    .setSigningKey(secret.getBytes(StandardCharsets.UTF_8))
                    .parseClaimsJws(refreshToken)
                    .getBody();
            if (!"refresh".equals(claims.get("type"))) {
                throw new InvalidTokenException(refreshToken);
            }
            return claims.getSubject();
        } catch (Exception e) {
            throw new InvalidTokenException(refreshToken);
        }
    }

    /*
    private static AuthenticationData getAuthenticationData(AuthenticationProviderTokens tokenResponse, AuthenticationProviderStateData authenticationProviderData) {
        var apiTokens = new ApiTokens();
        apiTokens.setAccessToken(tokenResponse.getAccessToken());
        apiTokens.setRenewTokenExpiresIn(tokenResponse.getRefreshExpiresIn());
        apiTokens.setRenewToken(tokenResponse.getRefreshToken());
        apiTokens.setRenewTokenExpiresIn(tokenResponse.getRefreshExpiresIn());
        var authenticationData = new AuthenticationData();
        authenticationData.setApiTokens(apiTokens);
        authenticationData.setUrl(authenticationProviderData.getStateParameterPayload().getRedirect());
        return authenticationData;
    }

    private ServerResponse authenticationErrorResponse(String uri) {
        var state = StateParameter.create(uri);
        var response = new ServerResponse();
        response.setStatus(303);
        response.setNextURL("/login.html?state=" + state);
        response.getValidatorMessages().getMessages().put("username", "Invalid username or password"); // TODO: i18n
        response.getValidatorMessages().getMessages().put("password", "Invalid username or password"); // TODO: i18n
        response.getValidatorMessages().getGlobalMessages().add("Invalid username or password"); // TODO: i18n
        return response;
    }

    private ServerResponse localAuthenticationErrorResponse(String uri) {
        var state = StateParameter.create(uri);
        var response = new ServerResponse();
        response.setStatus(303);
        response.setNextURL("/login.html?state=" + state);
        response.getValidatorMessages().getMessages().put("username", "Invalid username or password"); // TODO: i18n
        response.getValidatorMessages().getMessages().put("password", "Invalid username or password"); // TODO: i18n
        response.getValidatorMessages().getGlobalMessages().add("Invalid username or password"); // TODO: i18n
        return response;
    }

    @Override
    public AuthenticationData authenticationCallback(String provider, String queryString) {
        var service = Objects.requireNonNull(authenticationProviderServices.getAuthenticationProviderService(provider));
        var authenticationProviderData = service.verifyAndDecodeCodeAndStateQuery(queryString);
        var tokenResponse = service.requestTokens(authenticationProviderData.getCode(), authenticationProviderData.getState());
        return getAuthenticationData(tokenResponse, authenticationProviderData);
    }


    @Override
    public BearerTokens localTokenProviderGetTokens(String code, String state) throws AuthenticationException {
        var authenticationProviderService = authenticationProviderService();
        var tokenResponse = authenticationProviderService.issueToken(code, state);
        var bearerTokens = new BearerTokens();
        bearerTokens.setAccessToken(tokenResponse.getAccessToken());
        bearerTokens.setAccessTokenExpiresIn(tokenResponse.getExpiresIn());
        bearerTokens.setRenewToken(tokenResponse.getRefreshToken());
        bearerTokens.setRenewTokenExpiresIn(tokenResponse.getRefreshTokenExpiresIn());
        return bearerTokens;
    }
    */

}

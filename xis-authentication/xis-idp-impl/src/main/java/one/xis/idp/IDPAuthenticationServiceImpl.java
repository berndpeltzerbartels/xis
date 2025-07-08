package one.xis.idp;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import one.xis.auth.*;
import one.xis.auth.token.ApiTokens;
import one.xis.context.XISComponent;
import one.xis.security.SecurityUtil;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static java.time.temporal.ChronoUnit.MINUTES;

@XISComponent
@RequiredArgsConstructor
class IDPAuthenticationServiceImpl implements IDPAuthenticationService {

    private final IDPService idpService;
    private final IDPServerCodeStore idpCodeStore = new IDPServerCodeStore();
    private final String secret = SecurityUtil.createRandomKey(32);
    private final Duration lifetime = Duration.of(15, MINUTES);
    private final Duration refreshLifetime = Duration.of(30, MINUTES);

    @Override
    public String login(IDPServerLogin login) throws InvalidCredentialsException {
        if (!idpService.findUserInfo(login.getUsername())
                .map(IDPUserInfo::getPassword)
                .map(login.getPassword()::equals)
                .orElse(false)) {
            throw new InvalidCredentialsException();
        }
        String code = UUID.randomUUID().toString();
        idpCodeStore.store(code, login.getUsername());
        return code;
    }

    @Override
    public ApiTokens issueToken(String code) throws AuthenticationException {
        String userId = idpCodeStore.getUserIdForCode(code);
        if (userId == null) {
            throw new InvalidStateParameterException();
        }
        return generateTokenResponse(userId);
    }


    @Override
    public ApiTokens refresh(String refreshToken) throws InvalidTokenException, AuthenticationException {
        String userId = verifyRefreshToken(refreshToken);
        return generateTokenResponse(userId);
    }


    @Override
    public IDPUserInfo content(String accessToken) throws InvalidTokenException {
        try {
            var claims = Jwts.parser()
                    .setSigningKey(secret.getBytes(StandardCharsets.UTF_8))
                    .parseClaimsJws(accessToken)
                    .getBody();
            String userId = claims.getSubject();
            List<String> roles = (List<String>) claims.get("roles");
            Map<String, Object> additionalClaims = (Map<String, Object>) claims.get("claims");

            IDPUserInfoImpl userInfo = new IDPUserInfoImpl();
            userInfo.setUserId(userId);
            userInfo.setRoles(new HashSet<>(roles));
            userInfo.setClaims(additionalClaims);
            return userInfo;
        } catch (Exception e) {
            throw new InvalidTokenException(accessToken);
        }
    }


    public void checkRedirectUrl(String userId, String redirectUrl) throws InvalidRedirectUrlException {
        if (!idpService.findUserInfo(userId)
                .map(IDPUserInfo::getClientId)
                .map(idpService::findClientInfo)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(IDPClientInfo::getPermittedRedirectUrls)
                .map(urls -> urls.contains(redirectUrl))
                .orElse(false)) {
            throw new InvalidRedirectUrlException(redirectUrl);
        }
    }

    private ApiTokens generateTokenResponse(String userId) throws AuthenticationException {
        long now = System.currentTimeMillis();
        Date expiry = Date.from(Instant.now().plus(lifetime));
        Date expiryRefresh = Date.from(Instant.now().plus(refreshLifetime));

        UserInfo userInfo = idpService.findUserInfo(userId).orElseThrow(() -> new AuthenticationException("User not found: " + userId));

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

        ApiTokens tokens = new ApiTokens();
        tokens.setAccessToken(jwt);
        tokens.setRenewToken(refreshToken);
        tokens.setAccessTokenExpiresIn(lifetime);
        tokens.setRenewTokenExpiresIn(refreshLifetime);
        return tokens;
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
}

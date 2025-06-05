package one.xis.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.SECONDS;

@RequiredArgsConstructor
class LocalAuthenticationProviderServiceImpl implements LocalAuthenticationProviderService {

    private final LocalAuthenticationCodeStore codeStore = new LocalAuthenticationCodeStore();
    private final UserService userService;
    private final String secret = SecurityUtil.createRandomKey(32);
    private final Duration lifetime = Duration.of(15, MINUTES);

    @Override
    public String login(Login login) throws InvalidCredentialsException {
        if (!userService.checkCredentials(login.getUsername(), login.getPassword())) {
            throw new InvalidCredentialsException();
        }
        String code = UUID.randomUUID().toString();
        codeStore.store(code, login.getUsername());
        return code;
    }

    @Override
    public LocalAuthenticationTokenResponse issueToken(String code, String state) throws AuthenticationException {
        String userId = codeStore.getUserIdForCode(code);
        if (userId == null) {
            throw new InvalidStateParameterException();
        }

        return generateTokenResponse(userId, state);
    }

    @Override
    public LocalAuthenticationTokenResponse refresh(String refreshToken) throws InvalidTokenException, AuthenticationException {
        String userId = verifyRefreshToken(refreshToken);
        return generateTokenResponse(userId, null);
    }

    @Override
    public LocalUserInfo getUserInfo(String accessToken) throws InvalidTokenException {
        try {
            var claims = Jwts.parser()
                    .setSigningKey(secret.getBytes(StandardCharsets.UTF_8))
                    .parseClaimsJws(accessToken)
                    .getBody();
            String userId = claims.getSubject();
            List<String> roles = (List<String>) claims.get("roles");
            Map<String, Object> additionalClaims = (Map<String, Object>) claims.get("claims");

            LocalUserInfo userInfo = new LocalUserInfo();
            userInfo.setUserId(userId);
            userInfo.setRoles(new HashSet<>(roles));
            userInfo.setClaims(additionalClaims);
            return userInfo;
        } catch (Exception e) {
            throw new InvalidTokenException(accessToken);
        }
    }

    private LocalAuthenticationTokenResponse generateTokenResponse(String userId, String state) throws AuthenticationException {
        long now = System.currentTimeMillis();
        Date expiry = new Date(now + lifetime.get(SECONDS) * 1000L);

        LocalUserInfo userInfo = userService.getUserInfo(userId);

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
                .setExpiration(expiry)
                .signWith(SignatureAlgorithm.HS256, secret.getBytes(StandardCharsets.UTF_8))
                .compact();

        LocalAuthenticationTokenResponse response = new LocalAuthenticationTokenResponse();
        response.setAccessToken(jwt);
        response.setRefreshToken(refreshToken);
        response.setExpiresInSeconds(lifetime.getSeconds());
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
}

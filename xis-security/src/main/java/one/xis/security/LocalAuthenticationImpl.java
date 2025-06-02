package one.xis.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.context.XISInit;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.MILLIS;
import static java.time.temporal.ChronoUnit.MINUTES;


@XISComponent
@RequiredArgsConstructor
public class LocalAuthenticationImpl implements LocalAuthentication {

    private final List<UserService> userServices;
    private final String secret = SecurityUtil.createRandomKey(32);
    private final Duration lifetime = Duration.of(15, MINUTES);
    private UserService userService;

    @XISInit
    void initUniqueService() {
        if (userServices.size() == 1) {
            userService = userServices.get(0);
        } else {
            throw new IllegalStateException("Multiple UserService implementations found, please ensure only one is available.");
        }
    }

    @Override
    public LocalAuthenticationCodeResponse login(String userId, String password) throws InvalidCredentialsException {
        if (userService == null || !userService.checkCredentials(userId, password)) {
            throw new InvalidCredentialsException();
        }

        String code = UUID.randomUUID().toString();
        String state = UUID.randomUUID().toString();
        userService.storeLoginCode(code, userId);

        LocalAuthenticationCodeResponse response = new LocalAuthenticationCodeResponse();
        response.setCode(code);
        response.setState(state);
        response.setExpiresIn(lifetime.getSeconds());
        return response;
    }

    @Override
    public LocalAuthenticationTokenResponse issueToken(String code, String state) throws InvalidStateParameterException {
        String userId = userService.findUserIdForCode(code);
        if (userId == null) {
            throw new InvalidStateParameterException();
        }

        long now = System.currentTimeMillis();
        Date expiry = new Date(now + lifetime.get(MILLIS));

        String jwt = Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(new Date(now))
                .setExpiration(expiry)
                .signWith(SignatureAlgorithm.HS256, secret.getBytes(StandardCharsets.UTF_8))
                .compact();

        String refreshToken = UUID.randomUUID().toString(); // Optional: auch signiert, wenn nötig

        LocalAuthenticationTokenResponse response = new LocalAuthenticationTokenResponse();
        response.setAccessToken(jwt);
        response.setRefreshToken(refreshToken);
        response.setExpiresIn(lifetime.getSeconds());
        response.setState(state);
        return response;
    }

    @Override
    public LocalUserInfo getUserInfo(String accessToken) throws InvalidTokenException {
        try {
            String userId = Jwts.parser()
                    .setSigningKey(secret.getBytes(StandardCharsets.UTF_8))
                    .parseClaimsJws(accessToken)
                    .getBody()
                    .getSubject();
            return userService.getUserInfo(userId);
        } catch (Exception e) {
            throw new InvalidTokenException(accessToken);
        }
    }
}
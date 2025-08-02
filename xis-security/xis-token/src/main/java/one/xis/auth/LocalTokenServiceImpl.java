package one.xis.auth;

import lombok.RequiredArgsConstructor;
import one.xis.context.XISDefaultComponent;
import one.xis.server.LocalUrlHolder;

import java.security.KeyPair;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

@XISDefaultComponent
@RequiredArgsConstructor
public class LocalTokenServiceImpl implements LocalTokenService {

    private final TokenService tokenService;
    private final LocalKeyProvider localKeyProvider;
    private final LocalUrlHolder localUrlHolder;
    private final UserInfoService<UserInfo> userInfoService;

    private final AtomicInteger keyIdCounter = new AtomicInteger(-1);

    @Override
    public Collection<JsonWebKey> getJsonWebKeys() {
        return localKeyProvider.getJsonWebKeys();
    }

    @Override
    public ApiTokens newTokens(String userId) {
        UserInfo userInfo = userInfoService.getUserInfo(userId).orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        String keyId = nextKeyId();
        KeyPair keyPair = localKeyProvider.getKeyPair(keyId);
        return tokenService.newTokens(userInfo, localUrlHolder.getUrl(), keyId, keyPair);
    }

    @Override
    public ApiTokens renewTokens(String renewToken) throws InvalidTokenException {
        RenewTokenClaims oldRenewTokenClaims = decodeRenewToken(renewToken);
        return newTokens(oldRenewTokenClaims.getUserId());
    }

    @Override
    public RenewTokenClaims decodeRenewToken(String token) throws InvalidTokenException {
        String keyId = tokenService.extractKeyId(token);
        JsonWebKey jsonWebKey = localKeyProvider.getJsonWebKey(keyId);
        return tokenService.decodeRenewToken(token, jsonWebKey);
    }

    @Override
    public AccessTokenClaims decodeAccessToken(String token) throws InvalidTokenException {
        String keyId = tokenService.extractKeyId(token);
        JsonWebKey jsonWebKey = localKeyProvider.getJsonWebKey(keyId);
        return tokenService.decodeAccessToken(token, jsonWebKey);
    }

    @Override
    public String createToken(TokenClaims tokenClaims) {
        String keyId = nextKeyId();
        KeyPair keyPair = localKeyProvider.getKeyPair(keyId);
        return tokenService.createToken(tokenClaims, keyId, keyPair);
    }

    @Override
    public IDTokenClaims decodeIdToken(String idToken) {
        String keyId = tokenService.extractKeyId(idToken);
        JsonWebKey jsonWebKey = localKeyProvider.getJsonWebKey(keyId);
        return tokenService.decodeIdToken(idToken, jsonWebKey);
    }

    private String nextKeyId() {
        var keyIds = localKeyProvider.getKeyIds().stream().toList();
        int size = keyIds.size();
        if (size == 0) {
            throw new IllegalStateException("Keine Keys vorhanden");
        }
        int index = keyIdCounter.updateAndGet(i -> (i + 1) % size);
        return keyIds.get(index);
    }
}

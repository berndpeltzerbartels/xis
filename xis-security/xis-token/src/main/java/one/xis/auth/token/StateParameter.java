package one.xis.auth.token;

import com.google.gson.Gson;
import lombok.NonNull;
import one.xis.auth.ExpiredStateParameterException;
import one.xis.auth.InvalidStateParameterException;
import one.xis.security.SecurityUtil;
import one.xis.utils.lang.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static java.time.temporal.ChronoUnit.MINUTES;

// TODO move to xis-authentication module
public class StateParameter {

    public static final Duration STATE_PARAMETER_EXPIRATION = Duration.of(15, MINUTES);
    private static final Gson gson = new Gson();
    private static final String stateSignatureKey = SecurityUtil.createRandomKey(32);


    public static String create(String redirectUrl, String providerId) {
        StateParameterPayload payload = createStateParameterPayload(redirectUrl, providerId);
        String payloadJson = gson.toJson(payload);
        String encodedPayload = SecurityUtil.encodeBase64UrlSafe(payloadJson);
        String signature = SecurityUtil.signHmacSHA256(encodedPayload, stateSignatureKey);
        return encodedPayload + "." + signature;
    }

    public static StateParameterPayload decodeAndVerify(String stateParameter) {
        StateParameterPayload payload = decode(stateParameter);
        if (payload.getExpiresAtSeconds() < System.currentTimeMillis() / 1000) {
            throw new ExpiredStateParameterException();
        }
        return payload;
    }

    public static StateParameterPayload decode(@NonNull String stateParameter) {
        String[] parts = stateParameter.split("\\.");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid state parameter format");
        }
        String encodedPayload = parts[0];
        String signature = parts[1];
        String expectedSignature = SecurityUtil.signHmacSHA256(encodedPayload, stateSignatureKey);
        if (!expectedSignature.equals(signature)) {
            throw new IllegalArgumentException("Invalid state parameter signature");
        }
        String payloadJson = new String(SecurityUtil.decodeBase64UrlSafe(encodedPayload), StandardCharsets.UTF_8);
        return gson.fromJson(payloadJson, StateParameterPayload.class);
    }

    private static StateParameterPayload createStateParameterPayload(String urlAfterLogin, String providerId) {
        StateParameterPayload payload = new StateParameterPayload();
        payload.setCsrf(SecurityUtil.createRandomKey(32));
        payload.setRedirect(urlAfterLogin);
        payload.setIat(System.currentTimeMillis() / 1000);
        payload.setExpiresAtSeconds(payload.getIat() + STATE_PARAMETER_EXPIRATION.getSeconds());
        payload.setProviderId(providerId);
        return payload;
    }

    private StateParameterPayload verifyStateParameter(@NonNull String encodedPayload, String signature) {
        String expectedSignature = SecurityUtil.signHmacSHA256(encodedPayload, stateSignatureKey);
        if (!expectedSignature.equals(signature)) {
            throw new InvalidStateParameterException();
        }
        String payloadJson = new String(SecurityUtil.decodeBase64UrlSafe(encodedPayload));
        StateParameterPayload payload;
        try {
            payload = gson.fromJson(payloadJson, StateParameterPayload.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid state parameter payload", e);
        }
        if (StringUtils.isEmpty(payload.getCsrf())) {
            throw new IllegalArgumentException("Missing CSRF token in state parameter");
        }
        if (payload.getRedirect() == null || payload.getRedirect().isEmpty()) {
            throw new IllegalArgumentException("Missing redirect URI in state parameter");
        }
        long iat = payload.getIat();
        long currentTime = System.currentTimeMillis() / 1000;
        if (iat <= 0 || iat > currentTime) {
            throw new IllegalArgumentException("Invalid issued at time in state parameter");
        }
        long expiresAt = payload.getExpiresAtSeconds();
        if (expiresAt <= 0 || expiresAt <= iat || expiresAt < currentTime) {
            throw new IllegalArgumentException("State parameter has expired");
        }
        if (StringUtils.isEmpty(payload.getProviderId())) {
            throw new IllegalArgumentException("Missing provider ID in state parameter");
        }
        // Do not check redirect URI here, as it may be dynamic and not known in advance
        return payload;
    }

    private StateParameterPayload verifyStateParameter(@NonNull String stateParameter) {
        String[] parts = stateParameter.split("\\.");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid state parameter format");
        }
        String encodedPayload = parts[0];
        String signature = parts[1];
        return verifyStateParameter(encodedPayload, signature);
    }

}

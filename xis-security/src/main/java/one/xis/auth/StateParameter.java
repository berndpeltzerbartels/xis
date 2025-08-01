package one.xis.auth;

import com.google.gson.Gson;
import lombok.NonNull;
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


    public static String create(String redirectUrl, String issuer) {
        StateParameterPayload payload = createStateParameterPayload(redirectUrl, issuer);
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
            throw new InvalidStateParameterException("Invalid state parameter format");
        }
        String encodedPayload = parts[0];
        String signature = parts[1];
        String payloadJson = new String(SecurityUtil.decodeBase64UrlSafe(encodedPayload), StandardCharsets.UTF_8);
        StateParameterPayload payload = gson.fromJson(payloadJson, StateParameterPayload.class);
        String expectedSignature = SecurityUtil.signHmacSHA256(encodedPayload, stateSignatureKey);
        if (!expectedSignature.equals(signature)) {
            throw new InvalidStateParameterException("Invalid state parameter signature", payload);
        }
        return payload;
    }

    private static StateParameterPayload createStateParameterPayload(String urlAfterLogin, String issuer) {
        StateParameterPayload payload = new StateParameterPayload();
        payload.setCsrf(SecurityUtil.createRandomKey(32));
        payload.setRedirect(urlAfterLogin);
        payload.setIat(System.currentTimeMillis() / 1000);
        payload.setExpiresAtSeconds(payload.getIat() + STATE_PARAMETER_EXPIRATION.getSeconds());
        payload.setIssuer(issuer);
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
            throw new InvalidStateParameterException("Invalid state parameter payload", e);
        }
        if (StringUtils.isEmpty(payload.getCsrf())) {
            throw new InvalidStateParameterException("Missing CSRF token in state parameter", payload);
        }
        if (payload.getRedirect() == null || payload.getRedirect().isEmpty()) {
            throw new InvalidStateParameterException("Missing redirect URI in state parameter", payload);
        }
        long iat = payload.getIat();
        long currentTime = System.currentTimeMillis() / 1000;
        if (iat <= 0 || iat > currentTime) {
            throw new InvalidStateParameterException("Invalid issued at time in state parameter", payload);
        }
        long expiresAt = payload.getExpiresAtSeconds();
        if (expiresAt <= 0 || expiresAt <= iat || expiresAt < currentTime) {
            throw new InvalidStateParameterException("State parameter has expired", payload);
        }
        if (StringUtils.isEmpty(payload.getIssuer())) {
            throw new InvalidStateParameterException("Missing provider ID in state parameter", payload);
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

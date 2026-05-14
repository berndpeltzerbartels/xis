package one.xis.auth.token;

import lombok.Getter;

import java.time.Duration;

@Getter
public class TokenStatus {
    public static final String CONTEXT_KEY = "tokenStatus";
    private String accessToken;
    private String renewToken;
    private boolean renewed;
    private Duration expiresIn;
    private Duration renewExpiresIn;

    public TokenStatus(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.renewToken = refreshToken;
        this.renewed = false;

    }

    public void setRenewExpiresIn(Duration renewExpiresIn) {
        this.renewExpiresIn = renewExpiresIn;
        this.renewed = true;
    }

    public void setExpiresIn(Duration expiresIn) {
        this.expiresIn = expiresIn;
        this.renewed = true;
    }


    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
        this.renewed = true;
    }

    public void setRenewToken(String renewToken) {
        this.renewToken = renewToken;
        this.renewed = true;
    }
}

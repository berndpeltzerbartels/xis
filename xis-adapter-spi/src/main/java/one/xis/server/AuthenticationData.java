package one.xis.server;


import lombok.Data;

@Data
public class AuthenticationData {
    private String accessToken;
    private long accessTokenExpiresAt;
    private String renewToken;
    private String url;
}

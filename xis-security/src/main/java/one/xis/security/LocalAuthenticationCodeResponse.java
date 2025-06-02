package one.xis.security;

import lombok.Data;

@Data
public class LocalAuthenticationCodeResponse {
    private String code;
    private String state;
    private long expiresIn;
}

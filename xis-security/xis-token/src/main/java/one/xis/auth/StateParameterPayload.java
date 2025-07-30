package one.xis.auth;

import lombok.Data;

@Data
public class StateParameterPayload {
    private String csrf;
    private String redirect;
    private long iat; // issued at time in seconds since epoch
    private long expiresAtSeconds; // expiration time in seconds since epoch
    private String providerId;
}

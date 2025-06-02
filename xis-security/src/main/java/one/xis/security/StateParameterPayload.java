package one.xis.security;

import lombok.Data;

@Data
public class StateParameterPayload {
    private String csrf;
    private String redirect;
    private long iat; // issued at time in seconds since epoch
}

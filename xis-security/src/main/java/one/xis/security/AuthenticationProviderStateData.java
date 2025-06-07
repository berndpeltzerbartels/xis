package one.xis.security;

import lombok.Data;

@Data
public class AuthenticationProviderStateData {
    private final String code;
    private final String state;
    private final StateParameterPayload stateParameterPayload;
}

package one.xis.security;

import lombok.Data;

@Data
public class AuthenticationProviderStateData {
    private final String code;
    private final StateParameterPayload stateParameterPayload;
}

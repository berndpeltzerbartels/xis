package one.xis.idp;

import lombok.Data;
import one.xis.auth.token.StateParameterPayload;

@Data
public class ExternalIDPStateData {
    private final String code;
    private final String state;
    private final StateParameterPayload stateParameterPayload;
}

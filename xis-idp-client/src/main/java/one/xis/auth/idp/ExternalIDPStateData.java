package one.xis.auth.idp;

import lombok.Data;
import one.xis.auth.StateParameterPayload;

@Data
public class ExternalIDPStateData {
    private final String code;
    private final String state;
    private final StateParameterPayload stateParameterPayload;
}

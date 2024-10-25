package one.xis.context;

import lombok.Data;
import one.xis.validation.ValidatorMessages;

@Data
public class BackendBridgeResponse {
    public final String responseText;
    public final int status;
    public final ValidatorMessages validatorMessages;
}

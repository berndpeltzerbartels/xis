package one.xis.ajax;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class InvocationContext extends AjaxRequestMessage {
    private ClientAttributes clientAttributes;
}

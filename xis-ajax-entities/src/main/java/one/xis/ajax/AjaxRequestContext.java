package one.xis.ajax;

import lombok.Value;

import java.util.Date;
import java.util.List;

@Value
public class AjaxRequestContext {
    Date timestamp;
    ClientAttributes clientAttributes;
    List<? extends InvocationContext> invocationContexts;
}

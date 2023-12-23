package one.xis.server;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.lang.reflect.Parameter;

@Data
@EqualsAndHashCode(callSuper = true)
class ParameterException extends RuntimeException {
    private final Parameter parameter;
    private final Object value;

    // TODO toString(), getmessage();
}

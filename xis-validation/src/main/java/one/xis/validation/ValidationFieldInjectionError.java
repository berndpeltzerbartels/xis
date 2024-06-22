package one.xis.validation;


import lombok.Data;
import lombok.EqualsAndHashCode;

import java.lang.reflect.Field;

@Data
@EqualsAndHashCode(callSuper = true)
public class ValidationFieldInjectionError extends ValidationError {
    private Throwable throwable;
    private Field field;
    private Object value;
}

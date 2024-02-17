package one.xis.server;


import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ValidationFieldInjectionError extends ValidationError {
    private Throwable throwable;
}

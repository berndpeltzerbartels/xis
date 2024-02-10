package one.xis.server;


import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
class ValidationFieldInjectionError extends ValidationError {
    private Exception exception;
}

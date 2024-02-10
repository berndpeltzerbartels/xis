package one.xis.server;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
class ValidationFieldError extends ValidationError {
    private String messageKey;
}

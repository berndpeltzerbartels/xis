package one.xis.server;

import java.util.Collection;

interface ValidationErrorAware {

    Collection<ValidationError> getValidationErrors();
}

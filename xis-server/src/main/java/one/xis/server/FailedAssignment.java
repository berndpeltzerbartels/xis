package one.xis.server;

import lombok.Data;

import java.lang.reflect.AnnotatedElement;

@Data
class FailedAssignment {
    private final AnnotatedElement annotatedElement;
    
    private final Object value;
}

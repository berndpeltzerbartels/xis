package one.xis.validation;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.Collection;

public interface ValidationVisitor {

    ValidationErrors getErrors();

    void visitCollectionParameter(Parameter parameter, Collection<?> coll, ArrayPathElement pathElement);

    void visitArrayParameter(Parameter parameter, Object[] array, ArrayPathElement pathElement);

    void visitSingleParameter(Parameter parameter, Object value, DefaultPathElement pathElement);

    void visitCollectionField(Field field, Collection<?> coll, ArrayPathElement pathElement);

    void visitArrayField(Field field, Object[] array, ArrayPathElement pathElement);

    void visitSingleObjectField(Field field, Object value, DefaultPathElement pathElement);
}

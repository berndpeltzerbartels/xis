package one.xis.context.annofilter;

import lombok.Getter;
import one.xis.context.Component;
import one.xis.context.XISInject;

import java.util.Collection;

@Component
@Getter
class ComponentWithField {
    @XISInject(annotatedWith = TestAnnotation.class)
    private Collection<Object> field1;

}

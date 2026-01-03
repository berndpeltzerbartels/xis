package one.xis.context.annofilter;

import lombok.Getter;
import one.xis.context.Component;
import one.xis.context.Inject;

import java.util.Collection;

@Component
@Getter
class ComponentWithField {
    @Inject(annotatedWith = TestAnnotation.class)
    private Collection<Object> field1;

}

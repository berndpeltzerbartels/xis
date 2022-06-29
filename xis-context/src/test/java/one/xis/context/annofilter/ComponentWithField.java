package one.xis.context.annofilter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.context.XISInject;

import java.util.Collection;

@XISComponent
@Getter
@RequiredArgsConstructor
class ComponentWithField {
    @XISInject(annotatedWith = TestAnnotation.class)
    private Collection<Object> field1;

}

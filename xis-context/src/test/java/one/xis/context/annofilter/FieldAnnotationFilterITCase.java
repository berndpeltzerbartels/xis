package one.xis.context.annofilter;

import one.xis.context.AppContext;
import org.junit.jupiter.api.Test;

import static one.xis.utils.lang.CollectionUtils.findElementOfType;
import static org.assertj.core.api.Assertions.assertThat;

class FieldAnnotationFilterITCase {


    @Test
    void fieldWithAnnotationFilter() {

        AppContext appContext = AppContext.getInstance(getClass());

        var singletons = appContext.getSingletons();

        ComponentWithField componentWithField = findElementOfType(singletons, ComponentWithField.class);
        ComponentWithoutTestAnnotation componentWithoutTestAnnotation = findElementOfType(singletons, ComponentWithoutTestAnnotation.class);
        ComponentWithTestAnnotation componentWithTestAnnotation = findElementOfType(singletons, ComponentWithTestAnnotation.class);

        assertThat(componentWithField).isNotNull();
        assertThat(componentWithoutTestAnnotation).isNotNull();
        assertThat(componentWithTestAnnotation).isNotNull();

        assertThat(componentWithField.getField1()).containsExactly(componentWithTestAnnotation);

    }
}

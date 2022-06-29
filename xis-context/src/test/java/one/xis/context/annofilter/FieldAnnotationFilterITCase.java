package one.xis.context.annofilter;

import one.xis.context.AppContextInitializer;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static one.xis.utils.lang.CollectionUtils.findElementOfType;
import static org.assertj.core.api.Assertions.assertThat;

class FieldAnnotationFilterITCase {


    @Test
    void fieldWithAnnotationFilter() {

        AppContextInitializer initializer = new AppContextInitializer(getClass());
        initializer.initializeContext();

        Set<Object> singletons = initializer.getSingletons();

        ComponentWithField componentWithField = findElementOfType(singletons, ComponentWithField.class);
        ComponentWithoutTestAnnotation componentWithoutTestAnnotation = findElementOfType(singletons, ComponentWithoutTestAnnotation.class);
        ComponentWithTestAnnotation componentWithTestAnnotation = findElementOfType(singletons, ComponentWithTestAnnotation.class);

        assertThat(componentWithField).isNotNull();
        assertThat(componentWithoutTestAnnotation).isNotNull();
        assertThat(componentWithTestAnnotation).isNotNull();

        assertThat(componentWithField.getField1()).containsExactly(componentWithTestAnnotation);

    }
}

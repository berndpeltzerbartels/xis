package test.xis;

import one.xis.validation.EMail;
import one.xis.validation.Mandatory;
import org.junit.jupiter.api.Test;

import java.lang.reflect.RecordComponent;

import static org.assertj.core.api.Assertions.assertThat;

class RecordComponentAnnotationTest {

    record TestRecord(
            @Mandatory
            @EMail
            String email
    ) {}

    @Test
    void recordComponentShouldHaveAnnotations() {
        RecordComponent[] components = TestRecord.class.getRecordComponents();
        assertThat(components).hasSize(1);
        
        RecordComponent emailComponent = components[0];
        assertThat(emailComponent.getName()).isEqualTo("email");
        assertThat(emailComponent.getAnnotations()).hasSizeGreaterThan(0);
        assertThat(emailComponent.isAnnotationPresent(EMail.class)).isTrue();
        assertThat(emailComponent.isAnnotationPresent(Mandatory.class)).isTrue();
        
        System.out.println("Email component annotations:");
        for (var ann : emailComponent.getAnnotations()) {
            System.out.println("  - " + ann);
        }
    }
}

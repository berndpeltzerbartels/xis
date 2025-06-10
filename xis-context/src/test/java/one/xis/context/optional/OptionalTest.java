package one.xis.context.optional;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.context.AppContext;
import one.xis.context.XISBean;
import one.xis.context.XISComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class OptionalTest {


    @Nested
    class EmptyTest {

        @XISComponent
        static class Bean1 {

            @XISBean
            Optional<Bean2> bean2() {
                return Optional.empty();
            }
        }

        static class Bean2 {

        }

        @XISComponent
        @RequiredArgsConstructor
        static class Bean3 {
            @Getter
            private final List<Bean2> bean2List;
        }

        private AppContext context;

        @BeforeEach
        void setup() {
            context = AppContext.builder()
                    .withBasePackageClass(OptionalTest.class)
                    .build();
        }

        @Test
        void test() {
            var bean1 = context.getSingleton(Bean1.class);
            var bean2 = context.getOptionalSingleton(Bean2.class);
            var bean3 = context.getOptionalSingleton(Bean3.class);

            assertThat(bean1).isNotNull();
            assertThat(bean2).isEmpty();
            assertThat(bean3).isPresent();
            assertThat(bean3.get().getBean2List()).isEmpty();
        }
    }


}

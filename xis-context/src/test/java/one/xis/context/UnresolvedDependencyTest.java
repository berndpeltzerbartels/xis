package one.xis.context;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UnresolvedDependencyTest {

    @Nested
    @DisplayName("Circular Dependency with @Component")
    class ComponentCircularDependency {

        @Test
        void shouldThrowException() {
            var exception = assertThrows(IllegalStateException.class, () -> AppContext.builder()
                    .withSingletonClasses(CircularA.class, CircularB.class)
                    .build());

            String message = exception.getMessage();
            assertThat(message).contains("Circular Dependency Found");
            assertThat(message).contains("Bean 'one.xis.context.UnresolvedDependencyTest$ComponentCircularDependency$CircularA'");
            assertThat(message).contains("depends on 'one.xis.context.UnresolvedDependencyTest$ComponentCircularDependency$CircularB'");
            assertThat(message).contains("Bean 'one.xis.context.UnresolvedDependencyTest$ComponentCircularDependency$CircularB'");
            assertThat(message).contains("depends on 'one.xis.context.UnresolvedDependencyTest$ComponentCircularDependency$CircularA'");
        }

        @Component
        static class CircularA {
            CircularA(CircularB b) {
            }
        }

        @Component
        static class CircularB {

            CircularB(CircularA a) {
            }
        }
    }

    @Nested
    @DisplayName("Circular Dependency with @XISBean")
    class XISBeanCircularDependency {

        @Test
        void shouldThrowException() {
            var exception = assertThrows(IllegalStateException.class, () -> AppContext.builder()
                    .withSingletonClasses(CircularBeanConfig.class)
                    .build());

            String message = exception.getMessage();
            assertThat(message).contains("Circular Dependency Found");
            assertThat(message).contains("Bean 'one.xis.context.UnresolvedDependencyTest$XISBeanCircularDependency$BeanA'");
            assertThat(message).contains("depends on 'one.xis.context.UnresolvedDependencyTest$XISBeanCircularDependency$BeanB'");
            assertThat(message).contains("Bean 'one.xis.context.UnresolvedDependencyTest$XISBeanCircularDependency$BeanB'");
            assertThat(message).contains("depends on 'one.xis.context.UnresolvedDependencyTest$XISBeanCircularDependency$BeanA'");
        }

        static class BeanA {
        }

        static class BeanB {
        }

        @Component
        static class CircularBeanConfig {
            @XISBean
            BeanA beanA(BeanB b) {
                return new BeanA();
            }

            @XISBean
            BeanB beanB(BeanA a) {
                return new BeanB();
            }
        }
    }

    @Nested
    @DisplayName("Missing Dependency")
    class MissingDependency {

        @Test
        void shouldThrowExceptionForMissingBean() {
            var exception = assertThrows(IllegalStateException.class, () -> AppContext.builder()
                    .withSingletonClasses(Dependent.class) // MissingDependencyBean is not provided
                    .build());

            String message = exception.getMessage();
            assertThat(message).contains("Missing Bean");
            assertThat(message).contains("Bean 'one.xis.context.UnresolvedDependencyTest$MissingDependency$Dependent'");
            assertThat(message).contains("requires bean of type 'one.xis.context.UnresolvedDependencyTest$MissingDependency$MissingDependencyBean' which was not found");
        }

        @Component
        static class Dependent {

            Dependent(MissingDependencyBean missing) {
            }
        }

        // This bean is never registered with the context
        @Component
        static class MissingDependencyBean {
        }
    }
}
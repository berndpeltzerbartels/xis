package one.xis.utils.lang;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ClassUtilsTest {

    @Nested
    class LastDescendantTest {

        class A {

        }

        class B extends A {

        }

        class C extends B {

        }

        @Test
        void lastDescendant() {
            assertThat(ClassUtils.lastDescendant(Set.of(A.class, B.class, C.class))).isEqualTo(C.class);

        }
    }

    @Nested
    class GenericTypeTest {
        interface GenericInterface<T> {

        }

        class SimpleGenericTestType implements GenericInterface<String> {

        }

        class WildcarTypeTestType implements GenericInterface<GenericInterface<? extends GenericInterface>> {

        }

        @Test
        void testSimpleGenericType() {
            assertThat(ClassUtils.getGenericInterfacesTypeParameter(SimpleGenericTestType.class, GenericInterface.class, 0)).isEqualTo(String.class);
        }

        @Test
        void testWildcardType() {
            assertThat(ClassUtils.getGenericInterfacesTypeParameter(WildcarTypeTestType.class, GenericInterface.class, 0)).isEqualTo(GenericInterface.class);
        }

    }


}
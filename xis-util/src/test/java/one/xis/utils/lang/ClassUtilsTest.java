package one.xis.utils.lang;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ClassUtilsTest {

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
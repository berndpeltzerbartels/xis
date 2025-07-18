package one.xis.utils.lang;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class MethodUtilsTest {

    class GenericTestBean {
        void method(List<String> list) {

        }
    }

    @Test
    void getGenericTypeParameter() throws NoSuchMethodException {
        var param = GenericTestBean.class.getDeclaredMethod("method", List.class).getParameters()[0];
        var result = MethodUtils.getGenericTypeParameter(param);

        assertThat(result).isEqualTo(String.class);

    }

    Optional<String> optionalMethod() {
        return Optional.empty();
    }

    @Test
    void getGenericTypeParameterOptional() throws NoSuchMethodException {
        var method = this.getClass().getDeclaredMethod("optionalMethod");
        var result = MethodUtils.getGenericTypeParameterOfReturnType(method);

        assertThat(result).isEqualTo(String.class);
    }

    class TestClass1 {
        void test1() {
        }
    }

    class TestClass2 {
        void test2() {
        }
    }

    @Test
    void annotatedWith() {
    }

    @Test
    void methods() throws NoSuchMethodException {
        class TestClass1 {
            void test1(String arg) {
            }

            void test2() {
            }
        }

        class TestClass2 extends TestClass1 {

            void test1() {
            }

            @Override
            void test2() {
            }
        }

        var t1Test1 = TestClass1.class.getDeclaredMethod("test1", String.class); // Inheritance with different signature. Must be part result
        var t1Test2 = TestClass1.class.getDeclaredMethod("test2"); // is overriden. Should not be part of result
        var t2Test1 = TestClass2.class.getDeclaredMethod("test1");// part of the given class, so should be present
        var t2Test2 = TestClass2.class.getDeclaredMethod("test2"); // overrides method of base-class so should be found

        var result = MethodUtils.allMethods(new TestClass2());
        assertThat(result).containsAll(Set.of(t1Test1, t2Test1, t2Test2));
        assertThat(result).doesNotContain(t1Test2);

    }

    @Test
    void testMethods() {
    }

    @Test
    void methodSignature() {
    }

    @Test
    void invoke() {
    }

    @Test
    void hierarchy() {
        class TestClass1 {
        }

        class TestClass2 extends TestClass1 {
        }

        var result = MethodUtils.hierarchy(TestClass2.class);
        assertThat(result).isEqualTo(List.of(TestClass1.class, TestClass2.class));

    }
}
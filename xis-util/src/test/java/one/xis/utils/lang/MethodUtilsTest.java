package one.xis.utils.lang;

import org.junit.jupiter.api.Nested;
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


    @Nested
    @SuppressWarnings("unused")
    class FindGettersByFieldName {

        class TestClass1 {
            String getName() {
                return null;
            }
        }

        class TestClass2 extends TestClass1 {
            String getName() {
                return null;
            }

            int getAge() {
                return 0;
            }

            boolean isActive() {
                return true;
            }
        }


        @Test
        void findGettersByFieldName() {
            var result = MethodUtils.findGettersByFieldName(FindGettersByFieldName.TestClass2.class);
            
            assertThat(result).containsKey("name");
            assertThat(result).containsKey("age");
            assertThat(result.get("name").getDeclaringClass()).isEqualTo(FindGettersByFieldName.TestClass2.class);
            assertThat(result.get("age").getDeclaringClass()).isEqualTo(FindGettersByFieldName.TestClass2.class);

            // Assert the overridden method is the one from the current class
        }

        @Test
        void findGettersByFieldNameWithBooleanGetter() {
            var result = MethodUtils.findGettersByFieldName(FindGettersByFieldName.TestClass2.class);

            assertThat(result).containsKey("active");
            assertThat(result.get("active")).isNotNull();
            assertThat(result.get("active").getDeclaringClass()).isEqualTo(FindGettersByFieldName.TestClass2.class);
        }

    }

    /**
     * Tests the {@link MethodUtils#findSettersByFieldName(Class)} method.
     * It should return all methods of the class, including inherited ones
     * and should allow overriding methods and let the current class replace
     * methods of the base class.
     */

    @Test
    void findSettersByFieldName() {
        class TestClass1 {
            void setName(String name) {
            }
        }

        class TestClass2 extends TestClass1 {
            void setName(String name) {
            }

            void setAge(int age) {
            }
        }

        var result = MethodUtils.findSettersByFieldName(TestClass2.class);
        assertThat(result).hasSize(2);
        assertThat(result).containsKey("name");
        assertThat(result).containsKey("age");
        // Assert the overridden method is the one from the current class
        assertThat(result.get("name").getDeclaringClass()).isEqualTo(TestClass2.class);
    }

    @Test
    void findGettersByFieldName() {
        class TestClass1 {
            String getName() {
                return null;
            }
        }

        class TestClass2 extends TestClass1 {
            String getName() {
                return null;
            }

            int getAge() {
                return 0;
            }
        }

        var result = MethodUtils.findGettersByFieldName(TestClass2.class);
        assertThat(result).hasSize(2);
        assertThat(result).containsKey("name");
        assertThat(result).containsKey("age");
        // Assert the overridden method is the one from the current class
        assertThat(result.get("name").getDeclaringClass()).isEqualTo(TestClass2.class);
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
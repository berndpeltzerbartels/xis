package one.xis.utils.lang;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

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

        Optional<String> optionalMethod() {
            return Optional.empty();
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

    @Nested
    class NewInstanceWithDefaultsTest {

        @Nested
        class EmptyConstructorTest {
            static class EmptyConstructor {
                public EmptyConstructor() {
                }
            }

            @Test
            void shouldInstantiateEmptyConstructor() {
                EmptyConstructor instance = ClassUtils.newInstance(EmptyConstructor.class);
                assertThat(instance).isNotNull();
            }
        }

        @Nested
        class PrimitiveTypesTest {
            static class SinglePrimitiveConstructor {
                private final int value;

                public SinglePrimitiveConstructor(int value) {
                    this.value = value;
                }
            }

            static class MultiplePrimitivesConstructor {
                private final int intValue;
                private final boolean boolValue;
                private final double doubleValue;

                public MultiplePrimitivesConstructor(int intValue, boolean boolValue, double doubleValue) {
                    this.intValue = intValue;
                    this.boolValue = boolValue;
                    this.doubleValue = doubleValue;
                }
            }

            record AllPrimitives(
                    boolean boolValue,
                    byte byteValue,
                    short shortValue,
                    int intValue,
                    long longValue,
                    float floatValue,
                    double doubleValue,
                    char charValue
            ) {
            }

            @Test
            void shouldInstantiateSinglePrimitive() {
                SinglePrimitiveConstructor instance = ClassUtils.newInstance(SinglePrimitiveConstructor.class);
                assertThat(instance).isNotNull();
                assertThat(instance.value).isEqualTo(0);
            }

            @Test
            void shouldInstantiateMultiplePrimitives() {
                MultiplePrimitivesConstructor instance = ClassUtils.newInstance(MultiplePrimitivesConstructor.class);
                assertThat(instance).isNotNull();
                assertThat(instance.intValue).isEqualTo(0);
                assertThat(instance.boolValue).isFalse();
                assertThat(instance.doubleValue).isEqualTo(0.0);
            }

            @Test
            void shouldInstantiateAllPrimitiveTypes() {
                AllPrimitives instance = ClassUtils.newInstance(AllPrimitives.class);
                assertThat(instance).isNotNull();
                assertThat(instance.boolValue()).isFalse();
                assertThat(instance.byteValue()).isEqualTo((byte) 0);
                assertThat(instance.shortValue()).isEqualTo((short) 0);
                assertThat(instance.intValue()).isEqualTo(0);
                assertThat(instance.longValue()).isEqualTo(0L);
                assertThat(instance.floatValue()).isEqualTo(0.0f);
                assertThat(instance.doubleValue()).isEqualTo(0.0);
                assertThat(instance.charValue()).isEqualTo('\u0000');
            }
        }

        @Nested
        class StringAndWrapperTypesTest {
            static class StringConstructor {
                private final String value;

                public StringConstructor(String value) {
                    this.value = value;
                }
            }

            static class WrapperTypesConstructor {
                private final Integer intValue;
                private final Boolean boolValue;
                private final Double doubleValue;
                private final Long longValue;

                public WrapperTypesConstructor(Integer intValue, Boolean boolValue, Double doubleValue, Long longValue) {
                    this.intValue = intValue;
                    this.boolValue = boolValue;
                    this.doubleValue = doubleValue;
                    this.longValue = longValue;
                }
            }

            @Test
            void shouldInstantiateStringConstructor() {
                StringConstructor instance = ClassUtils.newInstance(StringConstructor.class);
                assertThat(instance).isNotNull();
                assertThat(instance.value).isEmpty();
            }

            @Test
            void shouldInstantiateWrapperTypes() {
                WrapperTypesConstructor instance = ClassUtils.newInstance(WrapperTypesConstructor.class);
                assertThat(instance).isNotNull();
                assertThat(instance.intValue).isEqualTo(0);
                assertThat(instance.boolValue).isFalse();
                assertThat(instance.doubleValue).isEqualTo(0.0);
                assertThat(instance.longValue).isEqualTo(0L);
            }
        }

        @Nested
        class CollectionsTest {
            static class CollectionConstructor {
                private final List<String> list;
                private final Set<Integer> set;
                private final Map<String, Object> map;

                public CollectionConstructor(List<String> list, Set<Integer> set, Map<String, Object> map) {
                    this.list = list;
                    this.set = set;
                    this.map = map;
                }
            }

            @Test
            void shouldInstantiateWithEmptyCollections() {
                CollectionConstructor instance = ClassUtils.newInstance(CollectionConstructor.class);
                assertThat(instance).isNotNull();
                assertThat(instance.list).isNotNull().isEmpty();
                assertThat(instance.set).isNotNull().isEmpty();
                assertThat(instance.map).isNotNull().isEmpty();
            }
        }

        @Nested
        class RecordsTest {
            record SimpleRecord(String name, int age) {
            }

            record NestedRecord(String title, SimpleRecord person) {
            }

            @Test
            void shouldInstantiateSimpleRecord() {
                SimpleRecord instance = ClassUtils.newInstance(SimpleRecord.class);
                assertThat(instance).isNotNull();
                assertThat(instance.name()).isEmpty();
                assertThat(instance.age()).isEqualTo(0);
            }

            @Test
            void shouldInstantiateNestedRecord() {
                NestedRecord instance = ClassUtils.newInstance(NestedRecord.class);
                assertThat(instance).isNotNull();
                assertThat(instance.title()).isEmpty();
                assertThat(instance.person()).isNotNull();
                assertThat(instance.person().name()).isEmpty();
                assertThat(instance.person().age()).isEqualTo(0);
            }
        }

        @Nested
        class ComplexNestedTypesTest {
            static class SimpleClass {
                private final int value;

                public SimpleClass(int value) {
                    this.value = value;
                }
            }

            static class NestedComplexConstructor {
                private final SimpleClass nested;
                private final String name;

                public NestedComplexConstructor(SimpleClass nested, String name) {
                    this.nested = nested;
                    this.name = name;
                }
            }

            record Level3(String value) {
            }

            record Level2(Level3 level3, int number) {
            }

            record Level1(Level2 level2, String name) {
            }

            @Test
            void shouldInstantiateNestedComplexObject() {
                NestedComplexConstructor instance = ClassUtils.newInstance(NestedComplexConstructor.class);
                assertThat(instance).isNotNull();
                assertThat(instance.nested).isNotNull();
                assertThat(instance.nested.value).isEqualTo(0);
                assertThat(instance.name).isEmpty();
            }

            @Test
            void shouldInstantiateDeeplyNestedRecords() {
                Level1 instance = ClassUtils.newInstance(Level1.class);
                assertThat(instance).isNotNull();
                assertThat(instance.level2()).isNotNull();
                assertThat(instance.level2().level3()).isNotNull();
                assertThat(instance.level2().level3().value()).isEmpty();
                assertThat(instance.level2().number()).isEqualTo(0);
                assertThat(instance.name()).isEmpty();
            }
        }

        @Nested
        class EnumsTest {
            enum TestEnum {
                FIRST, SECOND, THIRD
            }

            static class EnumConstructor {
                private final TestEnum enumValue;

                public EnumConstructor(TestEnum enumValue) {
                    this.enumValue = enumValue;
                }
            }

            @Test
            void shouldInstantiateWithFirstEnumValue() {
                EnumConstructor instance = ClassUtils.newInstance(EnumConstructor.class);
                assertThat(instance).isNotNull();
                assertThat(instance.enumValue).isEqualTo(TestEnum.FIRST);
            }
        }

        @Nested
        class ArraysTest {
            static class ArrayConstructor {
                private final String[] stringArray;
                private final int[] intArray;

                public ArrayConstructor(String[] stringArray, int[] intArray) {
                    this.stringArray = stringArray;
                    this.intArray = intArray;
                }
            }

            @Test
            void shouldInstantiateWithEmptyArrays() {
                ArrayConstructor instance = ClassUtils.newInstance(ArrayConstructor.class);
                assertThat(instance).isNotNull();
                assertThat(instance.stringArray).isNotNull().isEmpty();
                assertThat(instance.intArray).isNotNull().isEmpty();
            }
        }

        @Nested
        class MultipleConstructorsTest {
            static class MultipleConstructorsClass {
                private final String value;
                private final int number;

                public MultipleConstructorsClass() {
                    this.value = "default";
                    this.number = 0;
                }

                public MultipleConstructorsClass(String value) {
                    this.value = value;
                    this.number = 0;
                }

                public MultipleConstructorsClass(String value, int number) {
                    this.value = value;
                    this.number = number;
                }
            }

            @Test
            void shouldChooseSimplestConstructor() {
                MultipleConstructorsClass instance = ClassUtils.newInstance(MultipleConstructorsClass.class);
                assertThat(instance).isNotNull();
                assertThat(instance.value).isEqualTo("default");
                assertThat(instance.number).isEqualTo(0);
            }
        }
    }




}

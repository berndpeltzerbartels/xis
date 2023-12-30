package one.xis.parameter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import one.xis.utils.lang.CollectionUtils;
import one.xis.validation.Validation;
import one.xis.validation.ValidatorResultElement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ParameterDeserializerTest {

    private final Validation validation = mock(Validation.class);
    private final ParameterDeserializer deserializer = new ParameterDeserializerImpl(validation);

    @Nested
    class ParameterDeserializationOnlyTest {
        @Test
        @DisplayName("Parameter with complex object with string field is getting deserialized")
        void deserialzeObject() throws IOException, NoSuchMethodException {
            var json = "{ \"text\":\"Hello !\", \"b\": { \"c\": {\"value\":\"Huhu !\"}} }";
            var result = (A) deserializer.deserialze(json, TestPojo.class.getDeclaredMethod("test", A.class).getParameters()[0], ValidatorResultElement.rootResult(), Locale.GERMANY, ZoneId.of("Europe/Berlin"));

            assertThat(result.text).isEqualTo("Hello !");
            assertThat(result.b.c.value).isEqualTo("Huhu !");

        }

        @Test
        @SuppressWarnings("unchecked")
        @DisplayName("An array of integers as parameter is getting deserialized")
        void deserialzeArray() throws IOException, NoSuchMethodException {
            var json = "[1,2,3,4]";
            var result = (List<Integer>) deserializer.deserialze(json, TestPojo.class.getDeclaredMethod("test1", List.class).getParameters()[0], ValidatorResultElement.rootResult(), Locale.GERMANY, ZoneId.of("Europe/Berlin"));

            assertThat(result).containsExactly(1, 2, 3, 4);
        }


        @Test
        @SuppressWarnings("unchecked")
        @DisplayName("Deserialization of json-array nested in json-arrays is getting deserialized to list of list")
        void typeParametersInTypeParameters() throws NoSuchMethodException, IOException {
            var json = "[[1],[2],[3],[4]]";
            var result = (List<List<Integer>>) deserializer.deserialze(json, TestPojo.class.getDeclaredMethod("test2", List.class).getParameters()[0], ValidatorResultElement.rootResult(), Locale.GERMANY, ZoneId.of("Europe/Berlin"));

            assertThat(result.get(0)).isEqualTo(List.of(1));
            assertThat(result.get(1)).isEqualTo(List.of(2));
            assertThat(result.get(2)).isEqualTo(List.of(3));
            assertThat(result.get(3)).isEqualTo(List.of(4));
        }

        @Test
        @DisplayName("A parameter of hierarchy of multiple different collections types is deserialized from nested json arrays")
        void typeParametersInTypeParameters3() throws NoSuchMethodException, IOException {
            var json = "[[[1]]]";
            var result = deserializer.deserialze(json, TestPojo.class.getDeclaredMethod("test3", ArrayList.class).getParameters()[0], ValidatorResultElement.rootResult(), Locale.GERMANY, ZoneId.of("Europe/Berlin"));
            assertThat(result).isInstanceOf(ArrayList.class);
            result = CollectionUtils.onlyElement((Collection<?>) result);
            assertThat(result).isInstanceOf(LinkedList.class);
            result = CollectionUtils.onlyElement((Collection<?>) result);
            assertThat(result).isInstanceOf(HashSet.class);
            result = CollectionUtils.onlyElement((Collection<?>) result);
            assertThat(result).isEqualTo(1);

        }


        @Test
        @DisplayName("An integer as  string is deserialized to differen parameter types")
        void integer() throws NoSuchMethodException, IOException {
            var method = TestPojo.class.getDeclaredMethod("integer", String.class, Integer.TYPE, Integer.class, BigInteger.class, BigDecimal.class);

            var typeValidationResult = ValidatorResultElement.rootResult();

            assertThat(deserializer.deserialze("123", method.getParameters()[0], typeValidationResult, Locale.GERMANY, ZoneId.of("Europe/Berlin"))).isEqualTo("123");
            assertThat(deserializer.deserialze("123", method.getParameters()[1], typeValidationResult, Locale.GERMANY, ZoneId.of("Europe/Berlin"))).isEqualTo(123);
            assertThat(deserializer.deserialze("123", method.getParameters()[2], typeValidationResult, Locale.GERMANY, ZoneId.of("Europe/Berlin"))).isEqualTo(Integer.parseInt("123"));
            assertThat(deserializer.deserialze("123", method.getParameters()[3], typeValidationResult, Locale.GERMANY, ZoneId.of("Europe/Berlin"))).isEqualTo(BigInteger.valueOf(123));
            assertThat(deserializer.deserialze("123", method.getParameters()[4], typeValidationResult, Locale.GERMANY, ZoneId.of("Europe/Berlin"))).isEqualTo(BigDecimal.valueOf(123.0));
        }

        @Test
        @DisplayName("A date in iso and localized format is deserialized to LocalDate")
        void localDate() throws NoSuchMethodException, IOException {
            var method = TestPojo.class.getDeclaredMethod("localDate", LocalDate.class);
            var parameter = method.getParameters()[0];
            var typeValidationResult = ValidatorResultElement.rootResult();
            var expected = LocalDate.of(2016, Month.MAY, 1);

            assertThat(deserializer.deserialze("01.05.2016", parameter, typeValidationResult, Locale.GERMANY, ZoneId.of("Europe/Berlin"))).isEqualTo(expected);
            assertThat(deserializer.deserialze("2016-05-01", parameter, typeValidationResult, Locale.GERMANY, ZoneId.of("Europe/Berlin"))).isEqualTo(expected);
        }


        @Test
        @DisplayName("A datetime in localized format is deserialized for different parameter types")
        void dateTimeTypesLocalized() throws NoSuchMethodException, IOException {
            var method = TestPojo.class.getDeclaredMethod("dateTimeTypes", DateTimeValue.class);
            var typeValidationResult = ValidatorResultElement.rootResult();
            var json = "{\"localDateTime\":\"12.07.2000, 23:00:00\",\"zonedDateTime\":\"12.07.2000, 23:00:00\",\"offsetDateTime\":\"12.07.2000, 23:00:00\",\"date\":\"12.07.2000, 23:00:00\"}";
            var expected = Instant.from(ZonedDateTime.of(2000, 7, 12, 21, 0, 0, 0, ZoneId.of("UTC")));

            var params = method.getParameters();
            var result = (DateTimeValue) deserializer.deserialze(json, params[0], typeValidationResult, Locale.GERMANY, ZoneId.of("Europe/Berlin"));


            assertThat(result.date.toInstant()).isEqualTo(expected);
            assertThat(result.zonedDateTime.toInstant()).isEqualTo(expected);
            assertThat(result.offsetDateTime.toInstant()).isEqualTo(expected);
            assertThat(result.localDateTime.toInstant(ZoneOffset.ofHours(2))).isEqualTo(expected);
        }

        @Test
        @DisplayName("A datetime in iso format is deserialized for different parameter types")
        void dateTimeTypesIso() throws NoSuchMethodException, IOException {
            var method = TestPojo.class.getDeclaredMethod("dateTimeTypes", DateTimeValue.class);
            var typeValidationResult = ValidatorResultElement.rootResult();
            var json = "{\"localDateTime\":\"2000-07-12T23:00:00\",\"zonedDateTime\":\"2000-07-12T23:00:00\",\"offsetDateTime\":\"2000-07-12T23:00:00\",\"date\":\"2000-07-12T23:00:00\"}";
            var expected = Instant.from(ZonedDateTime.of(2000, 7, 12, 21, 0, 0, 0, ZoneId.of("UTC")));

            var params = method.getParameters();
            var result = (DateTimeValue) deserializer.deserialze(json, params[0], typeValidationResult, Locale.GERMANY, ZoneId.of("Europe/Berlin"));


            assertThat(result.date.toInstant()).isEqualTo(expected);
            assertThat(result.zonedDateTime.toInstant()).isEqualTo(expected);
            assertThat(result.offsetDateTime.toInstant()).isEqualTo(expected);
            assertThat(result.localDateTime.toInstant(ZoneOffset.ofHours(2))).isEqualTo(expected);
        }

        @Test
        void zonedDateTimeAsIso() throws NoSuchMethodException, IOException {
            var method = TestPojo.class.getDeclaredMethod("dateTimeTypes", DateTimeValue.class);
            var typeValidationResult = ValidatorResultElement.rootResult();
            var json = "{\"zonedDateTime\":\"2000-07-12T23:00:00+10:00\"}";
            var expected = Instant.from(ZonedDateTime.of(2000, 7, 12, 13, 0, 0, 0, ZoneId.of("UTC")));
            var params = method.getParameters();
            var result = (DateTimeValue) deserializer.deserialze(json, params[0], typeValidationResult, Locale.GERMANY, ZoneId.of("Europe/Berlin"));

            assertThat(result.zonedDateTime.toInstant()).isEqualTo(expected);
        }


        @Data
        static class TestPojo {

            void test(A a) {
            }


            void test1(List<Integer> list) {

            }

            void test2(List<List<Integer>> list) {

            }

            void test3(ArrayList<LinkedList<Set<Integer>>> list) {

            }

            void integer(String s, int i1, Integer i2, BigInteger bigInteger, BigDecimal bigDecimal) {
            }

            void localDate(LocalDate localDate) {

            }

            void dateTimeTypes(DateTimeValue value) {

            }
        }

        @Data
        static class DateTimeValue {
            private LocalDateTime localDateTime;
            private ZonedDateTime zonedDateTime;
            private OffsetDateTime offsetDateTime;
            private Date date;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        static
        class A {
            private String text;
            private B b;

        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        static
        class B {
            private C c;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        static
        class C {
            private String value;
        }
    }

    @Nested
    class IntegerValidationTest {


        @Data
        static class A {
            private B b;
        }

        @Data
        static class B {
            private int integer;
        }


        @Test
        void invalidInteger() throws NoSuchFieldException, IOException {
            var json = "{\"integer\": \"bla\"}";
            var validationElement = ValidatorResultElement.rootResult();
            var object = deserializer.deserialze(json, A.class.getDeclaredField("b"), validationElement, Locale.GERMANY, ZoneId.of("Europe/Berlin"));
        }

        @Test
        void validInteger() throws NoSuchFieldException, IOException {
            var json = "{\"integer\": \"123\"}";
            var validationElement = ValidatorResultElement.rootResult();
            var object = deserializer.deserialze(json, A.class.getDeclaredField("b"), validationElement, Locale.GERMANY, ZoneId.of("Europe/Berlin"));
        }


    }
}
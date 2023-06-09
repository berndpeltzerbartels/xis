package one.xis.server;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ParameterDeserializerTest {

    private final ParameterDeserializer deserializer = new ParameterDeserializer();

    @Test
    void deserialzeObject() throws IOException, NoSuchMethodException {
        var json = "{ \"text\":\"Hello !\", \"b\": { \"c\": {\"value\":\"Huhu !\"}} }";
        var result = (A) deserializer.deserialze(json, TestPojo.class.getDeclaredMethod("test", A.class).getParameters()[0]);

        assertThat(result.text).isEqualTo("Hello !");
        assertThat(result.b.c.value).isEqualTo("Huhu !");

    }

    @Test
    @SuppressWarnings("unchecked")
    void deserialzeArray() throws IOException, NoSuchMethodException {
        var json = "[1,2,3,4]";
        var result = (List<Integer>) deserializer.deserialze(json, TestPojo.class.getDeclaredMethod("test1", List.class).getParameters()[0]);

        assertThat(result).containsExactly(1, 2, 3, 4);
    }


    @Test
    @SuppressWarnings("unchecked")
    void typeParametersInTypeParameters() throws NoSuchMethodException, IOException {
        var json = "[[1],[2],[3],[4]]";
        var result = (List<List<Integer>>) deserializer.deserialze(json, TestPojo.class.getDeclaredMethod("test2", List.class).getParameters()[0]);

        assertThat(result.get(0)).isEqualTo(List.of(1));
        assertThat(result.get(1)).isEqualTo(List.of(2));
        assertThat(result.get(2)).isEqualTo(List.of(3));
        assertThat(result.get(3)).isEqualTo(List.of(4));
    }


    @Test
    void integer() throws NoSuchMethodException, IOException {
        var method = TestPojo.class.getDeclaredMethod("integer", String.class, Integer.TYPE, Integer.class, BigInteger.class, BigDecimal.class);

        assertThat(deserializer.deserialze("123", method.getParameters()[0])).isEqualTo("123");
        assertThat(deserializer.deserialze("123", method.getParameters()[1])).isEqualTo(123);
        assertThat(deserializer.deserialze("123", method.getParameters()[2])).isEqualTo(Integer.parseInt("123"));
        assertThat(deserializer.deserialze("123", method.getParameters()[3])).isEqualTo(BigInteger.valueOf(123));
        assertThat(deserializer.deserialze("123", method.getParameters()[4])).isEqualTo(BigDecimal.valueOf(123));
    }

    @Data
    static class TestPojo {

        void test(A a) {
        }


        void test1(List<Integer> list) {

        }

        void test2(List<List<Integer>> list) {

        }

        void integer(String s, int i1, Integer i2, BigInteger bigInteger, BigDecimal bigDecimal) {
        }
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
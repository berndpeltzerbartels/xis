package one.xis.js;

import lombok.AllArgsConstructor;
import lombok.Data;
import one.xis.test.js.JSUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static one.xis.js.JavascriptSource.CLASSES;
import static org.assertj.core.api.Assertions.assertThat;


@SuppressWarnings("unchecked")
class ClientStateTest {

    private String js;
    private BiConsumer<String, Runnable> registerListener;
    private Function<String, Object> getValue;
    private Consumer<Object> publish;

    @BeforeEach
    void init() throws ScriptException {
        js = Javascript.getScript(CLASSES);
        js += "var clientState = new ClientState();";
        js += "clientState";
        var result = JSUtil.execute(js);
        registerListener = result.getMember("registerListener").as(BiConsumer.class);
        getValue = result.getMember("getValue").as(Function.class);
        publish = result.getMember("publish").as(Consumer.class);
    }

    @Nested
    public class SimplePathTest {

        @Data
        @AllArgsConstructor
        public static class TestData {
            public Object value;
        }

        private boolean listenerInvoked;

        @BeforeEach
        void init() {
            registerListener.accept("value", this::run);
        }

        @Test
        void simpleValue() throws ScriptException {
            publish.accept(new TestData(200));

            var result = getValue.apply("value");
            assertThat(result).isEqualTo(200);
            assertThat(listenerInvoked).isTrue();
        }


        public void run() {
            listenerInvoked = true;
        }
    }

    @Nested
    public class PathWith2Elements {
        private boolean listenerInvoked;

        @Data
        @AllArgsConstructor
        public static class B {
            public Object c;
        }

        @Data
        @AllArgsConstructor
        public static class A {
            public B b;
        }

        @BeforeEach
        void init() {
            listenerInvoked = false;
        }

        @Test
        void fullPath() {
            registerListener.accept("b.c", this::run);
            publish.accept(new A(new B(200)));

            var result = getValue.apply("b.c");
            assertThat(result).isEqualTo(200);
            assertThat(listenerInvoked).isTrue();
        }

        @Test
        void parentElement() {
            registerListener.accept("b", this::run);
            publish.accept(new A(new B(200)));

            var result = (B) getValue.apply("b");
            assertThat(result.getC()).isEqualTo(200);
            assertThat(listenerInvoked).isTrue();
        }


        public void run() {
            listenerInvoked = true;
        }
    }

    @Nested
    class StoreTwiceTest {


        @Data
        @AllArgsConstructor
        public static class TestData2 {
            public Object value;
        }

        private int listenerInvokationCount;

        @BeforeEach
        void init() {
            listenerInvokationCount = 0;
        }

        @Test
        void storeTwice() {
            registerListener.accept("value", this::run);

            publish.accept(new TestData2(200));
            assertThat(getValue.apply("value")).isEqualTo(200);

            publish.accept(new TestData2(300));
            assertThat(getValue.apply("value")).isEqualTo(300);

            assertThat(listenerInvokationCount).isEqualTo(2);
        }

        public void run() {
            listenerInvokationCount++;
        }


    }
}



package one.xis.js;

import lombok.AllArgsConstructor;
import lombok.Data;
import one.xis.test.dom.Location;
import one.xis.test.dom.Window;
import one.xis.test.js.JSUtil;
import one.xis.test.js.SessionStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static one.xis.js.JavascriptSource.CLASSES;
import static org.assertj.core.api.Assertions.assertThat;


@SuppressWarnings("unchecked")
class ClientStateTest {

    private String js;
    private Consumer<String> activatePath;
    private Function<String, Object> getValue;
    private Consumer<Object> publish;

    @BeforeEach
    void init() throws ScriptException {
        js = Javascript.getScript(CLASSES);
        js += "var clientState = new ClientState();";
        js += "clientState";
        var result = JSUtil.execute(js, Map.of("sessionStorage", new SessionStorage(), "window", new Window(new Location())));
        activatePath = result.getMember("activatePath").as(Consumer.class);
        getValue = result.getMember("getValue").as(Function.class);
        publish = result.getMember("saveData").as(Consumer.class);
    }

    @Nested
    class CustomPathTests {

        @Data
        @AllArgsConstructor
        public static class NestedObject {
            public Object b;
        }

        @Data
        @AllArgsConstructor
        public static class ComplexObject {
            public NestedObject a;
        }

        @Data
        @AllArgsConstructor
        public static class DataContainer {
            public Object a;
        }

        @BeforeEach
        void init() {
            publish.accept(new ComplexObject(new NestedObject(100)));
        }

        @Test
        void nestedObjectPath() {
            // Beispiel 1: Verschachteltes Objekt
            activatePath.accept("a.b");

            var result = getValue.apply("a.b");
            assertThat(result).isEqualTo(100);
        }

        @Test
        void fullObject() {
            // Beispiel 2: Ganzes Objekt
            activatePath.accept("a");

            var result = (Map) getValue.apply("a");
            assertThat(result.get("b")).isEqualTo(100);
        }

        @Test
        void directKey() {
            // Beispiel 3: Direkter Schlüssel
            activatePath.accept("a.b");
            publish.accept(new NestedObject(100));

            var result = getValue.apply("a.b");
            assertThat(result).isEqualTo(100); // Hier wird das Ergebnis als Map erwartet
        }

        @Test
        void invalidPath() {
            // Beispiel 4: Ungültiger Pfad
            activatePath.accept("a.c");

            var result = getValue.apply("a.c");
            assertThat(result).isNull(); // Ungültiger Pfad sollte null zurückgeben
        }
    }

    @Nested
    class MapTextContentTests {

        @Data
        @AllArgsConstructor
        public static class TextContent {
            private String content;

            public void doRefresh() {
                // Simuliert das Aktualisieren des Textinhalts
                this.content = "Refreshed";
            }
        }

        private Consumer<String> activatePath;
        private Consumer<TextContent> mapTextContent;
        private Function<String, Object> getValue;

        @BeforeEach
        void init() throws ScriptException {
            js = Javascript.getScript(CLASSES);
            js += "var clientState = new ClientState();";
            js += "clientState";
            var result = JSUtil.execute(js, Map.of("sessionStorage", new SessionStorage(), "window", new Window(new Location())));
            activatePath = result.getMember("activatePath").as(Consumer.class);
            mapTextContent = result.getMember("mapTextContent").as(Consumer.class);
            getValue = result.getMember("getValue").as(Function.class);
        }

        @Test
        void mapSinglePathToTextContent() {
            // Aktiviere einen Pfad
            activatePath.accept("a.b");

            // Erstelle ein TextContent-Objekt und mappe es
            TextContent textContent = new TextContent("Initial");
            mapTextContent.accept(textContent);

            // Simuliere eine Änderung und prüfe, ob das TextContent-Objekt aktualisiert wird
            textContent.doRefresh();
            assertThat(textContent.getContent()).isEqualTo("Refreshed");
        }

        @Test
        void mapMultiplePathsToTextContent() {
            // Aktiviere mehrere Pfade
            activatePath.accept("a.b");
            activatePath.accept("a.c");

            // Erstelle ein TextContent-Objekt und mappe es
            TextContent textContent = new TextContent("Initial");
            mapTextContent.accept(textContent);

            // Simuliere eine Änderung und prüfe, ob das TextContent-Objekt aktualisiert wird
            textContent.doRefresh();
            assertThat(textContent.getContent()).isEqualTo("Refreshed");
        }

        @Test
        void mapNoPathToTextContent() {
            // Kein Pfad wird aktiviert

            // Erstelle ein TextContent-Objekt und mappe es
            TextContent textContent = new TextContent("Initial");
            mapTextContent.accept(textContent);

            // Simuliere eine Änderung und prüfe, ob das TextContent-Objekt nicht aktualisiert wird
            assertThat(textContent.getContent()).isEqualTo("Initial");
        }
    }
}



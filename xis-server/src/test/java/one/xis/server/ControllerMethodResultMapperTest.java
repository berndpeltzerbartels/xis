package one.xis.server;

import one.xis.FormData;
import one.xis.ClientState;
import one.xis.LocalStorage;
import one.xis.ModelData;
import one.xis.SessionStorage;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class ControllerMethodResultMapperTest {

    private final ControllerMethodResultMapper mapper = new ControllerMethodResultMapper(mock(), new PathResolver());

    @Test
    void unwrapsOptionalModelDataReturnValue() throws Exception {
        var result = new ControllerMethodResult();

        mapper.mapReturnValueToResult(result, method("optionalModel"), Optional.of("Ada"), new java.util.HashMap<>());

        assertThat(result.getModelData()).containsEntry("customer", "Ada");
    }

    @Test
    void mapsEmptyOptionalModelDataReturnValueToNull() throws Exception {
        var result = new ControllerMethodResult();

        mapper.mapReturnValueToResult(result, method("optionalModel"), Optional.empty(), new java.util.HashMap<>());

        assertThat(result.getModelData()).containsEntry("customer", null);
    }

    @Test
    void materializesAndClosesStreamModelDataReturnValue() throws Exception {
        var result = new ControllerMethodResult();
        var closed = new AtomicBoolean();
        Stream<String> stream = Stream.of("Ada", "Grace").onClose(() -> closed.set(true));

        mapper.mapReturnValueToResult(result, method("streamModel"), stream, new java.util.HashMap<>());

        assertThat(result.getModelData()).containsEntry("customers", List.of("Ada", "Grace"));
        assertThat(closed).isTrue();
    }

    @Test
    void normalizesFormDataReturnValue() throws Exception {
        var result = new ControllerMethodResult();

        mapper.mapReturnValueToResult(result, method("optionalForm"), Optional.of("form"), new java.util.HashMap<>());

        assertThat(result.getFormData()).containsEntry("customer", "form");
    }

    @Test
    void rejectsNullFormDataReturnValue() throws Exception {
        var result = new ControllerMethodResult();

        assertThatThrownBy(() -> mapper.mapReturnValueToResult(result, method("form"), null, new java.util.HashMap<>()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("@FormData method must not return null for key: customer");
    }

    @Test
    void rejectsEmptyOptionalFormDataReturnValue() throws Exception {
        var result = new ControllerMethodResult();

        assertThatThrownBy(() -> mapper.mapReturnValueToResult(result, method("optionalForm"), Optional.empty(), new java.util.HashMap<>()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("@FormData method must not return null for key: customer");
    }

    @Test
    void mapsSimpleStorageMethodReturnValues() throws Exception {
        var result = new ControllerMethodResult();

        mapper.mapReturnValueToResult(result, method("simpleStorage"), "selected", new java.util.HashMap<>());

        assertThat(result.getLocalStorage()).containsEntry("local", "selected");
        assertThat(result.getSessionStorage()).containsEntry("session", "selected");
        assertThat(result.getClientState()).containsEntry("client", "selected");
    }

    @Test
    void mapsNullStorageMethodReturnValueAsRemovalSignal() throws Exception {
        var result = new ControllerMethodResult();

        mapper.mapReturnValueToResult(result, method("simpleStorage"), null, new java.util.HashMap<>());

        assertThat(result.getLocalStorage()).containsEntry("local", null);
        assertThat(result.getSessionStorage()).containsEntry("session", null);
        assertThat(result.getClientState()).containsEntry("client", null);
    }

    private Method method(String name) throws NoSuchMethodException {
        return Methods.class.getDeclaredMethod(name);
    }

    static class Methods {

        @ModelData("customer")
        Optional<String> optionalModel() {
            return Optional.empty();
        }

        @ModelData("customers")
        Stream<String> streamModel() {
            return Stream.empty();
        }

        @FormData("customer")
        Optional<String> optionalForm() {
            return Optional.empty();
        }

        @FormData("customer")
        String form() {
            return "";
        }

        @LocalStorage("local")
        @SessionStorage("session")
        @ClientState("client")
        String simpleStorage() {
            return "";
        }
    }
}

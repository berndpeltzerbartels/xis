package one.xis.server;

import one.xis.ClientState;
import one.xis.LocalStorage;
import one.xis.SessionStorage;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AttributesFactoryTest {

    private final AttributesFactory attributesFactory = new AttributesFactory();

    @Test
    void methodStorageAnnotationsContributeRequestedStoreKeys() throws Exception {
        var attributes = new ComponentAttributes();

        attributesFactory.addParameterAttributes(Controller.class.getDeclaredMethod("storeValues"), attributes);

        assertThat(attributes.getLocalStorageKeys()).containsExactly("local");
        assertThat(attributes.getSessionStorageKeys()).containsExactly("session");
        assertThat(attributes.getClientStateKeys()).containsExactly("client");
    }

    static class Controller {
        @LocalStorage("local")
        @SessionStorage("session")
        @ClientState("client")
        String storeValues() {
            return "";
        }
    }
}

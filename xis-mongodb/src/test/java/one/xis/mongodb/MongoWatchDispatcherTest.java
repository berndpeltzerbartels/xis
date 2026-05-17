package one.xis.mongodb;

import one.xis.context.AppContext;
import one.xis.context.AppContextInitializedEvent;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MongoWatchDispatcherTest {

    @Test
    void rejectsWatchMethodWithoutParameter() {
        var dispatcher = new MongoWatchDispatcher(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> dispatcher.startWatchers(eventFor(new InvalidWatcher())));

        assertEquals("@MongoWatch method must have exactly one parameter: "
                + "void one.xis.mongodb.MongoWatchDispatcherTest$InvalidWatcher.changed()", exception.getMessage());
    }

    @Test
    void rejectsUnsupportedWatchParameter() {
        var dispatcher = new MongoWatchDispatcher(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> dispatcher.startWatchers(eventFor(new InvalidParameterWatcher())));

        assertEquals("@MongoWatch parameter must be MongoChangeEvent or @MongoDocument: "
                + "void one.xis.mongodb.MongoWatchDispatcherTest$InvalidParameterWatcher.changed(java.lang.String)",
                exception.getMessage());
    }

    static class InvalidWatcher {
        @MongoWatch(Customer.class)
        void changed() {
        }
    }

    static class InvalidParameterWatcher {
        @MongoWatch(Customer.class)
        void changed(String value) {
        }
    }

    @MongoDocument("customers")
    record Customer(String id) {
    }

    private AppContextInitializedEvent eventFor(Object... singletons) {
        return new AppContextInitializedEvent(new AppContext() {
            @Override
            public <T> T getSingleton(Class<T> type) {
                throw new UnsupportedOperationException();
            }

            @Override
            public <T> Optional<T> getOptionalSingleton(Class<T> type) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Collection<Object> getSingletons() {
                return List.of(singletons);
            }
        });
    }
}

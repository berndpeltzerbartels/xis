package one.xis.mongodb;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MongoWatchDispatcherTest {

    @Test
    void rejectsWatchMethodWithoutParameter() {
        var dispatcher = new MongoWatchDispatcher(null, List.of(new InvalidWatcher()));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, dispatcher::startWatchers);

        assertEquals("@MongoWatch method must have exactly one parameter: "
                + "void one.xis.mongodb.MongoWatchDispatcherTest$InvalidWatcher.changed()", exception.getMessage());
    }

    @Test
    void rejectsUnsupportedWatchParameter() {
        var dispatcher = new MongoWatchDispatcher(null, List.of(new InvalidParameterWatcher()));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, dispatcher::startWatchers);

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
}

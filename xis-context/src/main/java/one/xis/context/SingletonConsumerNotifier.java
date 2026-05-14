package one.xis.context;

import lombok.NonNull;

import java.util.List;

final class SingletonConsumerNotifier {

    private SingletonConsumerNotifier() {
    }

    static Object assignValueInConsumers(@NonNull Object value, List<SingletonConsumer> consumers) {
        assignValueInWrapperConsumers(value, consumers);
        Object exposedValue = exposedValue(value, consumers);
        assignValueInOtherConsumers(exposedValue, consumers);
        return exposedValue;
    }

    private static void assignValueInWrapperConsumers(Object value, List<SingletonConsumer> consumers) {
        for (var consumer : consumers) {
            if (consumer instanceof SingletonWrapper) {
                consumer.assignValueIfMatching(value);
            }
        }
    }

    private static void assignValueInOtherConsumers(Object value, List<SingletonConsumer> consumers) {
        for (var consumer : consumers) {
            if (!(consumer instanceof SingletonWrapper)) {
                consumer.assignValueIfMatching(value);
            }
        }
    }

    private static Object exposedValue(Object value, List<SingletonConsumer> consumers) {
        for (var consumer : consumers) {
            if (consumer instanceof SingletonWrapper wrapper && wrapper.getTarget() == value) {
                return wrapper.getBean();
            }
        }
        return value;
    }
}

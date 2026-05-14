package one.xis.mongodb;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import com.mongodb.client.model.changestream.FullDocument;
import lombok.RequiredArgsConstructor;
import one.xis.context.Component;
import one.xis.context.Init;
import org.bson.Document;

import java.lang.reflect.Method;
import java.util.Collection;

@Component
@RequiredArgsConstructor
class MongoWatchDispatcher {
    private final MongoDatabase database;
    private final Collection<Object> components;
    private final MongoMapper mapper = new MongoMapper();

    @Init
    void startWatchers() {
        for (Object component : components) {
            for (Method method : component.getClass().getDeclaredMethods()) {
                MongoWatch watch = method.getAnnotation(MongoWatch.class);
                if (watch != null) {
                    startWatcher(component, method, watch);
                }
            }
        }
    }

    private void startWatcher(Object component, Method method, MongoWatch watch) {
        validate(method);
        Thread thread = new Thread(() -> watch(component, method, watch), "xis-mongodb-watch-" + collectionName(watch));
        thread.setDaemon(true);
        thread.start();
    }

    private void watch(Object component, Method method, MongoWatch watch) {
        var collection = database.getCollection(collectionName(watch));
        try (var cursor = collection.watch().fullDocument(FullDocument.UPDATE_LOOKUP).iterator()) {
            while (cursor.hasNext()) {
                dispatch(component, method, watch, cursor.next());
            }
        }
    }

    private void dispatch(Object component, Method method, MongoWatch watch, ChangeStreamDocument<Document> change) {
        Document fullDocument = change.getFullDocument();
        Object document = fullDocument == null ? null : mapper.toObject(fullDocument, watch.value());
        @SuppressWarnings({"rawtypes", "unchecked"})
        Object argument = method.getParameterTypes()[0].equals(MongoChangeEvent.class)
                ? new MongoChangeEvent(watch.value(), collectionName(watch), change.getOperationType().getValue(), document)
                : document;
        try {
            method.setAccessible(true);
            method.invoke(component, argument);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Could not invoke @MongoWatch method " + method, e);
        }
    }

    private void validate(Method method) {
        if (method.getParameterCount() != 1) {
            throw new IllegalArgumentException("@MongoWatch method must have exactly one parameter: " + method);
        }
        Class<?> parameterType = method.getParameterTypes()[0];
        if (!parameterType.equals(MongoChangeEvent.class) && !parameterType.isAnnotationPresent(MongoDocument.class)) {
            throw new IllegalArgumentException("@MongoWatch parameter must be MongoChangeEvent or @MongoDocument: " + method);
        }
    }

    private String collectionName(MongoWatch watch) {
        if (!watch.collection().isBlank()) {
            return watch.collection();
        }
        MongoDocument annotation = watch.value().getAnnotation(MongoDocument.class);
        if (annotation == null) {
            throw new IllegalArgumentException("@MongoWatch type must be annotated with @MongoDocument: " + watch.value().getName());
        }
        return annotation.value();
    }
}

package one.xis.mongodb;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MongoRepositoryProxyFactoryTest {

    private FakeMongoCollection collection;
    private CustomerRepository repository;

    @BeforeEach
    void setUp() {
        collection = new FakeMongoCollection();
        collection.documents.add(new Document("_id", "c1").append("lastName", "Lovelace"));
        MongoDatabase database = proxy(MongoDatabase.class, (proxy, method, args) -> {
            if (method.getName().equals("getCollection")) {
                assertEquals("customers", args[0]);
                return collection.mongoCollection();
            }
            return defaultValue(method.getReturnType());
        });
        repository = new MongoRepositoryProxyFactory<CustomerRepository>(database).createProxy(CustomerRepository.class);
    }

    @Test
    void findsById() {
        var customer = repository.findById("c1");

        assertTrue(customer.isPresent());
        assertEquals("Lovelace", customer.get().lastName);
        assertEquals(new Document("_id", "c1"), collection.lastFilter);
    }

    @Test
    void findsAll() {
        List<Customer> customers = repository.findAll();

        assertEquals(1, customers.size());
        assertEquals("c1", customers.get(0).id);
    }

    @Test
    void savesWithUpsert() {
        var customer = new Customer("c2", "Hopper");

        Customer returned = repository.save(customer);

        assertSame(customer, returned);
        assertEquals(new Document("_id", "c2").append("lastName", "Hopper"), collection.replacement);
        assertTrue(collection.upsert);
    }

    @Test
    void deletesById() {
        assertTrue(repository.deleteById("c1"));

        assertEquals(new Document("_id", "c1"), collection.lastFilter);
    }

    @Test
    void executesJsonQuery() {
        repository.findByLastName("Lovelace");

        assertEquals(new Document("lastName", "Lovelace"), collection.lastFilter);
    }

    @MongoRepository
    interface CustomerRepository extends MongoCrudRepository<Customer, String> {
        @MongoQuery("{ lastName: ?0 }")
        List<Customer> findByLastName(String lastName);
    }

    @MongoDocument("customers")
    record Customer(String id, String lastName) {
    }

    private static class FakeMongoCollection {
        private final List<Document> documents = new ArrayList<>();
        private Document lastFilter;
        private Document replacement;
        private boolean upsert;

        MongoCollection<Document> mongoCollection() {
            return MongoRepositoryProxyFactoryTest.proxy(MongoCollection.class, (proxy, method, args) -> {
                switch (method.getName()) {
                    case "find" -> {
                        lastFilter = args == null || args.length == 0 ? null : (Document) args[0];
                        return findIterable();
                    }
                    case "replaceOne" -> {
                        lastFilter = (Document) args[0];
                        replacement = (Document) args[1];
                        upsert = args.length > 2 && args[2] instanceof ReplaceOptions options && options.isUpsert();
                        return UpdateResult.acknowledged(1, 1L, null);
                    }
                    case "deleteOne" -> {
                        lastFilter = (Document) args[0];
                        return DeleteResult.acknowledged(1);
                    }
                    case "countDocuments" -> {
                        return (long) documents.size();
                    }
                    default -> {
                        return defaultValue(method.getReturnType());
                    }
                }
            });
        }

        FindIterable<Document> findIterable() {
            return MongoRepositoryProxyFactoryTest.proxy(FindIterable.class, (proxy, method, args) -> switch (method.getName()) {
                case "first" -> documents.isEmpty() ? null : documents.get(0);
                case "iterator" -> documents.iterator();
                default -> defaultValue(method.getReturnType());
            });
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class[]{type}, handler);
    }

    private static Object defaultValue(Class<?> type) {
        if (type == boolean.class) return false;
        if (type == byte.class) return (byte) 0;
        if (type == short.class) return (short) 0;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0f;
        if (type == double.class) return 0d;
        if (type == char.class) return '\0';
        if (type == Iterator.class) return List.of().iterator();
        return null;
    }
}

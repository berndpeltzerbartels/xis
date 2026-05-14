package one.xis.mongodb;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class MongoDbSystemTest {
    @Container
    static final MongoDBContainer MONGODB = new MongoDBContainer("mongo:7.0");

    private static MongoDatabase database;
    private CustomerRepository repository;

    @BeforeAll
    static void createDatabase() {
        database = MongoClients.create(MONGODB.getReplicaSetUrl()).getDatabase("xis_system_tests");
    }

    @BeforeEach
    void setUp() {
        database.getCollection("customers").drop();
        repository = new MongoRepositoryProxyFactory<CustomerRepository>(database).createProxy(CustomerRepository.class);
        repository.save(new Customer("c1", "Ada", "Lovelace", CustomerType.PREMIUM, "not persisted"));
        repository.save(new Customer("c2", "Grace", "Hopper", CustomerType.DEFAULT, "not persisted"));
    }

    @Test
    void findsById() {
        var customer = repository.findById("c1").orElseThrow();

        assertThat(customer.lastName).isEqualTo("Lovelace");
        assertThat(customer.type).isEqualTo(CustomerType.PREMIUM);
    }

    @Test
    void findsAllDocuments() {
        List<Customer> customers = repository.findAll();

        assertThat(customers).extracting(customer -> customer.id).containsExactlyInAnyOrder("c1", "c2");
    }

    @Test
    void countsDocuments() {
        assertThat(repository.count()).isEqualTo(2);
    }

    @Test
    void savesWithUpsert() {
        repository.save(new Customer("c3", "Katherine", "Johnson", CustomerType.DEFAULT, "not persisted"));

        assertThat(repository.findById("c3").orElseThrow().lastName).isEqualTo("Johnson");
    }

    @Test
    void updatesExistingDocumentWithSave() {
        repository.save(new Customer("c1", "Augusta", "Lovelace", CustomerType.PREMIUM, "changed"));

        Customer customer = repository.findById("c1").orElseThrow();
        assertThat(customer.firstName).isEqualTo("Augusta");
        assertThat(customer.notStored).isNull();
    }

    @Test
    void deletesEntity() {
        assertThat(repository.delete(repository.findById("c1").orElseThrow())).isTrue();

        assertThat(repository.findById("c1")).isEmpty();
    }

    @Test
    void deletesById() {
        assertThat(repository.deleteById("c1")).isTrue();

        assertThat(repository.findById("c1")).isEmpty();
    }

    @Test
    void executesJsonQueryReturningList() {
        List<Customer> customers = repository.findByLastName("Lovelace");

        assertThat(customers).extracting(customer -> customer.id).containsExactly("c1");
    }

    @Test
    void executesJsonQueryReturningOptional() {
        var customer = repository.findOneByFirstName("Grace");

        assertThat(customer).isPresent();
        assertThat(customer.get().id).isEqualTo("c2");
    }

    @MongoRepository
    interface CustomerRepository extends MongoCrudRepository<Customer, String> {
        @MongoQuery("{ lastName: ?0 }")
        List<Customer> findByLastName(String lastName);

        @MongoQuery("{ first: ?0 }")
        Optional<Customer> findOneByFirstName(String firstName);
    }

    @MongoDocument("customers")
    static class Customer {
        @MongoId
        String id;

        @MongoField("first")
        String firstName;

        String lastName;
        CustomerType type;

        @MongoIgnore
        String notStored;

        Customer() {
        }

        Customer(String id, String firstName, String lastName, CustomerType type, String notStored) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
            this.type = type;
            this.notStored = notStored;
        }
    }

    enum CustomerType {
        DEFAULT,
        PREMIUM
    }
}

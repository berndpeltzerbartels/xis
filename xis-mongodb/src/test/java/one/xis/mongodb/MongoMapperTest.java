package one.xis.mongodb;

import org.bson.Document;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MongoMapperTest {

    private final MongoMapper mapper = new MongoMapper();

    @Test
    void mapsClassToDocument() {
        var customer = new Customer();
        customer.id = "c1";
        customer.firstName = "Ada";
        customer.lastName = "Lovelace";
        customer.internalNote = "ignored";

        Document document = mapper.toDocument(customer);

        assertEquals("c1", document.get("_id"));
        assertEquals("Ada", document.get("first_name"));
        assertEquals("Lovelace", document.get("lastName"));
        assertFalse(document.containsKey("internalNote"));
    }

    @Test
    void mapsDocumentToClass() {
        Document document = new Document("_id", "c1")
                .append("first_name", "Ada")
                .append("lastName", "Lovelace");

        Customer customer = mapper.toObject(document, Customer.class);

        assertEquals("c1", customer.id);
        assertEquals("Ada", customer.firstName);
        assertEquals("Lovelace", customer.lastName);
    }

    @Test
    void mapsRecord() {
        Document document = mapper.toDocument(new Product("p1", "Board"));

        assertEquals("p1", document.get("_id"));
        assertEquals("Board", document.get("name"));
        assertEquals(new Product("p1", "Board"), mapper.toObject(document, Product.class));
    }

    @MongoDocument("customers")
    static class Customer {
        String id;
        @MongoField("first_name")
        String firstName;
        String lastName;
        @MongoIgnore
        String internalNote;
    }

    @MongoDocument("products")
    record Product(String id, String name) {
    }
}

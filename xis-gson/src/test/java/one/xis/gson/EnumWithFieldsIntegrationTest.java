package one.xis.gson;

import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test verifying that enums with fields are correctly serialized
 * and accessible in HTML templates.
 */
class EnumWithFieldsIntegrationTest {

    private Gson gson;

    @BeforeEach
    void setUp() {
        gson = new GsonFactory().gson();
    }

    @Test
    void enumsWithFields_shouldBeSerializedAsObjectsAndAccessibleInTemplates() {
        // Given: A list of enums with displayName field
        TestStatus[] statuses = TestStatus.values();

        // When: Serialized to JSON
        String json = gson.toJson(statuses);

        // Then: Each enum should be an object with name and displayName
        assertTrue(json.contains("\"name\":\"ACTIVE\""), "Should contain ACTIVE name");
        assertTrue(json.contains("\"displayName\":\"Active\""), "Should contain Active displayName");
        assertTrue(json.contains("\"name\":\"PENDING\""), "Should contain PENDING name");
        assertTrue(json.contains("\"displayName\":\"Pending\""), "Should contain Pending displayName");
        assertTrue(json.contains("\"name\":\"CLOSED\""), "Should contain CLOSED name");
        assertTrue(json.contains("\"displayName\":\"Closed\""), "Should contain Closed displayName");
        
        // And: The JSON should be an array of objects, not strings
        assertFalse(json.equals("[\"ACTIVE\",\"PENDING\",\"CLOSED\"]"), "Should not be simple string array");
    }

    @Test
    void simpleEnums_shouldStillBeSerializedAsStrings() {
        // Given: Simple enums without additional fields
        SimpleStatus[] statuses = SimpleStatus.values();

        // When: Serialized to JSON
        String json = gson.toJson(statuses);

        // Then: Should be a simple string array
        assertEquals("[\"STATUS_A\",\"STATUS_B\",\"STATUS_C\"]", json);
    }

    // Test enums
    enum TestStatus {
        ACTIVE("Active"),
        PENDING("Pending"),
        CLOSED("Closed");

        private final String displayName;

        TestStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    enum SimpleStatus {
        STATUS_A,
        STATUS_B,
        STATUS_C
    }
}

package one.xis.gson;

import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EnumSerializationTest {

    private Gson gson;

    @BeforeEach
    void setUp() {
        gson = new GsonFactory().gson();
    }

    @Test
    void enumWithFields_shouldSerializeAsObject() {
        // Given: Enum with displayName field
        EnumWithFields enumValue = EnumWithFields.ACTIVE;

        // When
        String json = gson.toJson(enumValue);

        // Then: Should be an object with name and displayName
        assertTrue(json.contains("\"name\":\"ACTIVE\""));
        assertTrue(json.contains("\"displayName\":\"Active Status\""));
    }

    @Test
    void enumWithoutFields_shouldSerializeAsString() {
        // Given: Simple enum without additional fields
        SimpleEnum enumValue = SimpleEnum.VALUE_ONE;

        // When
        String json = gson.toJson(enumValue);

        // Then: Should be just a string
        assertEquals("\"VALUE_ONE\"", json);
    }

    @Test
    void listOfEnumsWithFields_shouldSerializeAsArrayOfObjects() {
        // Given
        List<EnumWithFields> enumList = Arrays.asList(EnumWithFields.values());

        // When
        String json = gson.toJson(enumList);

        // Then
        assertTrue(json.contains("\"name\":\"ACTIVE\""));
        assertTrue(json.contains("\"displayName\":\"Active Status\""));
        assertTrue(json.contains("\"name\":\"INACTIVE\""));
        assertTrue(json.contains("\"displayName\":\"Inactive Status\""));
    }

    @Test
    void listOfSimpleEnums_shouldSerializeAsArrayOfStrings() {
        // Given
        List<SimpleEnum> enumList = Arrays.asList(SimpleEnum.values());

        // When
        String json = gson.toJson(enumList);

        // Then
        assertEquals("[\"VALUE_ONE\",\"VALUE_TWO\",\"VALUE_THREE\"]", json);
    }

    @Test
    void enumWithMultipleGetters_shouldIncludeAllProperties() {
        // Given
        EnumWithMultipleGetters enumValue = EnumWithMultipleGetters.OPTION_A;

        // When
        String json = gson.toJson(enumValue);

        // Then
        assertTrue(json.contains("\"name\":\"OPTION_A\""));
        assertTrue(json.contains("\"displayName\":\"Option A\""));
        assertTrue(json.contains("\"code\":\"OPT_A\""));
        assertTrue(json.contains("\"priority\":1"));
    }

    @Test
    void enumWithIsGetter_shouldSerializeWithCorrectPropertyName() {
        // Given
        EnumWithBooleanGetter enumValue = EnumWithBooleanGetter.ENABLED;

        // When
        String json = gson.toJson(enumValue);

        // Then
        assertTrue(json.contains("\"name\":\"ENABLED\""));
        assertTrue(json.contains("\"active\":true"));
    }

    // Test Enums

    enum EnumWithFields {
        ACTIVE("Active Status"),
        INACTIVE("Inactive Status"),
        PENDING("Pending Status");

        private final String displayName;

        EnumWithFields(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    enum SimpleEnum {
        VALUE_ONE,
        VALUE_TWO,
        VALUE_THREE
    }

    enum EnumWithMultipleGetters {
        OPTION_A("Option A", "OPT_A", 1),
        OPTION_B("Option B", "OPT_B", 2),
        OPTION_C("Option C", "OPT_C", 3);

        private final String displayName;
        private final String code;
        private final int priority;

        EnumWithMultipleGetters(String displayName, String code, int priority) {
            this.displayName = displayName;
            this.code = code;
            this.priority = priority;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getCode() {
            return code;
        }

        public int getPriority() {
            return priority;
        }
    }

    enum EnumWithBooleanGetter {
        ENABLED(true),
        DISABLED(false);

        private final boolean active;

        EnumWithBooleanGetter(boolean active) {
            this.active = active;
        }

        public boolean isActive() {
            return active;
        }
    }
}

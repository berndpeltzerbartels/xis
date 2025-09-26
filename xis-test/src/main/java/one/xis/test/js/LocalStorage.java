package one.xis.test.js;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class LocalStorage {

    private final Map<String, String> items = new HashMap<>();
    private final Gson gson = new Gson();

    public void setItem(String name, String value) {
        items.put(name, value);
    }

    public String getItem(String name) {
        return items.get(name);
    }

    /**
     * Get the parsed value from localStorage, handling XIS JSON wrapper format.
     * LocalStorage values are stored as JSON objects like {"value": actualValue}.
     * This method extracts the actual value from the wrapper.
     *
     * @param name the key name
     * @return the unwrapped value, or null if not found or invalid JSON
     */
    public Object getParsedValue(String name) {
        String jsonString = getItem(name);
        if (jsonString == null) {
            return null;
        }

        try {
            JsonElement element = JsonParser.parseString(jsonString);
            if (element.isJsonObject()) {
                JsonObject jsonObject = element.getAsJsonObject();
                if (jsonObject.has("value")) {
                    JsonElement valueElement = jsonObject.get("value");
                    if (valueElement.isJsonPrimitive()) {
                        if (valueElement.getAsJsonPrimitive().isNumber()) {
                            // Try integer first, then double
                            try {
                                return valueElement.getAsInt();
                            } catch (NumberFormatException e) {
                                return valueElement.getAsDouble();
                            }
                        } else if (valueElement.getAsJsonPrimitive().isBoolean()) {
                            return valueElement.getAsBoolean();
                        } else if (valueElement.getAsJsonPrimitive().isString()) {
                            return valueElement.getAsString();
                        }
                    }
                    // Return the element as-is for complex objects
                    return valueElement;
                }
            }
        } catch (Exception e) {
            // If JSON parsing fails, return the raw string
            return jsonString;
        }

        return null;
    }

    /**
     * Get all localStorage items as a Map with parsed values.
     * This deserializes the JSON wrapper format used by XIS.
     *
     * @return Map of key->parsedValue pairs
     */
    public Map<String, Object> getAsMap() {
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, String> entry : items.entrySet()) {
            Object parsedValue = getParsedValue(entry.getKey());
            if (parsedValue != null) {
                result.put(entry.getKey(), parsedValue);
            }
        }
        return result;
    }

    /**
     * Get the parsed integer value from localStorage.
     * Convenience method for integer values.
     *
     * @param name the key name
     * @return the integer value, or null if not found or not an integer
     */
    public Integer getIntValue(String name) {
        Object value = getParsedValue(name);
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return null;
    }

    /**
     * Get the parsed string value from localStorage.
     * Convenience method for string values.
     *
     * @param name the key name
     * @return the string value, or null if not found
     */
    public String getStringValue(String name) {
        Object value = getParsedValue(name);
        return value != null ? value.toString() : null;
    }

    public void reset() {
        items.clear();
    }

}

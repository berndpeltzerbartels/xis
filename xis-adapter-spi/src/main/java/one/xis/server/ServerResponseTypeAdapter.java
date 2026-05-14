package one.xis.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

class ServerResponseTypeAdapter implements JsonSerializer<ServerResponse> {

    @Override
    public JsonElement serialize(ServerResponse response, Type type, JsonSerializationContext context) {
        return toJsonObject(response, context);
    }

    private JsonObject toJsonObject(ServerResponse response, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        for (Field field : ServerResponse.class.getDeclaredFields()) {
            if (Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            Object value = readField(field, response);
            JsonElement jsonValue = context.serialize(value);
            if (hasPayload(value, jsonValue)) {
                jsonObject.add(field.getName(), jsonValue);
            }
        }
        return jsonObject;
    }

    private Object readField(Field field, ServerResponse response) {
        try {
            field.setAccessible(true);
            return field.get(response);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Could not serialize ServerResponse field " + field.getName(), e);
        }
    }

    private boolean hasPayload(Object value, JsonElement jsonValue) {
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof Number number) {
            return number.doubleValue() != 0d;
        }
        if (value instanceof CharSequence text) {
            return !text.isEmpty();
        }
        if (value instanceof Collection<?> collection) {
            return !collection.isEmpty();
        }
        if (value instanceof Map<?, ?> map) {
            return !map.isEmpty();
        }
        if (value instanceof Enum<?> enumValue) {
            return !"NONE".equals(enumValue.name());
        }
        return hasJsonPayload(jsonValue);
    }

    private boolean hasJsonPayload(JsonElement element) {
        if (element == null || element instanceof JsonNull) {
            return false;
        }
        if (element.isJsonPrimitive()) {
            return true;
        }
        if (element.isJsonArray()) {
            return !element.getAsJsonArray().isEmpty();
        }
        if (element.isJsonObject()) {
            return element.getAsJsonObject().entrySet().stream()
                    .anyMatch(entry -> hasJsonPayload(entry.getValue()));
        }
        return true;
    }
}

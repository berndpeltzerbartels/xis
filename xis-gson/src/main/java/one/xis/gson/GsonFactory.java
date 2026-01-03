package one.xis.gson;

import com.google.gson.*;
import io.goodforgod.gson.configuration.GsonConfiguration;
import lombok.RequiredArgsConstructor;
import one.xis.context.Bean;
import one.xis.context.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class GsonFactory {

    @Bean // TODO Wrapper verwenden um Konflikte zu vermeiden
    public Gson gson() {
        return new GsonConfiguration().builder()
                .serializeNulls() // Enable null serialization for SessionStorage deletion behavior
                .registerTypeAdapter(Duration.class, new JsonSerializer<Duration>() {
                    @Override
                    public JsonElement serialize(Duration src, java.lang.reflect.Type typeOfSrc, JsonSerializationContext context) {
                        return new JsonPrimitive(src.toString());
                    }
                })
                .registerTypeAdapter(Duration.class, new JsonDeserializer<Duration>() {
                    @Override
                    public Duration deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                        return Duration.parse(json.getAsString());
                    }
                })
                .registerTypeAdapter(JsonMap.class, new JsonMapTypeAdapter())
                .registerTypeHierarchyAdapter(Enum.class, new JsonSerializer<Enum<?>>() {
                    @Override
                    public JsonElement serialize(Enum<?> src, Type typeOfSrc, JsonSerializationContext context) {
                        // Check if enum has additional fields (beyond name/ordinal)
                        if (hasAdditionalFields(src.getClass())) {
                            JsonObject obj = new JsonObject();
                            obj.addProperty("name", src.name());

                            // Serialize all public getters
                            for (Method method : src.getClass().getMethods()) {
                                if (isGetter(method) && !isEnumBuiltInMethod(method)) {
                                    try {
                                        String propertyName = getPropertyName(method);
                                        Object value = method.invoke(src);
                                        obj.add(propertyName, context.serialize(value));
                                    } catch (Exception e) {
                                        // Skip this property on error
                                    }
                                }
                            }
                            return obj;
                        } else {
                            // Simple enum without additional fields - return as string
                            return new JsonPrimitive(src.name());
                        }
                    }
                })
                .create();
    }

    /**
     * Checks if an enum class has additional fields beyond the standard enum fields (name, ordinal).
     */
    private boolean hasAdditionalFields(Class<?> enumClass) {
        return Arrays.stream(enumClass.getDeclaredFields())
                .anyMatch(field -> !field.isSynthetic() &&
                        !field.isEnumConstant() &&
                        !Modifier.isStatic(field.getModifiers()));
    }

    /**
     * Checks if a method is a getter (public, no parameters, non-void return, starts with get/is).
     */
    private boolean isGetter(Method method) {
        if (!Modifier.isPublic(method.getModifiers())) {
            return false;
        }
        if (method.getParameterCount() != 0) {
            return false;
        }
        if (method.getReturnType() == void.class) {
            return false;
        }
        String name = method.getName();
        return (name.startsWith("get") && name.length() > 3) ||
                (name.startsWith("is") && name.length() > 2);
    }

    /**
     * Checks if a method is a built-in enum method (name, ordinal, etc.).
     */
    private boolean isEnumBuiltInMethod(Method method) {
        String name = method.getName();
        return "name".equals(name) ||
                "ordinal".equals(name) ||
                "getDeclaringClass".equals(name) ||
                "getClass".equals(name);
    }

    /**
     * Extracts property name from getter method name.
     */
    private String getPropertyName(Method method) {
        String name = method.getName();
        if (name.startsWith("get")) {
            return Character.toLowerCase(name.charAt(3)) + name.substring(4);
        } else if (name.startsWith("is")) {
            return Character.toLowerCase(name.charAt(2)) + name.substring(3);
        }
        return name;
    }
}
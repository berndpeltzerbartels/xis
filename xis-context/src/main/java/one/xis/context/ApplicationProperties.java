package one.xis.context;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

class ApplicationProperties {

    private static final Map<String, String> PROPERTIES;

    static {
        Map<String, String> props = new HashMap<>();

        // Load base application.properties
        loadPropertiesFromResource("application.properties", props);

        // Load profile-specific properties (they override base properties)
        Set<String> activeProfiles = ActiveProfiles.getProfiles();
        for (String profile : activeProfiles) {
            loadPropertiesFromResource("application-" + profile + ".properties", props);
        }

        PROPERTIES = Collections.unmodifiableMap(props);
    }

    public static String getProperty(String key) {
        return PROPERTIES.get(key);
    }

    public static String getProperty(String key, String defaultValue) {
        return PROPERTIES.getOrDefault(key, defaultValue);
    }

    public static Map<String, String> getAllProperties() {
        return PROPERTIES;
    }

    private static void loadPropertiesFromResource(String resourceName, Map<String, String> targetMap) {
        try (InputStream input = ApplicationProperties.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (input != null) {
                Properties props = new Properties();
                props.load(input);
                for (String key : props.stringPropertyNames()) {
                    targetMap.put(key, props.getProperty(key));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load properties from " + resourceName, e);
        }
    }
}

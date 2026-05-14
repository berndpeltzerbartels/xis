package one.xis.distributed;

import lombok.extern.slf4j.Slf4j;
import one.xis.context.ApplicationProperties;
import one.xis.context.Component;
import one.xis.context.DefaultComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Default implementation of {@link XisDistributedConfig} that reads host
 * mappings from {@code application.properties} (or a profile-specific variant).
 * <p>
 * Property format:
 * <pre>
 * # Optional: explicit remote hosts per frontlet-id or normalised page path
 * xis.remote.frontlet.ProductFrontlet=https://shop.example.com
 * xis.remote.frontlet.CartFrontlet=https://shop.example.com
 * xis.remote.page./product/*.html=https://shop.example.com
 * xis.remote.origin.shell=https://app.example.com
 * </pre>
 * <p>
 * This bean is annotated {@link DefaultComponent}: if the application provides
 * its own {@link XisDistributedConfig} bean, this implementation is ignored.
 */
@Slf4j
@Component
@DefaultComponent
public class PropertiesXisDistributedConfig implements XisDistributedConfig {

    private static final String FRONTLET_PREFIX = "xis.remote.frontlet.";
    private static final String FRONTLET_URL_PREFIX = "xis.remote.frontlet-url.";
    private static final String PAGE_PREFIX = "xis.remote.page.";
    private static final String ORIGIN_PREFIX = "xis.remote.origin.";

    private final Map<String, String> frontletHosts = new HashMap<>();
    private final Map<String, String> frontletUrls = new HashMap<>();
    private final Map<String, String> pageHosts = new HashMap<>();
    private final Map<String, String> allowedOrigins = new HashMap<>();

    public PropertiesXisDistributedConfig() {
        var all = ApplicationProperties.getAllProperties();

        for (var entry : all.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (key.startsWith(FRONTLET_PREFIX)) {
                String frontletId = key.substring(FRONTLET_PREFIX.length());
                validateHostValue(key, value);
                frontletHosts.put(frontletId, value);
                log.debug("Distributed config: frontlet '{}' → {}", frontletId, value);
            } else if (key.startsWith(FRONTLET_URL_PREFIX)) {
                String frontletId = key.substring(FRONTLET_URL_PREFIX.length());
                validateHostValue(key, value);
                frontletUrls.put(frontletId, value);
                log.debug("Distributed config: frontlet-url '{}' → {}", frontletId, value);
            } else if (key.startsWith(PAGE_PREFIX)) {
                String normalizedPath = key.substring(PAGE_PREFIX.length());
                validateHostValue(key, value);
                pageHosts.put(normalizedPath, value);
                log.debug("Distributed config: page '{}' → {}", normalizedPath, value);
            } else if (key.startsWith(ORIGIN_PREFIX)) {
                String originId = key.substring(ORIGIN_PREFIX.length());
                validateHostValue(key, value);
                allowedOrigins.put(originId, value);
                log.debug("Distributed config: allowed origin '{}' → {}", originId, value);
            }
        }
    }

    private void validateHostValue(String key, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Distributed host mapping '" + key + "' must not be blank.");
        }
    }

    @Override
    public Map<String, String> getFrontletHosts() {
        return Map.copyOf(frontletHosts);
    }

    @Override
    public Map<String, String> getFrontletUrls() {
        return Map.copyOf(frontletUrls);
    }

    @Override
    public Map<String, String> getPageHosts() {
        return Map.copyOf(pageHosts);
    }

    @Override
    public Set<String> getAllowedOrigins() {
        return Stream.of(frontletHosts.values().stream(), pageHosts.values().stream(), allowedOrigins.values().stream())
                .flatMap(stream -> stream)
                .collect(Collectors.toUnmodifiableSet());
    }
}

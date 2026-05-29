package one.xis.distributed;

import lombok.extern.slf4j.Slf4j;
import one.xis.context.ApplicationProperties;
import one.xis.context.Component;
import one.xis.context.DefaultComponent;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link XisDistributedConfig} that reads distributed
 * hosts from {@code application.properties} (or a profile-specific variant).
 * <p>
 * Property format:
 * <pre>
 * xis.distributed.hosts=https://shop.example.com,https://catalog.example.com
 * xis.distributed.allowed-origins=https://app.example.com
 * </pre>
 * <p>
 * If {@code xis.distributed.allowed-origins} is omitted, the configured host list is
 * used for CORS as well.
 * <p>
 * This bean is annotated {@link DefaultComponent}: if the application provides
 * its own {@link XisDistributedConfig} bean, this implementation is ignored.
 */
@Slf4j
@Component
@DefaultComponent
public class PropertiesXisDistributedConfig implements XisDistributedConfig {

    private static final String HOSTS_PROPERTY = "xis.distributed.hosts";
    private static final String ALLOWED_ORIGINS_PROPERTY = "xis.distributed.allowed-origins";

    private final List<String> hosts;
    private final Set<String> allowedOrigins;

    public PropertiesXisDistributedConfig() {
        var value = ApplicationProperties.getProperty(HOSTS_PROPERTY);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing distributed host configuration '" + HOSTS_PROPERTY + "'.");
        }
        this.hosts = readOrigins(value);
        if (hosts.isEmpty()) {
            throw new IllegalStateException("Distributed host configuration '" + HOSTS_PROPERTY + "' must contain at least one host.");
        }
        var allowedOriginValue = ApplicationProperties.getProperty(ALLOWED_ORIGINS_PROPERTY);
        this.allowedOrigins = allowedOriginValue == null || allowedOriginValue.isBlank()
                ? Set.copyOf(hosts)
                : Set.copyOf(readOrigins(allowedOriginValue));
        if (allowedOrigins.isEmpty()) {
            throw new IllegalStateException("Distributed allowed origin configuration '" + ALLOWED_ORIGINS_PROPERTY + "' must contain at least one origin.");
        }
        log.debug("Distributed config: hosts {}, allowed origins {}", hosts, allowedOrigins);
    }

    @Override
    public List<String> getHosts() {
        return List.copyOf(hosts);
    }

    @Override
    public Set<String> getAllowedOrigins() {
        return Set.copyOf(allowedOrigins);
    }

    private List<String> readOrigins(String value) {
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(host -> !host.isEmpty())
                .map(this::normalizeHost)
                .distinct()
                .collect(Collectors.toList());
    }

    private String normalizeHost(String value) {
        URI uri = URI.create(value);
        String scheme = uri.getScheme();
        String host = uri.getHost();
        int port = uri.getPort();
        if (scheme == null || host == null) {
            throw new IllegalStateException("Distributed host must include scheme and host: " + value);
        }
        return scheme + "://" + host + (port >= 0 ? ":" + port : "");
    }
}

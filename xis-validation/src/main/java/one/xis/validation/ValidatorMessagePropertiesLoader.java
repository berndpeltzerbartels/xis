package one.xis.validation;


import one.xis.context.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is a result of my desperation when using ResourceBundle.loadProperties for validation messages.
 * It alway ignored the message.properties.
 */
@Component
class ValidatorMessagePropertiesLoader {

    private final Map<Locale, Properties> propertiesCache = new ConcurrentHashMap<>();
    private final ClassLoader classLoader;

    ValidatorMessagePropertiesLoader() {
        classLoader = Thread.currentThread().getContextClassLoader() != null
                ? Thread.currentThread().getContextClassLoader()
                : ValidatorMessagePropertiesLoader.class.getClassLoader();
    }

    public String getMessage(String messageKey, Locale locale) {
        Properties props = propertiesForLocaleCached(locale);
        String message = props.getProperty(messageKey);
        return message == null ? "[" + messageKey + "]" : message;
    }

    public Map<String, String> getMessages(Locale locale) {
        Properties props = propertiesForLocaleCached(locale);
        Map<String, String> messages = new LinkedHashMap<>();
        props.forEach((key, value) -> messages.put(String.valueOf(key), String.valueOf(value)));
        return messages;
    }

    private Properties propertiesForLocaleCached(Locale locale) {
        return propertiesCache.computeIfAbsent(locale == null ? Locale.ROOT : locale, this::propertiesForLocale);
    }

    private Properties propertiesForLocale(Locale locale) {
        Properties props = new Properties();
        props.putAll(loadProperties("default-messages"));
        props.putAll(loadProperties("messages"));
        props.putAll(loadProperties("default-messages_" + locale.getLanguage()));
        props.putAll(loadProperties("messages_" + locale.getLanguage()));
        return props;
    }

    private Properties loadProperties(String name) {
        Properties props = new Properties();
        try {
            Enumeration<URL> resources = classLoader.getResources(name + ".properties");
            while (resources.hasMoreElements()) {
                Properties resourceProps = new Properties();
                try (InputStream stream = resources.nextElement().openStream()) {
                    resourceProps.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
                }
                resourceProps.forEach(props::putIfAbsent);
            }
        } catch (IOException ignored) {
        }
        return props;
    }
}

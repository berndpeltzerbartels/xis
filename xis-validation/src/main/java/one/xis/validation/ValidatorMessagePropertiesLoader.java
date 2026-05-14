package one.xis.validation;


import one.xis.context.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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

    public String getMessage(String messageKey, Locale locale) {
        Properties props = propertiesCache.computeIfAbsent(locale, this::propertiesForLocale);
        String message = props.getProperty(messageKey);
        return message == null ? "[" + messageKey + "]" : message;
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
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(name + ".properties")) {
            if (stream != null) {
                props.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
            }
        } catch (IOException ignored) {
        }
        return props;
    }
}

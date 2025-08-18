package one.xis.validation;


import one.xis.context.XISComponent;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is a result of my desperation when using ResourceBundle.loadProperties for validation messages.
 * It alway ignored the message.properties.
 */
@XISComponent
class ValidatorMessagePropertiesLoader {

    private final Map<Locale, Properties> propertiesCache = new ConcurrentHashMap<>();

    public String getMessage(String messageKey, Locale locale) {
        Properties props = propertiesCache.computeIfAbsent(locale, this::propertiesForLocale);
        String message = props.getProperty(messageKey);
        return message == null ? "[" + messageKey + "]" : message;
    }

    private Properties propertiesForLocale(Locale locale) {
        Properties props = new Properties();

        // 1. Benutzerdefinierte Datei für das Land
        String userFile = "messages_" + locale.getLanguage();
        Properties userProps = loadProperties(userFile);

        if (!userProps.isEmpty()) {
            props.putAll(userProps);

            // 2. Default für das Land
            String defaultFile = "default-messages_" + locale.getLanguage();
            Properties defaultProps = loadProperties(defaultFile);

            // Fehlende Keys aus Default ergänzen
            defaultProps.forEach(props::putIfAbsent);
            return props;
        }

        // 3. Nur Default-Dateien (wenn keine User-Datei existiert)
        props.putAll(loadProperties("default-messages"));
        props.putAll(loadProperties("messages"));
        props.putAll(loadProperties("default-messages_" + locale.getLanguage()));
        return props;
    }

    private Properties loadProperties(String name) {
        Properties props = new Properties();
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(name + ".properties")) {
            if (stream != null) {
                props.load(stream);
            }
        } catch (IOException ignored) {
        }
        return props;
    }
}
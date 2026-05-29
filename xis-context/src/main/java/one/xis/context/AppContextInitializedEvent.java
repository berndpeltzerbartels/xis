package one.xis.context;

import lombok.Data;

/**
 * Event emitted after the application context has created and initialized its
 * singletons.
 */
@Data
public class AppContextInitializedEvent {
    private final AppContext appContext;
}

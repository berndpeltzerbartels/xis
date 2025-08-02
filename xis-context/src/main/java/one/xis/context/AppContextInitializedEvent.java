package one.xis.context;

import lombok.Data;

@Data
public class AppContextInitializedEvent {
    private final AppContext appContext;
}

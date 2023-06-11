package one.xis.context;

import lombok.Setter;

import java.util.Collection;

class AppContextWrapper implements AppContext {

    @Setter
    private AppContext appContext;

    @Override
    public <T> T getSingleton(Class<T> type) {
        return appContext.getSingleton(type);
    }

    @Override
    public Collection<Object> getSingletons() {
        return appContext.getSingletons();
    }
}

package one.xis.server;

import one.xis.context.XISComponent;

import java.util.Collection;
import java.util.HashSet;
import java.util.function.Consumer;


@XISComponent
class LocalUrlHolderImpl implements LocalUrlHolder {

    private String localUrl;
    private final Collection<Consumer<String>> urlAssignmentListeners = new HashSet<>();

    @Override
    public void setLocalUrl(String localUrl) {
        this.localUrl = localUrl;
        invokeUrlAssignmentListeners(localUrl);
    }

    @Override
    public String getUrl() {
        if (localUrl == null) {
            throw new IllegalStateException("Local URL is not set");
        }
        return localUrl;
    }

    @Override
    public boolean localUrlIsSet() {
        return localUrl != null;
    }

    @Override
    public void addUrlAssignmentListener(Consumer<String> listener) {
        urlAssignmentListeners.add(listener);
    }

    @Override
    public boolean isSecure() {
        return localUrl != null && localUrl.startsWith("https://");
    }

    private void invokeUrlAssignmentListeners(String url) {
        for (Consumer<String> listener : urlAssignmentListeners) {
            listener.accept(url);
        }
    }
}

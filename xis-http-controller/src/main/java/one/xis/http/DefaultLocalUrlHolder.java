package one.xis.http;

import lombok.RequiredArgsConstructor;
import one.xis.context.DefaultComponent;
import one.xis.context.EventEmitter;
import one.xis.server.LocalUrlAssignedEvent;
import one.xis.server.LocalUrlHolder;

@DefaultComponent
@RequiredArgsConstructor
public class DefaultLocalUrlHolder implements LocalUrlHolder {

    private final EventEmitter eventEmitter;
    private String localUrl;

    @Override
    public void setLocalUrl(String localUrl) {
        this.localUrl = localUrl;
        eventEmitter.emitEvent(new LocalUrlAssignedEvent(localUrl));
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
    public boolean isSecure() {
        return localUrl != null && localUrl.startsWith("https://");
    }
}

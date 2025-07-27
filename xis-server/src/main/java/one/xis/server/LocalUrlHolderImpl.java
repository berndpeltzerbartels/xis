package one.xis.server;

import lombok.RequiredArgsConstructor;
import one.xis.context.EventEmitter;
import one.xis.context.XISComponent;


@XISComponent
@RequiredArgsConstructor
class LocalUrlHolderImpl implements LocalUrlHolder {

    private String localUrl;
    private final EventEmitter eventEmitter;

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

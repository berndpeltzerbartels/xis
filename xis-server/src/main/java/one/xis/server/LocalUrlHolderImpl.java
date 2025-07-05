package one.xis.server;

import lombok.Setter;
import one.xis.context.XISComponent;


@Setter
@XISComponent
class LocalUrlHolderImpl implements LocalUrlHolder {

    private String localUrl;

    @Override
    public String getLocalUrl() {
        if (localUrl == null) {
            throw new IllegalStateException("Local URL is not set");
        }
        return localUrl;
    }
}

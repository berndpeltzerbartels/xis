package one.xis.server;

import lombok.Data;
import one.xis.context.XISComponent;

@Data
@XISComponent
class LocalUrlHolderImpl implements LocalUrlHolder {
    private String localUrl;
}

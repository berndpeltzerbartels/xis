package one.xis.ws.spring;

import one.xis.ImportInstances;

@ImportInstances
public interface SpringWSHandlerSPI {
    void setWSService(Object wsService);
}

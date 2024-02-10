package one.xis.server;

import java.util.Map;

record ControllerMethodResult(Object returnValue, Map<String, Object> modelData, Map<String, Throwable> errors) {
    
}

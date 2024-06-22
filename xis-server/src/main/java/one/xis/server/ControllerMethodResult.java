package one.xis.server;

import one.xis.validation.ValidationErrors;

import java.util.Map;

record ControllerMethodResult(Object returnValue, Map<String, Object> modelData, ValidationErrors errors) {

}

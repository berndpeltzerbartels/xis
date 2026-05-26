package one.xis.server;

import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import one.xis.*;
import one.xis.PathVariable;
import one.xis.auth.AuthenticationException;
import one.xis.deserialize.MainDeserializer;
import one.xis.deserialize.DeserializationContext;
import one.xis.deserialize.InvalidValueError;
import one.xis.deserialize.PostProcessingResults;
import one.xis.http.HttpRequest;
import one.xis.http.HttpResponse;
import one.xis.http.RequestContext;
import one.xis.utils.lang.ClassUtils;
import one.xis.utils.lang.FieldUtil;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.function.Supplier;

@RequiredArgsConstructor
class ControllerMethodParameter {
    private final Method method;
    private final Parameter parameter;
    private final MainDeserializer deserializer;
    private final int parameterIndex;
    private final int positionalParameterIndex;
    private final UploadConfiguration uploadConfiguration;

    // TODO Validation: only one of these annotation in parameter
    Object prepareParameter(ClientRequest request, PostProcessingResults postProcessingResults, Map<String, Object> requestScope) throws Exception {
        var userContext = UserContextImpl.getInstance();
        if (parameter.getType().equals(HttpRequest.class)) {
            return RequestContext.getInstance().getRequest();
        } else if (parameter.getType().equals(HttpResponse.class)) {
            return RequestContext.getInstance().getResponse();
        } else if (parameter.getType().equals(RequestContext.class)) {
            return RequestContext.getInstance();
        } else if (parameter.isAnnotationPresent(FormData.class)) {
            return deserializeFormDataParameter(parameter, request, postProcessingResults);
        } else if (parameter.isAnnotationPresent(Upload.class)) {
            return uploadParameter(parameter, postProcessingResults);
        } else if (parameter.isAnnotationPresent(UserId.class)) {
            checkAuthenticated();
            return validateAndRetrieve(userContext::getUserId, "UserId expected, but it was null");
        } else if (parameter.isAnnotationPresent(ClientId.class)) {
            return validateAndRetrieve(request::getClientId, "ClientId expected, but it was null");
        } else if (parameter.isAnnotationPresent(QueryParameter.class)) {
            return deserializeUrlParameter(parameter, request, postProcessingResults);
        } else if (parameter.isAnnotationPresent(one.xis.PathVariable.class)) {
            return deserializePathVariable(parameter, request, postProcessingResults);
        } else if (parameter.isAnnotationPresent(ActionParameter.class)) {
            return deserializeActionParameter(parameter, request, postProcessingResults);
        } else if (parameter.isAnnotationPresent(FrontletParameter.class)) {
            return deserializeComponentParameter(parameter, request, postProcessingResults, parameter.getAnnotation(FrontletParameter.class).value(), request.getFrontletParameters(), "frontlet");
        } else if (parameter.isAnnotationPresent(ModalParameter.class)) {
            return deserializeComponentParameter(parameter, request, postProcessingResults, parameter.getAnnotation(ModalParameter.class).value(), request.getModalParameters(), "modal");
        } else if (parameter.isAnnotationPresent(SharedValue.class)) {
            var key = parameter.getAnnotation(SharedValue.class).value();
            var sharedValue = requestScope.get(key);
            if (sharedValue == null) {
                if (isMandatory(parameter)) {
                    var defaultValue = createDefault(parameter);
                    requestScope.put(key, defaultValue);
                    return defaultValue;
                } else {
                    return null;
                }
            }
            return sharedValue;
        } else if (parameter.isAnnotationPresent(SessionStorage.class)) {
            var key = parameter.getAnnotation(SessionStorage.class).value();
            return storageParameter(StorageParameterScope.SESSION, key, request.getSessionStorageData(), request, postProcessingResults, requestScope);
        } else if (parameter.isAnnotationPresent(LocalStorage.class)) {
            var key = parameter.getAnnotation(LocalStorage.class).value();
            return storageParameter(StorageParameterScope.LOCAL, key, request.getLocalStorageData(), request, postProcessingResults, requestScope);
        } else if (parameter.isAnnotationPresent(ClientState.class)) {
            var key = parameter.getAnnotation(ClientState.class).value();
            return storageParameter(StorageParameterScope.CLIENT_STATE, key, request.getClientStateData(), request, postProcessingResults, requestScope);
        } else if (UserContext.class.isAssignableFrom(parameter.getType())) {
            return UserContext.getInstance();
        } else if (method.isAnnotationPresent(Action.class)
                && positionalParameterIndex >= 0
                && request.getActionParameters().containsKey("$" + positionalParameterIndex)) {
            var paramValue = request.getActionParameters().get("$" + positionalParameterIndex);
            return deserializeParameter(paramValue, request, parameter, postProcessingResults);
        } else {
            throw new IllegalStateException(method + ": illegal parameter=" + parameter);
        }
    }

    void addParameterValueToResult(ControllerMethodResult controllerMethodResult, Object parameterValue, ClientRequest request) {
        if (parameter.isAnnotationPresent(ActionParameter.class)) {
            addActionParameterValueToResult(controllerMethodResult, parameterValue);
        } else if (parameter.isAnnotationPresent(FrontletParameter.class)) {
            addFrontletParameterValueToResult(controllerMethodResult, parameterValue, parameter.getAnnotation(FrontletParameter.class).value());
        } else if (parameter.isAnnotationPresent(ModalParameter.class)) {
            addModalParameterValueToResult(controllerMethodResult, parameterValue, parameter.getAnnotation(ModalParameter.class).value());
        } else if (parameter.isAnnotationPresent(FormData.class)) {
            controllerMethodResult.getFormData().put(parameter.getAnnotation(FormData.class).value(), parameterValue);
        } else if (parameter.isAnnotationPresent(QueryParameter.class)) {
            controllerMethodResult.getUrlParameters().put(parameter.getAnnotation(QueryParameter.class).value(), parameterValue);
        } else if (parameter.isAnnotationPresent(PathVariable.class)) {
            controllerMethodResult.getPathVariables().put(parameter.getAnnotation(PathVariable.class).value(), parameterValue);
        } else if (parameter.isAnnotationPresent(SessionStorage.class)) {
            controllerMethodResult.getSessionStorage().put(parameter.getAnnotation(SessionStorage.class).value(), parameterValue);
        } else if (parameter.isAnnotationPresent(LocalStorage.class)) {
            controllerMethodResult.getLocalStorage().put(parameter.getAnnotation(LocalStorage.class).value(), parameterValue);
        } else if (parameter.isAnnotationPresent(ClientState.class)) {
            controllerMethodResult.getClientState().put(parameter.getAnnotation(ClientState.class).value(), parameterValue);
        }
    }

    private void addActionParameterValueToResult(ControllerMethodResult controllerMethodResult, Object parameterValue) {
        var key = parameter.getAnnotation(ActionParameter.class).value();
        if (!key.isEmpty()) {
            controllerMethodResult.getModelData().put(key, parameterValue);
        }
    }

    private void addFrontletParameterValueToResult(ControllerMethodResult controllerMethodResult, Object parameterValue, String key) {
        if (key.isEmpty() && parameterValue instanceof Map<?, ?> map) {
            map.forEach((name, value) -> controllerMethodResult.getFrontletParameters().put(String.valueOf(name), value));
        } else if (!key.isEmpty()) {
            controllerMethodResult.getFrontletParameters().put(key, parameterValue);
        }
    }

    private void addModalParameterValueToResult(ControllerMethodResult controllerMethodResult, Object parameterValue, String key) {
        if (key.isEmpty() && parameterValue instanceof Map<?, ?> map) {
            map.forEach((name, value) -> controllerMethodResult.getModalParameters().put(String.valueOf(name), value));
        } else if (!key.isEmpty()) {
            controllerMethodResult.getModalParameters().put(key, parameterValue);
        }
    }

    private String actionParameterKey() {
        var actionParameter = parameter.getAnnotation(ActionParameter.class);
        if (!actionParameter.value().isEmpty()) {
            return actionParameter.value();
        }
        if (actionParameter.index() == 0) {
            throw new IllegalStateException(method + ": @ActionParameter index is 1-based; use index=1 for the first action argument");
        }
        if (actionParameter.index() < 0) {
            throw new IllegalStateException(method + ": @ActionParameter must define value or index");
        }
        var index = actionParameter.index() - 1;
        if (index < 0) {
            throw new IllegalStateException(method + ": positional @ActionParameter cannot be resolved for " + parameter);
        }
        return "$" + index;
    }

    private void checkAuthenticated() {
        if (!UserContextImpl.getInstance().isAuthenticated()) {
            throw new AuthenticationException("User is not authenticated");
        }
    }

    private Object validateAndRetrieve(Supplier<Object> valueSupplier, String exceptionMessage) {
        var value = valueSupplier.get();
        if (value == null) {
            throw new IllegalStateException(exceptionMessage);
        }
        return value;
    }

    private Object deserializeFormDataParameter(Parameter parameter, ClientRequest request, PostProcessingResults postProcessingResults) throws IOException {
        var key = parameter.getAnnotation(FormData.class).value();
        var paramValue = request.getFormData().get(key);
        if (!isMandatory(parameter) && paramValue == null) {
            return null;
        }
        Object formData = deserializeParameter(paramValue, request, parameter, postProcessingResults);
        injectUploadedFiles(formData, postProcessingResults, key);
        return formData;
    }

    private Object uploadParameter(Parameter parameter, PostProcessingResults postProcessingResults) {
        Upload upload = parameter.getAnnotation(Upload.class);
        String fieldName = uploadFieldName(upload, parameter.getName());
        List<UploadedFile> files = RequestContext.getInstance().getRequest().getUploadedFiles(fieldName);
        validateUploadSize(upload, files, parameter, "/" + fieldName, postProcessingResults);
        return convertUploadValue(parameter.getType(), files);
    }

    private void injectUploadedFiles(Object formData, PostProcessingResults postProcessingResults, String formKey) {
        if (formData == null) {
            return;
        }
        for (Field field : FieldUtil.getAllFields(formData.getClass())) {
            if (!field.isAnnotationPresent(Upload.class)) {
                continue;
            }
            Upload upload = field.getAnnotation(Upload.class);
            String fieldName = uploadFieldName(upload, field.getName());
            List<UploadedFile> files = RequestContext.getInstance().getRequest().getUploadedFiles(fieldName);
            validateUploadSize(upload, files, field, "/" + formKey + "/" + field.getName(), postProcessingResults);
            FieldUtil.setFieldValue(formData, field, convertUploadValue(field.getType(), files));
        }
    }

    private String uploadFieldName(Upload upload, String fallback) {
        if (upload.value() != null && !upload.value().isBlank()) {
            return upload.value();
        }
        return fallback;
    }

    private void validateUploadSize(Upload upload, List<UploadedFile> files, java.lang.reflect.AnnotatedElement target, String path, PostProcessingResults postProcessingResults) {
        long maxSize = upload.maxSize() >= 0 ? upload.maxSize() : uploadConfiguration.getMaxFileSize();
        for (UploadedFile file : files) {
            if (file.getSize() > maxSize) {
                postProcessingResults.add(uploadTooLargeError(target, path, file, maxSize));
            }
        }
    }

    private InvalidValueError uploadTooLargeError(java.lang.reflect.AnnotatedElement target, String path, UploadedFile file, long maxSize) {
        var error = new InvalidValueError(
                new DeserializationContext(path, target, Upload.class, UserContext.getInstance()),
                "validation.upload.max-size",
                "validation.upload.max-size.global",
                file.getFileName()
        );
        error.addMessageParameter("fileName", file.getFileName());
        error.addMessageParameter("maxSize", maxSize);
        error.addMessageParameter("size", file.getSize());
        return error;
    }

    private Object convertUploadValue(Class<?> targetType, List<UploadedFile> files) {
        if (Collection.class.isAssignableFrom(targetType)) {
            return files;
        }
        UploadedFile file = files.isEmpty() ? null : files.get(0);
        if (UploadedFile.class.equals(targetType)) {
            return file;
        }
        if (byte[].class.equals(targetType)) {
            return file == null ? null : file.getBytes();
        }
        if (String.class.equals(targetType)) {
            return file == null ? null : file.getUtf8Text();
        }
        throw new IllegalArgumentException("@Upload supports only UploadedFile, List<UploadedFile>, byte[], and String. Unsupported type: " + targetType.getName());
    }

    private Object deserializeUrlParameter(Parameter parameter, ClientRequest request, PostProcessingResults postProcessingResults) throws IOException {
        var key = parameter.getAnnotation(QueryParameter.class).value();
        var paramValue = request.getUrlParameters().get(key);
        if (!isMandatory(parameter) && paramValue == null) {
            return null;
        }
        var deserialized = deserializeParameter(paramValue, request, parameter, postProcessingResults);
        if (deserialized instanceof String str) {
            try {
                return URLDecoder.decode(str, StandardCharsets.UTF_8);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to decode URL parameter '" + key + "' with value '" + str + "'", e);
            }
        } else {
            return deserialized;
        }
    }

    private Object deserializePathVariable(Parameter parameter, ClientRequest request, PostProcessingResults postProcessingResults) throws IOException {
        var key = parameter.getAnnotation(PathVariable.class).value();
        if (isMandatory(parameter) && !request.getPathVariables().containsKey(key)) {
            throw new IllegalStateException("No path variable found for key " + key);
        }
        var paramValue = request.getPathVariables().get(key);
        return deserializeParameter(paramValue, request, parameter, postProcessingResults);
    }

    private Object storageParameter(StorageParameterScope storageScope,
                                    String key,
                                    Map<String, String> storageData,
                                    ClientRequest request,
                                    PostProcessingResults postProcessingResults,
                                    Map<String, Object> requestScope) throws IOException {
        var storageParameters = storageParameterValues(requestScope);
        var existingValue = storageParameters.value(storageScope, key);
        if (existingValue.isPresent()) {
            return existingValue.get();
        }
        var paramValue = storageData.get(key);
        if (paramValue == null) {
            if (!isMandatory(parameter)) {
                return null;
            }
            var defaultValue = createDefault(parameter);
            storageParameters.put(storageScope, key, defaultValue);
            return defaultValue;
        }
        var value = deserializeParameter(paramValue, request, parameter, postProcessingResults);
        storageParameters.put(storageScope, key, value);
        return value;
    }

    private StorageParameterValues storageParameterValues(Map<String, Object> requestScope) {
        return (StorageParameterValues) requestScope.computeIfAbsent(StorageParameterValues.REQUEST_SCOPE_KEY,
                ignored -> new StorageParameterValues());
    }

    private Object deserializeActionParameter(Parameter parameter, ClientRequest request, PostProcessingResults postProcessingResults) throws IOException {
        var key = actionParameterKey();
        if (isMandatory(parameter) && !request.getActionParameters().containsKey(key)) {
            throw new IllegalStateException("No action parameter found for key " + key);
        }
        var paramValue = request.getActionParameters().get(key);
        return deserializeParameter(paramValue, request, parameter, postProcessingResults);
    }

    private Object deserializeComponentParameter(Parameter parameter, ClientRequest request, PostProcessingResults postProcessingResults, String key, Map<String, String> parameters, String source) throws IOException {
        if (key.isEmpty() && Map.class.isAssignableFrom(parameter.getType())) {
            return deserializeParameterMap(parameters);
        }
        if (isMandatory(parameter) && !parameters.containsKey(key)) {
            throw new IllegalStateException("No " + source + " parameter found for key " + key);
        }
        var paramValue = parameters.get(key);
        return deserializeParameter(paramValue, request, parameter, postProcessingResults);
    }

    private Map<String, String> deserializeParameterMap(Map<String, String> parameters) {
        var result = new LinkedHashMap<String, String>();
        parameters.forEach((key, value) -> result.put(key, deserializeParameterMapValue(value)));
        return result;
    }

    private String deserializeParameterMapValue(String value) {
        if (value == null) {
            return null;
        }
        try {
            var element = JsonParser.parseString(value);
            if (element.isJsonPrimitive()) {
                return element.getAsString();
            }
            return element.toString();
        } catch (Exception e) {
            return value;
        }
    }

    private Object deserializeParameter(String jsonValue, ClientRequest request, Parameter parameter, PostProcessingResults postProcessingResults) throws IOException {
        if (!isMandatory(parameter) && jsonValue == null) {
            return null;
        }
        var userContext = UserContextImpl.getInstance();
        return deserializer.deserialize(jsonValue, parameter, userContext, postProcessingResults);
    }

    private boolean isMandatory(Parameter parameter) {
        return !parameter.isAnnotationPresent(NullAllowed.class);
    }


    private Object createDefault(Parameter parameter) {
        try {
            return ClassUtils.newInstance(parameter.getType());
        } catch (Exception e) {
            throw new IllegalStateException("Cannot create default instance of type " + parameter.getType());
        }
    }

}

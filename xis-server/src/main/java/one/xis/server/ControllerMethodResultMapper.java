package one.xis.server;

import com.google.gson.JsonParser;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import one.xis.*;
import one.xis.context.Component;
import one.xis.context.Inject;
import one.xis.deserialize.PostProcessingResult;
import one.xis.utils.lang.MethodUtils;
import one.xis.utils.lang.StringUtils;
import one.xis.validation.ValidatorMessageResolver;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptyMap;

@Component
@RequiredArgsConstructor
class ControllerMethodResultMapper {

    private final ValidatorMessageResolver validatorMessageResolver;
    private final PathResolver pathResolver;

    @Inject
    private PageControllerWrappers pageControllerWrappers;

    @Inject
    private FrontletControllerWrappers frontletControllerWrappers;

    @Inject
    private ComponentHostResolver hostResolver;

    void mapReturnValueToResult(ControllerMethodResult controllerMethodResult, Method method, Object returnValue, Map<String, Object> requestScope) {
        var originalReturnValue = returnValue;
        if (returnValue instanceof String str && isStringNavigationMethod(method)) {
            var match = pageControllerWrappers.findByRealPath(str);
            if (match.isPresent()) {
                var pageResponse = new PageResponse(match.get().getPageControllerWrapper().getControllerClass());
                pageResponse.getPathVariables().putAll(match.get().getPathVariables());
                pageResponse.getQueryParameters().putAll(match.get().getQueryParameters());
                returnValue = pageResponse;
            } else {
                returnValue = new PageUrlResponse(str);
            }
        }
        if (returnValue instanceof PageResponse pageResponse) {
            mapPageResponse(pageResponse, controllerMethodResult);
        } else if (returnValue instanceof PageUrlResponse pageUrlResponse) {
            controllerMethodResult.setRedirectUrl(pageUrlResponse.getUrl());
        } else if (returnValue instanceof FrontletResponse frontletResponse && frontletResponse.getControllerClass() == null) {
            mapFrontletResponse(frontletResponse, controllerMethodResult);
        } else if (returnValue instanceof FrontletResponse frontletResponse) {
            mapFrontletResponse(frontletResponse, controllerMethodResult);
        } else if (returnValue instanceof ModalResponse modalResponse) {
            mapModalResponse(modalResponse, controllerMethodResult);
        } else if (returnValue instanceof Class<?> controllerClass) {
            updateController(controllerMethodResult, controllerClass, emptyMap());
        } else if (returnValue instanceof RedirectControllerResponse redirectControllerResponse) {
            controllerMethodResult.setRedirectUrl(redirectControllerResponse.getRedirectUrl());
        }
        if (method.isAnnotationPresent(ModelData.class)) {
            mapModelResult(getModelDataKey(method), originalReturnValue, controllerMethodResult);
        }
        if (method.isAnnotationPresent(FormData.class)) {
            mapFormData(getFormDataKey(method), originalReturnValue, controllerMethodResult);
        }
        if (method.isAnnotationPresent(SharedValue.class)) {
            var key = method.getAnnotation(SharedValue.class).value();
            requestScope.put(key, originalReturnValue);
        }
        if (method.isAnnotationPresent(SessionStorage.class)) {
            var key = method.getAnnotation(SessionStorage.class).value();
            controllerMethodResult.getSessionStorage().put(key, originalReturnValue);
        }
        if (method.isAnnotationPresent(LocalStorage.class)) {
            var key = method.getAnnotation(LocalStorage.class).value();
            controllerMethodResult.getLocalStorage().put(key, originalReturnValue);
        }
        if (method.isAnnotationPresent(ClientStorage.class)) {
            var key = method.getAnnotation(ClientStorage.class).value();
            controllerMethodResult.getClientStorage().put(key, originalReturnValue);
        }
        if (method.isAnnotationPresent(Title.class)) {
            controllerMethodResult.setAnnotatedTitle(originalReturnValue != null ? originalReturnValue.toString() : "");
        }

    }

    private boolean isStringNavigationMethod(Method method) {
        if (!method.getReturnType().equals(String.class)) {
            return false;
        }
        if (method.getDeclaringClass().isAnnotationPresent(Router.class)) {
            return true;
        }
        return method.isAnnotationPresent(Action.class) && !isDataProvidingMethod(method);
    }

    private boolean isDataProvidingMethod(Method method) {
        return method.isAnnotationPresent(ModelData.class)
                || method.isAnnotationPresent(FormData.class)
                || method.isAnnotationPresent(SharedValue.class)
                || method.isAnnotationPresent(SessionStorage.class)
                || method.isAnnotationPresent(LocalStorage.class)
                || method.isAnnotationPresent(ClientStorage.class)
                || method.isAnnotationPresent(Title.class);
    }

    void mapMethodParameterToResultAfterInvocation(ControllerMethodResult controllerMethodResult, ControllerMethodParameter[] parameters, Object[] args, ClientRequest request) {
        for (var i = 0; i < parameters.length; i++) {
            parameters[i].addParameterValueToResult(controllerMethodResult, args[i], request);
        }
    }

    void mapRequestToResult(ClientRequest request, ControllerMethodResult controllerMethodResult) {
        // Do not map frontletId or pageURL here !
        controllerMethodResult.setFrontletContainerId(request.getFrontletContainerId());
        controllerMethodResult.getFrontletParameters().putAll(parameterMap(request.getFrontletParameters()));
        controllerMethodResult.getPathVariables().putAll(castStringMap(request.getPathVariables()));
        controllerMethodResult.getUrlParameters().putAll(castStringMap(request.getUrlParameters()));
    }

    void mapValidationErrors(ControllerMethodResult controllerMethodResult, Collection<PostProcessingResult> errors) {
        controllerMethodResult.getValidatorMessages().getGlobalMessages().addAll(mapGlobalErrors(errors));
        controllerMethodResult.getValidatorMessages().getMessages().putAll(mapErrors(errors));
        controllerMethodResult.setValidationFailed(true);
    }

    private void mapModelResult(String key, Object value, ControllerMethodResult controllerMethodResult) {
        controllerMethodResult.getModelData().put(key, value);
    }

    private void mapFormData(String key, Object value, ControllerMethodResult controllerMethodResult) {
        controllerMethodResult.getFormData().put(key, value);
    }

    private void mapFrontletResponse(FrontletResponse frontletResponse, ControllerMethodResult result) {
        if (frontletResponse.getControllerClass() != null) {
            updateController(result, frontletResponse.getControllerClass(), frontletResponse.getPathVariables());
        }
        if (frontletResponse.getFrontlet() != null) {
            updateFrontlet(result, frontletResponse.getFrontlet());
        }
        if (frontletResponse.getPathVariables() != null) {
            result.getPathVariables().putAll(frontletResponse.getPathVariables());
        }
        if (frontletResponse.getTargetContainer() != null) {
            result.setFrontletContainerId(frontletResponse.getTargetContainer());
        }
        if (frontletResponse.getFrontletsToReload() != null) {
            result.getFrontletsToReload().addAll(frontletResponse.getFrontletsToReload());
        }

        if (frontletResponse.getFrontletParameters() != null) {
            result.getFrontletParameters().putAll(frontletResponse.getFrontletParameters());
        }

    }

    private void updateFrontlet(@NonNull ControllerMethodResult result, @NonNull String frontlet) {
        result.getFrontletParameters().putAll(queryParameters(frontlet));
        var target = stripQuery(frontlet);
        if (target.startsWith("/")) {
            var localMatch = frontletControllerWrappers.findFrontletByRealPath(target);
            if (localMatch.isPresent()) {
                var wrapper = localMatch.get().frontletControllerWrapper();
                result.setNextFrontletId(wrapper.getId());
                result.getPathVariables().putAll(localMatch.get().pathVariables());
                setFrontletAnnotationMetadata(result, wrapper.getControllerClass());
                return;
            }
            var remoteFrontletId = frontletIdForRemoteUrl(target);
            if (remoteFrontletId.isPresent()) {
                result.setNextFrontletId(remoteFrontletId.get());
                return;
            }
        }
        result.setNextFrontletId(target);
    }

    private void mapPageResponse(PageResponse pageResponse, ControllerMethodResult controllerMethodResult) {
        if (pageResponse.getPathVariables() != null) {
            controllerMethodResult.getPathVariables().putAll(pageResponse.getPathVariables());
        }
        if (pageResponse.getQueryParameters() != null) {
            controllerMethodResult.getUrlParameters().putAll(pageResponse.getQueryParameters());
        }
        if (pageResponse.getControllerClass() != null) {
            updateController(controllerMethodResult,
                    pageResponse.getControllerClass(),
                    controllerMethodResult.getPathVariables(),
                    controllerMethodResult.getUrlParameters());
        }
    }

    private void mapModalResponse(ModalResponse modalResponse, ControllerMethodResult result) {
        if (modalResponse.getControllerClass() != null) {
            updateModal(result, modalResponse.getControllerClass(), modalResponse.getPathVariables());
        }
        if (modalResponse.getModal() != null) {
            updateModal(result, modalResponse.getModal());
        }
        if (modalResponse.getPathVariables() != null) {
            result.getPathVariables().putAll(modalResponse.getPathVariables());
        }
        if (modalResponse.getParameters() != null) {
            result.getFrontletParameters().putAll(modalResponse.getParameters());
        }
        if (modalResponse.isClose()) {
            result.setCloseModal(true);
        }
        if (modalResponse.isReloadParent()) {
            result.setReloadModalParent(true);
        }
        result.setActionProcessing(ActionProcessing.MODAL);
    }

    private void updateModal(@NonNull ControllerMethodResult result, @NonNull Class<?> controllerClass, Map<String, Object> pathVariables) {
        if (!controllerClass.isAnnotationPresent(Modal.class)) {
            throw new IllegalStateException("not a modal-controller:" + controllerClass);
        }
        result.setNextModalId(ModalUtil.getId(controllerClass));
        var modalUrl = ModalUtil.getUrl(controllerClass);
        if (!modalUrl.isEmpty()) {
            result.setAnnotatedAddress(modalUrl);
        }
        var title = ModalUtil.getTitle(controllerClass);
        if (!title.isEmpty()) {
            result.setAnnotatedTitle(title);
        }
        if (pathVariables != null) {
            result.getPathVariables().putAll(pathVariables);
        }
    }

    private void updateModal(@NonNull ControllerMethodResult result, @NonNull String modal) {
        result.getFrontletParameters().putAll(queryParameters(modal));
        var target = stripQuery(modal);
        if (target.startsWith("/")) {
            var localMatch = frontletControllerWrappers.findFrontletByRealPath(target);
            if (localMatch.isPresent() && localMatch.get().frontletControllerWrapper().isModalController()) {
                var wrapper = localMatch.get().frontletControllerWrapper();
                result.setNextModalId(wrapper.getId());
                result.getPathVariables().putAll(localMatch.get().pathVariables());
                return;
            }
        }
        result.setNextModalId(target);
    }

    private void updateController(@NonNull ControllerMethodResult result, @NonNull Class<?> controllerClass, Map<String, Object> pathVariables) {
        updateController(result, controllerClass, pathVariables, emptyMap());
    }

    private void updateController(@NonNull ControllerMethodResult result,
                                  @NonNull Class<?> controllerClass,
                                  Map<String, Object> pathVariables,
                                  Map<String, Object> queryParameters) {
        if (controllerClass.isAnnotationPresent(Modal.class)) {
            updateModal(result, controllerClass, pathVariables);
        } else if (controllerClass.isAnnotationPresent(Frontlet.class)) {
            result.setNextFrontletId(FrontletUtil.getId(controllerClass));
            setFrontletAnnotationMetadata(result, controllerClass);
        } else if (controllerClass.isAnnotationPresent(Page.class)) {
            var realPath = pathResolver.createPath(PageUtil.getUrl(controllerClass));
            var pathString = pathResolver.evaluateRealPath(realPath, pathVariables, queryParameters);
            result.setNextURL(pathString);
            result.setNextPageId(realPath.normalized());
        } else {
            throw new IllegalStateException("not a frontlet-controller or page-controller:" + controllerClass);
        }
    }

    private void setFrontletAnnotationMetadata(@NonNull ControllerMethodResult result, @NonNull Class<?> controllerClass) {
        var url = FrontletUtil.getUrl(controllerClass);
        if (!url.isEmpty()) {
            result.setAnnotatedAddress(url);
        }
        var title = FrontletUtil.getTitle(controllerClass);
        if (!title.isEmpty()) {
            result.setAnnotatedTitle(title);
        }
        var containerId = FrontletUtil.getContainerId(controllerClass);
        if (!containerId.isEmpty()) {
            result.setFrontletContainerId(containerId);
        }
    }

    private Optional<String> frontletIdForRemoteUrl(String url) {
        return hostResolver.getFrontletUrls().entrySet().stream()
                .filter(entry -> url.equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .findFirst();
    }

    private Map<String, Object> queryParameters(String url) {
        var index = url.indexOf('?');
        if (index < 0 || index == url.length() - 1) {
            return emptyMap();
        }
        var parameters = new HashMap<String, Object>();
        var query = url.substring(index + 1);
        for (String keyValue : query.split("&")) {
            var separator = keyValue.indexOf('=');
            if (separator < 0) {
                parameters.put(keyValue, "");
            } else {
                parameters.put(keyValue.substring(0, separator), keyValue.substring(separator + 1));
            }
        }
        return parameters;
    }

    private String stripQuery(String url) {
        var index = url.indexOf('?');
        return index < 0 ? url : url.substring(0, index);
    }

    private Map<String, Object> castStringMap(Map<String, String> map) {
        return map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map<String, Object> parameterMap(Map<String, String> map) {
        return map.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> parameterValue(entry.getValue())));
    }

    private Object parameterValue(String value) {
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

    private Map<String, String> mapErrors(Collection<PostProcessingResult> errors) {
        var errorMessageMap = new HashMap<String, String>();
        errors.stream()
                .filter(e -> StringUtils.isNotEmpty(e.getMessageKey()))
                .forEach(error -> mapError(error, errorMessageMap));
        return errorMessageMap;
    }

    private List<String> mapGlobalErrors(Collection<PostProcessingResult> errors) {
        return errors.stream()
                .map(this::globalErrorMessages)
                .filter(Optional::isPresent)
                .map(Optional::get).toList();
    }

    private void mapError(PostProcessingResult error, Map<String, String> errorMessageMap) {
        var key = error.getDeserializationContext().getPath();
        if (errorMessageMap.containsKey(key)) {
            return;
        }
        var parameterMap = new HashMap<String, Object>(errorMessageMap);
        parameterMap.put("value", error.getValue());
        var message = validatorMessageResolver.createMessage(error.getMessageKey(),
                parameterMap,
                error.getDeserializationContext().getTarget(),
                error.getDeserializationContext().getUserContext());
        errorMessageMap.put(key, message);
    }

    private Optional<String> globalErrorMessages(PostProcessingResult error) {
        if (error.getGlobalMessageKey() == null || error.getGlobalMessageKey().isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(validatorMessageResolver.createMessage(error.getGlobalMessageKey(),
                error.getMessageParameters(),
                error.getDeserializationContext().getTarget(),
                error.getDeserializationContext().getUserContext()));
    }

    private String getModelDataKey(Method method) {
        var modelData = method.getAnnotation(ModelData.class);
        if (!modelData.value().isEmpty()) {
            return modelData.value();
        }
        return MethodUtils.propertyNameByGetter(method).orElse(method.getName());
    }

    private String getFormDataKey(Method method) {
        var formData = method.getAnnotation(FormData.class);
        if (!formData.value().isEmpty()) {
            return formData.value();
        }
        return MethodUtils.propertyNameByGetter(method).orElse(method.getName());
    }
}

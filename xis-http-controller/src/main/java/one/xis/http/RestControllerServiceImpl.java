package one.xis.http;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import one.xis.context.XISComponent;
import one.xis.context.XISDefaultComponent;
import one.xis.context.XISInit;
import one.xis.context.XISInject;
import one.xis.utils.lang.ClassUtils;
import one.xis.utils.lang.FieldUtil;
import one.xis.utils.lang.MethodUtils;
import one.xis.utils.lang.TypeUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

@XISComponent
public class RestControllerServiceImpl implements RestControllerService {

    @XISInject
    private ResponseWriter responseWriter;

    @XISInject(annotatedWith = Controller.class)
    private Collection<Object> controllers;

    @XISInject
    private Collection<ControllerExceptionHandler<?>> exceptionHandlers;

    @XISInject
    private Gson gson;

    private Map<MethodMatcher, Method> methods;
    private Map<Class<? extends Exception>, ControllerExceptionHandler<?>> exceptionHandlerMap;

    @XISInit
    void initMethods() {
        methods = new HashMap<>();
        for (Object controller : controllers) {
            Class<?> controllerClass = controller.getClass();
            Controller controllerAnnotation = controllerClass.getAnnotation(Controller.class);
            String basePath = controllerAnnotation.value();
            addController(basePath, controller);
        }
    }

    @XISInit
    @SuppressWarnings("unchecked")
    void initExceptionHandlers() {
        exceptionHandlerMap = new HashMap<>();
        Map<Class<? extends Exception>, ControllerExceptionHandler<?>> defaultHandlers = new HashMap<>();
        Map<Class<? extends Exception>, ControllerExceptionHandler<?>> handlers = new HashMap<>();
        Collection<Class<? extends Exception>> exceptions = new HashSet<>();
        for (ControllerExceptionHandler<?> handler : exceptionHandlers) {
            Class<? extends Exception> exceptionType = (Class<? extends Exception>)
                    ClassUtils.getGenericInterfacesTypeParameter(handler.getClass(), ControllerExceptionHandler.class, 0);
            exceptions.add(exceptionType);
            if (handler.getClass().isAnnotationPresent(XISDefaultComponent.class)) {
                defaultHandlers.put(exceptionType, handler);
            } else {
                if (handlers.containsKey(exceptionType)) {
                    throw new IllegalStateException("Ambiguous ExceptionHandlers for " + exceptionType.getName() + ": "
                            + handlers.get(exceptionType).getClass().getName() + " and " + handler.getClass().getName());
                }
                handlers.put(exceptionType, handler);
            }
        }

        // Füge alle Default-Handler hinzu, die nicht von Component überschrieben wurden
        for (var exceptionType : exceptions) {
            if (handlers.containsKey(exceptionType)) {
                exceptionHandlerMap.put(exceptionType, handlers.get(exceptionType));
            } else {
                exceptionHandlerMap.put(exceptionType, defaultHandlers.get(exceptionType));
            }
        }
    }

    @Override
    public void addController(String basePath, Object controller) {
        Class<?> controllerClass = controller.getClass();
        for (Method method : controllerClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Get.class)) {
                Get getAnnotation = method.getAnnotation(Get.class);
                String fullPath = combinePaths(basePath, getAnnotation.value());
                MethodMatcher matcher = new MethodMatcher(HttpMethod.GET, new Path(fullPath));
                methods.put(matcher, method);
            } else if (method.isAnnotationPresent(Post.class)) {
                Post postAnnotation = method.getAnnotation(Post.class);
                String fullPath = combinePaths(basePath, postAnnotation.value());
                MethodMatcher matcher = new MethodMatcher(HttpMethod.POST, new Path(fullPath));
                methods.put(matcher, method);
            } else if (method.isAnnotationPresent(Put.class)) {
                Put putAnnotation = method.getAnnotation(Put.class);
                String fullPath = combinePaths(basePath, putAnnotation.value());
                MethodMatcher matcher = new MethodMatcher(HttpMethod.PUT, new Path(fullPath));
                methods.put(matcher, method);
            } else if (method.isAnnotationPresent(Delete.class)) {
                Delete deleteAnnotation = method.getAnnotation(Delete.class);
                String fullPath = combinePaths(basePath, deleteAnnotation.value());
                MethodMatcher matcher = new MethodMatcher(HttpMethod.DELETE, new Path(fullPath));
                methods.put(matcher, method);
            } else if (method.isAnnotationPresent(Head.class)) {
                Head headAnnotation = method.getAnnotation(Head.class);
                String fullPath = combinePaths(basePath, headAnnotation.value());
                MethodMatcher matcher = new MethodMatcher(HttpMethod.HEAD, new Path(fullPath));
                methods.put(matcher, method);
            } else if (method.isAnnotationPresent(Options.class)) {
                Options optionsAnnotation = method.getAnnotation(Options.class);
                String fullPath = combinePaths(basePath, optionsAnnotation.value());
                MethodMatcher matcher = new MethodMatcher(HttpMethod.OPTIONS, new Path(fullPath));
                methods.put(matcher, method);
            } else if (method.isAnnotationPresent(Trace.class)) {
                Trace traceAnnotation = method.getAnnotation(Trace.class);
                String fullPath = combinePaths(basePath, traceAnnotation.value());
                MethodMatcher matcher = new MethodMatcher(HttpMethod.TRACE, new Path(fullPath));
                methods.put(matcher, method);
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void addExceptionHandler(ControllerExceptionHandler<?> handler) {
        Class<? extends Exception> exceptionType = (Class<? extends Exception>) ClassUtils.getGenericInterfacesTypeParameter(handler.getClass(), ControllerExceptionHandler.class, 0);
        exceptionHandlerMap.put(exceptionType, handler);
    }

    @Override
    public void doInvocation(HttpRequest request, HttpResponse response) {
        Optional<InvocationContext> invocationContextOptional = findInvocationContext(request);

        if (invocationContextOptional.isEmpty()) {
            response.setStatusCode(404);
            return;
        }

        InvocationContext context = invocationContextOptional.get();
        doInvoke(context, request, response);
        if (response.getStatusCode() == null || response.getStatusCode() == 0) {
            response.setStatusCode(200); // Default to 200 OK if no status code was set
        }
    }


    private void doInvoke(InvocationContext context, HttpRequest request, HttpResponse response) {
        Method method = context.method();
        Object controllerInstance = context.controllerInstance();
        MethodMatchResult methodMatchResult = context.matchResult();

        Object[] args = prepareParameters(method, request, response, methodMatchResult);

        RequestContext.createInstance(request, response);
        Object result;
        try {
            result = MethodUtils.invoke(controllerInstance, method, args);
        } catch (InvocationTargetException e) {
            result = handleControllerException(e, method, args);
        } finally {
            RequestContext.clear();
        }
        handleResponse(result, method, request, response);
    }

    private Object handleControllerException(InvocationTargetException e, Method method, Object[] args) {
        Throwable targetException = e.getTargetException();
        if (targetException instanceof InvocationTargetException invocationTargetException) {
            targetException = invocationTargetException.getTargetException();
        }

        if (targetException instanceof Exception exception) {
            return findExceptionHandler(exception)
                    .map(handler -> handler.handleException(method, args, exception))
                    .orElseThrow(() -> new RuntimeException("Unhandled exception in controller method: " + method.getName(), e));
        } else {
            throw new RuntimeException("Unexpected error invoking controller method: " + method.getName(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private <E extends Exception> Optional<ControllerExceptionHandler<E>> findExceptionHandler(E e) {
        for (var key : exceptionHandlerMap.keySet()) {
            if (key.isAssignableFrom(e.getClass())) {
                return Optional.of((ControllerExceptionHandler<E>) exceptionHandlerMap.get(key));
            }
        }
        return Optional.empty();
    }

    private void handleResponse(Object returnValue, Method method, HttpRequest request, HttpResponse response) {
        responseWriter.write(returnValue, method, request, response);

    }

    private Object[] prepareParameters(Method method, HttpRequest request, HttpResponse response, MethodMatchResult methodMatchResult) {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];
        Map<String, String> pathVariables = methodMatchResult.getPathVariables();
        Map<String, String> cookies = cookies(request);
        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            if (param.isAnnotationPresent(PathVariable.class)) {
                args[i] = handlePathVariable(param, pathVariables);
            } else if (param.isAnnotationPresent(UrlParameter.class)) {
                args[i] = handleRequestParam(param, request);
            } else if (param.isAnnotationPresent(RequestBody.class)) {
                args[i] = handleRequestBody(param, request);
            } else if (param.isAnnotationPresent(Header.class)) {
                args[i] = handleHeader(param, request);
            } else if (param.isAnnotationPresent(CookieValue.class)) {
                args[i] = handleCookieValue(param, cookies);
            } else if (param.isAnnotationPresent(BearerToken.class)) {
                args[i] = handleBearerToken(param, request);
            } else if (param.getType().isAssignableFrom(HttpRequest.class)) {
                args[i] = request;
            } else if (param.getType().isAssignableFrom(HttpResponse.class)) {
                args[i] = response;
            } else {
                throw new IllegalArgumentException("Unsupported parameter type: " + param.getType().getName() +
                        " in method: " + method.getName() + " of controller: " + method.getDeclaringClass().getName());
            }
        }
        return args;
    }

    private String handleBearerToken(Parameter param, HttpRequest request) {
        BearerToken annotation = param.getAnnotation(BearerToken.class);
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7).trim();
        }
        if (!annotation.optional()) {
            throw new IllegalArgumentException("Bearer token is required but not provided in the request.");
        }
        return null;
    }

    private Object handleCookieValue(Parameter param, Map<String, String> cookies) {
        CookieValue annotation = param.getAnnotation(CookieValue.class);
        String cookieName = annotation.value();
        String value = cookies.get(cookieName);
        return TypeUtils.convertSimple(value, param.getType());

    }

    private Object handlePathVariable(Parameter param, Map<String, String> pathVariables) {
        PathVariable annotation = param.getAnnotation(PathVariable.class);
        String varName = annotation.value();
        String value = pathVariables.get(varName);
        return TypeUtils.convertSimple(value, param.getType());
    }

    private Object handleRequestParam(Parameter param, HttpRequest request) {
        UrlParameter annotation = param.getAnnotation(UrlParameter.class);
        String paramName = annotation.value();
        String value = request.getQueryParameters().get(paramName);
        // Hier könnte man Logik für `required` und `defaultValue` hinzufügen
        return TypeUtils.convertSimple(value, param.getType());
    }

    private Object handleRequestBody(Parameter param, HttpRequest request) {
        RequestBody annotation = param.getAnnotation(RequestBody.class);
        BodyType bodyType = annotation.value();
        Class<?> targetType = param.getType();

        switch (bodyType) {
            case JSON:
                if (targetType.equals(String.class)) {
                    return request.getBodyAsString();
                }
                if (request.getContentLength() == 0) {
                    return null; // Leerer Body, kein JSON zu deserialisieren
                }
                System.out.println("**** target=" + targetType + ",body:" + request.getBodyAsString());
                return gson.fromJson(request.getBodyAsString(), targetType);

            case TEXT:
                return TypeUtils.convertSimple(request.getBodyAsString(), targetType);

            case BINARY:
                if (!targetType.equals(byte[].class)) {
                    throw new IllegalArgumentException("Parameter annotated with @RequestBody(BodyType.BINARY) must be of type byte[].");
                }
                return request.getBodyAsBytes();

            case FORM_URLENCODED:
                Map<String, String> formParameters = request.getFormParameters();
                if (targetType.isAssignableFrom(Map.class)) {
                    return formParameters;
                } else {
                    // Binden der Formulardaten an ein POJO
                    try {
                        Object targetObject = ClassUtils.newInstance(targetType);
                        Collection<Field> fields = FieldUtil.getAllFields(targetType);
                        for (Field field : fields) {
                            SerializedName serializedName = field.getAnnotation(SerializedName.class);
                            String paramName = serializedName != null ? serializedName.value() : field.getName();
                            if (formParameters.containsKey(paramName)) {
                                String paramValue = formParameters.get(paramName);
                                Object convertedValue = TypeUtils.convertSimple(paramValue, field.getType());
                                FieldUtil.setFieldValue(targetObject, field, convertedValue);
                            }
                        }
                        return targetObject;
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to instantiate or populate object of type " + targetType.getName(), e);
                    }
                }

            default:
                throw new UnsupportedOperationException("Unsupported BodyType: " + bodyType);
        }
    }

    private Object handleHeader(Parameter param, HttpRequest request) {
        Header annotation = param.getAnnotation(Header.class);
        String headerName = annotation.value();
        String value = request.getHeader(headerName);
        return TypeUtils.convertSimple(value, param.getType());
    }


    private String combinePaths(String base, String method) {
        // Verhindert doppelte Schrägstriche, z.B. wenn base="/api" und method="/users"
        String cleanBase = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
        String cleanMethod = method.startsWith("/") ? method : "/" + method;
        return cleanBase + cleanMethod;
    }

    private Map<String, String> cookies(HttpRequest request) {
        Map<String, String> cookies = new HashMap<>();
        String cookieHeader = request.getHeader("Cookie");
        if (cookieHeader != null) {
            String[] cookiePairs = cookieHeader.split(";");
            for (String pair : cookiePairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    cookies.put(keyValue[0].trim(), keyValue[1].trim());
                }
            }
        }
        return cookies;
    }


    private record InvocationContext(Object controllerInstance, Method method, MethodMatchResult matchResult) {
    }

    private Optional<InvocationContext> findInvocationContext(HttpRequest request) {
        for (Map.Entry<MethodMatcher, Method> entry : methods.entrySet()) {
            MethodMatcher matcher = entry.getKey();
            Method method = entry.getValue();
            MethodMatchResult result = matcher.matches(request.getHttpMethod(), request.getPath());

            if (result.isMatch()) {
                // Methode gefunden, jetzt die passende Controller-Instanz suchen
                return findControllerForMethod(method)
                        .map(controllerInstance -> new InvocationContext(controllerInstance, method, result));
            }
        }
        return Optional.empty();
    }

    private Optional<Object> findControllerForMethod(Method method) {
        Class<?> controllerClass = method.getDeclaringClass();
        return controllers.stream()
                .filter(controller -> controller.getClass().equals(controllerClass))
                .findFirst();
    }


}

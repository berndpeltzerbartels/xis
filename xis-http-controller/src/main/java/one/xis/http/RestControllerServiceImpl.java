package one.xis.http;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import one.xis.context.*;
import one.xis.utils.lang.ClassUtils;
import one.xis.utils.lang.FieldUtil;
import one.xis.utils.lang.MethodUtils;
import one.xis.utils.lang.TypeUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

@Component
public class RestControllerServiceImpl implements RestControllerService {

    @Inject
    private ResponseWriter responseWriter;

    @Inject(annotatedWith = Controller.class)
    private Collection<Object> controllers;

    @Inject
    private SortedSet<HttpFilter> httpFilters;

    @Inject
    private Collection<ControllerExceptionHandler<?>> exceptionHandlers;

    @Inject
    private Gson gson;

    @Inject
    private EventEmitter eventEmitter;

    private Map<MethodMatcher, Method> methods;
    private Map<Class<? extends Exception>, ControllerExceptionHandler<?>> exceptionHandlerMap;
    private PublicResourceHandler publicResourceHandler;

    /**
     * Cache: controller class -> controller instance
     * Prevents per-request streaming over all controllers.
     */
    private Map<Class<?>, Object> controllerByClass;

    /**
     * Cache: form target type -> binding fields
     * Prevents repeated reflection scanning for every form request.
     */
    private final Map<Class<?>, List<FormFieldBinding>> formBindingCache = new HashMap<>();

    @Init
    void initMethods() {
        methods = new HashMap<>();
        controllerByClass = new HashMap<>();

        List<String> publicPaths = new ArrayList<>();

        for (Object controller : controllers) {
            controllerByClass.put(controller.getClass(), controller);

            Class<?> controllerClass = controller.getClass();
            Controller controllerAnnotation = controllerClass.getAnnotation(Controller.class);
            String basePath = controllerAnnotation.value();

            addController(basePath, controller);

            if (controllerClass.isAnnotationPresent(PublicResources.class)) {
                String[] paths = controllerClass.getAnnotation(PublicResources.class).value();
                publicPaths.addAll(Arrays.asList(paths));
            }
        }

        if (!publicPaths.isEmpty()) {
            publicResourceHandler = new PublicResourceHandler(publicPaths);
        }
    }

    @Init
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

            if (handler.getClass().isAnnotationPresent(DefaultComponent.class)) {
                defaultHandlers.put(exceptionType, handler);
            } else {
                if (handlers.containsKey(exceptionType)) {
                    throw new IllegalStateException("Ambiguous ExceptionHandlers for " + exceptionType.getName() + ": "
                            + handlers.get(exceptionType).getClass().getName() + " and " + handler.getClass().getName());
                }
                handlers.put(exceptionType, handler);
            }
        }

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
                register(method, HttpMethod.GET, combinePaths(basePath, method.getAnnotation(Get.class).value()));
            } else if (method.isAnnotationPresent(Post.class)) {
                register(method, HttpMethod.POST, combinePaths(basePath, method.getAnnotation(Post.class).value()));
            } else if (method.isAnnotationPresent(Put.class)) {
                register(method, HttpMethod.PUT, combinePaths(basePath, method.getAnnotation(Put.class).value()));
            } else if (method.isAnnotationPresent(Delete.class)) {
                register(method, HttpMethod.DELETE, combinePaths(basePath, method.getAnnotation(Delete.class).value()));
            }
        }
    }

    @Override
    public void addExceptionHandler(ControllerExceptionHandler<?> handler) {
        // Keep behavior compatible: dynamic handler registration.
        // We do not try to resolve ambiguity here; initExceptionHandlers() already has that logic.
        // If you want it robust, we can add the same checks here.
        @SuppressWarnings("unchecked")
        Class<? extends Exception> exceptionType = (Class<? extends Exception>)
                ClassUtils.getGenericInterfacesTypeParameter(handler.getClass(), ControllerExceptionHandler.class, 0);

        exceptionHandlerMap.put(exceptionType, handler);
    }

    @Override
    public void doInvocation(HttpRequest request, HttpResponse response) {
        createFilterChain().doFilter(request, response);
    }

    private void doControllerInvocation(HttpRequest request, HttpResponse response) {
        eventEmitter.emitEvent(new BeforeRequestProcessingEvent(request));

        Optional<ControllerInvocationContext> ctxOpt = findInvocationContext(request);
        if (ctxOpt.isPresent()) {
            doInvokeController(ctxOpt.get(), request, response);
            ensureDefaultStatusCode(response);
            eventEmitter.emitEvent(new RequestProcessedEvent(request, response));
            return;
        }

        // No controller match: try public resources (if configured) otherwise 404.
        if (publicResourceHandler == null || !publicResourceHandler.handle(request, response)) {
            response.setStatusCode(404);
        }

        eventEmitter.emitEvent(new RequestProcessedEvent(request, response));
    }

    private FilterChain createFilterChain() {
        return new FilterChainImpl(
                new ArrayList<>(httpFilters),
                this::doControllerInvocation
        );
    }

    private void ensureDefaultStatusCode(HttpResponse response) {
        if (response.getStatusCode() == null || response.getStatusCode() == 0) {
            response.setStatusCode(200);
        }
    }

    private void register(Method method, HttpMethod httpMethod, String fullPath) {
        MethodMatcher matcher = new MethodMatcher(httpMethod, new Path(fullPath));
        methods.put(matcher, method);
    }

    private void doInvokeController(ControllerInvocationContext context, HttpRequest request, HttpResponse response) {
        Method method = context.method();
        Object controllerInstance = context.controllerInstance();
        MethodMatchResult match = context.matchResult();

        Object[] args = prepareParameters(method, request, response, match);

        RequestContext.createInstance(request, response);
        eventEmitter.emitEvent(new RequestContextCreatedEvent(RequestContext.getInstance()));

        Object result;
        try {
            result = MethodUtils.invoke(controllerInstance, method, args);
        } catch (InvocationTargetException e) {
            result = handleControllerException(e, method, args);
        } finally {
            RequestContext.clear();
        }

        responseWriter.write(result, method, request, response);
    }


    private Object handleControllerException(InvocationTargetException e, Method method, Object[] args) {
        Throwable t = unwrapInvocationTarget(e);

        if (t instanceof Exception ex) {
            final Throwable finalException = ex;
            return findExceptionHandler(ex)
                    .map(handler -> (Object) handler.handleException(method, args, ex))
                    .orElseGet(() -> defaultErrorResponse(finalException));
        }

        return defaultErrorResponse(t);
    }

    private Throwable unwrapInvocationTarget(InvocationTargetException e) {
        Throwable t = e.getTargetException();
        while (t instanceof InvocationTargetException ite && ite.getTargetException() != null) {
            t = ite.getTargetException();
        }
        return t;
    }

    private ResponseEntity<?> defaultErrorResponse(Throwable exception) {
        // Avoid stdout/stderr flooding; production should use a proper logger.
        ErrorResponse errorResponse = new ErrorResponse(exception.getMessage());
        return ResponseEntity.status(500).body(errorResponse);
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

    private Object[] prepareParameters(Method method, HttpRequest request, HttpResponse response, MethodMatchResult match) {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];

        Map<String, String> pathVariables = match.getPathVariables();
        Map<String, String> cookies = parseCookies(request.getHeader("Cookie"));

        for (int i = 0; i < parameters.length; i++) {
            args[i] = resolveArgument(parameters[i], request, response, pathVariables, cookies, method);
        }

        return args;
    }

    private Object resolveArgument(
            Parameter param,
            HttpRequest request,
            HttpResponse response,
            Map<String, String> pathVariables,
            Map<String, String> cookies,
            Method method
    ) {
        if (param.isAnnotationPresent(PathVariable.class)) {
            return handlePathVariable(param, pathVariables);
        }
        if (param.isAnnotationPresent(UrlParameter.class)) {
            return handleRequestParam(param, request);
        }
        if (param.isAnnotationPresent(RequestBody.class)) {
            return handleRequestBody(param, request);
        }
        if (param.isAnnotationPresent(RequestHeader.class)) {
            return handleHeader(param, request);
        }
        if (param.isAnnotationPresent(CookieValue.class)) {
            return handleCookieValue(param, cookies);
        }
        if (param.isAnnotationPresent(BearerToken.class)) {
            return handleBearerToken(param, request);
        }
        if (param.getType().isAssignableFrom(HttpRequest.class)) {
            return request;
        }
        if (param.getType().isAssignableFrom(HttpResponse.class)) {
            return response;
        }

        throw new IllegalArgumentException(
                "Unsupported parameter type: " + param.getType().getName()
                        + " in method: " + method.getName()
                        + " of controller: " + method.getDeclaringClass().getName()
        );
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
        return TypeUtils.convertSimple(value, param.getType());
    }

    private Object handleRequestBody(Parameter param, HttpRequest request) {
        RequestBody annotation = param.getAnnotation(RequestBody.class);
        BodyType bodyType = annotation.value();
        Class<?> targetType = param.getType();

        // Read once to avoid repeated allocations.
        String bodyString = (request.getContentLength() == 0) ? "" : request.getBodyAsString();

        return switch (bodyType) {
            case JSON -> handleJsonBody(targetType, bodyString);
            case TEXT -> TypeUtils.convertSimple(bodyString, targetType);
            case FORM_URLENCODED -> handleFormUrlEncodedBody(targetType, request.getFormParameters());
            default -> throw new UnsupportedOperationException("Unsupported BodyType: " + bodyType);
        };
    }

    private Object handleJsonBody(Class<?> targetType, String bodyString) {
        if (targetType.equals(String.class)) {
            return bodyString;
        }
        if (bodyString == null || bodyString.isBlank()) {
            return null;
        }
        return gson.fromJson(bodyString, targetType);
    }

    private Object handleFormUrlEncodedBody(Class<?> targetType, Map<String, String> formParameters) {
        if (targetType.isAssignableFrom(Map.class)) {
            return formParameters;
        }

        Object targetObject = ClassUtils.newInstance(targetType);
        for (FormFieldBinding binding : getOrBuildFormBindings(targetType)) {
            String raw = formParameters.get(binding.paramName());
            if (raw == null) {
                continue;
            }
            Object converted = TypeUtils.convertSimple(raw, binding.field().getType());
            FieldUtil.setFieldValue(targetObject, binding.field(), converted);
        }
        return targetObject;
    }

    private List<FormFieldBinding> getOrBuildFormBindings(Class<?> targetType) {
        List<FormFieldBinding> cached = formBindingCache.get(targetType);
        if (cached != null) {
            return cached;
        }

        List<FormFieldBinding> bindings = new ArrayList<>();
        Collection<Field> fields = FieldUtil.getAllFields(targetType);

        for (Field field : fields) {
            SerializedName serializedName = field.getAnnotation(SerializedName.class);
            String paramName = (serializedName != null) ? serializedName.value() : field.getName();
            bindings.add(new FormFieldBinding(field, paramName));
        }

        formBindingCache.put(targetType, bindings);
        return bindings;
    }

    private Object handleHeader(Parameter param, HttpRequest request) {
        RequestHeader annotation = param.getAnnotation(RequestHeader.class);
        String headerName = annotation.value();
        String value = request.getHeader(headerName);
        return TypeUtils.convertSimple(value, param.getType());
    }

    /**
     * Fast cookie parsing without regex-based split().
     * Format: "a=b; c=d; e=f"
     */
    private Map<String, String> parseCookies(String cookieHeader) {
        if (cookieHeader == null || cookieHeader.isBlank()) {
            return Collections.emptyMap();
        }

        Map<String, String> cookies = new HashMap<>();

        int len = cookieHeader.length();
        int i = 0;

        while (i < len) {
            // skip separators/spaces
            while (i < len && (cookieHeader.charAt(i) == ' ' || cookieHeader.charAt(i) == ';')) {
                i++;
            }
            if (i >= len) break;

            int keyStart = i;
            int eq = cookieHeader.indexOf('=', keyStart);
            if (eq < 0) break;

            int keyEnd = eq;
            int valStart = eq + 1;

            int semi = cookieHeader.indexOf(';', valStart);
            int valEnd = (semi < 0) ? len : semi;

            String key = cookieHeader.substring(keyStart, keyEnd).trim();
            String val = cookieHeader.substring(valStart, valEnd).trim();

            if (!key.isEmpty()) {
                cookies.put(key, val);
            }

            i = (semi < 0) ? len : (semi + 1);
        }

        return cookies;
    }

    private Optional<ControllerInvocationContext> findInvocationContext(HttpRequest request) {
        for (Map.Entry<MethodMatcher, Method> entry : methods.entrySet()) {
            MethodMatcher matcher = entry.getKey();
            Method method = entry.getValue();

            MethodMatchResult result = matcher.matches(request.getHttpMethod(), request.getPath());
            if (!result.isMatch()) {
                continue;
            }

            Object controllerInstance = controllerByClass.get(method.getDeclaringClass());
            if (controllerInstance == null) {
                // Should not happen, but keep it safe.
                return Optional.empty();
            }
            return Optional.of(new ControllerInvocationContext(controllerInstance, method, result));
        }
        return Optional.empty();
    }

    private String combinePaths(String base, String path) {
        if (base == null) base = "";
        if (path == null) path = "";

        if (!base.startsWith("/")) base = "/" + base;
        if (base.endsWith("/")) base = base.substring(0, base.length() - 1);

        if (!path.startsWith("/")) path = "/" + path;

        String combined = base + path;
        return combined.replaceAll("//+", "/");
    }

    record ControllerInvocationContext(Object controllerInstance, Method method, MethodMatchResult matchResult) {
    }

    private record FormFieldBinding(Field field, String paramName) {
    }
}

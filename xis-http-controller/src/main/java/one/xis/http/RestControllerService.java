package one.xis.http;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import one.xis.context.XISComponent;
import one.xis.context.XISInit;
import one.xis.context.XISInject;
import one.xis.utils.lang.ClassUtils;
import one.xis.utils.lang.FieldUtil;
import one.xis.utils.lang.MethodUtils;
import one.xis.utils.lang.TypeUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@XISComponent
public class RestControllerService {

    @XISInject
    private ResponseWriter responseWriter;

    @XISInject(annotatedWith = Controller.class)
    private Collection<Object> controllers;

    @XISInject
    private Gson gson;

    private Map<MethodMatcher, Method> methods;

    @XISInit
    void initMethods() {
        methods = new HashMap<>();
        for (Object controller : controllers) {
            Class<?> controllerClass = controller.getClass();
            Controller controllerAnnotation = controllerClass.getAnnotation(Controller.class);
            String basePath = controllerAnnotation.value();
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
                }
            }
        }
    }

    public void doInvocation(HttpRequest request, HttpResponse response) {
        Optional<InvocationContext> invocationContextOptional = findInvocationContext(request);

        if (invocationContextOptional.isEmpty()) {
            response.setStatusCode(404);
            return;
        }

        InvocationContext context = invocationContextOptional.get();
        doInvoke(context, request, response);
    }


    private void doInvoke(InvocationContext context, HttpRequest request, HttpResponse response) {
        Method method = context.method();
        Object controllerInstance = context.controllerInstance();
        MethodMatchResult methodMatchResult = context.matchResult();

        // Parameter vorbereiten
        Object[] args = prepareParameters(method, request, response, methodMatchResult);

        RequestContext.createInstance(request, response);
        Object result;
        try {
            result = MethodUtils.invoke(controllerInstance, method, args);
        } finally {
            RequestContext.clear();
        }
        handleResponse(result, method, request, response);
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
        String value = request.getHeaders().get(headerName);
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
        String cookieHeader = request.getHeaders().get("Cookie");
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

package one.xis.sql;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

class SQLMethodSupport {

    ParsedSql parseSql(String sql, Method method, String annotationName) {
        return parseSql(sql, method, annotationName, Set.of());
    }

    ParsedSql parseSql(String sql, Method method, String annotationName, Set<Integer> ignoredParameterIndexes) {
        if (sql.indexOf('{') < 0 && sql.indexOf('}') < 0) {
            return positionalSql(sql, method, annotationName, ignoredParameterIndexes);
        }
        if (sql.contains("?")) {
            throw new IllegalArgumentException(annotationName + " must not mix ? and named parameters: " + method);
        }
        return namedSql(sql, method, annotationName, ignoredParameterIndexes);
    }

    Class<?> genericReturnType(Method method, String annotationName) {
        Type type = method.getGenericReturnType();
        if (!(type instanceof ParameterizedType parameterizedType)) {
            throw new IllegalArgumentException(annotationName + " collection/optional method needs a generic result type: " + method);
        }
        Type argument = parameterizedType.getActualTypeArguments()[0];
        if (argument instanceof Class<?> clazz) {
            return clazz;
        }
        if (argument instanceof ParameterizedType nested && nested.getRawType() instanceof Class<?> clazz) {
            return clazz;
        }
        throw new IllegalArgumentException("Unsupported " + annotationName + " result type " + argument + " on " + method);
    }

    private ParsedSql positionalSql(String sql, Method method, String annotationName, Set<Integer> ignoredParameterIndexes) {
        int placeholders = countPlaceholders(sql);
        int bindableParameters = method.getParameterCount() - ignoredParameterIndexes.size();
        if (placeholders != bindableParameters) {
            throw new IllegalArgumentException(annotationName + " parameter count mismatch on " + method
                    + ": SQL has " + placeholders + " placeholders but method has " + bindableParameters + " parameters");
        }
        var indexes = new java.util.ArrayList<BindParameter>(placeholders);
        for (int i = 0; i < method.getParameterCount(); i++) {
            if (!ignoredParameterIndexes.contains(i)) {
                indexes.add(new BindParameter(i, null));
            }
        }
        return new ParsedSql(sql, List.copyOf(indexes));
    }

    private int countPlaceholders(String sql) {
        int count = 0;
        for (int i = 0; i < sql.length(); i++) {
            if (sql.charAt(i) == '?') {
                count++;
            }
        }
        return count;
    }

    private ParsedSql namedSql(String sql, Method method, String annotationName, Set<Integer> ignoredParameterIndexes) {
        Map<String, Integer> parameters = namedParameters(method, ignoredParameterIndexes);
        Integer implicitEntityParameter = implicitEntityParameter(method, ignoredParameterIndexes);
        var bindIndexes = new java.util.ArrayList<BindParameter>();
        var usedParameters = new java.util.HashSet<String>();
        var parsed = new StringBuilder(sql.length());
        int index = 0;
        while (index < sql.length()) {
            char c = sql.charAt(index);
            if (c == '}') {
                throw new IllegalArgumentException("Unmatched } in " + annotationName + " SQL on " + method);
            }
            QuotedParameter quotedParameter = quotedNamedParameter(sql, index);
            if (quotedParameter != null) {
                bindNamedParameter(method, annotationName, parameters, implicitEntityParameter, bindIndexes, usedParameters, parsed,
                        quotedParameter.name());
                index = quotedParameter.endIndex();
                continue;
            }
            if (c != '{') {
                parsed.append(c);
                index++;
                continue;
            }
            int end = sql.indexOf('}', index + 1);
            if (end < 0) {
                throw new IllegalArgumentException("Unmatched { in " + annotationName + " SQL on " + method);
            }
            String name = sql.substring(index + 1, end).trim();
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Empty named parameter in " + annotationName + " SQL on " + method);
            }
            bindNamedParameter(method, annotationName, parameters, implicitEntityParameter, bindIndexes, usedParameters, parsed, name);
            index = end + 1;
        }
        if (!usedParameters.containsAll(parameters.keySet())
                || implicitEntityParameter == null && usedParameters.size() != parameters.size()) {
            throw new IllegalArgumentException("Named " + annotationName + " parameters do not match method parameters on " + method);
        }
        return new ParsedSql(parsed.toString(), List.copyOf(bindIndexes));
    }

    private void bindNamedParameter(Method method, String annotationName, Map<String, Integer> parameters,
                                    Integer implicitEntityParameter,
                                    java.util.ArrayList<BindParameter> bindIndexes,
                                    java.util.HashSet<String> usedParameters,
                                    StringBuilder parsed, String name) {
        ParameterReference reference = parameterReference(method, annotationName, parameters, implicitEntityParameter, name);
        Integer parameterIndex = reference.parameterIndex();
        String propertyName = reference.propertyName();
        if (propertyName == null) {
            usedParameters.add(name);
        } else if (name.indexOf('.') < 0) {
            usedParameters.add(name);
        } else {
            usedParameters.add(name.substring(0, name.indexOf('.')));
        }
        parsed.append('?');
        bindIndexes.add(new BindParameter(parameterIndex, propertyName));
    }

    private ParameterReference parameterReference(Method method, String annotationName, Map<String, Integer> parameters,
                                                  Integer implicitEntityParameter, String name) {
        String parameterName = name;
        String propertyName = null;
        int dot = name.indexOf('.');
        if (dot > 0) {
            parameterName = name.substring(0, dot);
            propertyName = name.substring(dot + 1);
        }
        Integer parameterIndex = parameters.get(parameterName);
        if (parameterIndex == null && implicitEntityParameter != null && dot < 0) {
            parameterIndex = implicitEntityParameter;
            propertyName = name;
        }
        if (parameterIndex == null) {
            throw new IllegalArgumentException("No @Param(\"" + name + "\") on " + method);
        }
        return new ParameterReference(parameterIndex, propertyName);
    }

    private QuotedParameter quotedNamedParameter(String sql, int index) {
        if (sql.charAt(index) != '\'') {
            return null;
        }
        int endQuote = sql.indexOf('\'', index + 1);
        if (endQuote < 0) {
            return null;
        }
        String content = sql.substring(index + 1, endQuote).trim();
        if (!content.startsWith("{") || !content.endsWith("}")) {
            return null;
        }
        String name = content.substring(1, content.length() - 1).trim();
        if (name.isEmpty()) {
            return null;
        }
        return new QuotedParameter(name, endQuote + 1);
    }

    private Map<String, Integer> namedParameters(Method method, Set<Integer> ignoredParameterIndexes) {
        var result = new HashMap<String, Integer>();
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            if (ignoredParameterIndexes.contains(i)) {
                continue;
            }
            Param param = parameters[i].getAnnotation(Param.class);
            if (param != null) {
                if (result.put(param.value(), i) != null) {
                    throw new IllegalArgumentException("Duplicate @Param(\"" + param.value() + "\") on " + method);
                }
            }
        }
        return result;
    }

    private Integer implicitEntityParameter(Method method, Set<Integer> ignoredParameterIndexes) {
        int result = -1;
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].isAnnotationPresent(Param.class)) {
                continue;
            }
            if (parameters[i].getType().isAnnotationPresent(Entity.class)) {
                if (result >= 0) {
                    return null;
                }
                result = i;
            }
        }
        return result < 0 ? null : result;
    }

    record ParsedSql(String sql, List<BindParameter> bindParameterIndexes) {
    }

    record BindParameter(int parameterIndex, String propertyName) {
    }

    private record ParameterReference(int parameterIndex, String propertyName) {
    }

    private record QuotedParameter(String name, int endIndex) {
    }
}

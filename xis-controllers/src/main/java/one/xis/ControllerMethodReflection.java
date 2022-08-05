package one.xis;

import lombok.RequiredArgsConstructor;
import one.xis.utils.lang.MethodUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
class ControllerMethodReflection {
    private final Collection<Class<? extends Annotation>> methodAnnotations;
    private Map<Class<? extends Annotation>, Collection<Method>> methodsByAnnotation;

    Map<Class<? extends Annotation>, Collection<Method>> methodsByAnnotation(Object controller) {
        Map<Class<? extends Annotation>, Collection<Method>> map = new HashMap<>();
        MethodUtils.callableMethods(controller).forEach(m -> {
            //methodAnnotations.stream().filter(m::isAnnotationPresent)
        });
        return map;
    }


}

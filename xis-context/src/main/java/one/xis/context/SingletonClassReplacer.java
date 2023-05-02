package one.xis.context;


import lombok.RequiredArgsConstructor;
import one.xis.utils.lang.CollectionUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
class SingletonClassReplacer {

    Set<Class<?>> doReplacement(Set<Class<?>> allSingletonClasses) {
        var rv = new HashSet<Class<?>>(allSingletonClasses);
        var replacements = allSingletonClasses.stream().filter(this::isReplacement).collect(Collectors.toSet());
        while (!replacements.isEmpty()) {
            var replacementClass = CollectionUtils.removeOne(replacements);
            var classToReplace = classToReplace(replacementClass);
            if (!rv.contains(classToReplace)) {
                throw new IllegalStateException("there is no singleton to do the replacement defined in @XISComponent-annotation in " + replacementClass);
            }
            rv.remove(classToReplace);
            replacements.remove(replacementClass);
        }
        return rv;
    }

    private boolean isReplacement(Class<?> singletonClass) {
        if (!singletonClass.isAnnotationPresent(XISComponent.class)) {
            return false;
        }
        return !singletonClass.getAnnotation(XISComponent.class).replacementFor().equals(None.class);
    }

    private Class<?> classToReplace(Class<?> replacment) {
        return replacment.getAnnotation(XISComponent.class).replacementFor();
    }


}

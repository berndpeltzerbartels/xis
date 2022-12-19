package one.xis.context;

import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Set;

class DefaultClassReflection implements ClassReflection {

    private final Reflections reflections;

    DefaultClassReflection(Class<?> basePackageClass) {
        this(basePackageClass.getPackageName());
    }

    DefaultClassReflection(String basePackage) {
        reflections = new Reflections(basePackage, new SubTypesScanner(),
                new TypeAnnotationsScanner(),
                new FieldAnnotationsScanner());
    }

    @Override
    public <A extends Annotation> Set<Field> getFieldsAnnotatedWith(Class<A> anno) {
        return reflections.getFieldsAnnotatedWith(anno);
    }

    @Override
    public Set<Class<?>> getTypesAnnotatedWith(final Class<? extends Annotation> annotation) {
        return reflections.getTypesAnnotatedWith(annotation);
    }
}

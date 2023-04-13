package one.xis.context;

import org.mockito.Mockito;

import java.util.Arrays;

public class TestContextBuilder extends AppContextBuilderImpl implements AppContextBuilder {


    public TestContextBuilder withMock(Object singleton) {
        if (singleton instanceof Class) {
            withSingelton(Mockito.mock((Class<?>) singleton));
        } else {
            withSingelton(singleton);
        }
        return this;
    }

    public TestContextBuilder withMocks(Object... mocks) {
        Arrays.stream(mocks).forEach(this::withMock);
        return this;
    }

    public TestContextBuilder withXisApi() {
        withPackage("one.xis");
        return this;
    }



    /*
    private Set<Class<? extends Annotation>> getAllXISAnnotations() {
        return new Reflections("one.xis", new TypeAnnotationsScanner(), new SubTypesScanner()).getTypesAnnotatedWith(Target.class).stream()
                .filter(Class::isAnnotation)
                .map(c -> (Class<? extends Annotation>) c).collect(Collectors.toSet());
    }
    */

}

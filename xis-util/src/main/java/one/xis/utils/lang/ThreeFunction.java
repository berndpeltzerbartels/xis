package one.xis.utils.lang;

@FunctionalInterface
public interface ThreeFunction<A, B, C, R> {

    R apply(A a, B b, C c);

}

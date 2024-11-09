package one.xis.context2;

import lombok.RequiredArgsConstructor;

import java.lang.reflect.Method;

@RequiredArgsConstructor
class Annotations {

    boolean isAnnotatedMethod(Method method) {
        return true;
    }

}

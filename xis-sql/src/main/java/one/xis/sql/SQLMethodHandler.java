package one.xis.sql;

import java.lang.reflect.Method;

interface SQLMethodHandler {

    boolean matches(Method method);

    void init(Method method);

    Object invoke(Object[] args);


}

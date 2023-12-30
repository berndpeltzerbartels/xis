package one.xis.parameter;

import java.lang.reflect.Type;

public interface Target {
    String getName();

    Class<?> getType();

    Type getElementType();


}

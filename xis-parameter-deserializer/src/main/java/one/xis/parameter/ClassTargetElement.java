package one.xis.parameter;

import lombok.Value;

@Value
class ClassTargetElement implements Target {
    String name;
    Class<?> type;

    @Override
    public Class<?> getElementType() {
        return type;
    }

}

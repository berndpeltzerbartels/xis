package one.xis.server;

import lombok.Data;

import java.lang.reflect.Field;

@Data
class ValidationError {
    private String path;
    private Field field;
    private Object value;
}

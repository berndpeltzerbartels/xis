package one.xis.server;

import lombok.Data;

@Data
public class ValidationError {
    private String path;
    private Object value;
}

package one.xis.parameter;

import lombok.Data;

@Data
class Error {
    private String path;
    private String message;
}

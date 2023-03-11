package one.xis.server;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MethodParameter {

    private ParameterType parameterType;

    private String key;
}

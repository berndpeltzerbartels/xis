package one.xis;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class InvocationResult {
    private Object model;
    private Map<String, Object> clientState = new HashMap<>();
    private Object next;
    private String nextjavascriptClass;
}

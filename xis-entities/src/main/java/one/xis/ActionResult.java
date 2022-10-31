package one.xis;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class ActionResult {
    private Map<String, Object> model = new HashMap<>();
    private Map<String, Object> clientState = new HashMap<>();
    private String next;

    public void addModel(String name, Object value) {
        model.put(name, value);
    }

    public void addClientState(String name, Object value) {
        clientState.put(name, value);
    }
}

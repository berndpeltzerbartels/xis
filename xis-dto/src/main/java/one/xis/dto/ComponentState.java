package one.xis.dto;

import lombok.experimental.Delegate;

import java.util.HashMap;
import java.util.Map;

public class ComponentState {

    @Delegate
    private final Map<String, Object> values = new HashMap<>();
}

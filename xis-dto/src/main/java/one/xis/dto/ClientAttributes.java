package one.xis.dto;

import lombok.experimental.Delegate;

import java.util.HashMap;
import java.util.Map;

public class ClientState implements Map<String, Object> {

    @Delegate
    private final Map<String, Object> values = new HashMap<>();
}

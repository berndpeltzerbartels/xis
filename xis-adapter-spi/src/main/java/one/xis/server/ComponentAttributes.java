package one.xis.server;

import lombok.Data;

import java.util.Collection;
import java.util.HashSet;

@Data
class ComponentAttributes {
    private final Collection<String> sessionStorageKeys = new HashSet<>();
    private final Collection<String> localStorageKeys = new HashSet<>();
    private final Collection<String> clientStorageKeys = new HashSet<>();
    private final Collection<String> globalVariableKeys = new HashSet<>();
    private final Collection<String> localDatabaseKeys = new HashSet<>(); // TODO
    private final Collection<String> updateEventKeys = new HashSet<>();
}

package one.xis.server;

import lombok.Data;

import java.util.Collection;
import java.util.HashSet;

@Data
class ComponentAttributes {
    private final Collection<String> clientStateKeys = new HashSet<>();
    private final Collection<String> localStorageKeys = new HashSet<>();
    private final Collection<String> localDatabaseKeys = new HashSet<>(); // TODO
}
